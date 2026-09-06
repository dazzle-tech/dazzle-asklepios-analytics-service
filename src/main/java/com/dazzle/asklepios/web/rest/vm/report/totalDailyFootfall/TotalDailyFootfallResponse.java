package com.dazzle.asklepios.web.rest.vm.report.totalDailyFootfall;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TotalDailyFootfallResponse {

    private String code;
    private String name;
    private String definition;
    private String unit;
    private String frequency;
    private String target;
    private String benchmark;
    private String owner;

    private LocalDate startDate;
    private LocalDate endDate;
    private Long value;

    public TotalDailyFootfallResponse() {
    }

}
