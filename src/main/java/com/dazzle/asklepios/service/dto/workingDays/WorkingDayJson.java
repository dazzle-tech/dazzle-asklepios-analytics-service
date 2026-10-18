package com.dazzle.asklepios.service.dto.workingDays;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingDayJson {
    private String dayOfWeek;
    private Boolean isWorking;
}
