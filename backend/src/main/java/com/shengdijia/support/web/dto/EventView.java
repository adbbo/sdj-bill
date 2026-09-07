package com.shengdijia.support.web.dto;

import com.shengdijia.support.domain.EventType;
import com.shengdijia.support.domain.TicketEvent;
import com.shengdijia.support.domain.User;

import java.time.LocalDateTime;

public record EventView(
        Long id,
        EventType eventType,
        String content,
        String authorName,
        String authorCompany,
        LocalDateTime createdAt
) {
    public static EventView from(TicketEvent event) {
        User author = event.getAuthor();
        return new EventView(
                event.getId(),
                event.getEventType(),
                event.getContent(),
                author == null ? "系统" : author.getDisplayName(),
                author == null ? "盛迪嘉支付" : author.getCompany(),
                event.getCreatedAt()
        );
    }
}
