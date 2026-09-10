package com.dazzle.asklepios.web.rest.vm.report;

import java.util.List;

public record DesignerDataSourceVM(
        String name,
        String description,
        List<DesignerFieldVM> fields
) {
}
