package com.shengdijia.support.web.dto;

import com.shengdijia.support.domain.Role;

public record UserView(
        Long id,
        String username,
        String displayName,
        String company,
        Role role,
        String phone,
        String email,
        String merchantId
) {
}
