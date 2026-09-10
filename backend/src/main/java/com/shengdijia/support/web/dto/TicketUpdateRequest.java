package com.shengdijia.support.web.dto;

import com.shengdijia.support.domain.TicketStatus;
import jakarta.validation.constraints.Size;

public record TicketUpdateRequest(
        TicketStatus status,
        Long handlerId,
        @Size(max = 2000, message = "备注不超过 2000 字")
        String remark
) {
}
