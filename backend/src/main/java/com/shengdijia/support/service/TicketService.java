package com.shengdijia.support.service;

import com.shengdijia.support.domain.Category;
import com.shengdijia.support.domain.EventType;
import com.shengdijia.support.domain.Priority;
import com.shengdijia.support.domain.Role;
import com.shengdijia.support.domain.Ticket;
import com.shengdijia.support.domain.TicketEvent;
import com.shengdijia.support.domain.TicketStatus;
import com.shengdijia.support.domain.User;
import com.shengdijia.support.repo.TicketEventRepository;
import com.shengdijia.support.repo.TicketRepository;
import com.shengdijia.support.repo.UserRepository;
import com.shengdijia.support.web.dto.PageResponse;
import com.shengdijia.support.web.dto.RemarkRequest;
import com.shengdijia.support.web.dto.TicketCreateRequest;
import com.shengdijia.support.web.dto.TicketDetailView;
import com.shengdijia.support.web.dto.TicketSummaryView;
import com.shengdijia.support.web.dto.TicketUpdateRequest;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TicketRepository ticketRepository;
    private final TicketEventRepository eventRepository;
    private final UserRepository userRepository;

    public TicketService(
            TicketRepository ticketRepository,
            TicketEventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TicketDetailView create(User actor, TicketCreateRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTicketNo(nextTicketNo());
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setInitiator(userRepository.getReferenceById(actor.getId()));
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setPriority(request.priority());
        ticket.setCategory(request.category());
        ticket.setContactPhone(blankToNull(request.contactPhone()));
        ticket.setContactEmail(blankToNull(request.contactEmail()));
        String merchantId = blankToNull(request.merchantId());
        ticket.setMerchantId(merchantId != null ? merchantId : actor.getMerchantId());
        ticketRepository.save(ticket);

        addEvent(ticket, actor, EventType.CREATED,
                "发起人 " + actor.getDisplayName() + "（" + actor.getCompany() + "）提交工单。");
        return toDetail(ticket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketSummaryView> search(
            User actor,
            String keyword,
            TicketStatus status,
            String company,
            Long handlerId,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Specification<Ticket> spec = (root, query, cb) -> {
            boolean isCount = query == null
                    || query.getResultType() == Long.class
                    || query.getResultType() == long.class;
            if (query != null) {
                query.distinct(true);
            }
            if (!isCount) {
                root.fetch("initiator", JoinType.INNER);
                root.fetch("handler", JoinType.LEFT);
            }
            var initiator = isCount
                    ? root.join("initiator", JoinType.INNER)
                    : root.get("initiator");
            List<Predicate> predicates = new ArrayList<>();
            if (!actor.isInternal()) {
                predicates.add(cb.equal(initiator.get("company"), actor.getCompany()));
            } else if (StringUtils.hasText(company)) {
                predicates.add(cb.like(initiator.get("company"), "%" + company.trim() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (handlerId != null) {
                predicates.add(cb.equal(root.get("handler").get("id"), handlerId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("ticketNo"), like),
                        cb.like(root.get("description"), like),
                        cb.like(initiator.get("displayName"), like),
                        cb.like(initiator.get("company"), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<Ticket> result = ticketRepository.findAll(
                spec,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );
        return new PageResponse<>(
                result.map(TicketSummaryView::from).getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }

    @Transactional(readOnly = true)
    public TicketDetailView get(User actor, Long id) {
        return toDetail(loadVisible(actor, id));
    }

    @Transactional
    public TicketDetailView update(User actor, Long id, TicketUpdateRequest request) {
        Ticket ticket = loadVisible(actor, id);
        if (request.status() != null && request.status() != ticket.getStatus()) {
            requireInternal(actor);
            TicketStatus old = ticket.getStatus();
            ticket.setStatus(request.status());
            applyResolvedAt(ticket, old, request.status());
            addEvent(ticket, actor, EventType.STATUS_CHANGE,
                    "状态由「" + Labels.status(old) + "」变更为「" + Labels.status(request.status()) + "」。");
        }
        if (request.handlerId() != null) {
            requireInternal(actor);
            Long currentHandlerId = ticket.getHandler() == null ? null : ticket.getHandler().getId();
            if (!request.handlerId().equals(currentHandlerId)) {
                User handler = userRepository.findById(request.handlerId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "处理人不存在"));
                if (handler.getRole() != Role.INTERNAL) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只能指派内部处理人");
                }
                String previous = ticket.getHandler() == null ? "未指派" : ticket.getHandler().getDisplayName();
                ticket.setHandler(handler);
                if (ticket.getStatus() == TicketStatus.PENDING) {
                    ticket.setStatus(TicketStatus.IN_PROGRESS);
                    addEvent(ticket, actor, EventType.STATUS_CHANGE,
                            "指派处理人后，状态由「待处理」变更为「处理中」。");
                }
                addEvent(ticket, actor, EventType.HANDLER_CHANGE,
                        "处理人由「" + previous + "」变更为「" + handler.getDisplayName() + "」。");
            }
        }
        if (StringUtils.hasText(request.remark())) {
            addEvent(ticket, actor, EventType.REMARK, request.remark().trim());
        }
        ticketRepository.save(ticket);
        return toDetail(ticket);
    }

    @Transactional
    public TicketDetailView addRemark(User actor, Long id, RemarkRequest request) {
        Ticket ticket = loadVisible(actor, id);
        addEvent(ticket, actor, EventType.REMARK, request.content().trim());
        ticketRepository.save(ticket);
        return toDetail(ticket);
    }

    private Ticket loadVisible(User actor, Long id) {
        Ticket ticket = ticketRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "工单不存在"));
        if (!actor.isInternal() && !actor.getCompany().equals(ticket.getInitiator().getCompany())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看该工单");
        }
        return ticket;
    }

    private void requireInternal(User actor) {
        if (!actor.isInternal()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "对接方不可变更状态或处理人");
        }
    }

    private synchronized String nextTicketNo() {
        String prefix = "SDJ-" + LocalDate.now().format(DAY) + "-";
        int seq = ticketRepository.findMaxTicketNoByPrefix(prefix)
                .map(max -> Integer.parseInt(max.substring(prefix.length())) + 1)
                .orElse(1);
        return prefix + String.format("%04d", seq);
    }

    private void applyResolvedAt(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        boolean nowResolved = newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED;
        boolean wasResolved = oldStatus == TicketStatus.RESOLVED || oldStatus == TicketStatus.CLOSED;
        if (nowResolved && !wasResolved) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (!nowResolved && wasResolved) {
            ticket.setResolvedAt(null);
        }
    }

    private void addEvent(Ticket ticket, User author, EventType type, String content) {
        TicketEvent event = new TicketEvent();
        event.setTicket(ticket);
        event.setAuthor(author == null ? null : userRepository.getReferenceById(author.getId()));
        event.setEventType(type);
        event.setContent(content);
        eventRepository.save(event);
    }

    private TicketDetailView toDetail(Ticket ticket) {
        List<TicketEvent> events = eventRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());
        return TicketDetailView.from(ticket, events);
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    public static final class Labels {
        private Labels() {
        }

        public static String status(TicketStatus status) {
            return switch (status) {
                case PENDING -> "待处理";
                case IN_PROGRESS -> "处理中";
                case RESOLVED -> "已解决";
                case CLOSED -> "已关闭";
            };
        }

        public static String priority(Priority priority) {
            return switch (priority) {
                case LOW -> "低";
                case MEDIUM -> "中";
                case HIGH -> "高";
                case URGENT -> "紧急";
            };
        }

        public static String category(Category category) {
            return switch (category) {
                case INTEGRATION -> "对接";
                case TRANSACTION -> "交易";
                case SETTLEMENT -> "结算";
                case OTHER -> "其他";
            };
        }
    }
}
