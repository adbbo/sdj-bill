package com.shengdijia.support.web.dto;

import com.shengdijia.support.domain.Category;
import com.shengdijia.support.domain.Priority;
import com.shengdijia.support.domain.Ticket;
import com.shengdijia.support.domain.TicketStatus;
import com.shengdijia.support.domain.User;

import java.time.LocalDateTime;

public record TicketSummaryView(
        Long id,
        String ticketNo,
        String title,
        TicketStatus status,
        Priority priority,
        Category category,
        String initiatorName,
        String initiatorCompany,
        String handlerName,
        String merchantId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt
) {
    public static TicketSummaryView from(Ticket ticket) {
        User initiator = ticket.getInitiator();
        User handler = ticket.getHandler();
        return new TicketSummaryView(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getTitle(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory(),
                initiator.getDisplayName(),
                initiator.getCompany(),
                handler == null ? null : handler.getDisplayName(),
                ticket.getMerchantId(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt()
        );
    }
}
