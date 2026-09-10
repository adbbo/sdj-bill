package com.shengdijia.support.web;

import com.shengdijia.support.service.CurrentUser;
import com.shengdijia.support.service.DashboardService;
import com.shengdijia.support.web.dto.DashboardView;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardView summary(Authentication authentication) {
        return dashboardService.summary(CurrentUser.require(authentication));
    }
}
