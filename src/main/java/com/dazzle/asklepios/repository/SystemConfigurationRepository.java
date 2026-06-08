package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.SystemConfiguration;
import com.dazzle.asklepios.domain.enumeration.SystemConfigKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigurationRepository
        extends JpaRepository<SystemConfiguration, Long> {

    Optional<SystemConfiguration> findByConfigKey(SystemConfigKey configKey);

    List<SystemConfiguration> findAllByOrderByConfigKeyAsc();
}
