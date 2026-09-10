package com.shengdijia.support.service;

import com.shengdijia.support.domain.User;
import com.shengdijia.support.security.SdjUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static User require(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof SdjUserDetails details)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return details.getUser();
    }
}
