package com.shengdijia.support.web;

import com.shengdijia.support.domain.Role;
import com.shengdijia.support.repo.UserRepository;
import com.shengdijia.support.web.dto.TicketDetailView;
import com.shengdijia.support.web.dto.UserView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MetaController {

    private final UserRepository userRepository;

    public MetaController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "brand", "盛迪嘉支付");
    }

    @GetMapping("/handlers")
    public List<UserView> handlers() {
        return userRepository.findByRoleAndEnabledTrueOrderByDisplayNameAsc(Role.INTERNAL)
                .stream()
                .map(TicketDetailView::toUser)
                .toList();
    }
}
