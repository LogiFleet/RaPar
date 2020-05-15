import com.opencsv.bean.CsvBindByName;

public class TeltonikaFotaWebDeviceInfoBean {

    @CsvBindByName(column = "imei")
    private String imei;

    @CsvBindByName(column = "sn")
    private String sn;

    @CsvBindByName(column = "model")
    private String model;

    @CsvBindByName(column = "firmware")
    private String firmware;

    @CsvBindByName(column = "configuration")
    private String configuration;

    @CsvBindByName(column = "description")
    private String description;

    @CsvBindByName(column = "companyname")
    private String companyName;

    @CsvBindByName(column = "group")
    private String group;

    @CsvBindByName(column = "lastlogin")
    private String lastLogin;

    // to string

    @Override
    public String toString() {
        return "TeltonikaFotaWebDeviceInfoBean{" +
                "imei='" + imei + '\'' +
                ", sn='" + sn + '\'' +
                ", model='" + model + '\'' +
                ", firmware='" + firmware + '\'' +
                ", configuration='" + configuration + '\'' +
                ", description='" + description + '\'' +
                ", companyName='" + companyName + '\'' +
                ", group='" + group + '\'' +
                ", lastLogin='" + lastLogin + '\'' +
                '}';
    }

    // getters and setters

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }
}