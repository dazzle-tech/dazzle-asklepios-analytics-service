package com.dazzle.asklepios.web.rest.vm.kpis;

import com.dazzle.asklepios.domain.enumeration.KpiStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class KpiResponse {

    private String kpi;
    private String label;
    private BigDecimal value;
    private String unit;

    private BigDecimal target;
    private String targetOperator;
    private KpiStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    private Long numerator;
    private Long denominator;

    public KpiResponse() {

    }
}