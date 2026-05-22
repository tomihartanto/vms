package com.asset.VMS.service;

import com.asset.VMS.domain.Vessel;
import com.asset.VMS.domain.VesselInbox;
import com.asset.VMS.domain.VesselInfo;
import com.asset.VMS.domain.VesselLocation;
import com.asset.VMS.mapper.VesselMapper;
import com.asset.VMS.util.LatLngConverter;
import com.asset.VMS.util.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailProcessorService {

    private static final Logger logger = LogManager.getLogger(EmailProcessorService.class);

    private final VesselMapper vesselMapper;

    @Autowired
    public EmailProcessorService(VesselMapper vesselMapper) {
        this.vesselMapper = vesselMapper;
    }

    // =========================
    // Common Helpers
    // =========================
    private Matcher getMatcher(String regex, String content) {
        content = StringUtils.normalizeSpace(content);
        content = RegExUtils.replaceAll(content, "[\\*\\- ]", "");
        Pattern p = Pattern.compile(normalizeSpeedRegex(regex));
        return p.matcher(content);
    }

    private String normalizeSpeedRegex(String regex) {
        if (StringUtils.isBlank(regex)) {
            return regex;
        }
        return regex.replaceAll("\\(\\?<speed>[^)]*\\)", "(?<speed>[^,]*)");
    }

    private String sanitizeSpeed(String rawSpeed) {
        if (StringUtils.isBlank(rawSpeed)) {
            return "00.00";
        }

        String value = rawSpeed.trim().replace(",", ".");
        if (!value.matches("\\d+(\\.\\d+)?")) {
            return "00.00";
        }

        try {
            java.math.BigDecimal speed = new java.math.BigDecimal(value);
            if (speed.compareTo(java.math.BigDecimal.ZERO) < 0) {
                return "00.00";
            }

            java.math.BigDecimal max = new java.math.BigDecimal("99.99");
            if (speed.compareTo(max) > 0) {
                return "00.00";
            }

            speed = speed.setScale(2, java.math.RoundingMode.HALF_UP);
            if (speed.compareTo(max) > 0) {
                return "00.00";
            }

            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
            DecimalFormat format = new DecimalFormat("0.00", symbols);
            format.setRoundingMode(java.math.RoundingMode.HALF_UP);
            return format.format(speed);
        } catch (Exception e) {
            return "00.00";
        }
    }

    private String safeGroup(Matcher matcher, String groupName) {
        try {
            return matcher.group(groupName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void markInboxNotProcessed(VesselInbox vesselInbox) {
        try {
            vesselInbox.setIsProcessed(2);
            if (this.vesselMapper.updateVesselInbox(vesselInbox) > 0) {
                logger.info("Mark inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                        + vesselInbox.getSentDate() + "as not processed... done.");
            } else {
                logger.warn("Mark inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                        + vesselInbox.getSentDate() + "as not processed... failed.");
            }
        } catch (Exception e) {
            logger.warn("Mark inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                    + vesselInbox.getSentDate() + "as not processed... failed.");
        }
    }

    private void markInboxProcessed(VesselInbox vesselInbox) {
        try {
            vesselInbox.setIsProcessed(1);
            if (this.vesselMapper.updateVesselInbox(vesselInbox) > 0) {
                logger.info("Mark inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                        + vesselInbox.getSentDate() + "as processed... done.");
            } else {
                logger.warn("Mark inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                        + vesselInbox.getSentDate() + "as processed... failed.");
            }
        } catch (Exception e) {
            logger.warn("Mark inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                    + vesselInbox.getSentDate() + "as processed... failed.");
        }
    }

    /**
     * NOTE: Ini sengaja dibuat sama seperti code kamu sebelumnya:
     * - latitudeDir/longitudeDir untuk SAT lama
     * - latDir/lonDir untuk GSM
     * - kalau group tidak ada, tidak error (safeGroup)
     */
    private void tryCreateVesselLocation(Matcher matcher, VesselInfo vesselInfo, VesselInbox vesselInbox,
            String categoryOrNull) {
        try {
            if (matcher.group("latitude") != null && matcher.group("longitude") != null) {
                String latitude = matcher.group("latitude");
                String longitude = matcher.group("longitude");

                String latDir = null;
                String lonDir = null;

                // 1) SAT format lama
                if (latDir == null)
                    latDir = safeGroup(matcher, "latitudeDir");
                if (lonDir == null)
                    lonDir = safeGroup(matcher, "longitudeDir");

                // 2) GSM / format baru
                if (latDir == null)
                    latDir = safeGroup(matcher, "latDir");
                if (lonDir == null)
                    lonDir = safeGroup(matcher, "lonDir");

                double latDecimal = LatLngConverter.convertDMSToDecimal(latitude, latDir);
                double lonDecimal = LatLngConverter.convertDMSToDecimal(longitude, lonDir);

                VesselLocation vesselLocation = new VesselLocation();
                vesselLocation.setVesselEmail(vesselInfo.getSender());
                vesselLocation.setMessageId(vesselInfo.getEmailId());
                vesselLocation.setVesselId(vesselInfo.getSender());
                vesselLocation.setLatitude(String.valueOf(latDecimal));
                vesselLocation.setLongitude(String.valueOf(lonDecimal));
                vesselLocation.setReceiveDate(vesselInfo.getSentDate());
                vesselLocation.setVesselName(vesselInfo.getName()); // <- tetap sama seperti code kamu

                if (categoryOrNull != null) {
                    vesselLocation.setCategoryGsm(categoryOrNull); // GSM only
                }

                if (matcher.group("course") != null) {
                    vesselLocation.setHeading(matcher.group("course"));
                }

                this.vesselMapper.createVesselLocation(vesselLocation);
            }
        } catch (IllegalArgumentException e) {
            // sama seperti code kamu: SAT log error message, GSM log custom message
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Latitude/Longitude conversion failed: " + e.getMessage());
        }
    }

    // =========================
    // SAT Scheduler
    // =========================
    @Scheduled(cron = "${emailProcessSchedule}")
    public void processEmailInbox() {
        List<Vessel> vesselList = this.vesselMapper.searchVesselEmail(new HashMap<>());

        logger.info("Start checking unprocessed email for all vessels...");

        for (Vessel vessel : vesselList) {
            if (vessel.getDataType() <= 0) {
                continue;
            }

            final String vesselEmail = vessel.getEmail();

            // NOTE: ini sengaja TIDAK diubah, sesuai code kamu sebelumnya
            // final String vesselName = vessel.getName();
            final String vesselName = "TP 223";

            logger.info("Start checking unprocessed email for vessel " + vesselName + " with email "
                    + vesselEmail);

            Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("sender", vesselEmail);
            parameterMap.put("vesselName", vesselName);
            parameterMap.put("isProcessed", "0");
            parameterMap.put("typeInbox", "SAT");

            List<VesselInbox> vesselInboxList = this.vesselMapper.selectVesselInbox(parameterMap);
            logger.info("Number of unprocessed email for " + vesselEmail + ": " + vesselInboxList.size());

            if (vesselInboxList.size() > 0) {
                for (VesselInbox vesselInbox : vesselInboxList) {
                    this.processDataType(vesselInbox);
                }
            }
        }
    }

    private void processDataType(VesselInbox vesselInbox) {
        logger.info("Start processing vessel inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                + vesselInbox.getSentDate());

        if (!vesselInbox.getContent().equalsIgnoreCase("")) {

            Matcher matcher = this.getMatcher(
                    vesselInbox.getVesselDataType().getDataTypeRegex(),
                    vesselInbox.getContent());

            if (matcher.find()) {
                VesselInfo vesselInfo = new VesselInfo();
                vesselInfo.setEmailId(vesselInbox.getEmailId());
                vesselInfo.setSentDate(vesselInbox.getSentDate());
                vesselInfo.setSender(vesselInbox.getSender());

                // ======================
                // Parsing (SAT) - sama persis logic kamu, hanya dirapikan
                // ======================

                // Speed
                try {
                    if (matcher.group("speed") != null) {
                        vesselInfo.setSpeed(sanitizeSpeed(matcher.group("speed")));
                    } else {
                        vesselInfo.setSpeed("00.00");
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setSpeed("00.00");
                }

                // Port RPM
                try {
                    if (matcher.group("portRpm") != null) {
                        vesselInfo.setPortRpm(Util.stringToInt(matcher.group("portRpm")));
                    } else {
                        vesselInfo.setPortRpm(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setPortRpm(0);
                }

                // Starboard RPM
                try {
                    if (matcher.group("starboardRpm") != null) {
                        vesselInfo.setStarboardRpm(Util.stringToInt(matcher.group("starboardRpm")));
                    } else {
                        vesselInfo.setStarboardRpm(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setStarboardRpm(0);
                }

                // Parameter A
                try {
                    if (matcher.group("parameterA") != null) {
                        vesselInfo.setParameterA(Integer.parseInt(matcher.group("parameterA")));
                    } else {
                        vesselInfo.setParameterA(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setParameterA(0);
                }

                // Parameter B
                try {
                    if (matcher.group("parameterB") != null) {
                        vesselInfo.setParameterB(Integer.parseInt(matcher.group("parameterB")));
                    } else {
                        vesselInfo.setParameterB(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setParameterB(0);
                }

                // Parameter C
                try {
                    if (matcher.group("parameterC") != null) {
                        vesselInfo.setParameterC(Integer.parseInt(matcher.group("parameterC")));
                    } else {
                        vesselInfo.setParameterC(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setParameterC(0);
                }

                // Parameter D
                try {
                    String portD = null;
                    String stbdD = null;

                    if (matcher.group("parameterD") != null) {
                        vesselInfo.setParameterD(matcher.group("parameterD"));

                        String parameterD = matcher.group("parameterD");
                        if (parameterD.contains(":")) {
                            String[] parts = parameterD.split(":");
                            portD = parts.length > 0 ? parts[0] : null;
                            stbdD = parts.length > 1 ? parts[1] : null;
                        } else {
                            int len = parameterD.length();
                            switch (len) {
                                case 12:
                                    portD = parameterD.substring(0, 6);
                                    stbdD = parameterD.substring(6, 12);
                                    break;
                                case 8:
                                    portD = parameterD.substring(0, 4);
                                    stbdD = parameterD.substring(4, 8);
                                    break;
                                case 6:
                                    portD = parameterD.substring(0, 3);
                                    stbdD = parameterD.substring(3, 6);
                                    break;
                                case 4:
                                    portD = parameterD;
                                    stbdD = null;
                                    break;
                                default:
                                    portD = null;
                                    stbdD = null;
                                    break;
                            }
                        }

                        vesselInfo.setPortERH(portD != null ? Integer.parseInt(portD) : 0);
                        vesselInfo.setStbdERH(stbdD != null ? Integer.parseInt(stbdD) : 0);

                    } else {
                        vesselInfo.setParameterD("00000000");
                        vesselInfo.setPortERH(0);
                        vesselInfo.setStbdERH(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setParameterD("00000000");
                    vesselInfo.setPortERH(0);
                    vesselInfo.setStbdERH(0);
                }

                // Location (SAT) - sama
                tryCreateVesselLocation(matcher, vesselInfo, vesselInbox, null);

                // Parameter E
                try {
                    if (matcher.group("parameterE") != null) {
                        vesselInfo.setParameterE(Integer.parseInt(matcher.group("parameterE")));
                    } else {
                        vesselInfo.setParameterE(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setParameterE(0);
                }

                int consumption = vesselInfo.getParameterA() - vesselInfo.getParameterB() + vesselInfo.getParameterC();
                vesselInfo.setConsumption(consumption);

                // Port Pitch
                try {
                    if (matcher.group("portPitch") != null) {
                        vesselInfo.setPortPitch(Integer.parseInt(matcher.group("portPitch")));
                    } else {
                        vesselInfo.setPortPitch(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setPortPitch(0);
                }

                // Starboard Pitch
                try {
                    if (matcher.group("starboardPitch") != null) {
                        vesselInfo.setStarboardPitch(Integer.parseInt(matcher.group("starboardPitch")));
                    } else {
                        vesselInfo.setStarboardPitch(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setStarboardPitch(0);
                }

                // Port Shaft
                try {
                    if (matcher.group("portShaft") != null) {
                        vesselInfo.setPortShaft(Integer.parseInt(matcher.group("portShaft")));
                    } else {
                        vesselInfo.setPortShaft(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setPortShaft(0);
                }

                // Starboard Shaft
                try {
                    if (matcher.group("starboardShaft") != null) {
                        vesselInfo.setStarboardShaft(Integer.parseInt(matcher.group("starboardShaft")));
                    } else {
                        vesselInfo.setStarboardShaft(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setStarboardShaft(0);
                }

                // Main Engine RPM
                try {
                    if (matcher.group("mainEngineRpm") != null) {
                        vesselInfo.setMainEngineRpm(Integer.parseInt(matcher.group("mainEngineRpm")));
                    } else {
                        vesselInfo.setMainEngineRpm(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setMainEngineRpm(0);
                }

                // Main Engine Time
                try {
                    if (matcher.group("mainEngineTime") != null) {
                        vesselInfo.setMainEngineTime(Integer.parseInt(matcher.group("mainEngineTime")));
                    } else {
                        vesselInfo.setMainEngineTime(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setMainEngineTime(0);
                }

                // Genset 1
                try {
                    if (matcher.group("genset1Time") != null) {
                        vesselInfo.setGenset1Time(Integer.parseInt(matcher.group("genset1Time")));
                    } else {
                        vesselInfo.setGenset1Time(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setGenset1Time(0);
                }

                // Genset 2
                try {
                    if (matcher.group("genset2Time") != null) {
                        vesselInfo.setGenset2Time(Integer.parseInt(matcher.group("genset2Time")));
                    } else {
                        vesselInfo.setGenset2Time(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setGenset2Time(0);
                }

                // Genset 3
                try {
                    if (matcher.group("genset3Time") != null) {
                        vesselInfo.setGenset3Time(Integer.parseInt(matcher.group("genset3Time")));
                    } else {
                        vesselInfo.setGenset3Time(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setGenset3Time(0);
                }

                // Genset 4
                try {
                    if (matcher.group("genset4Time") != null) {
                        vesselInfo.setGenset4Time(Integer.parseInt(matcher.group("genset4Time")));
                    } else {
                        vesselInfo.setGenset4Time(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setGenset4Time(0);
                }

                // Genset 5
                try {
                    if (matcher.group("genset5Time") != null) {
                        vesselInfo.setGenset5Time(Integer.parseInt(matcher.group("genset5Time")));
                    } else {
                        vesselInfo.setGenset5Time(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setGenset5Time(0);
                }

                // MDO
                try {
                    if (matcher.group("mdo") != null) {
                        vesselInfo.setMdo(Integer.parseInt(matcher.group("mdo")));
                    } else {
                        vesselInfo.setMdo(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setMdo(0);
                }

                // HFO
                try {
                    if (matcher.group("hfo") != null) {
                        vesselInfo.setHfo(Integer.parseInt(matcher.group("hfo")));
                    } else {
                        vesselInfo.setHfo(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setHfo(0);
                }

                // Bostr Pump
                try {
                    if (matcher.group("bostrPump") != null) {
                        vesselInfo.setBostrPump(Double.parseDouble(matcher.group("bostrPump")));
                    } else {
                        vesselInfo.setBostrPump(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setBostrPump(0);
                }

                // Engine In
                try {
                    if (matcher.group("engineIn") != null) {
                        vesselInfo.setEngineIn(Double.parseDouble(matcher.group("engineIn")));
                    } else {
                        vesselInfo.setEngineIn(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setEngineIn(0);
                }

                // Daily Tank 1
                try {
                    if (matcher.group("dailyTank1") != null) {
                        vesselInfo.setDailyTank1(Double.parseDouble(matcher.group("dailyTank1")));
                    } else {
                        vesselInfo.setDailyTank1(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setDailyTank1(0);
                }

                // Daily Tank 2
                try {
                    if (matcher.group("dailyTank2") != null) {
                        vesselInfo.setDailyTank2(Double.parseDouble(matcher.group("dailyTank2")));
                    } else {
                        vesselInfo.setDailyTank2(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setDailyTank2(0);
                }

                // Boiler Run Time
                try {
                    if (matcher.group("boilerRunTime") != null) {
                        vesselInfo.setBoilerRunTime(Integer.parseInt(matcher.group("boilerRunTime")));
                    } else {
                        vesselInfo.setBoilerRunTime(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setBoilerRunTime(0);
                }

                // Parameter F
                try {
                    String portF = null;
                    String stbdF = null;
                    if (matcher.group("parameterF") != null) {
                        vesselInfo.setParameterF(matcher.group("parameterF"));

                        String parameterF = matcher.group("parameterF");
                        int len = parameterF.length();

                        switch (len) {
                            case 12:
                                portF = parameterF.substring(0, 6);
                                stbdF = parameterF.substring(6, 12);
                                break;
                            case 8:
                                portF = parameterF.substring(0, 4);
                                stbdF = parameterF.substring(4, 8);
                                break;
                            case 4:
                                portF = parameterF;
                                stbdF = null;
                                break;
                            default:
                                break;
                        }

                        vesselInfo.setPortGRH(portF != null ? Integer.parseInt(portF) : 0);
                        vesselInfo.setStbdGRH(stbdF != null ? Integer.parseInt(stbdF) : 0);
                    } else {
                        vesselInfo.setParameterF("00000000");
                        vesselInfo.setPortGRH(0);
                        vesselInfo.setStbdGRH(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setParameterF("00000000");
                    vesselInfo.setPortGRH(0);
                    vesselInfo.setStbdGRH(0);
                }

                // Parameter G
                try {
                    String portG = null;
                    String stbdG = null;
                    if (matcher.group("parameterG") != null) {
                        vesselInfo.setParameterG(matcher.group("parameterG"));

                        String parameterG = matcher.group("parameterG");
                        int len = parameterG.length();

                        switch (len) {
                            case 8:
                                portG = parameterG.substring(0, 4);
                                stbdG = parameterG.substring(4, 8);
                                break;
                            case 4:
                                portG = parameterG;
                                stbdG = null;
                                break;
                            default:
                                break;
                        }

                        vesselInfo.setPortShaftRPM(portG != null ? Integer.parseInt(portG) : 0);
                        vesselInfo.setStbdShaftRPM(stbdG != null ? Integer.parseInt(stbdG) : 0);
                    } else {
                        vesselInfo.setParameterG("00000000");
                        vesselInfo.setPortShaftRPM(0);
                        vesselInfo.setStbdShaftRPM(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setParameterG("00000000");
                    vesselInfo.setPortShaftRPM(0);
                    vesselInfo.setStbdShaftRPM(0);
                }

                // Parameter H
                try {
                    String portH = null;
                    String stbdH = null;
                    if (matcher.group("parameterH") != null) {
                        vesselInfo.setParameterH(matcher.group("parameterH"));

                        String parameterH = matcher.group("parameterH");
                        int len = parameterH.length();

                        switch (len) {
                            case 12:
                                portH = parameterH.substring(0, 6);
                                stbdH = parameterH.substring(6, 12);
                                break;
                            case 8:
                                portH = parameterH.substring(0, 4);
                                stbdH = parameterH.substring(4, 8);
                                break;
                            case 4:
                                portH = parameterH;
                                stbdH = null;
                                break;
                            default:
                                break;
                        }

                        vesselInfo.setPortShaftRunningHours(portH != null ? Integer.parseInt(portH) : 0);
                        vesselInfo.setStbdShaftRunningHours(stbdH != null ? Integer.parseInt(stbdH) : 0);
                    } else {
                        vesselInfo.setParameterH("00000000");
                        vesselInfo.setPortShaftRunningHours(0);
                        vesselInfo.setStbdShaftRunningHours(0);
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setParameterH("00000000");
                    vesselInfo.setPortShaftRunningHours(0);
                    vesselInfo.setStbdShaftRunningHours(0);
                }

                // Parameter I
                try {
                    if (matcher.group("parameterI") != null) {
                        vesselInfo.setParameterI(matcher.group("parameterI"));
                    } else {
                        vesselInfo.setParameterI("0");
                    }
                } catch (IllegalArgumentException e) {
                    vesselInfo.setParameterI("0");
                }

                this.processVesselInfo(vesselInfo, vesselInbox);
            } else {
                markInboxNotProcessed(vesselInbox);
            }

        } else {
            markInboxNotProcessed(vesselInbox);
        }
    }

    private void processVesselInfo(VesselInfo vesselInfo, VesselInbox vesselInbox) {
        try {
            if (this.vesselMapper.createVesselInfo(vesselInfo) > 0) {
                logger.info("Processing vessel inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                        + vesselInbox.getSentDate() + "... done.");
            }
        } catch (Exception e) {
            logger.warn("Processing vessel inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                    + vesselInbox.getSentDate() + "... failed. Data exist.");
        } finally {
            markInboxProcessed(vesselInbox);
        }
    }

    // =========================
    // Windy API (same functions)
    // =========================
    public Map<String, Object> getWindyForecastData() {
        double latitude = 0.8058633333333333;
        double longitude = 117.90344666666667;
        String timeSent = "2025-06-16 10:40:52";

        String forecastData = getPointForecast(latitude, longitude);
        String timestampStr = timestampConverter(timeSent);

        Map<String, Object> result = new HashMap<>();
        result.put("convertedTimestamp", timestampStr);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(forecastData);
            JsonNode gfsNode = root.path("gfs");
            JsonNode gfsWaveNode = root.path("gfsWave");

            JsonNode tsNode = gfsNode.path("ts");
            List<Long> tsList = new ArrayList<>();
            for (JsonNode tsElem : tsNode) {
                tsList.add(tsElem.asLong());
            }

            long targetTimestamp = Long.parseLong(timestampStr);
            int nearestIndex = findNearestTimestampIndex(tsList, targetTimestamp);
            result.put("timestamp", tsList.get(nearestIndex));

            // ================== GFS ==================
            JsonNode windUSurface = gfsNode.path("wind_u-surface");
            JsonNode windVSurface = gfsNode.path("wind_v-surface");
            JsonNode pressureSurface = gfsNode.path("pressure-surface");

            if (nearestIndex < windUSurface.size() && nearestIndex < windVSurface.size()
                    && nearestIndex < pressureSurface.size()) {

                double windU = windUSurface.get(nearestIndex).asDouble();
                double windV = windVSurface.get(nearestIndex).asDouble();
                double pressure = pressureSurface.get(nearestIndex).asDouble();
                double pressureHpa = pressure / 100.0;
                double windSpeed = Math.sqrt(windU * windU + windV * windV);
                double windDirection = (Math.toDegrees(Math.atan2(windU, windV)) + 180) % 360;

                result.put("windU", windU);
                result.put("windV", windV);
                result.put("windSpeed", round(windSpeed, 2));
                result.put("windDirection", round(windDirection, 2));
                result.put("pressure", round(pressureHpa, 2));
                result.put("unit", gfsNode.path("units").path("wind_u-surface").asText());
            } else {
                result.put("message_gfs", "Wind or pressure data unavailable");
            }

            // ================== GFS WAVE ==================
            JsonNode waveHeightNode = gfsWaveNode.path("waves_height-surface");
            JsonNode windWavePeriodNode = gfsWaveNode.path("wwaves_period-surface");
            JsonNode swell1PeriodNode = gfsWaveNode.path("swell1_period-surface");
            JsonNode swell1DirectionNode = gfsWaveNode.path("swell1_direction-surface");
            JsonNode waveUnits = gfsWaveNode.path("units");

            if (nearestIndex < waveHeightNode.size()
                    && nearestIndex < windWavePeriodNode.size()
                    && nearestIndex < swell1PeriodNode.size()
                    && nearestIndex < swell1DirectionNode.size()) {

                double waveHeight = waveHeightNode.get(nearestIndex).asDouble();
                double windWavePeriod = windWavePeriodNode.get(nearestIndex).asDouble(0);
                double primaryWavePeriod = swell1PeriodNode.get(nearestIndex).asDouble(0);
                double primaryWaveDirection = swell1DirectionNode.get(nearestIndex).asDouble(0);

                result.put("waveHeight",
                        round(waveHeight, 2) + " " + waveUnits.path("waves_height-surface").asText("m"));
                result.put("windWavePeriod",
                        round(windWavePeriod, 2) + " " + waveUnits.path("wwaves_period-surface").asText("s"));
                result.put("primaryWavePeriod",
                        round(primaryWavePeriod, 2) + " " + waveUnits.path("swell1_period-surface").asText("s"));
                result.put("primaryWaveDirection", round(primaryWaveDirection, 2) + " "
                        + waveUnits.path("swell1_direction-surface").asText("deg"));
            } else {
                result.put("message_wave", "Wave data unavailable");
            }

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return result;
    }

    private double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public String getPointForecast(double latitude, double longitude) {
        String apiKey = "${WINDY_API_KEY}";
        String url = "https://api.windy.com/api/point-forecast/v2";

        try {
            HttpClient client = HttpClient.newHttpClient();

            // === Request 1: Atmospheric (GFS) ===
            String payloadAtmos = String.format(
                    """
                            {
                                "lat": %.6f,
                                "lon": %.6f,
                                "model": "gfs",
                                "parameters": [
                                    "pressure",
                                    "wind"
                                ],
                                "levels": ["surface"],
                                "key": "%s"
                            }
                            """,
                    latitude, longitude, apiKey);

            HttpRequest requestAtmos = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadAtmos))
                    .build();

            HttpResponse<String> responseAtmos = client.send(requestAtmos, HttpResponse.BodyHandlers.ofString());

            // === Request 2: Marine (GFSWave) ===
            String payloadMarine = String.format(
                    """
                            {
                                "lat": %.6f,
                                "lon": %.6f,
                                "model": "gfsWave",
                                "parameters": [
                                    "waves",
                                    "windWaves",
                                    "swell1",
                                    "swell2"
                                ],
                                "levels": ["surface"],
                                "key": "%s"
                            }
                            """,
                    latitude, longitude, apiKey);

            HttpRequest requestMarine = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadMarine))
                    .build();

            HttpResponse<String> responseMarine = client.send(requestMarine, HttpResponse.BodyHandlers.ofString());

            return String.format(
                    "{ \"gfs\": %s, \"gfsWave\": %s }",
                    responseAtmos.body(),
                    responseMarine.body());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getClosestWindData(String jsonResponse, long vesselTimestamp) {
        JSONObject json = new JSONObject(jsonResponse);

        JSONArray tsArray = json.getJSONArray("ts");
        JSONArray windUArray = json.getJSONArray("wind_u-surface");
        JSONArray windVArray = json.getJSONArray("wind_v-surface");

        int closestIndex = 0;
        long minDiff = Long.MAX_VALUE;

        for (int i = 0; i < tsArray.length(); i++) {
            long ts = tsArray.getLong(i);
            long diff = Math.abs(vesselTimestamp - ts);
            if (diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }

        long closestTimestamp = tsArray.getLong(closestIndex);
        double windU = windUArray.getDouble(closestIndex);
        double windV = windVArray.getDouble(closestIndex);

        return String.format("Closest TS: %d\nWind U: %.2f\nWind V: %.2f",
                closestTimestamp, windU, windV);
    }

    public static String timestampConverter(String timeSent) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(timeSent, formatter);
            long timestamp = dateTime.atZone(ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();
            return String.valueOf(timestamp);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int findNearestTimestampIndex(List<Long> timestamps, long targetTimestamp) {
        int nearestIndex = 0;
        long minDiff = Math.abs(targetTimestamp - timestamps.get(0));

        System.out.println("==============");
        System.out.println("Target Timestamp (millis): " + targetTimestamp);
        System.out.println("Target Time (LocalDateTime): " +
                Instant.ofEpochMilli(targetTimestamp)
                        .atZone(ZoneId.of("Asia/Jakarta"))
                        .toLocalDateTime());

        for (int i = 1; i < timestamps.size(); i++) {
            long currentDiff = Math.abs(targetTimestamp - timestamps.get(i));
            if (currentDiff < minDiff) {
                minDiff = currentDiff;
                nearestIndex = i;
            }
        }

        System.out.println("Nearest Timestamp (millis): " + timestamps.get(nearestIndex));
        System.out.println("Nearest Time (LocalDateTime): " +
                Instant.ofEpochMilli(timestamps.get(nearestIndex))
                        .atZone(ZoneId.of("Asia/Jakarta"))
                        .toLocalDateTime());
        System.out.println("At index: " + nearestIndex);
        System.out.println("==============");

        return nearestIndex;
    }

    // =========================
    // GSM Scheduler
    // =========================
    @Scheduled(cron = "${emailProcessScheduleGsm}")
    public void getGSMData() {
        // NOTE: ini variabel memang tidak dipakai di code kamu, tetap aman kalau mau
        // dibiarkan.
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("isProcessed", "0");
        parameterMap.put("typeInbox", "GSM");

        List<Vessel> vesselList = this.vesselMapper.searchVesselEmail(new HashMap<>());

        List<VesselInbox> vesselInboxList = this.vesselMapper.selectVesselInboxGsm(parameterMap);

        Pattern categoryPattern = Pattern.compile("Category\\s*:\\s*(\\d+)");
        Pattern messagePattern = Pattern.compile("(?m)^S[\\d.,A-Z']+");

        logger.info("Number of unprocessed inbox: " + vesselInboxList.size());
        if (vesselInboxList.size() > 0) {
            for (VesselInbox vesselInbox : vesselInboxList) {
                this.processDataTypeGsm(vesselInbox);
            }
        }
    }

    private void processDataTypeGsm(VesselInbox vesselInbox) {
        logger.info("Start processing vessel inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                + vesselInbox.getSentDate());

        if (!vesselInbox.getContent().equalsIgnoreCase("")) {

            Pattern categoryPattern = Pattern.compile("Category\\s*:\\s*(\\d+)");
            Pattern messagePattern = Pattern.compile(
                    "Message\\s*:\\s*(?!\\*+$)(?:\\R)?((?:[\\s\\S](?!^\\w+\\s*:))*[\\s\\S]*)",
                    Pattern.MULTILINE);

            String category = null;
            String message = null;

            Matcher categoryMatcher = categoryPattern.matcher(vesselInbox.getContent());
            boolean foundCategory = categoryMatcher.find();
            if (foundCategory) {
                category = categoryMatcher.group(1);
            }

            Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("categoryGsm", category);
            List<Vessel> vesselList = this.vesselMapper.searchVesselEmail(parameterMap);

            for (Vessel vessel : vesselList) {
                vesselInbox.setName(vessel.getName());
            }

            Matcher messageMatcher = messagePattern.matcher(vesselInbox.getContent());
            boolean foundMessage = messageMatcher.find();
            if (foundMessage) {
                message = messageMatcher.group(1)
                        .replaceAll("\\R", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
            }

            String regex = null;

            // Ambil vessel by category_gsm (kamu sudah lakukan ini)
            Map<String, Object> parameterMap2 = new HashMap<>();
            parameterMap2.put("categoryGsm", category);
            List<Vessel> vesselList2 = this.vesselMapper.searchVesselEmail(parameterMap2);

            // Set nama kapal ke inbox (kamu sudah lakukan)
            for (Vessel vessel : vesselList2) {
                vesselInbox.setName(vessel.getName());
            }

            // Ambil regex dari t_data_type via association (getVesselDataType)
            if (vesselList2 != null && !vesselList2.isEmpty()) {
                Vessel v = vesselList2.get(0);
                if (v.getVesselDataType() != null && StringUtils.isNotBlank(v.getVesselDataType().getDataTypeRegex())) {
                    regex = v.getVesselDataType().getDataTypeRegex();
                }
            }

            // Fallback kalau data_type belum diset / regex kosong
            if (StringUtils.isBlank(regex)) {
                logger.warn("GSM regex not found in DB (category_gsm={}), fallback to default GSM regex", category);

                regex = "(S)(?<speed>\\d+\\.\\d+)(,)?(P(?<portRpm>\\d+))?(,)?(S(?<starboardRpm>\\d+))?(,)?"
                        + "(A(?<parameterA>\\d+))?(,)?(B(?<parameterB>\\d+))?(,)?(C(?<parameterC>\\d+))?(,)?"
                        + "(D(?<parameterD>\\d+))?(,)?(F(?<parameterF>\\d+))?(,)?(G(?<parameterG>\\d+))?(,)?"
                        + "(?<lonDir>[EW])(?<latitude>\\d+'\\d+\\.\\d+)(,)?(?<latDir>[NS])(?<longitude>\\d+'\\d+\\.\\d+)?(,)?"
                        + "(E)?(\\s*)?(,)?(?<course>\\d+)?(,)?(H(?<parameterH>\\d+))?(,)?(I(?<parameterI>\\d*\\.?\\d+))?";
            }

            if (message != null) {
                Matcher matcher = this.getMatcher(regex, message);

                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

                if (matcher.find()) {
                    VesselInfo vesselInfo = new VesselInfo();

                    String originalEmailId = vesselInbox.getEmailId();
                    boolean hasTimestamp = originalEmailId.matches(".*_\\d{14}$");
                    String finalEmailId = hasTimestamp ? originalEmailId : originalEmailId + "_" + timestamp;

                    // NOTE: ini sengaja sama seperti code kamu:
                    // vesselInfo.setEmailId(finalEmailId); <-- tetap dikomentari
                    logger.info("finalEmailId : " + finalEmailId);

                    vesselInfo.setEmailId(vesselInbox.getEmailId()); // <- tetap seperti sebelumnya
                    vesselInfo.setSentDate(vesselInbox.getSentDate());
                    vesselInfo.setSender(vesselInbox.getSender() + "_" + timestamp);
                    vesselInfo.setCategoryGsm(category);

                    // Speed
                    try {
                        if (matcher.group("speed") != null) {
                            vesselInfo.setSpeed(sanitizeSpeed(matcher.group("speed")));
                        } else {
                            vesselInfo.setSpeed("00.00");
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setSpeed("00.00");
                    }

                    // Port RPM
                    try {
                        if (matcher.group("portRpm") != null) {
                            vesselInfo.setPortRpm(Util.stringToInt(matcher.group("portRpm")));
                        } else {
                            vesselInfo.setPortRpm(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setPortRpm(0);
                    }

                    // Starboard RPM
                    try {
                        if (matcher.group("starboardRpm") != null) {
                            vesselInfo.setStarboardRpm(Util.stringToInt(matcher.group("starboardRpm")));
                        } else {
                            vesselInfo.setStarboardRpm(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setStarboardRpm(0);
                    }

                    // Parameter A
                    try {
                        if (matcher.group("parameterA") != null) {
                            vesselInfo.setParameterA(Integer.parseInt(matcher.group("parameterA")));
                        } else {
                            vesselInfo.setParameterA(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setParameterA(0);
                    }

                    // Parameter B
                    try {
                        if (matcher.group("parameterB") != null) {
                            vesselInfo.setParameterB(Integer.parseInt(matcher.group("parameterB")));
                        } else {
                            vesselInfo.setParameterB(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setParameterB(0);
                    }

                    // Parameter C
                    try {
                        if (matcher.group("parameterC") != null) {
                            vesselInfo.setParameterC(Integer.parseInt(matcher.group("parameterC")));
                        } else {
                            vesselInfo.setParameterC(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setParameterC(0);
                    }

                    // Parameter D
                    try {
                        String portD = null;
                        String stbdD = null;

                        if (matcher.group("parameterD") != null) {
                            vesselInfo.setParameterD(matcher.group("parameterD"));

                            String parameterD = matcher.group("parameterD");
                            if (parameterD.contains(":")) {
                                String[] parts = parameterD.split(":");
                                portD = parts.length > 0 ? parts[0] : null;
                                stbdD = parts.length > 1 ? parts[1] : null;
                            } else {
                                int len = parameterD.length();
                                switch (len) {
                                    case 12:
                                        portD = parameterD.substring(0, 6);
                                        stbdD = parameterD.substring(6, 12);
                                        break;
                                    case 8:
                                        portD = parameterD.substring(0, 4);
                                        stbdD = parameterD.substring(4, 8);
                                        break;
                                    case 6:
                                        portD = parameterD.substring(0, 3);
                                        stbdD = parameterD.substring(3, 6);
                                        break;
                                    case 4:
                                        portD = parameterD;
                                        stbdD = null;
                                        break;
                                    default:
                                        portD = null;
                                        stbdD = null;
                                        break;
                                }
                            }

                            vesselInfo.setPortERH(portD != null ? Integer.parseInt(portD) : 0);
                            vesselInfo.setStbdERH(stbdD != null ? Integer.parseInt(stbdD) : 0);

                        } else {
                            vesselInfo.setParameterD("00000000");
                            vesselInfo.setPortERH(0);
                            vesselInfo.setStbdERH(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setParameterD("00000000");
                        vesselInfo.setPortERH(0);
                        vesselInfo.setStbdERH(0);
                    }

                    // Location (GSM) - sama (pakai category)
                    try {
                        tryCreateVesselLocation(matcher, vesselInfo, vesselInbox, category);
                    } catch (Exception e) {
                        logger.error("Latitude/Longitude conversion failed: " + e.getMessage());
                    }

                    // Parameter E
                    try {
                        if (matcher.group("parameterE") != null) {
                            vesselInfo.setParameterE(Integer.parseInt(matcher.group("parameterE")));
                        } else {
                            vesselInfo.setParameterE(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setParameterE(0);
                    }

                    int consumption = vesselInfo.getParameterA() - vesselInfo.getParameterB()
                            + vesselInfo.getParameterC();
                    vesselInfo.setConsumption(consumption);

                    // Port Pitch
                    try {
                        if (matcher.group("portPitch") != null) {
                            vesselInfo.setPortPitch(Integer.parseInt(matcher.group("portPitch")));
                        } else {
                            vesselInfo.setPortPitch(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setPortPitch(0);
                    }

                    // Starboard Pitch
                    try {
                        if (matcher.group("starboardPitch") != null) {
                            vesselInfo.setStarboardPitch(Integer.parseInt(matcher.group("starboardPitch")));
                        } else {
                            vesselInfo.setStarboardPitch(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setStarboardPitch(0);
                    }

                    // Port Shaft
                    try {
                        if (matcher.group("portShaft") != null) {
                            vesselInfo.setPortShaft(Integer.parseInt(matcher.group("portShaft")));
                        } else {
                            vesselInfo.setPortShaft(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setPortShaft(0);
                    }

                    // Starboard Shaft
                    try {
                        if (matcher.group("starboardShaft") != null) {
                            vesselInfo.setStarboardShaft(Integer.parseInt(matcher.group("starboardShaft")));
                        } else {
                            vesselInfo.setStarboardShaft(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setStarboardShaft(0);
                    }

                    // Main Engine RPM
                    try {
                        if (matcher.group("mainEngineRpm") != null) {
                            vesselInfo.setMainEngineRpm(Integer.parseInt(matcher.group("mainEngineRpm")));
                        } else {
                            vesselInfo.setMainEngineRpm(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setMainEngineRpm(0);
                    }

                    // Main Engine Time
                    try {
                        if (matcher.group("mainEngineTime") != null) {
                            vesselInfo.setMainEngineTime(Integer.parseInt(matcher.group("mainEngineTime")));
                        } else {
                            vesselInfo.setMainEngineTime(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setMainEngineTime(0);
                    }

                    // Gensets
                    try {
                        if (matcher.group("genset1Time") != null) {
                            vesselInfo.setGenset1Time(Integer.parseInt(matcher.group("genset1Time")));
                        } else {
                            vesselInfo.setGenset1Time(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setGenset1Time(0);
                    }

                    try {
                        if (matcher.group("genset2Time") != null) {
                            vesselInfo.setGenset2Time(Integer.parseInt(matcher.group("genset2Time")));
                        } else {
                            vesselInfo.setGenset2Time(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setGenset2Time(0);
                    }

                    try {
                        if (matcher.group("genset3Time") != null) {
                            vesselInfo.setGenset3Time(Integer.parseInt(matcher.group("genset3Time")));
                        } else {
                            vesselInfo.setGenset3Time(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setGenset3Time(0);
                    }

                    try {
                        if (matcher.group("genset4Time") != null) {
                            vesselInfo.setGenset4Time(Integer.parseInt(matcher.group("genset4Time")));
                        } else {
                            vesselInfo.setGenset4Time(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setGenset4Time(0);
                    }

                    try {
                        if (matcher.group("genset5Time") != null) {
                            vesselInfo.setGenset5Time(Integer.parseInt(matcher.group("genset5Time")));
                        } else {
                            vesselInfo.setGenset5Time(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setGenset5Time(0);
                    }

                    // MDO
                    try {
                        if (matcher.group("mdo") != null) {
                            vesselInfo.setMdo(Integer.parseInt(matcher.group("mdo")));
                        } else {
                            vesselInfo.setMdo(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setMdo(0);
                    }

                    // HFO
                    try {
                        if (matcher.group("hfo") != null) {
                            vesselInfo.setHfo(Integer.parseInt(matcher.group("hfo")));
                        } else {
                            vesselInfo.setHfo(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setHfo(0);
                    }

                    // Bostr Pump
                    try {
                        if (matcher.group("bostrPump") != null) {
                            vesselInfo.setBostrPump(Double.parseDouble(matcher.group("bostrPump")));
                        } else {
                            vesselInfo.setBostrPump(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setBostrPump(0);
                    }

                    // Engine In
                    try {
                        if (matcher.group("engineIn") != null) {
                            vesselInfo.setEngineIn(Double.parseDouble(matcher.group("engineIn")));
                        } else {
                            vesselInfo.setEngineIn(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setEngineIn(0);
                    }

                    // Daily Tank 1
                    try {
                        if (matcher.group("dailyTank1") != null) {
                            vesselInfo.setDailyTank1(Double.parseDouble(matcher.group("dailyTank1")));
                        } else {
                            vesselInfo.setDailyTank1(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setDailyTank1(0);
                    }

                    // Daily Tank 2
                    try {
                        if (matcher.group("dailyTank2") != null) {
                            vesselInfo.setDailyTank2(Double.parseDouble(matcher.group("dailyTank2")));
                        } else {
                            vesselInfo.setDailyTank2(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setDailyTank2(0);
                    }

                    // Boiler Run Time
                    try {
                        if (matcher.group("boilerRunTime") != null) {
                            vesselInfo.setBoilerRunTime(Integer.parseInt(matcher.group("boilerRunTime")));
                        } else {
                            vesselInfo.setBoilerRunTime(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setBoilerRunTime(0);
                    }

                    // Parameter F
                    try {
                        String portF = null;
                        String stbdF = null;
                        if (matcher.group("parameterF") != null) {
                            vesselInfo.setParameterF(matcher.group("parameterF"));

                            String parameterF = matcher.group("parameterF");
                            int len = parameterF.length();

                            switch (len) {
                                case 12:
                                    portF = parameterF.substring(0, 6);
                                    stbdF = parameterF.substring(6, 12);
                                    break;
                                case 8:
                                    portF = parameterF.substring(0, 4);
                                    stbdF = parameterF.substring(4, 8);
                                    break;
                                case 4:
                                    portF = parameterF;
                                    stbdF = null;
                                    break;
                                default:
                                    break;
                            }

                            vesselInfo.setPortGRH(portF != null ? Integer.parseInt(portF) : 0);
                            vesselInfo.setStbdGRH(stbdF != null ? Integer.parseInt(stbdF) : 0);
                        } else {
                            vesselInfo.setParameterF("00000000");
                            vesselInfo.setPortGRH(0);
                            vesselInfo.setStbdGRH(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setParameterF("00000000");
                        vesselInfo.setPortGRH(0);
                        vesselInfo.setStbdGRH(0);
                    }

                    // Parameter G
                    try {
                        String portG = null;
                        String stbdG = null;
                        if (matcher.group("parameterG") != null) {
                            vesselInfo.setParameterG(matcher.group("parameterG"));

                            String parameterG = matcher.group("parameterG");
                            int len = parameterG.length();

                            switch (len) {
                                case 8:
                                    portG = parameterG.substring(0, 4);
                                    stbdG = parameterG.substring(4, 8);
                                    break;
                                case 4:
                                    portG = parameterG;
                                    stbdG = null;
                                    break;
                                default:
                                    break;
                            }

                            vesselInfo.setPortShaftRPM(portG != null ? Integer.parseInt(portG) : 0);
                            vesselInfo.setStbdShaftRPM(stbdG != null ? Integer.parseInt(stbdG) : 0);
                        } else {
                            vesselInfo.setParameterG("00000000");
                            vesselInfo.setPortShaftRPM(0);
                            vesselInfo.setStbdShaftRPM(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setParameterG("00000000");
                        vesselInfo.setPortShaftRPM(0);
                        vesselInfo.setStbdShaftRPM(0);
                    }

                    // Parameter H
                    try {
                        String portH = null;
                        String stbdH = null;
                        if (matcher.group("parameterH") != null) {
                            vesselInfo.setParameterH(matcher.group("parameterH"));

                            String parameterH = matcher.group("parameterH");
                            int len = parameterH.length();

                            switch (len) {
                                case 12:
                                    portH = parameterH.substring(0, 6);
                                    stbdH = parameterH.substring(6, 12);
                                    break;
                                case 8:
                                    portH = parameterH.substring(0, 4);
                                    stbdH = parameterH.substring(4, 8);
                                    break;
                                case 4:
                                    portH = parameterH;
                                    stbdH = null;
                                    break;
                                default:
                                    break;
                            }

                            vesselInfo.setPortShaftRunningHours(portH != null ? Integer.parseInt(portH) : 0);
                            vesselInfo.setStbdShaftRunningHours(stbdH != null ? Integer.parseInt(stbdH) : 0);
                        } else {
                            vesselInfo.setParameterH("00000000");
                            vesselInfo.setPortShaftRunningHours(0);
                            vesselInfo.setStbdShaftRunningHours(0);
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setParameterH("00000000");
                        vesselInfo.setPortShaftRunningHours(0);
                        vesselInfo.setStbdShaftRunningHours(0);
                    }

                    // Parameter I
                    try {
                        if (matcher.group("parameterI") != null) {
                            vesselInfo.setParameterI(matcher.group("parameterI"));
                        } else {
                            vesselInfo.setParameterI("0");
                        }
                    } catch (IllegalArgumentException e) {
                        vesselInfo.setParameterI("0");
                    }

                    this.processVesselInfoGsm(vesselInfo, vesselInbox);

                } else {
                    markInboxNotProcessed(vesselInbox);
                }
            } else {
                markInboxNotProcessed(vesselInbox);
            }

        } else {
            logger.info("gagal");
            markInboxNotProcessed(vesselInbox);
        }
    }

    private void processVesselInfoGsm(VesselInfo vesselInfo, VesselInbox vesselInbox) {
        try {
            if (this.vesselMapper.createVesselInfo(vesselInfo) > 0) {
                logger.info("Processing vessel inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                        + vesselInbox.getSentDate() + "... done.");
            }
        } catch (Exception e) {
            logger.warn("Processing vessel inbox for vessel " + vesselInbox.getName() + ". Inbox time at "
                    + vesselInbox.getSentDate() + "... failed. Data exist.");
        } finally {
            markInboxProcessed(vesselInbox);
        }
    }
}
