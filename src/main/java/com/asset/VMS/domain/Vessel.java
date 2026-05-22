package com.asset.VMS.domain;

import java.io.Serializable;
import java.time.LocalDate;

public class Vessel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String email;
    private String emailOld;
    private String name;
    private int maxPortRpm;
    private int maxStarboardRpm;
    private String engineType;
    private int dataType;
    private int deleted;
    private int reportType;
    private String emailTrackingAccount;
    private String passwordTrackingAccount;
    private String trackingProtocol;
    private String trackingHost;
    private String trackingPort;
    private VesselDataType vesselDataType;
    private String categoryGsm;

    // === FORMULA CONFIG (DB FIELDS) ===
    // calc_genset_mode : enum('RAW','DIFF')
    // calc_genset_cutoff : date
    // use_safe_diff_erh : tinyint(1)
    private String calcGensetMode; // "RAW" / "DIFF"
    private LocalDate calcGensetCutoff; // bisa null
    private boolean useSafeDiffErh; // true/false

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailOld() {
        return emailOld;
    }

    public void setEmailOld(String emailOld) {
        this.emailOld = emailOld;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxPortRpm() {
        return maxPortRpm;
    }

    public void setMaxPortRpm(int maxPortRpm) {
        this.maxPortRpm = maxPortRpm;
    }

    public int getMaxStarboardRpm() {
        return maxStarboardRpm;
    }

    public void setMaxStarboardRpm(int maxStarboardRpm) {
        this.maxStarboardRpm = maxStarboardRpm;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public int getReportType() {
        return reportType;
    }

    public void setReportType(int reportType) {
        this.reportType = reportType;
    }

    public String getEmailTrackingAccount() {
        return emailTrackingAccount;
    }

    public void setEmailTrackingAccount(String emailTrackingAccount) {
        this.emailTrackingAccount = emailTrackingAccount;
    }

    public String getPasswordTrackingAccount() {
        return passwordTrackingAccount;
    }

    public void setPasswordTrackingAccount(String passwordTrackingAccount) {
        this.passwordTrackingAccount = passwordTrackingAccount;
    }

    public String getTrackingProtocol() {
        return trackingProtocol;
    }

    public void setTrackingProtocol(String trackingProtocol) {
        this.trackingProtocol = trackingProtocol;
    }

    public String getTrackingHost() {
        return trackingHost;
    }

    public void setTrackingHost(String trackingHost) {
        this.trackingHost = trackingHost;
    }

    public String getTrackingPort() {
        return trackingPort;
    }

    public void setTrackingPort(String trackingPort) {
        this.trackingPort = trackingPort;
    }

    public VesselDataType getVesselDataType() {
        return vesselDataType;
    }

    public void setVesselDataType(VesselDataType vesselDataType) {
        this.vesselDataType = vesselDataType;
    }

    public String getCategoryGsm() {
        return categoryGsm;
    }

    public void setCategoryGsm(String categoryGsm) {
        this.categoryGsm = categoryGsm;
    }

    // ===== NEW: getters/setters untuk aturan dari DB =====
    public String getCalcGensetMode() {
        return calcGensetMode;
    }

    public void setCalcGensetMode(String calcGensetMode) {
        this.calcGensetMode = calcGensetMode;
    }

    public LocalDate getCalcGensetCutoff() {
        return calcGensetCutoff;
    }

    public void setCalcGensetCutoff(LocalDate calcGensetCutoff) {
        this.calcGensetCutoff = calcGensetCutoff;
    }

    public boolean isUseSafeDiffErh() {
        return useSafeDiffErh;
    }

    public void setUseSafeDiffErh(boolean useSafeDiffErh) {
        this.useSafeDiffErh = useSafeDiffErh;
    }
}
