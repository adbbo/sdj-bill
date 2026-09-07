package com.shengdijia.support.service;

import com.shengdijia.support.domain.TicketStatus;
import com.shengdijia.support.domain.User;
import com.shengdijia.support.repo.TicketRepository;
import com.shengdijia.support.web.dto.DashboardView;
import com.shengdijia.support.web.dto.TicketSummaryView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public DashboardView summary(User actor) {
        List<Object[]> grouped = actor.isInternal()
                ? ticketRepository.countGroupByStatus()
                : ticketRepository.countGroupByStatusForCompany(actor.getCompany());

        Map<TicketStatus, Long> counts = new EnumMap<>(TicketStatus.class);
        for (TicketStatus status : TicketStatus.values()) {
            counts.put(status, 0L);
        }
        long total = 0;
        for (Object[] row : grouped) {
            TicketStatus status = (TicketStatus) row[0];
            long count = (Long) row[1];
            counts.put(status, count);
            total += count;
        }

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        long todayNew = actor.isInternal()
                ? ticketRepository.countByCreatedAtBetween(start, end)
                : ticketRepository.countByInitiatorCompanyAndCreatedAtBetween(actor.getCompany(), start, end);

        List<TicketSummaryView> recent = (actor.isInternal()
                ? ticketRepository.findTop8ByOrderByUpdatedAtDesc()
                : ticketRepository.findTop8ByInitiatorCompanyOrderByUpdatedAtDesc(actor.getCompany())
        ).stream().map(TicketSummaryView::from).toList();

        return new DashboardView(counts, total, todayNew, recent);
    }
}
