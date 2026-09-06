package com.dazzle.asklepios.web.rest.vm.report;

import java.util.List;

public record DesignerSchemaVM(
        List<DesignerDataSourceVM> dataSources
) {
}
