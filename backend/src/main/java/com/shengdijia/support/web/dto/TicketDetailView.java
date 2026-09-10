package com.shengdijia.support.web.dto;

import com.shengdijia.support.domain.Category;
import com.shengdijia.support.domain.Priority;
import com.shengdijia.support.domain.Ticket;
import com.shengdijia.support.domain.TicketEvent;
import com.shengdijia.support.domain.TicketStatus;
import com.shengdijia.support.domain.User;

import java.time.LocalDateTime;
import java.util.List;

public record TicketDetailView(
        Long id,
        String ticketNo,
        String title,
        String description,
        TicketStatus status,
        Priority priority,
        Category category,
        String contactPhone,
        String contactEmail,
        String merchantId,
        UserView initiator,
        UserView handler,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt,
        List<EventView> timeline
) {
    public static TicketDetailView from(Ticket ticket, List<TicketEvent> events) {
        User initiator = ticket.getInitiator();
        User handler = ticket.getHandler();
        return new TicketDetailView(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory(),
                ticket.getContactPhone(),
                ticket.getContactEmail(),
                ticket.getMerchantId(),
                toUser(initiator),
                handler == null ? null : toUser(handler),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt(),
                events.stream().map(EventView::from).toList()
        );
    }

    public static UserView toUser(User user) {
        return new UserView(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getCompany(),
                user.getRole(),
                user.getPhone(),
                user.getEmail(),
                user.getMerchantId()
        );
    }
}
