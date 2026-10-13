package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UserDashboardDefault;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDashboardDefaultRepository extends JpaRepository<UserDashboardDefault, Long> {

    Optional<UserDashboardDefault> findByUserId(Long userId);
}
