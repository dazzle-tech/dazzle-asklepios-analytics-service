package com.dazzle.asklepios.service.dto.reports.procedure;

import java.util.List;

/**
 * Display-ready data for the patient procedure report. Every text value is already formatted
 * (dates in the requested timezone, durations, units), so the template only lays it out.
 */
public record ProcedureReportDTO(
        Header header,
        PatientInfo patient,
        List<Allergy> allergies,
        Request request,
        Registration registration,
        List<TimelineStep> timeline,
        List<Kpi> kpis,
        List<StaffMember> staff,
        PreAssessment preAssessment,
        Performance performance,
        Monitoring monitoring,
        List<VitalsComparisonRow> vitalsComparison,
        List<Medication> medications,
        List<Complication> complications,
        PostCare postCare,
        List<EquipmentItem> equipment,
        Cancellation cancellation,
        List<Signature> signatures
) {

    /**
     * @param statusCode raw {@code ProcStatus} name, used for the localized label
     * @param statusTone css tone: success, danger, info, warning or neutral
     * @param preliminary true while the procedure is neither completed nor cancelled
     */
    public record Header(
            Long procedureRequestId,
            String documentNumber,
            String facilityName,
            String departmentName,
            String procedureName,
            String procedureCode,
            String category,
            String level,
            String priority,
            String statusCode,
            String statusTone,
            boolean preliminary,
            boolean cancelled,
            String printedAt,
            String printedBy
    ) {
    }

    public record PatientInfo(
            String name,
            String mrn,
            String gender,
            String dateOfBirth,
            String age,
            String encounterNumber,
            String encounterDate,
            String encounterType,
            String encounterDepartment,
            String attendingPractitioner
    ) {
    }

    public record Allergy(String allergen, String type, String severity, boolean severe) {
    }

    public record Request(
            String requestedBy,
            String requestedAt,
            String fromDepartment,
            String toFacility,
            String toDepartment,
            String scheduledAt,
            String indication,
            String bodyPart,
            String side,
            String notes,
            String extraDocumentation
    ) {
    }

    public record Registration(
            boolean recorded,
            String consentForm,
            String registeredAt,
            String location,
            String performingPractitioner,
            String performingPractitionerSpecialty
    ) {
    }

    /**
     * @param state done, current or pending
     */
    public record TimelineStep(String labelKey, String time, String subtitle, String state, String tone) {
    }

    /**
     * @param tone css tone used for the accent stripe
     */
    public record Kpi(String labelKey, String value, String hint, String tone) {
    }

    public record StaffMember(String name, String jobRole, String responsibility, boolean present) {
    }

    public record CheckItem(String labelKey, boolean checked) {
    }

    public record VitalsSet(
            String bloodPressure,
            String heartRate,
            String temperature,
            String oxygenSaturation
    ) {
    }

    public record PreAssessment(
            boolean recorded,
            boolean fastingRequired,
            boolean patientPrepared,
            String specialInstructions,
            List<CheckItem> safetyChecklist,
            int safetyChecked,
            VitalsSet vitals,
            String asaScore,
            String airwayGrade
    ) {
    }

    public record Performance(
            boolean recorded,
            String actualStart,
            String actualEnd,
            String duration,
            boolean anesthesiaUsed,
            String anesthesiaType,
            String anesthesiaStart,
            String anesthesiaEnd,
            String anesthesiaDuration,
            String anesthesiaAdministeredBy,
            boolean timeOut,
            String outcome,
            String observations,
            String result,
            String additionalNotes,
            String homeInstruction,
            String homeInstructionNotes
    ) {
    }

    public record VitalReading(
            String recordedAt,
            String heartRate,
            String oxygenSaturation,
            String bloodPressure,
            String respiratoryRate,
            String temperature,
            String etco2,
            String fluidsGiven,
            String urineOutput
    ) {
    }

    /**
     * @param chartSvg inline SVG trend chart, or null when there are fewer than one plottable reading
     */
    public record Monitoring(
            List<VitalReading> readings,
            String chartSvg,
            String heartRateRange,
            String oxygenSaturationLowest,
            String systolicRange,
            String totalFluids,
            String totalUrine
    ) {
    }

    /**
     * @param trend up, down, same or none, comparing post to pre values
     */
    public record VitalsComparisonRow(String labelKey, String pre, String intra, String post, String trend) {
    }

    public record Medication(String name, String dose, String givenAt, String givenBy) {
    }

    /**
     * @param tone css tone derived from the severity text
     */
    public record Complication(String type, String severity, String tone, String actionsTaken, String recordedAt) {
    }

    /**
     * @param score Aldrete points for the selected option, or null when the option has no score
     */
    public record AldreteComponent(String labelKey, String value, String score) {
    }

    public record PostCare(
            boolean recorded,
            List<String> diagnoses,
            String patientCondition,
            VitalsSet vitals,
            String painAssessmentType,
            String painScore,
            Integer painScoreValue,
            String painTone,
            String painDescription,
            List<AldreteComponent> aldreteComponents,
            String aldreteScore,
            Integer aldreteScoreValue,
            boolean aldreteDischargeReady,
            String aldreteRingDash,
            List<CheckItem> dischargeCriteria,
            int dischargeCriteriaMet,
            int dischargeCriteriaPercent,
            boolean nauseaVomiting,
            boolean equipmentCountDone,
            String countStatus,
            String recoveryNotes,
            String additionalObservations,
            String recordedAt,
            String recordedBy
    ) {
    }

    public record EquipmentItem(String name, String type, String quantity, String notes) {
    }

    public record Cancellation(String cancelledAt, String cancelledBy, String reason) {
    }

    public record Signature(String labelKey, String name, String subtitle) {
    }
}
