package com.shengdijia.support.security;

import com.shengdijia.support.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SdjUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public SdjUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(SdjUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("账号不存在"));
    }
}
