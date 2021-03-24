package parser;

import com.opencsv.bean.CsvBindByName;

public class TeltonikaFotaWebDeviceInfoBean {

    @CsvBindByName(column = "imei")
    private String imei;

    @CsvBindByName(column = "description")
    private String description;

    @CsvBindByName(column = "model")
    private String model;

    @CsvBindByName(column = "current_firmware")
    private String currentFirmware;

    @CsvBindByName(column = "current_configuration")
    private String currentConfiguration;

    @CsvBindByName(column = "serial")
    private String serial;

    @CsvBindByName(column = "company_name")
    private String companyName;

    @CsvBindByName(column = "group_name")
    private String groupName;

    @CsvBindByName(column = "seen_at")
    private String seenAt;

    @CsvBindByName(column = "created_at")
    private String createdAt;

    @CsvBindByName(column = "updated_at")
    private String updatedAt;

    // to string

    @Override
    public String toString() {
        return "TeltonikaFotaWebDeviceInfoBean{" +
                "imei='" + imei + '\'' +
                ", description='" + description + '\'' +
                ", model='" + model + '\'' +
                ", currentFirmware='" + currentFirmware + '\'' +
                ", currentConfiguration='" + currentConfiguration + '\'' +
                ", serial='" + serial + '\'' +
                ", companyName='" + companyName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", seenAt='" + seenAt + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    // getters and setters

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCurrentFirmware() {
        return currentFirmware;
    }

    public void setCurrentFirmware(String currentFirmware) {
        this.currentFirmware = currentFirmware;
    }

    public String getCurrentConfiguration() {
        return currentConfiguration;
    }

    public void setCurrentConfiguration(String currentConfiguration) {
        this.currentConfiguration = currentConfiguration;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(String seenAt) {
        this.seenAt = seenAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}
