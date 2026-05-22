package com.asset.VMS.domain;

import java.io.Serializable;
import java.sql.Timestamp;

public class VesselLocation extends Vessel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String vesselEmail;
    private String messageId;
    private String vesselId;
    private String latitude;
    private String longitude;
    private Timestamp receiveDate;
    private String speed;
    private String heading;
    private String proximity;
    private String proximityBearing;
    private String proximityDirection;
    private String proximityLatitude;
    private String proximityLongitude;
    private String customerName;
    private String vesselLocationEmail;
    private String vesselName;
    private String deviceType;
    private String emailReport;
    private String emailReportBcc;
    private String vesselHistoryId;
    private String temperature;
    private String feelsLike;
    private String temperatureMin;
    private String temperatureMax;
    private String pressure;
    private String humidity;
    private String seaLevel;
    private String groundLevel;
    private String visibility;
    private String windSpeed;
    private String windDeg;
    private String windGust;
    private String cloud;
    private String rain1h;
    private String rain3h;
    private String summary;
    private String icon;
    private Timestamp updateAt;
    private Timestamp createAt;
    private String id;
    private String mapImageLocation;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getVesselEmail() {
        return vesselEmail;
    }

    public void setVesselEmail(String vesselEmail) {
        this.vesselEmail = vesselEmail;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getVesselId() {
        return vesselId;
    }

    public void setVesselId(String vesselId) {
        this.vesselId = vesselId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Timestamp getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(Timestamp receiveDate) {
        this.receiveDate = receiveDate;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getProximity() {
        return proximity;
    }

    public void setProximity(String proximity) {
        this.proximity = proximity;
    }

    public String getProximityBearing() {
        return proximityBearing;
    }

    public void setProximityBearing(String proximityBearing) {
        this.proximityBearing = proximityBearing;
    }

    public String getProximityDirection() {
        return proximityDirection;
    }

    public void setProximityDirection(String proximityDirection) {
        this.proximityDirection = proximityDirection;
    }

    public String getProximityLatitude() {
        return proximityLatitude;
    }

    public void setProximityLatitude(String proximityLatitude) {
        this.proximityLatitude = proximityLatitude;
    }

    public String getProximityLongitude() {
        return proximityLongitude;
    }

    public void setProximityLongitude(String proximityLongitude) {
        this.proximityLongitude = proximityLongitude;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getVesselLocationEmail() {
        return vesselLocationEmail;
    }

    public void setVesselLocationEmail(String vesselLocationEmail) {
        this.vesselLocationEmail = vesselLocationEmail;
    }

    public String getVesselName() {
        return vesselName;
    }

    public void setVesselName(String vesselName) {
        this.vesselName = vesselName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getEmailReport() {
        return emailReport;
    }

    public void setEmailReport(String emailReport) {
        this.emailReport = emailReport;
    }

    public String getEmailReportBcc() {
        return emailReportBcc;
    }

    public void setEmailReportBcc(String emailReportBcc) {
        this.emailReportBcc = emailReportBcc;
    }

    public String getVesselHistoryId() {
        return vesselHistoryId;
    }

    public void setVesselHistoryId(String vesselHistoryId) {
        this.vesselHistoryId = vesselHistoryId;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(String feelsLike) {
        this.feelsLike = feelsLike;
    }

    public String getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMin(String temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public String getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperatureMax(String temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getSeaLevel() {
        return seaLevel;
    }

    public void setSeaLevel(String seaLevel) {
        this.seaLevel = seaLevel;
    }

    public String getGroundLevel() {
        return groundLevel;
    }

    public void setGroundLevel(String groundLevel) {
        this.groundLevel = groundLevel;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDeg() {
        return windDeg;
    }

    public void setWindDeg(String windDeg) {
        this.windDeg = windDeg;
    }

    public String getWindGust() {
        return windGust;
    }

    public void setWindGust(String windGust) {
        this.windGust = windGust;
    }

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    public String getRain1h() {
        return rain1h;
    }

    public void setRain1h(String rain1h) {
        this.rain1h = rain1h;
    }

    public String getRain3h() {
        return rain3h;
    }

    public void setRain3h(String rain3h) {
        this.rain3h = rain3h;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Timestamp getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Timestamp updateAt) {
        this.updateAt = updateAt;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMapImageLocation() {
        return mapImageLocation;
    }

    public void setMapImageLocation(String mapImageLocation) {
        this.mapImageLocation = mapImageLocation;
    }
}
