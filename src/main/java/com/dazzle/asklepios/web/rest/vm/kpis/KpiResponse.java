package com.dazzle.asklepios.web.rest.vm.kpis;

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
    private String status;

    private LocalDate startDate;
    private LocalDate endDate;

    private Long numerator;
    private Long denominator;

    public KpiResponse() {

    }
}