package com.shengdijia.support.repo;

import com.shengdijia.support.domain.Role;
import com.shengdijia.support.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByRoleAndEnabledTrueOrderByDisplayNameAsc(Role role);

    boolean existsByUsername(String username);
}
