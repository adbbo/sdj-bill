package com.shengdijia.support.web;

import com.shengdijia.support.domain.TicketStatus;
import com.shengdijia.support.domain.User;
import com.shengdijia.support.service.CurrentUser;
import com.shengdijia.support.service.TicketService;
import com.shengdijia.support.web.dto.PageResponse;
import com.shengdijia.support.web.dto.RemarkRequest;
import com.shengdijia.support.web.dto.TicketCreateRequest;
import com.shengdijia.support.web.dto.TicketDetailView;
import com.shengdijia.support.web.dto.TicketSummaryView;
import com.shengdijia.support.web.dto.TicketUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public PageResponse<TicketSummaryView> list(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Long handlerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        User actor = CurrentUser.require(authentication);
        return ticketService.search(actor, keyword, status, company, handlerId, from, to, page, size);
    }

    @PostMapping
    public TicketDetailView create(Authentication authentication, @Valid @RequestBody TicketCreateRequest request) {
        return ticketService.create(CurrentUser.require(authentication), request);
    }

    @GetMapping("/{id}")
    public TicketDetailView detail(Authentication authentication, @PathVariable Long id) {
        return ticketService.get(CurrentUser.require(authentication), id);
    }

    @PatchMapping("/{id}")
    public TicketDetailView update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody TicketUpdateRequest request
    ) {
        return ticketService.update(CurrentUser.require(authentication), id, request);
    }

    @PostMapping("/{id}/remarks")
    public TicketDetailView remark(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody RemarkRequest request
    ) {
        return ticketService.addRemark(CurrentUser.require(authentication), id, request);
    }
}
