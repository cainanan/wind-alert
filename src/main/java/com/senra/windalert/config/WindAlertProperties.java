package com.senra.windalert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wind-alert")
public class WindAlertProperties {

    private double latitude;
    private double longitude;
    private double thresholdKmh;
    private int forecastStartHour;
    private int forecastEndHour;
    private String messageWind;
    private String messageCalm;
    private String whatsapperPhp;
    private String whatsapperScript;
    private String stateFile;
    private boolean dryRun;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getThresholdKmh() {
        return thresholdKmh;
    }

    public void setThresholdKmh(double thresholdKmh) {
        this.thresholdKmh = thresholdKmh;
    }

    public int getForecastStartHour() {
        return forecastStartHour;
    }

    public void setForecastStartHour(int forecastStartHour) {
        this.forecastStartHour = forecastStartHour;
    }

    public int getForecastEndHour() {
        return forecastEndHour;
    }

    public void setForecastEndHour(int forecastEndHour) {
        this.forecastEndHour = forecastEndHour;
    }

    public String getMessageWind() {
        return messageWind;
    }

    public void setMessageWind(String messageWind) {
        this.messageWind = messageWind;
    }

    public String getMessageCalm() {
        return messageCalm;
    }

    public void setMessageCalm(String messageCalm) {
        this.messageCalm = messageCalm;
    }

    public String getWhatsapperPhp() {
        return whatsapperPhp;
    }

    public void setWhatsapperPhp(String whatsapperPhp) {
        this.whatsapperPhp = whatsapperPhp;
    }

    public String getWhatsapperScript() {
        return whatsapperScript;
    }

    public void setWhatsapperScript(String whatsapperScript) {
        this.whatsapperScript = whatsapperScript;
    }

    public String getStateFile() {
        return stateFile;
    }

    public void setStateFile(String stateFile) {
        this.stateFile = stateFile;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
}
