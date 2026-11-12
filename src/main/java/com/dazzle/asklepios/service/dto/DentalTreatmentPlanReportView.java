package com.dazzle.asklepios.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * View model for the dental treatment plan PDF. Values are already formatted
 * for the Thymeleaf template.
 */
public class DentalTreatmentPlanReportView {

    private String lang = "en";
    private String dir = "ltr";
    private String facilityName = "";
    private String planNumber = "";
    private String title = "";
    private String status = "";
    private String patientName = "";
    private String mrn = "";
    private String dob = "";
    private String age = "";
    private String gender = "";
    private String practitioner = "";
    private String originEncounter = "";
    private String created = "";
    private String updated = "";
    private String generatedAt = "";
    private boolean chartAvailable;
    private String chartMessage = "Dental chart unavailable";
    private List<ChartToothView> upperTeeth = new ArrayList<>();
    private List<ChartToothView> lowerTeeth = new ArrayList<>();
    private List<SessionView> sessions = new ArrayList<>();
    private String gross = "0.00";
    private String discount = "0.00";
    private String net = "0.00";
    private List<ImageView> images = new ArrayList<>();

    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }
    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }
    public String getPlanNumber() { return planNumber; }
    public void setPlanNumber(String planNumber) { this.planNumber = planNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getMrn() { return mrn; }
    public void setMrn(String mrn) { this.mrn = mrn; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPractitioner() { return practitioner; }
    public void setPractitioner(String practitioner) { this.practitioner = practitioner; }
    public String getOriginEncounter() { return originEncounter; }
    public void setOriginEncounter(String originEncounter) { this.originEncounter = originEncounter; }
    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }
    public String getUpdated() { return updated; }
    public void setUpdated(String updated) { this.updated = updated; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
    public boolean isChartAvailable() { return chartAvailable; }
    public void setChartAvailable(boolean chartAvailable) { this.chartAvailable = chartAvailable; }
    public String getChartMessage() { return chartMessage; }
    public void setChartMessage(String chartMessage) { this.chartMessage = chartMessage; }
    public List<ChartToothView> getUpperTeeth() { return upperTeeth; }
    public void setUpperTeeth(List<ChartToothView> upperTeeth) { this.upperTeeth = upperTeeth; }
    public List<ChartToothView> getLowerTeeth() { return lowerTeeth; }
    public void setLowerTeeth(List<ChartToothView> lowerTeeth) { this.lowerTeeth = lowerTeeth; }
    public List<SessionView> getSessions() { return sessions; }
    public void setSessions(List<SessionView> sessions) { this.sessions = sessions; }
    public String getGross() { return gross; }
    public void setGross(String gross) { this.gross = gross; }
    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }
    public String getNet() { return net; }
    public void setNet(String net) { this.net = net; }
    public List<ImageView> getImages() { return images; }
    public void setImages(List<ImageView> images) { this.images = images; }

    public static class ChartToothView {
        private String label;
        private String image;
        private boolean highlighted;
        private boolean missing;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        public boolean isHighlighted() { return highlighted; }
        public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }
        public boolean isMissing() { return missing; }
        public void setMissing(boolean missing) { this.missing = missing; }
    }

    public static class SessionView {
        private String title;
        private List<LineView> lines = new ArrayList<>();

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<LineView> getLines() { return lines; }
        public void setLines(List<LineView> lines) { this.lines = lines; }
    }

    public static class LineView {
        private String area;
        private String surface;
        private String cdt;
        private String procedure;
        private String price;
        private String quantity;
        private String discount;
        private String net;
        private String status;
        private boolean cancelled;
        private List<String> details = new ArrayList<>();

        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }
        public String getSurface() { return surface; }
        public void setSurface(String surface) { this.surface = surface; }
        public String getCdt() { return cdt; }
        public void setCdt(String cdt) { this.cdt = cdt; }
        public String getProcedure() { return procedure; }
        public void setProcedure(String procedure) { this.procedure = procedure; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
        public String getDiscount() { return discount; }
        public void setDiscount(String discount) { this.discount = discount; }
        public String getNet() { return net; }
        public void setNet(String net) { this.net = net; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isCancelled() { return cancelled; }
        public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
        public List<String> getDetails() { return details; }
        public void setDetails(List<String> details) { this.details = details; }
    }

    public static class ImageView {
        private String name;
        private String description;
        private String encounter;
        private String uploaded;
        private String src;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getEncounter() { return encounter; }
        public void setEncounter(String encounter) { this.encounter = encounter; }
        public String getUploaded() { return uploaded; }
        public void setUploaded(String uploaded) { this.uploaded = uploaded; }
        public String getSrc() { return src; }
        public void setSrc(String src) { this.src = src; }
    }
}
