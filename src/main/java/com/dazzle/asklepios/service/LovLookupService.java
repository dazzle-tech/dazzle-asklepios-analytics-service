package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LovLookupService {

    private static final Logger LOG = LoggerFactory.getLogger(LovLookupService.class);

    private final ApLovValueRepository apLovValueRepository;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String findDisplayValue(String key) {
        if (key == null || key.isBlank()) return null;
        try {
            return apLovValueRepository.findById(key)
                    .map(ApLovValue::getLovDisplayVale)
                    .orElse(null);
        } catch (Exception e) {
            LOG.warn("[LovLookup] lookup failed for key={}: {}", key, e.getMessage());
            return null;
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, String> findDisplayValues(List<String> keys) {
        if (keys == null || keys.isEmpty()) return Map.of();
        try {
            return apLovValueRepository.findByKeyIn(keys).stream()
                    .collect(Collectors.toMap(ApLovValue::getKey, ApLovValue::getLovDisplayVale));
        } catch (Exception e) {
            LOG.warn("[LovLookup] bulk lookup failed: {}", e.getMessage());
            return Map.of();
        }
    }
}
