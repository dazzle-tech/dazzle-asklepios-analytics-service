package com.dazzle.asklepios.service;

import com.dazzle.asklepios.web.rest.vm.report.DesignerDataSourceVM;
import com.dazzle.asklepios.web.rest.vm.report.DesignerFieldVM;
import com.dazzle.asklepios.web.rest.vm.report.DesignerSchemaVM;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StimulsoftDesignerSchemaService {

    public DesignerSchemaVM getSchema() {

        return new DesignerSchemaVM(
                List.of(
                        patient(),
                        encounter(),
                        practitioner(),
                        department()
                )
        );
    }

    private DesignerDataSourceVM patient() {

        return new DesignerDataSourceVM(
                "Patient",
                "Patient information",
                List.of(
                        new DesignerFieldVM(
                                "Id",
                                "Long",
                                "Patient ID"
                        ),
                        new DesignerFieldVM(
                                "Name",
                                "String",
                                "Patient full name"
                        ),
                        new DesignerFieldVM(
                                "MedicalRecordNumber",
                                "String",
                                "Medical record number"
                        ),
                        new DesignerFieldVM(
                                "DateOfBirth",
                                "Date",
                                "Patient date of birth"
                        )
                )
        );
    }

    private DesignerDataSourceVM encounter() {

        return new DesignerDataSourceVM(
                "Encounter",
                "Encounter information",
                List.of(
                        new DesignerFieldVM(
                                "Id",
                                "Long",
                                "Encounter ID"
                        ),
                        new DesignerFieldVM(
                                "Date",
                                "DateTime",
                                "Encounter date"
                        ),
                        new DesignerFieldVM(
                                "Type",
                                "String",
                                "Encounter type"
                        )
                )
        );
    }

    private DesignerDataSourceVM practitioner() {

        return new DesignerDataSourceVM(
                "Practitioner",
                "Practitioner information",
                List.of(
                        new DesignerFieldVM(
                                "Id",
                                "Long",
                                "Practitioner ID"
                        ),
                        new DesignerFieldVM(
                                "Name",
                                "String",
                                "Practitioner name"
                        )
                )
        );
    }

    private DesignerDataSourceVM department() {

        return new DesignerDataSourceVM(
                "Department",
                "Department information",
                List.of(
                        new DesignerFieldVM(
                                "Id",
                                "Long",
                                "Department ID"
                        ),
                        new DesignerFieldVM(
                                "Name",
                                "String",
                                "Department name"
                        )
                )
        );
    }
}