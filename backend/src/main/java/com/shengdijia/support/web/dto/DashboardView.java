package com.shengdijia.support.web.dto;

import com.shengdijia.support.domain.TicketStatus;

import java.util.List;
import java.util.Map;

public record DashboardView(
        Map<TicketStatus, Long> statusCounts,
        long total,
        long todayNew,
        List<TicketSummaryView> recentTickets
) {
}
