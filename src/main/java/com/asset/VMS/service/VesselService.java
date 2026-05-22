package com.asset.VMS.service;

import com.asset.VMS.domain.*;
import com.asset.VMS.mapper.VesselMapper;
import com.asset.VMS.util.Util;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import java.time.LocalDate;

@Service
public class VesselService {
        private static final Logger logger = LogManager.getLogger(AccountService.class);

        private final VesselMapper vesselMapper;

        @Autowired
        public VesselService(VesselMapper vesselMapper) {
                this.vesselMapper = vesselMapper;
        }

        public List<VesselLocation> selectLastLocation(Account account) {
                try {
                        System.out.println("get account : " + account);
                        return this.vesselMapper.selectLastLocation(account);
                } catch (Exception e) {
                        logger.error(e.getMessage());
                        return null;
                }
        }

        public List<VesselLocation> selectLocation(Map<String, Object> map) {
                try {
                        String vesselEmail = (String) map.get("vessel_email");
                        String categoryGsm = null;

                        Map<String, Object> parameterMap = new HashMap<>();
                        parameterMap.put("email", vesselEmail);
                        List<Vessel> vesselList = this.vesselMapper.searchVesselEmail(parameterMap);

                        for (Vessel vessel : vesselList) {
                                categoryGsm = vessel.getCategoryGsm();
                        }

                        map.put("categoryGsm", categoryGsm);

                        System.out.println("tomi location : " + map);
                        return this.vesselMapper.selectLocation(map);
                } catch (Exception e) {
                        logger.error(e.getMessage());
                        return null;
                }
        }

        public JSONObject searchVessel(Map<String, Object> map) {
                JSONObject jsonObject;
                try {
                        int recordsTotal = this.vesselMapper.countVessel(map);
                        int recordsFiltered = this.vesselMapper.countVessel(map);
                        List<Vessel> vesselList = this.vesselMapper.searchVessel(map);
                        jsonObject = Util.resultToJSONObject(recordsTotal, recordsFiltered, new JSONArray(vesselList));
                } catch (JSONException e) {
                        jsonObject = Util.exceptionToJSONObject(e);
                }
                return jsonObject;
        }

        public JSONObject createVessel(Vessel vessel) {
                JSONObject object = new JSONObject();
                try {
                        if (this.vesselMapper.existsByEmail(vessel.getEmail()) > 0) {
                                JSONObject o = new JSONObject();
                                o.put("success", false);
                                o.put("message", "Email vessel sudah terdaftar");
                                return o;
                        }

                        if (this.vesselMapper.createVessel(vessel) > 0) {
                                object.put("success", true);
                                object.put("message", "success");
                        } else {
                                object = Util.sendCreateOrUpdateFailedMessage();
                        }
                } catch (Exception e) {
                        object = Util.exceptionToJSONObject(e);
                }
                return object;
        }

        public JSONObject updateVessel(Vessel vessel) {
                JSONObject object = new JSONObject();
                try {
                        if (vessel.getEmail() != null && vessel.getEmailOld() != null) {
                                if (!vessel.getEmail().equalsIgnoreCase(vessel.getEmailOld())) {
                                        if (this.vesselMapper.existsByEmail(vessel.getEmail()) > 0) {
                                                JSONObject o = new JSONObject();
                                                o.put("success", false);
                                                o.put("message", "Email vessel sudah terdaftar");
                                                return o;
                                        }
                                }
                        }

                        this.vesselMapper.updateEmailAccountShipAllocation(vessel);
                        this.vesselMapper.updateEmailAccountSubscribeVesselAllocation(vessel);
                        if (this.vesselMapper.updateVessel(vessel) > 0) {
                                object.put("success", true);
                                object.put("message", "success");
                        } else {
                                object = Util.sendCreateOrUpdateFailedMessage();
                        }

                } catch (Exception e) {
                        object = Util.exceptionToJSONObject(e);
                }
                return object;
        }

        public JSONObject selectVesselAllocation(Account account) {
                JSONObject object = new JSONObject();
                try {
                        object.put("success", true);
                        object.put("vesselAllocation",
                                        new JSONArray(this.vesselMapper.selectVesselAllocation(account)));
                        object.put("vesselSubscriptionAllocation",
                                        new JSONArray(this.vesselMapper.selectSubscribeVesselAllocation(account)));
                } catch (Exception e) {
                        object = Util.exceptionToJSONObject(e);
                }
                return object;
        }

        public JSONObject deleteVesselAllocation(Account account) {
                JSONObject object = new JSONObject();
                try {
                        if (this.vesselMapper.deleteVesselAllocation(account) > 0) {
                                object.put("success", true);
                                object.put("message", "success");
                        } else {
                                object = Util.sendCreateOrUpdateFailedMessage();
                        }
                } catch (Exception e) {
                        object = Util.exceptionToJSONObject(e);
                }
                return object;
        }

        public JSONObject deleteVesselSubscribeAllocation(Account account) {
                JSONObject object = new JSONObject();
                try {
                        if (this.vesselMapper.deleteVesselSubscribeAllocation(account) >= 0) {
                                object.put("success", true);
                                object.put("message", "success");
                        } else {
                                object = Util.sendCreateOrUpdateFailedMessage();
                        }
                } catch (Exception e) {
                        object = Util.exceptionToJSONObject(e);
                }
                return object;
        }

        public JSONObject insertVesselAllocation(Account account) {
                JSONObject object = new JSONObject();
                try {
                        if (this.vesselMapper.insertVesselAllocation(account) > 0) {
                                object.put("success", true);
                                object.put("message", "success");
                        } else {
                                object = Util.sendCreateOrUpdateFailedMessage();
                        }
                } catch (Exception e) {
                        object = Util.exceptionToJSONObject(e);
                }
                return object;
        }

        public JSONObject insertVesselSubscribeAllocation(Account account) {
                JSONObject object = new JSONObject();
                try {
                        if (this.vesselMapper.insertVesselSubscribeAllocation(account) > 0) {
                                object.put("success", true);
                                object.put("message", "success");
                        } else {
                                object = Util.sendCreateOrUpdateFailedMessage();
                        }
                } catch (Exception e) {
                        object = Util.exceptionToJSONObject(e);
                }
                return object;
        }

        public Vessel getVessel(String email) {
                return this.vesselMapper.getVessel(email);
        }

        public JSONObject selectVesselInfo(Map<String, Object> map) {
                JSONObject jsonObject = new JSONObject();

                String vesselEmail = (String) map.get("vessel_email");
                String categoryGsm = null;

                Map<String, Object> parameterMap = new HashMap<>();
                parameterMap.put("email", vesselEmail);
                List<Vessel> vesselList = this.vesselMapper.searchVesselEmail(parameterMap);

                for (Vessel vessel : vesselList) {
                        categoryGsm = vessel.getCategoryGsm();
                }

                map.put("categoryGsm", categoryGsm);

                try {
                        List<VesselInfo> vesselInfoList = this.vesselMapper.selectVesselInfo(map);

                        // baseline = 1 data terakhir sebelum range report dipilih
                        VesselInfo baseline = null;
                        try {
                                baseline = this.vesselMapper.selectVesselInfoBaseline(map);
                        } catch (Exception ex) {
                                baseline = null; // fallback aman
                        }

                        if (vesselInfoList.size() > 0) {
                                PeriodFormatter fmt = new PeriodFormatterBuilder()
                                                .printZeroAlways()
                                                .minimumPrintedDigits(2)
                                                .appendHours()
                                                .appendSeparator(":")
                                                .printZeroAlways()
                                                .minimumPrintedDigits(2)
                                                .appendMinutes()
                                                .toFormatter();
                                int counter = 0;
                                int consumptionCalculationResult = 0;
                                int totalConsumptionCalculationResult = 0;
                                int consumption;
                                int fuelRefill = 0;
                                int totalFuelRefill = 0;
                                int totalPortRpm = 0;
                                int avgPortRpm = 0;
                                int totalStarboardRpm = 0;
                                int avgStarboardRpm = 0;
                                double totalSpeed = 0.0;
                                double avgSpeed = 0.0;
                                String shipName = "";
                                int numberOfCalculatedPortRpm = 0;
                                int numberOfCalculatedStarboardRpm = 0;
                                int numberOfCalculatedSpeed = 0;
                                int prevFuel = 0;
                                Timestamp prevTime = null;
                                Timestamp currentTime;
                                double consumptionPerHour;
                                int totalRpm;
                                int prevRunMinutes = 0;
                                int runMinutes;
                                int prevPortRpm = 0;
                                int prevStarBoardRpm = 0;
                                double totalPortPitch = 0.0;
                                double avgPortPitch = 0.0;
                                double totalStarboardPitch = 0.0;
                                double avgStarboardPitch = 0.0;
                                double totalPortShaft = 0.0;
                                double avgPortShaft = 0.0;
                                double totalStarboardShaft = 0.0;
                                double avgStarboardShaft = 0.0;
                                int prevEngineInletConsume = 0;
                                int prevEngineOutletConsume = 0;
                                double avgMainEngineRpm = 0;
                                int numberOfCalculatedMainEngineRpm = 0;
                                long mainEngineTime = 0;
                                long genset1Time = 0;
                                long genset2Time = 0;
                                long genset3Time = 0;
                                long genset4Time = 0;
                                long genset5Time = 0;
                                int totalMainEngineTime = 0;
                                int totalGenset1Time = 0;
                                int totalGenset2Time = 0;
                                int totalGenset3Time = 0;
                                int totalGenset4Time = 0;
                                int totalGenset5Time = 0;
                                long boilerRunTimeMinutes = 0L;
                                long mdoMinutes = 0L;
                                long hfoMinutes = 0L;
                                int totalMdo = 0;
                                int totalHfo = 0;
                                double avgBostrPump = 0;
                                double avgEngineIn = 0;
                                double avgDailyTank1 = 0;
                                double avgDailyTank2 = 0;
                                int totalBoilerRunTime = 0;
                                int numberOfCalculatedPortRunHour = 0;
                                int totalPortRunHour = 0;
                                int avgPortRunHour = 0;
                                int numberOfCalculatedStarboardRunHour = 0;
                                int totalStarboardRunHour = 0;
                                int avgStarboardRunHour = 0;
                                double totalConsumptionPortPerHour = 0;
                                double totalConsumptionStarboardPerHour = 0;
                                int totalAE1RunHour = 0;
                                int totalAE2RunHour = 0;
                                double totalConsumptionAE1PerHour = 0;
                                double totalConsumptionAE2PerHour = 0;
                                int portProgressiveRunHour = 0;
                                int starboardProgressiveRunHour = 0;
                                int AE1ProgressiveRunHour = 0;
                                int AE2ProgressiveRunHour = 0;
                                long portERH = 0;
                                long stbdERH = 0;
                                long portGRH = 0;
                                long stbdGRH = 0;
                                long portShaft = 0;
                                long stbdShaft = 0;
                                long portShaftRunningHours = 0;
                                long stbdShaftRunningHours = 0;
                                double totalPortERH = 0;
                                double totalPortGRH = 0;
                                double totalStbdERH = 0;
                                double totalStbdGRH = 0;
                                long totalPortShaftRPM = 0;
                                long totalStbdShaftRPM = 0;
                                int numberOfCalculatedPortShaftRPM = 0;
                                int numberOfCalculatedStbdShaftRPM = 0;
                                double totalPortShaftRunningHours = 0;
                                double totalStbdShaftRunningHours = 0;
                                double avgRunHour = 0;
                                double totalRunHour = 0;

                                final boolean hasAnyRpmSignal = vesselInfoList.stream()
                                                .anyMatch(v -> v.getPortRpm() > 0 || v.getStarboardRpm() > 0);

                                for (int i = 0; i < vesselInfoList.size(); i++) {
                                        counter++;
                                        VesselInfo vesselInfo = vesselInfoList.get(i);
                                        double hoursSincePrev = 0d;

                                        // previous record: kalau i==0 pakai baseline, kalau i>0 pakai row sebelumnya di
                                        // list
                                        VesselInfo previousInfo = (i == 0) ? baseline : vesselInfoList.get(i - 1);

                                        int portRunHour = resolveEngineRunHourFromCounter(vesselInfo, previousInfo, true);
                                        vesselInfo.setPortRunHour(portRunHour);
                                        if (portRunHour == 0) {
                                                portProgressiveRunHour = 0;
                                        } else {
                                                portProgressiveRunHour += portRunHour;
                                        }

                                        int starboardRunHour = resolveEngineRunHourFromCounter(vesselInfo, previousInfo,
                                                        false);
                                        vesselInfo.setStarboardRunHour(starboardRunHour);
                                        if (starboardRunHour == 0) {
                                                starboardProgressiveRunHour = 0;
                                        } else {
                                                starboardProgressiveRunHour += starboardRunHour;
                                        }

                                        vesselInfo.setFormattedPortRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getPortRunHour() * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setFormattedStarboardRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getStarboardRunHour() * 60
                                                                                        * 1000,
                                                                        "HH:mm"));

                                        vesselInfo.setPortProgressiveRunHour(portProgressiveRunHour);
                                        vesselInfo.setFormattedPortProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getPortProgressiveRunHour()
                                                                                        * 60
                                                                                        * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setStarboardProgressiveRunHour(starboardProgressiveRunHour);
                                        vesselInfo.setFormattedStarboardProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo
                                                                                        .getStarboardProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));

                                        if (vesselInfo.getParameterG() != null
                                                        && !vesselInfo.getParameterG().equalsIgnoreCase("")
                                                        && vesselInfo.getParameterG().length() == 8) {
                                                vesselInfo.setAE1RunHour(Util.stringToInt(
                                                                vesselInfo.getParameterG().substring(0, 4)));
                                                vesselInfo.setAE2RunHour(Util.stringToInt(
                                                                vesselInfo.getParameterG().substring(4, 8)));

                                                if (Util.stringToInt(vesselInfo.getParameterG().substring(0, 4)) == 0) {
                                                        AE1ProgressiveRunHour = 0;
                                                } else {
                                                        AE1ProgressiveRunHour = AE1ProgressiveRunHour
                                                                        + vesselInfo.getAE1RunHour();
                                                }

                                                if (Util.stringToInt(vesselInfo.getParameterG().substring(4, 8)) == 0) {
                                                        AE2ProgressiveRunHour = 0;
                                                } else {
                                                        AE2ProgressiveRunHour = AE2ProgressiveRunHour
                                                                        + vesselInfo.getAE2RunHour();
                                                }
                                        } else {
                                                AE1ProgressiveRunHour = 0;
                                                AE2ProgressiveRunHour = 0;
                                                vesselInfo.setAE1RunHour(0);
                                                vesselInfo.setAE2RunHour(0);
                                        }

                                        totalAE1RunHour += vesselInfo.getAE1RunHour();
                                        totalAE2RunHour += vesselInfo.getAE2RunHour();

                                        vesselInfo.setFormattedAE1RunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE1RunHour() * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setFormattedAE2RunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE2RunHour() * 60 * 1000,
                                                                        "HH:mm"));

                                        vesselInfo.setAE1ProgressiveRunHour(AE1ProgressiveRunHour);
                                        vesselInfo.setFormattedAE1ProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE1ProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setAE2ProgressiveRunHour(AE2ProgressiveRunHour);
                                        vesselInfo.setFormattedAE2ProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE2ProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));

                                        totalPortPitch += vesselInfo.getPortPitch();
                                        totalStarboardPitch += vesselInfo.getStarboardPitch();
                                        totalPortShaft += vesselInfo.getPortShaft();
                                        totalStarboardShaft += vesselInfo.getStarboardShaft();
                                        if (vesselInfo.getMainEngineRpm() > 0) {
                                                avgMainEngineRpm += vesselInfo.getMainEngineRpm();
                                                numberOfCalculatedMainEngineRpm++;
                                        }
                                        avgBostrPump += vesselInfo.getBostrPump();
                                        avgEngineIn += vesselInfo.getEngineIn();
                                        avgDailyTank1 += vesselInfo.getDailyTank1();
                                        avgDailyTank2 += vesselInfo.getDailyTank2();

                                        if (vesselInfo.getPortRpm() > 0) {
                                                numberOfCalculatedPortRpm++;
                                                totalPortRpm += vesselInfo.getPortRpm();
                                        }
                                        if (vesselInfo.getStarboardRpm() > 0) {
                                                numberOfCalculatedStarboardRpm++;
                                                totalStarboardRpm += vesselInfo.getStarboardRpm();
                                        }

                                        // ==== Average SPEED: skip null / "" / "null" / <= 0.40 ====
                                        String spd = vesselInfo.getSpeed();
                                        if (spd != null && !spd.trim().isEmpty()
                                                        && !spd.trim().equalsIgnoreCase("null")) {
                                                try {
                                                        double speed = Double.parseDouble(spd.trim().replace(",", ".")); // jaga-jaga
                                                                                                                         // kalau
                                                                                                                         // ada
                                                                                                                         // "0,45"
                                                        if (speed > 0.40d) {
                                                                numberOfCalculatedSpeed++;
                                                                totalSpeed += speed;
                                                        }
                                                } catch (Exception ex) {
                                                        // skip kalau format tidak valid
                                                }
                                        }

                                        int prevB = (previousInfo != null) ? previousInfo.getParameterB() : 0;
                                        int prevC = (previousInfo != null) ? previousInfo.getParameterC() : 0;

                                        vesselInfo.setEngineInletConsume(
                                                        safeDiffInt(vesselInfo.getParameterB(), prevB));
                                        vesselInfo.setEngineOutletConsume(
                                                        safeDiffInt(vesselInfo.getParameterC(), prevC));

                                        // keep tracker kalau masih dipakai di tempat lain (optional aman)
                                        prevEngineInletConsume = vesselInfo.getParameterB();
                                        prevEngineOutletConsume = vesselInfo.getParameterC();

                                        // ============================================================
                                        // PERBAIKAN INTI:
                                        // UI TotalCons baca consumptionCalculationResult.
                                        // Untuk SAT/Stratos (yang memang TotalCons harus = (B diff) - (C diff)),
                                        // kita set ulang consumptionCalculationResult agar konsisten dengan F2/F3.
                                        // ============================================================
                                        final String senderStr = String.valueOf(vesselInfo.getSender());
                                        final String reportTypeStr = String.valueOf(vesselInfo.getReportType());

                                        final boolean hasBC = vesselInfo.getParameterB() > 0
                                                        && vesselInfo.getParameterC() > 0;
                                        final boolean isStratos = senderStr.toLowerCase().contains("stratosmobile.net");
                                        final boolean isSat = "SAT".equalsIgnoreCase(reportTypeStr);

                                        // (opsional) kalau kamu mau lebih ketat, bisa tambah condition categoryGsm
                                        // tertentu.
                                        final boolean wantTotalFromBC = hasBC && (isSat || isStratos);

                                        if (i == 0) {
                                                jsonObject.put("reportType", vesselInfo.getReportType());
                                                jsonObject.put("vesselName", vesselInfo.getName());

                                                // ==== NEW: kalau baseline ada, row pertama dihitung dari baseline ====
                                                if (previousInfo != null) {

                                                        // 1) Fuel refill row pertama = max(currA - baselineA, 0)
                                                        int baselineA = previousInfo.getParameterA();
                                                        int refillFirst = calcRefuelDiff(
                                                                        vesselInfo.getParameterA(), baselineA);
                                                        if (isRefuelGapTooLong(previousInfo.getSentDate(),
                                                                        vesselInfo.getSentDate())) {
                                                                refillFirst = 0;
                                                        }
                                                        vesselInfo.setFuelRefill(refillFirst);
                                                        totalFuelRefill += refillFirst;

                                                        // 2) Consumption row pertama (ikut pola perhitungan existing
                                                        // kamu)
                                                        // baseline consumption dianggap sebagai
                                                        // "consumptionCalculationResult" sebelumnya
                                                        consumptionCalculationResult = previousInfo.getConsumption();

                                                        // sama seperti branch else: kurangi efek refill
                                                        vesselInfo.setConsumption(
                                                                        vesselInfo.getConsumption() - refillFirst);
                                                        int consumptionNow = vesselInfo.getConsumption();

                                                        // hitung actual first row
                                                        int firstCalc = consumptionCalculationResult - consumptionNow
                                                                        + refillFirst;
                                                        vesselInfo.setConsumptionCalculationResult(firstCalc);

                                                        // update state supaya row ke-2 dst tetap pakai logic lama
                                                        fuelRefill = vesselInfo.getParameterA();
                                                        prevFuel = refillFirst;

                                                        // 3) Interval waktu untuk row pertama (baseline time -> current
                                                        // time)
                                                        prevTime = previousInfo.getSentDate();
                                                        currentTime = vesselInfo.getSentDate();

                                                        Duration duration = new Duration(
                                                                        Util.timestampToDateTime(prevTime),
                                                                        Util.timestampToDateTime(currentTime));

                                                        long millis = duration.getMillis();
                                                        double hours = millis / 3600000d; // ms -> jam
                                                        hoursSincePrev = hours;

                                                        if (hours > 0d) {
                                                                consumptionPerHour = (double) firstCalc / hours;
                                                        } else {
                                                                consumptionPerHour = 0d;
                                                        }
                                                        vesselInfo.setConsumptionPerHour(consumptionPerHour);

                                                        // OPTIONAL (kalau runhour mau ikut “2 menit” bukan 1 menit):
                                                        long minutesDuration = Math.round(millis / 60000d); // rounding,
                                                                                                            // bukan
                                                                                                            // floor
                                                        if (millis > 0 && minutesDuration == 0)
                                                                minutesDuration = 1;

                                                        // 4) RunHour row pertama (pakai RPM baseline untuk decide
                                                        // running / not)
                                                        prevPortRpm = previousInfo.getPortRpm();
                                                        prevStarBoardRpm = previousInfo.getStarboardRpm();

                                                        int totalRpmFirst = prevPortRpm + prevStarBoardRpm;
                                                        if (totalRpmFirst == 0) {
                                                                // tidak running
                                                                prevRunMinutes = 0;
                                                                vesselInfo.setFormattedRunHour("00:00");

                                                                // tapi kalau kamu mau tetap hitung saat mainEngineTime
                                                                // ada (sesuai pola kamu):
                                                                if (vesselInfo.getMainEngineTime() > 0
                                                                                && minutesDuration > 0) {
                                                                        prevRunMinutes += (int) minutesDuration;
                                                                        vesselInfo.setFormattedRunHour(
                                                                                        DurationFormatUtils
                                                                                                        .formatDuration((long) prevRunMinutes
                                                                                                                        * 60
                                                                                                                        * 1000,
                                                                                                                        "HH:mm"));
                                                                }
                                                        } else {
                                                                // running
                                                                prevRunMinutes = (minutesDuration > 0)
                                                                                ? (int) minutesDuration
                                                                                : 0;
                                                                vesselInfo.setFormattedRunHour(
                                                                                DurationFormatUtils.formatDuration(
                                                                                                (long) prevRunMinutes
                                                                                                                * 60
                                                                                                                * 1000,
                                                                                                "HH:mm"));
                                                        }

                                                        totalRunHour += prevRunMinutes;

                                                        // move state ke current row
                                                        prevTime = vesselInfo.getSentDate();
                                                        prevPortRpm = vesselInfo.getPortRpm();
                                                        prevStarBoardRpm = vesselInfo.getStarboardRpm();

                                                } else {
                                                        // ==== fallback lama kalau baseline tidak ada ====
                                                        vesselInfo.setConsumptionCalculationResult(0);
                                                        vesselInfo.setFuelRefill(0);
                                                        consumptionCalculationResult = vesselInfo.getConsumption();
                                                        totalConsumptionCalculationResult = 0;
                                                        fuelRefill = vesselInfo.getParameterA();
                                                        totalFuelRefill = vesselInfo.getFuelRefill();
                                                        prevFuel = 0;
                                                        prevTime = vesselInfo.getSentDate();
                                                        vesselInfo.setFormattedRunHour("00:00");
                                                        prevRunMinutes = 0;
                                                        prevPortRpm = vesselInfo.getPortRpm();
                                                        prevStarBoardRpm = vesselInfo.getStarboardRpm();
                                                }
                                        } else {
                                                currentTime = vesselInfo.getSentDate();
                                                boolean longRefuelGap = isRefuelGapTooLong(prevTime, currentTime);
                                                int refuelDiff = calcRefuelDiff(
                                                                vesselInfo.getParameterA(), fuelRefill);
                                                if (longRefuelGap) {
                                                        refuelDiff = 0;
                                                }
                                                vesselInfo.setConsumption(
                                                                vesselInfo.getConsumption() - refuelDiff);
                                                consumption = vesselInfo.getConsumption();

                                                vesselInfo.setConsumptionCalculationResult(
                                                                consumptionCalculationResult - consumption + prevFuel);
                                                vesselInfo.setFuelRefill(refuelDiff);
                                                totalFuelRefill += refuelDiff;
                                                consumptionCalculationResult = vesselInfo.getConsumption();
                                                prevFuel = refuelDiff;
                                                fuelRefill = vesselInfo.getParameterA();
                                                Duration duration = new Duration(
                                                                Util.timestampToDateTime(prevTime),
                                                                Util.timestampToDateTime(currentTime));

                                                long millis = duration.getMillis();
                                                double hours = millis / 3600000d;
                                                hoursSincePrev = hours;

                                                if (hours > 0d) {
                                                        consumptionPerHour = (double) vesselInfo
                                                                        .getConsumptionCalculationResult() / hours;
                                                } else {
                                                        consumptionPerHour = 0d;
                                                }
                                                vesselInfo.setConsumptionPerHour(consumptionPerHour);

                                                // OPTIONAL (kalau runhour mau ikut “2 menit” bukan 1 menit):
                                                long minutesDuration = Math.round(millis / 60000d);
                                                if (millis > 0 && minutesDuration == 0)
                                                        minutesDuration = 1;

                                                totalRpm = prevPortRpm + prevStarBoardRpm;
                                                if (totalRpm == 0) {
                                                        vesselInfo.setFormattedRunHour("00:00");
                                                        prevRunMinutes = 0;

                                                        if (vesselInfo.getMainEngineTime() > 0) {
                                                                Duration tempDuration2 = new Duration(
                                                                                Util.timestampToDateTime(prevTime),
                                                                                Util.timestampToDateTime(currentTime));
                                                                prevRunMinutes = prevRunMinutes
                                                                                + (int) tempDuration2
                                                                                                .getStandardMinutes();
                                                                vesselInfo.setFormattedRunHour(
                                                                                DurationFormatUtils.formatDuration(
                                                                                                (long) prevRunMinutes
                                                                                                                * 60
                                                                                                                * 1000,
                                                                                                "HH:mm"));
                                                        }
                                                } else {
                                                        DateTime tempPrevTime = Util.timestampToDateTime(prevTime)
                                                                        .plusMinutes(prevRunMinutes);
                                                        Duration tempDuration = new Duration(
                                                                        tempPrevTime,
                                                                        Util.timestampToDateTime(currentTime));

                                                        Duration tempDuration2 = new Duration(
                                                                        Util.timestampToDateTime(prevTime),
                                                                        Util.timestampToDateTime(currentTime));
                                                        prevRunMinutes = prevRunMinutes
                                                                        + (int) tempDuration2.getStandardMinutes();
                                                        vesselInfo.setFormattedRunHour(
                                                                        DurationFormatUtils.formatDuration(
                                                                                        (long) prevRunMinutes * 60
                                                                                                        * 1000,
                                                                                        "HH:mm"));
                                                }
                                                totalRunHour += prevRunMinutes;
                                                prevTime = vesselInfo.getSentDate();
                                                prevPortRpm = vesselInfo.getPortRpm();
                                                prevStarBoardRpm = vesselInfo.getStarboardRpm();
                                        }

                                        // FINAL: TotalCons = (F2-EIC diff) - (F3-EOC diff)
                                        int totalConsFromBC = calcTotalConsFromBC(vesselInfo, previousInfo);
                                        vesselInfo.setConsumptionCalculationResult(totalConsFromBC);
                                        totalConsumptionCalculationResult += totalConsFromBC;
                                        if (hoursSincePrev > 0d) {
                                                vesselInfo.setConsumptionPerHour(
                                                                (double) totalConsFromBC / hoursSincePrev);
                                        } else {
                                                vesselInfo.setConsumptionPerHour(0d);
                                        }

                                        double consumptionPortPerHour = 0;
                                        if (vesselInfo.getConsumptionCalculationResult() > 0) {
                                                consumptionPortPerHour = (double) vesselInfo
                                                                .getConsumptionCalculationResult()
                                                                / ((double) vesselInfo.getPortRunHour()
                                                                                + (double) vesselInfo
                                                                                                .getStarboardRunHour())
                                                                * (double) vesselInfo.getPortRunHour();
                                        }
                                        if (Double.isNaN(consumptionPortPerHour)) {
                                                vesselInfo.setConsumptionPortPerHour(0);
                                                totalConsumptionPortPerHour += 0;
                                        } else {
                                                vesselInfo.setConsumptionPortPerHour(consumptionPortPerHour);
                                                totalConsumptionPortPerHour += consumptionPortPerHour;
                                        }

                                        double consumptionStarboardPerHour = 0;
                                        if (vesselInfo.getConsumptionCalculationResult() > 0) {
                                                consumptionStarboardPerHour = (double) vesselInfo
                                                                .getConsumptionCalculationResult()
                                                                / ((double) vesselInfo.getPortRunHour()
                                                                                + (double) vesselInfo
                                                                                                .getStarboardRunHour())
                                                                * (double) vesselInfo.getStarboardRunHour();
                                        }
                                        if (Double.isNaN(consumptionStarboardPerHour)) {
                                                vesselInfo.setConsumptionStarboardPerHour(0);
                                                totalConsumptionStarboardPerHour += 0;
                                        } else {
                                                vesselInfo.setConsumptionStarboardPerHour(consumptionStarboardPerHour);
                                                totalConsumptionStarboardPerHour += consumptionStarboardPerHour;
                                        }

                                        double AE1ConsumptionPerHour = ((double) vesselInfo.getAE1RunHour() / 60)
                                                        * 43.65;
                                        double AE2ConsumptionPerHour = ((double) vesselInfo.getAE2RunHour() / 60)
                                                        * 38.4;
                                        vesselInfo.setAE1ConsumptionPerHour(AE1ConsumptionPerHour);
                                        totalConsumptionAE1PerHour += AE1ConsumptionPerHour;
                                        vesselInfo.setAE2ConsumptionPerHour(AE2ConsumptionPerHour);
                                        totalConsumptionAE2PerHour += AE2ConsumptionPerHour;

                                        if (vesselInfo.getSender().equalsIgnoreCase("VESSEL_SENDER_EMAIL_PLACEHOLDER")) {
                                                if (vesselInfo.getPortRunHour() > 0
                                                                && vesselInfo.getStarboardRunHour() > 0) {
                                                        vesselInfo.setConsumptionPortPerHour(
                                                                        vesselInfo.getConsumptionPortPerHour()
                                                                                        + 1d);
                                                        vesselInfo.setConsumptionStarboardPerHour(
                                                                        vesselInfo.getConsumptionStarboardPerHour()
                                                                                        + 1d);
                                                        vesselInfo.setConsumptionCalculationResult(
                                                                        vesselInfo.getConsumptionCalculationResult()
                                                                                        + 2);
                                                } else if (vesselInfo.getPortRunHour() > 0
                                                                && vesselInfo.getStarboardRunHour() == 0) {
                                                        vesselInfo.setConsumptionPortPerHour(
                                                                        vesselInfo.getConsumptionPortPerHour()
                                                                                        + 2d);
                                                        vesselInfo.setConsumptionStarboardPerHour(
                                                                        vesselInfo.getConsumptionStarboardPerHour()
                                                                                        + 0d);
                                                        vesselInfo.setConsumptionCalculationResult(
                                                                        vesselInfo.getConsumptionCalculationResult()
                                                                                        + 2);
                                                } else if (vesselInfo.getPortRunHour() == 0
                                                                && vesselInfo.getStarboardRunHour() > 0) {
                                                        vesselInfo.setConsumptionPortPerHour(
                                                                        vesselInfo.getConsumptionPortPerHour()
                                                                                        + 0d);
                                                        vesselInfo.setConsumptionStarboardPerHour(
                                                                        vesselInfo.getConsumptionStarboardPerHour()
                                                                                        + 2d);
                                                        vesselInfo.setConsumptionCalculationResult(
                                                                        vesselInfo.getConsumptionCalculationResult()
                                                                                        + 2);
                                                } else {
                                                        vesselInfo.setConsumptionPortPerHour(0);
                                                        vesselInfo.setConsumptionStarboardPerHour(0);
                                                        vesselInfo.setConsumptionCalculationResult(0);
                                                }
                                        }

                                        double totalFuelUsed = vesselInfo.getConsumptionPortPerHour()
                                                        + vesselInfo.getConsumptionStarboardPerHour()
                                                        + AE1ConsumptionPerHour
                                                        + AE2ConsumptionPerHour;
                                        vesselInfo.setTotalFuelUsed(totalFuelUsed);

                                        if (vesselInfo.getPortRunHour() > 0) {
                                                numberOfCalculatedPortRunHour++;
                                                totalPortRunHour += vesselInfo.getPortRunHour();
                                        }
                                        if (vesselInfo.getStarboardRunHour() > 0) {
                                                numberOfCalculatedStarboardRunHour++;
                                                totalStarboardRunHour += vesselInfo.getStarboardRunHour();
                                        }

                                        // 1) Sent date -> LocalDate
                                        LocalDate sentLocalDate = null;
                                        if (vesselInfo.getSentDate() != null) {
                                                sentLocalDate = vesselInfo.getSentDate()
                                                                .toInstant()
                                                                .atZone(ZoneId.systemDefault())
                                                                .toLocalDate();
                                        }

                                        // 2) mode kalkulasi genset (RAW vs DIFF)
                                        final LocalDate cutoff = vesselInfo.getCalcGensetCutoff();

                                        // Formula 2 ON/OFF cukup dari flag
                                        final boolean isFormula2 = vesselInfo.isUseSafeDiffErh();

                                        // Jika cutoff diisi: Formula 2 mulai dari tanggal cutoff (inclusive)
                                        // Jika cutoff null: langsung aktif
                                        final boolean afterCutoff = (sentLocalDate != null)
                                                        && (cutoff == null || !sentLocalDate.isBefore(cutoff)); // >=
                                                                                                                // cutoff

                                        final boolean useFormula2 = isFormula2 && afterCutoff;

                                        // 3) Hitung & format run hour genset/main engine
                                        if (useFormula2) {
                                                final VesselInfo current = vesselInfoList.get(i);
                                                final VesselInfo previous = previousInfo;

                                                if (previous == null) {
                                                        current.setFormattedMainEngineTime("00:00");
                                                        current.setFormattedGenset1RunHour("00:00");
                                                        current.setFormattedGenset2RunHour("00:00");
                                                        current.setFormattedGenset3RunHour("00:00");
                                                        current.setFormattedGenset4RunHour("00:00");
                                                        current.setFormattedGenset5RunHour("00:00");
                                                        current.setFormattedBoilerRunTime("00:00");
                                                        current.setFormattedMdo("00:00");
                                                        current.setFormattedHfo("00:00");

                                                        mainEngineTime = 0;
                                                        genset1Time = genset2Time = genset3Time = genset4Time = genset5Time = 0;
                                                        boilerRunTimeMinutes = 0L;
                                                        mdoMinutes = 0L;
                                                        hfoMinutes = 0L;
                                                } else {
                                                        mainEngineTime = normalizeCounterMinutesForDisplay(
                                                                        calcMainEngineTimeMinutes(
                                                                                        current.getMainEngineTime(),
                                                                                        previous.getMainEngineTime()));
                                                        genset1Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset1Time(),
                                                                                        previous.getGenset1Time()));
                                                        genset2Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset2Time(),
                                                                                        previous.getGenset2Time()));
                                                        genset3Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset3Time(),
                                                                                        previous.getGenset3Time()));
                                                        genset4Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset4Time(),
                                                                                        previous.getGenset4Time()));
                                                        genset5Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset5Time(),
                                                                                        previous.getGenset5Time()));

                                                        boilerRunTimeMinutes = safeDiff(current.getBoilerRunTime(),
                                                                        previous.getBoilerRunTime());
                                                        mdoMinutes = safeDiff(current.getMdo(), previous.getMdo());
                                                        hfoMinutes = safeDiff(current.getHfo(), previous.getHfo());

                                                        current.setFormattedMainEngineTime(formatHHmm(mainEngineTime));
                                                        current.setFormattedGenset1RunHour(genset1Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset1Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset2RunHour(genset2Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset2Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset3RunHour(genset3Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset3Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset4RunHour(genset4Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset4Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset5RunHour(genset5Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset5Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");

                                                        current.setFormattedBoilerRunTime(boilerRunTimeMinutes > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        boilerRunTimeMinutes * 60L
                                                                                                        * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedMdo(mdoMinutes > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        mdoMinutes * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedHfo(hfoMinutes > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        hfoMinutes * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                }
                                        } else {
                                                mainEngineTime = vesselInfo.getMainEngineTime();
                                                genset1Time = vesselInfo.getGenset1Time();
                                                genset2Time = vesselInfo.getGenset2Time();
                                                genset3Time = vesselInfo.getGenset3Time();
                                                genset4Time = vesselInfo.getGenset4Time();
                                                genset5Time = vesselInfo.getGenset5Time();

                                                boilerRunTimeMinutes = vesselInfo.getBoilerRunTime();
                                                mdoMinutes = vesselInfo.getMdo();
                                                hfoMinutes = vesselInfo.getHfo();

                                                vesselInfo.setFormattedMainEngineTime(formatHHmm(mainEngineTime));
                                                vesselInfo.setFormattedGenset1RunHour(genset1Time > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                genset1Time * 60L * 1000,
                                                                                "HH:mm")
                                                                : "00:00");
                                                vesselInfo.setFormattedGenset2RunHour(genset2Time > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                genset2Time * 60L * 1000,
                                                                                "HH:mm")
                                                                : "00:00");
                                                vesselInfo.setFormattedGenset3RunHour(genset3Time > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                genset3Time * 60L * 1000,
                                                                                "HH:mm")
                                                                : "00:00");
                                                vesselInfo.setFormattedGenset4RunHour(genset4Time > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                genset4Time * 60L * 1000,
                                                                                "HH:mm")
                                                                : "00:00");
                                                vesselInfo.setFormattedGenset5RunHour(genset5Time > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                genset5Time * 60L * 1000,
                                                                                "HH:mm")
                                                                : "00:00");

                                                vesselInfo.setFormattedBoilerRunTime(boilerRunTimeMinutes > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                boilerRunTimeMinutes * 60L * 1000,
                                                                                "HH:mm")
                                                                : "00:00");
                                                vesselInfo.setFormattedMdo(mdoMinutes > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                mdoMinutes * 60L * 1000,
                                                                                "HH:mm")
                                                                : "00:00");
                                                vesselInfo.setFormattedHfo(hfoMinutes > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                hfoMinutes * 60L * 1000,
                                                                                "HH:mm")
                                                                : "00:00");
                                        }

                                        if (shouldZeroMainEngineTimeAtEngineOff(vesselInfo, previousInfo)) {
                                                mainEngineTime = 0;
                                                vesselInfo.setFormattedMainEngineTime("00:00");
                                        }

                                        if (mainEngineTime > 0)
                                                totalMainEngineTime += mainEngineTime;
                                        if (genset1Time > 0)
                                                totalGenset1Time += genset1Time;
                                        if (genset2Time > 0)
                                                totalGenset2Time += genset2Time;
                                        if (genset3Time > 0)
                                                totalGenset3Time += genset3Time;
                                        if (genset4Time > 0)
                                                totalGenset4Time += genset4Time;
                                        if (genset5Time > 0)
                                                totalGenset5Time += genset5Time;
                                        if (boilerRunTimeMinutes > 0)
                                                totalBoilerRunTime += boilerRunTimeMinutes;
                                        if (mdoMinutes > 0)
                                                totalMdo += mdoMinutes;
                                        if (hfoMinutes > 0)
                                                totalHfo += hfoMinutes;

                                        // 5) ERH/GRH/Shaft Running Hours
                                        if (useFormula2) {
                                                final VesselInfo current = vesselInfoList.get(i);
                                                final VesselInfo previous = previousInfo;

                                                if (previous == null) {
                                                        current.setFormattedPortERH("00:00");
                                                        current.setFormattedStbdERH("00:00");
                                                        current.setFormattedPortGRH("00:00");
                                                        current.setFormattedStbdGRH("00:00");
                                                        current.setFormattedPortShaftRunningHours("00:00");
                                                        current.setFormattedStbdShaftRunningHours("00:00");

                                                        portERH = stbdERH = portGRH = stbdGRH = portShaftRunningHours = stbdShaftRunningHours = 0;
                                                } else {
                                                        portERH = resolveCounterMinutes(current.getPortERH(),
                                                                        previous.getPortERH(), true);
                                                        stbdERH = resolveCounterMinutes(current.getStbdERH(),
                                                                        previous.getStbdERH(), true);
                                                        portERH = applyEngineRunHoursByRpm(portERH,
                                                                        current.getPortRpm(), hasAnyRpmSignal);
                                                        stbdERH = applyEngineRunHoursByRpm(stbdERH,
                                                                        current.getStarboardRpm(), hasAnyRpmSignal);
                                                        portGRH = resolveCounterMinutes(current.getPortGRH(),
                                                                        previous.getPortGRH(), true);
                                                        stbdGRH = resolveCounterMinutes(current.getStbdGRH(),
                                                                        previous.getStbdGRH(), true);
                                                        portShaftRunningHours = resolveCounterMinutes(
                                                                        current.getPortShaftRunningHours(),
                                                                        previous.getPortShaftRunningHours(), true);
                                                        stbdShaftRunningHours = resolveCounterMinutes(
                                                                        current.getStbdShaftRunningHours(),
                                                                        previous.getStbdShaftRunningHours(), true);

                                                        current.setFormattedPortERH(formatHHmm(portERH));
                                                        current.setFormattedStbdERH(formatHHmm(stbdERH));
                                                        current.setFormattedPortGRH(formatHHmm(portGRH));
                                                        current.setFormattedStbdGRH(formatHHmm(stbdGRH));
                                                        current.setFormattedPortShaftRunningHours(
                                                                        formatHHmm(portShaftRunningHours));
                                                        current.setFormattedStbdShaftRunningHours(
                                                                        formatHHmm(stbdShaftRunningHours));
                                                }
                                        } else {
                                                if (!hasAnyRpmSignal) {
                                                        if (previousInfo == null) {
                                                                portERH = 0;
                                                                stbdERH = 0;
                                                        } else {
                                                                portERH = resolveCounterMinutes(vesselInfo.getPortERH(),
                                                                                previousInfo.getPortERH(), true);
                                                                stbdERH = resolveCounterMinutes(vesselInfo.getStbdERH(),
                                                                                previousInfo.getStbdERH(), true);
                                                        }
                                                } else {
                                                        portERH = resolveCounterMinutes(vesselInfo.getPortERH(), 0,
                                                                        false);
                                                        stbdERH = resolveCounterMinutes(vesselInfo.getStbdERH(), 0,
                                                                        false);
                                                }
                                                portERH = applyEngineRunHoursByRpm(portERH, vesselInfo.getPortRpm(),
                                                                hasAnyRpmSignal);
                                                stbdERH = applyEngineRunHoursByRpm(stbdERH,
                                                                vesselInfo.getStarboardRpm(), hasAnyRpmSignal);
                                                portGRH = resolveCounterMinutes(vesselInfo.getPortGRH(), 0, false);
                                                stbdGRH = resolveCounterMinutes(vesselInfo.getStbdGRH(), 0, false);
                                                portShaftRunningHours = resolveCounterMinutes(
                                                                vesselInfo.getPortShaftRunningHours(), 0, false);
                                                stbdShaftRunningHours = resolveCounterMinutes(
                                                                vesselInfo.getStbdShaftRunningHours(), 0, false);

                                                vesselInfo.setFormattedPortERH(formatHHmm(portERH));
                                                vesselInfo.setFormattedStbdERH(formatHHmm(stbdERH));
                                                vesselInfo.setFormattedPortGRH(formatHHmm(portGRH));
                                                vesselInfo.setFormattedStbdGRH(formatHHmm(stbdGRH));
                                                vesselInfo.setFormattedPortShaftRunningHours(
                                                                formatHHmm(portShaftRunningHours));
                                                vesselInfo.setFormattedStbdShaftRunningHours(
                                                                formatHHmm(stbdShaftRunningHours));
                                        }

                                        if (shouldZeroEngineAndShaftHoursAtEngineOff(vesselInfo, previousInfo)) {
                                                portERH = 0;
                                                stbdERH = 0;
                                                portShaftRunningHours = 0;
                                                stbdShaftRunningHours = 0;
                                                vesselInfo.setFormattedPortERH("00:00");
                                                vesselInfo.setFormattedStbdERH("00:00");
                                                vesselInfo.setFormattedPortShaftRunningHours("00:00");
                                                vesselInfo.setFormattedStbdShaftRunningHours("00:00");
                                        }

                                        // 6) RPM Shaft: null + 0 tidak dihitung untuk average
                                        int portShaftRPM = vesselInfo.getPortShaftRPM();
                                        int stbdShaftRPM = vesselInfo.getStbdShaftRPM();

                                        if (portShaftRPM > 0) {
                                                portShaft = portShaftRPM;
                                                vesselInfo.setFormattedPortShaftRPM(formatHHmm(portShaft));
                                        } else {
                                                portShaft = 0;
                                                vesselInfo.setFormattedPortShaftRPM("00:00");
                                        }

                                        if (stbdShaftRPM > 0) {
                                                stbdShaft = stbdShaftRPM;
                                                vesselInfo.setFormattedStbdShaftRPM(formatHHmm(stbdShaft));
                                        } else {
                                                stbdShaft = 0;
                                                vesselInfo.setFormattedStbdShaftRPM("00:00");
                                        }

                                        // 7) Akumulasi total lain (hanya jika > 0)
                                        if (portERH > 0)
                                                totalPortERH += portERH;
                                        if (portGRH > 0)
                                                totalPortGRH += portGRH;
                                        if (stbdERH > 0)
                                                totalStbdERH += stbdERH;
                                        if (stbdGRH > 0)
                                                totalStbdGRH += stbdGRH;

                                        if (portShaftRPM > 0) {
                                                totalPortShaftRPM += portShaftRPM;
                                                numberOfCalculatedPortShaftRPM++;
                                        }
                                        if (stbdShaftRPM > 0) {
                                                totalStbdShaftRPM += stbdShaftRPM;
                                                numberOfCalculatedStbdShaftRPM++;
                                        }

                                        if (portShaftRunningHours > 0)
                                                totalPortShaftRunningHours += portShaftRunningHours;
                                        if (stbdShaftRunningHours > 0)
                                                totalStbdShaftRunningHours += stbdShaftRunningHours;
                                }

                                double totalMainEngineConsumption = 0;
                                double totalPortConsumption = 0;
                                double totalStarboardConsumption = 0;
                                double totalAE1Consumption = 0;
                                double totalAE2Consumption = 0;

                                for (VesselInfo vesselInfo : vesselInfoList) {
                                        if (vesselInfo.getSender().equalsIgnoreCase("VESSEL_SENDER_EMAIL_PLACEHOLDER")) {
                                                vesselInfo.setConsumptionPortPerHour(
                                                                vesselInfo.getConsumptionPortPerHour()
                                                                                * vesselInfo.getPortConsumptionCoefficient());
                                                vesselInfo.setConsumptionStarboardPerHour(
                                                                vesselInfo.getConsumptionStarboardPerHour()
                                                                                * vesselInfo.getStarboardConsumptionCoefficient());
                                                vesselInfo.setAE1ConsumptionPerHour(
                                                                vesselInfo.getAE1ConsumptionPerHour()
                                                                                * vesselInfo.getAE1ConsumptionCoefficient());
                                                vesselInfo.setAE2ConsumptionPerHour(
                                                                vesselInfo.getAE2ConsumptionPerHour()
                                                                                * vesselInfo.getAE2ConsumptionCoefficient());
                                                double tempConsumptionCalculationResult = vesselInfo
                                                                .getConsumptionPortPerHour()
                                                                + vesselInfo.getConsumptionStarboardPerHour();
                                                vesselInfo.setConsumptionCalculationResult(
                                                                (int) Math.round(tempConsumptionCalculationResult));
                                                vesselInfo.setTotalFuelUsed(
                                                                vesselInfo.getConsumptionPortPerHour()
                                                                                + vesselInfo.getConsumptionStarboardPerHour()
                                                                                + vesselInfo.getAE1ConsumptionPerHour()
                                                                                + vesselInfo.getAE2ConsumptionPerHour());
                                                totalMainEngineConsumption += tempConsumptionCalculationResult;
                                                totalPortConsumption += vesselInfo.getConsumptionPortPerHour();
                                                totalStarboardConsumption += vesselInfo
                                                                .getConsumptionStarboardPerHour();
                                                totalAE1Consumption += vesselInfo.getAE1ConsumptionPerHour();
                                                totalAE2Consumption += vesselInfo.getAE2ConsumptionPerHour();
                                        }
                                }

                                if (numberOfCalculatedPortRpm > 0) {
                                        avgPortRpm = totalPortRpm / numberOfCalculatedPortRpm;
                                }
                                if (numberOfCalculatedStarboardRpm > 0) {
                                        avgStarboardRpm = totalStarboardRpm / numberOfCalculatedStarboardRpm;
                                }
                                if (numberOfCalculatedSpeed > 0) {
                                        avgSpeed = totalSpeed / numberOfCalculatedSpeed;
                                } else {
                                        avgSpeed = 0;
                                }

                                avgPortPitch = totalPortPitch / counter;
                                avgStarboardPitch = totalStarboardPitch / counter;
                                avgPortShaft = totalPortShaft / counter;
                                avgStarboardShaft = totalStarboardShaft / counter;
                                avgRunHour = totalRunHour / counter;

                                Map<String, Object> summary = new HashMap<>();
                                summary.put("consumption", String.valueOf(totalConsumptionCalculationResult));
                                if (vesselInfoList.size() > 0) {
                                        VesselInfo vesselInfo = vesselInfoList.get(0);
                                        if (vesselInfo.getSender().equalsIgnoreCase("VESSEL_SENDER_EMAIL_PLACEHOLDER")) {
                                                summary.put("consumption",
                                                                String.valueOf(totalMainEngineConsumption));
                                        }
                                }

                                summary.put("avgPortRpm", avgPortRpm);
                                summary.put("avgStarboardRpm", avgStarboardRpm);
                                summary.put("avgSpeed", avgSpeed);
                                summary.put("totalFuelRefill", totalFuelRefill);
                                summary.put("avgPortPitch", avgPortPitch);
                                summary.put("avgStarboardPitch", avgStarboardPitch);
                                summary.put("avgPortShaft", avgPortShaft);
                                summary.put("avgStarboardShaft", avgStarboardShaft);
                                int avgMainEngineRpmValue = 0;
                                if (numberOfCalculatedMainEngineRpm > 0) {
                                        avgMainEngineRpmValue = (int) Math.round(
                                                        avgMainEngineRpm / numberOfCalculatedMainEngineRpm);
                                }
                                summary.put("avgMainEngineRpm", avgMainEngineRpmValue);
                                summary.put("avgRunHour", avgRunHour > 0
                                                ? DurationFormatUtils.formatDuration(
                                                                (long) (avgRunHour * 60L * 1000),
                                                                "HH:mm")
                                                : "00:00");
                                summary.put("totalRunHour", totalRunHour > 0
                                                ? DurationFormatUtils.formatDuration(
                                                                (long) (totalRunHour * 60L * 1000),
                                                                "HH:mm")
                                                : "00:00");
                                summary.put("totalMainEngineTime", totalMainEngineTime > 0
                                                ? DurationFormatUtils.formatDuration(
                                                                (long) (totalMainEngineTime * 60L * 1000), "HH:mm")
                                                : "00:00");
                                summary.put("totalGenset1Time", totalGenset1Time);
                                summary.put("totalGenset2Time", totalGenset2Time);
                                summary.put("totalGenset3Time", totalGenset3Time);
                                summary.put("totalGenset4Time", totalGenset4Time);
                                summary.put("totalGenset5Time", totalGenset5Time);
                                summary.put("formattedTotalGenset1RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset1Time * 60 * 1000,
                                                                "HH:mm"));
                                summary.put("formattedTotalGenset2RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset2Time * 60 * 1000,
                                                                "HH:mm"));
                                summary.put("formattedTotalGenset3RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset3Time * 60 * 1000,
                                                                "HH:mm"));
                                summary.put("formattedTotalGenset4RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset4Time * 60 * 1000,
                                                                "HH:mm"));
                                summary.put("formattedTotalGenset5RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset5Time * 60 * 1000,
                                                                "HH:mm"));
                                summary.put("totalMdo", totalMdo > 0
                                                ? DurationFormatUtils.formatDuration(
                                                                (long) (totalMdo * 60L * 1000),
                                                                "HH:mm")
                                                : "00:00");
                                summary.put("totalHfo", totalHfo > 0
                                                ? DurationFormatUtils.formatDuration(
                                                                (long) (totalHfo * 60L * 1000),
                                                                "HH:mm")
                                                : "00:00");
                                summary.put("avgBostrPump", avgBostrPump / counter);
                                summary.put("avgEngineIn", avgEngineIn / counter);
                                summary.put("avgDailyTank1", avgDailyTank1 / counter);
                                summary.put("avgDailyTank2", avgDailyTank2 / counter);
                                summary.put("totalBoilerRunTime", totalBoilerRunTime > 0
                                                ? DurationFormatUtils.formatDuration(
                                                                (long) (totalBoilerRunTime * 60L
                                                                                * 1000),
                                                                "HH:mm")
                                                : "00:00");
                                summary.put("totalPortRunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalPortRunHour * 60 * 1000,
                                                                "HH:mm"));
                                summary.put("totalStarboardRunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalStarboardRunHour * 60
                                                                                * 1000,
                                                                "HH:mm"));
                                summary.put("totalConsumptionPortPerHour", totalPortConsumption);
                                summary.put("totalConsumptionStarboardPerHour",
                                                totalStarboardConsumption);
                                summary.put("totalAE1RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalAE1RunHour * 60 * 1000,
                                                                "HH:mm"));
                                summary.put("totalAE2RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalAE2RunHour * 60 * 1000,
                                                                "HH:mm"));
                                summary.put("totalConsumptionAE1PerHour", totalAE1Consumption);
                                summary.put("totalConsumptionAE2PerHour", totalAE2Consumption);
                                summary.put("totalConsumption",
                                                totalPortConsumption
                                                                + totalStarboardConsumption
                                                                + totalAE1Consumption
                                                                + totalAE2Consumption);
                                summary.put("totalPortERH",
                                                totalPortERH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalPortERH
                                                                                                * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalPortGRH",
                                                totalPortGRH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalPortGRH
                                                                                                * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalStbdERH",
                                                totalStbdERH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalStbdERH
                                                                                                * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalStbdGRH",
                                                totalStbdGRH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalStbdGRH
                                                                                                * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");

                                summary.put("totalPortShaftRPM", totalPortShaftRPM);
                                summary.put("totalStbdShaftRPM", totalStbdShaftRPM);

                                double avgPortShaftRPM = 0.0;
                                double avgStbdShaftRPM = 0.0;

                                if (numberOfCalculatedPortShaftRPM > 0) {
                                        avgPortShaftRPM = (double) totalPortShaftRPM
                                                        / numberOfCalculatedPortShaftRPM;
                                }
                                if (numberOfCalculatedStbdShaftRPM > 0) {
                                        avgStbdShaftRPM = (double) totalStbdShaftRPM
                                                        / numberOfCalculatedStbdShaftRPM;
                                }

                                summary.put("averagePortShaftRPM",
                                                String.valueOf((int) Math.round(avgPortShaftRPM)));
                                summary.put("averageStbdShaftRPM",
                                                String.valueOf((int) Math.round(avgStbdShaftRPM)));

                                summary.put("totalPortShaftRunningHours",
                                                totalPortShaftRunningHours > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalPortShaftRunningHours
                                                                                                * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalStbdShaftRunningHours",
                                                totalStbdShaftRunningHours > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalStbdShaftRunningHours
                                                                                                * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");

                                jsonObject.put("success", true);
                                jsonObject.put("data", new JSONArray(vesselInfoList));
                                jsonObject.put("summary", summary);
                        } else {
                                jsonObject.put("success", true);
                                jsonObject.put("data", new JSONArray());
                                jsonObject.put("summary", JSONObject.NULL);
                        }
                } catch (Exception e) {
                        jsonObject = Util.exceptionToJSONObject(e);
                }
                return jsonObject;
        }

        public JSONObject selectVesselDailyInfo(Map<String, Object> map) {
                JSONObject jsonObject = new JSONObject();

                String vesselEmail = (String) map.get("vessel_email");
                String categoryGsm = null;

                Map<String, Object> parameterMap = new HashMap<>();
                parameterMap.put("email", vesselEmail);
                List<Vessel> vesselList = this.vesselMapper.searchVesselEmail(parameterMap);

                for (Vessel vessel : vesselList) {
                        categoryGsm = vessel.getCategoryGsm();
                }

                map.put("categoryGsm", categoryGsm);

                try {
                        List<VesselInfo> vesselInfoList = this.vesselMapper.selectVesselInfo(map);

                        // baseline = 1 data terakhir sebelum range report dipilih
                        VesselInfo baseline = null;
                        try {
                                baseline = this.vesselMapper.selectVesselInfoBaseline(map);
                        } catch (Exception ex) {
                                baseline = null; // fallback aman
                        }

                        if (vesselInfoList.size() > 0) {
                                PeriodFormatter fmt = new PeriodFormatterBuilder()
                                                .printZeroAlways()
                                                .minimumPrintedDigits(2)
                                                .appendHours()
                                                .appendSeparator(":")
                                                .printZeroAlways()
                                                .minimumPrintedDigits(2)
                                                .appendMinutes()
                                                .toFormatter();

                                int counter = 0;
                                int consumptionCalculationResult = 0;
                                int totalConsumptionCalculationResult = 0;
                                int consumption;
                                int fuelRefill = 0;
                                int totalFuelRefill = 0;
                                int totalPortRpm = 0;
                                int avgPortRpm = 0;
                                int totalStarboardRpm = 0;
                                int avgStarboardRpm = 0;
                                double totalSpeed = 0.0;
                                double avgSpeed = 0.0;
                                String shipName = "";
                                int numberOfCalculatedPortRpm = 0;
                                int numberOfCalculatedStarboardRpm = 0;
                                int numberOfCalculatedSpeed = 0;
                                int prevFuel = 0;
                                Timestamp prevTime = null;
                                Timestamp currentTime;
                                double consumptionPerHour;
                                int totalRpm;
                                int prevRunMinutes = 0;
                                int runMinutes;
                                int prevPortRpm = 0;
                                int prevStarBoardRpm = 0;
                                double totalPortPitch = 0.0;
                                double avgPortPitch = 0.0;
                                double totalStarboardPitch = 0.0;
                                double avgStarboardPitch = 0.0;
                                double totalPortShaft = 0.0;
                                double avgPortShaft = 0.0;
                                double totalStarboardShaft = 0.0;
                                double avgStarboardShaft = 0.0;
                                int prevEngineInletConsume = 0;
                                int prevEngineOutletConsume = 0;
                                double avgMainEngineRpm = 0;
                                int numberOfCalculatedMainEngineRpm = 0;
                                int totalMainEngineTime = 0;
                                long mainEngineTime = 0;
                                long genset1Time = 0;
                                long genset2Time = 0;
                                long genset3Time = 0;
                                long genset4Time = 0;
                                long genset5Time = 0;
                                int totalGenset1Time = 0;
                                int totalGenset2Time = 0;
                                int totalGenset3Time = 0;
                                int totalGenset4Time = 0;
                                int totalGenset5Time = 0;
                                long boilerRunTimeMinutes = 0L;
                                long mdoMinutes = 0L;
                                long hfoMinutes = 0L;
                                int totalMdo = 0;
                                int totalHfo = 0;
                                double avgBostrPump = 0;
                                double avgEngineIn = 0;
                                double avgDailyTank1 = 0;
                                double avgDailyTank2 = 0;
                                int totalBoilerRunTime = 0;
                                int numberOfCalculatedPortRunHour = 0;
                                int totalPortRunHour = 0;
                                int avgPortRunHour = 0;
                                int numberOfCalculatedStarboardRunHour = 0;
                                int totalStarboardRunHour = 0;
                                int avgStarboardRunHour = 0;
                                double totalConsumptionPortPerHour = 0;
                                double totalConsumptionStarboardPerHour = 0;
                                int totalAE1RunHour = 0;
                                int totalAE2RunHour = 0;
                                double totalConsumptionAE1PerHour = 0;
                                double totalConsumptionAE2PerHour = 0;
                                int portProgressiveRunHour = 0;
                                int starboardProgressiveRunHour = 0;
                                int AE1ProgressiveRunHour = 0;
                                int AE2ProgressiveRunHour = 0;
                                long portERH = 0;
                                long stbdERH = 0;
                                long portGRH = 0;
                                long stbdGRH = 0;
                                long portShaft = 0;
                                long stbdShaft = 0;
                                long portShaftRunningHours = 0;
                                long stbdShaftRunningHours = 0;
                                double totalPortERH = 0;
                                double totalPortGRH = 0;
                                double totalStbdERH = 0;
                                double totalStbdGRH = 0;
                                long totalPortShaftRPM = 0;
                                long totalStbdShaftRPM = 0;
                                int numberOfCalculatedPortShaftRPM = 0;
                                int numberOfCalculatedStbdShaftRPM = 0;
                                double totalPortShaftRunningHours = 0;
                                double totalStbdShaftRunningHours = 0;
                                double avgRunHour = 0;
                                double totalRunHour = 0;

                                final boolean hasAnyRpmSignal = vesselInfoList.stream()
                                                .anyMatch(v -> v.getPortRpm() > 0 || v.getStarboardRpm() > 0);

                                for (int i = 0; i < vesselInfoList.size(); i++) {
                                        counter++;
                                        VesselInfo vesselInfo = vesselInfoList.get(i);
                                        double hoursSincePrev = 0d;

                                        // previous record: kalau i==0 pakai baseline, kalau i>0 pakai row sebelumnya di
                                        // list
                                        VesselInfo previousInfo = (i == 0) ? baseline : vesselInfoList.get(i - 1);

                                        // ===== Port / Starboard Run Hour (D parameter) =====
                                        int portRunHour = resolveEngineRunHourFromCounter(vesselInfo, previousInfo, true);
                                        vesselInfo.setPortRunHour(portRunHour);
                                        if (portRunHour == 0) {
                                                portProgressiveRunHour = 0;
                                        } else {
                                                portProgressiveRunHour += portRunHour;
                                        }

                                        int starboardRunHour = resolveEngineRunHourFromCounter(vesselInfo, previousInfo,
                                                        false);
                                        vesselInfo.setStarboardRunHour(starboardRunHour);
                                        if (starboardRunHour == 0) {
                                                starboardProgressiveRunHour = 0;
                                        } else {
                                                starboardProgressiveRunHour += starboardRunHour;
                                        }

                                        vesselInfo.setFormattedPortRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getPortRunHour() * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setFormattedStarboardRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getStarboardRunHour() * 60
                                                                                        * 1000,
                                                                        "HH:mm"));

                                        vesselInfo.setPortProgressiveRunHour(portProgressiveRunHour);
                                        vesselInfo.setFormattedPortProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getPortProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setStarboardProgressiveRunHour(starboardProgressiveRunHour);
                                        vesselInfo.setFormattedStarboardProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo
                                                                                        .getStarboardProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));

                                        // ===== AE Run Hour (G parameter) =====
                                        if (vesselInfo.getParameterG() != null
                                                        && !vesselInfo.getParameterG().equalsIgnoreCase("")
                                                        && vesselInfo.getParameterG().length() == 8) {

                                                vesselInfo.setAE1RunHour(
                                                                Util.stringToInt(vesselInfo.getParameterG().substring(0,
                                                                                4)));
                                                vesselInfo.setAE2RunHour(
                                                                Util.stringToInt(vesselInfo.getParameterG().substring(4,
                                                                                8)));

                                                if (Util.stringToInt(vesselInfo.getParameterG().substring(0, 4)) == 0) {
                                                        AE1ProgressiveRunHour = 0;
                                                } else {
                                                        AE1ProgressiveRunHour = AE1ProgressiveRunHour
                                                                        + vesselInfo.getAE1RunHour();
                                                }

                                                if (Util.stringToInt(vesselInfo.getParameterG().substring(4, 8)) == 0) {
                                                        AE2ProgressiveRunHour = 0;
                                                } else {
                                                        AE2ProgressiveRunHour = AE2ProgressiveRunHour
                                                                        + vesselInfo.getAE2RunHour();
                                                }
                                        } else {
                                                AE1ProgressiveRunHour = 0;
                                                AE2ProgressiveRunHour = 0;
                                                vesselInfo.setAE1RunHour(0);
                                                vesselInfo.setAE2RunHour(0);
                                        }

                                        totalAE1RunHour += vesselInfo.getAE1RunHour();
                                        totalAE2RunHour += vesselInfo.getAE2RunHour();

                                        vesselInfo.setFormattedAE1RunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE1RunHour() * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setFormattedAE2RunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE2RunHour() * 60 * 1000,
                                                                        "HH:mm"));

                                        vesselInfo.setAE1ProgressiveRunHour(AE1ProgressiveRunHour);
                                        vesselInfo.setFormattedAE1ProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE1ProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setAE2ProgressiveRunHour(AE2ProgressiveRunHour);
                                        vesselInfo.setFormattedAE2ProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE2ProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));

                                        // ===== Akumulasi pitch / shaft / tank / pump / rpm =====
                                        totalPortPitch += vesselInfo.getPortPitch();
                                        totalStarboardPitch += vesselInfo.getStarboardPitch();
                                        totalPortShaft += vesselInfo.getPortShaft();
                                        totalStarboardShaft += vesselInfo.getStarboardShaft();
                                        if (vesselInfo.getMainEngineRpm() > 0) {
                                                avgMainEngineRpm += vesselInfo.getMainEngineRpm();
                                                numberOfCalculatedMainEngineRpm++;
                                        }
                                        avgBostrPump += vesselInfo.getBostrPump();
                                        avgEngineIn += vesselInfo.getEngineIn();
                                        avgDailyTank1 += vesselInfo.getDailyTank1();
                                        avgDailyTank2 += vesselInfo.getDailyTank2();

                                        if (vesselInfo.getPortRpm() > 0) {
                                                numberOfCalculatedPortRpm++;
                                                totalPortRpm += vesselInfo.getPortRpm();
                                        }
                                        if (vesselInfo.getStarboardRpm() > 0) {
                                                numberOfCalculatedStarboardRpm++;
                                                totalStarboardRpm += vesselInfo.getStarboardRpm();
                                        }

                                        // ==== Average SPEED: skip null / "" / "null" / <= 0.40 ====
                                        String spd = vesselInfo.getSpeed();
                                        if (spd != null && !spd.trim().isEmpty()
                                                        && !spd.trim().equalsIgnoreCase("null")) {
                                                try {
                                                        double speed = Double.parseDouble(spd.trim().replace(",", ".")); // jaga-jaga
                                                                                                                         // kalau
                                                                                                                         // ada
                                                                                                                         // "0,45"
                                                        if (speed > 0.40d) {
                                                                numberOfCalculatedSpeed++;
                                                                totalSpeed += speed;
                                                        }
                                                } catch (Exception ex) {
                                                        // skip kalau format tidak valid
                                                }
                                        }

                                        // ===== Engine Inlet / Outlet Consume =====
                                        int prevB = (previousInfo != null) ? previousInfo.getParameterB() : 0;
                                        int prevC = (previousInfo != null) ? previousInfo.getParameterC() : 0;

                                        vesselInfo.setEngineInletConsume(
                                                        safeDiffInt(vesselInfo.getParameterB(), prevB));
                                        vesselInfo.setEngineOutletConsume(
                                                        safeDiffInt(vesselInfo.getParameterC(), prevC));

                                        // keep tracker kalau masih dipakai di tempat lain (optional aman)
                                        prevEngineInletConsume = vesselInfo.getParameterB();
                                        prevEngineOutletConsume = vesselInfo.getParameterC();

                                        // ============================================================
                                        // PERBAIKAN INTI:
                                        // UI TotalCons baca consumptionCalculationResult.
                                        // Untuk SAT/Stratos (yang memang TotalCons harus = (B diff) - (C diff)),
                                        // kita set ulang consumptionCalculationResult agar konsisten dengan F2/F3.
                                        // ============================================================
                                        final String senderStr = String.valueOf(vesselInfo.getSender());
                                        final String reportTypeStr = String.valueOf(vesselInfo.getReportType());

                                        final boolean hasBC = vesselInfo.getParameterB() > 0
                                                        && vesselInfo.getParameterC() > 0;
                                        final boolean isStratos = senderStr.toLowerCase().contains("stratosmobile.net");
                                        final boolean isSat = "SAT".equalsIgnoreCase(reportTypeStr);

                                        // (opsional) kalau kamu mau lebih ketat, bisa tambah condition categoryGsm
                                        // tertentu.
                                        final boolean wantTotalFromBC = hasBC && (isSat || isStratos);

                                        if (i == 0) {
                                                jsonObject.put("reportType", vesselInfo.getReportType());
                                                jsonObject.put("vesselName", vesselInfo.getName());

                                                // ==== NEW: kalau baseline ada, row pertama dihitung dari baseline ====
                                                if (previousInfo != null) {

                                                        // 1) Fuel refill row pertama = max(currA - baselineA, 0)
                                                        int baselineA = previousInfo.getParameterA();
                                                        int refillFirst = calcRefuelDiff(
                                                                        vesselInfo.getParameterA(), baselineA);
                                                        if (isRefuelGapTooLong(previousInfo.getSentDate(),
                                                                        vesselInfo.getSentDate())) {
                                                                refillFirst = 0;
                                                        }
                                                        vesselInfo.setFuelRefill(refillFirst);
                                                        totalFuelRefill += refillFirst;

                                                        // 2) Consumption row pertama (ikut pola perhitungan existing
                                                        // kamu)
                                                        // baseline consumption dianggap sebagai
                                                        // "consumptionCalculationResult" sebelumnya
                                                        consumptionCalculationResult = previousInfo.getConsumption();

                                                        // sama seperti branch else: kurangi efek refill
                                                        vesselInfo.setConsumption(
                                                                        vesselInfo.getConsumption() - refillFirst);
                                                        int consumptionNow = vesselInfo.getConsumption();

                                                        // hitung actual first row
                                                        int firstCalc = consumptionCalculationResult - consumptionNow
                                                                        + refillFirst;
                                                        vesselInfo.setConsumptionCalculationResult(firstCalc);

                                                        // update state supaya row ke-2 dst tetap pakai logic lama
                                                        fuelRefill = vesselInfo.getParameterA();
                                                        prevFuel = refillFirst;

                                                        // 3) Interval waktu untuk row pertama (baseline time -> current
                                                        // time)
                                                        prevTime = previousInfo.getSentDate();
                                                        currentTime = vesselInfo.getSentDate();

                                                        Duration duration = new Duration(
                                                                        Util.timestampToDateTime(prevTime),
                                                                        Util.timestampToDateTime(currentTime));

                                                        long millis = duration.getMillis();
                                                        double hours = millis / 3600000d; // ms -> jam
                                                        hoursSincePrev = hours;

                                                        if (hours > 0d) {
                                                                consumptionPerHour = (double) firstCalc / hours;
                                                        } else {
                                                                consumptionPerHour = 0d;
                                                        }
                                                        vesselInfo.setConsumptionPerHour(consumptionPerHour);

                                                        // OPTIONAL (kalau runhour mau ikut “2 menit” bukan 1 menit):
                                                        long minutesDuration = Math.round(millis / 60000d); // rounding,
                                                                                                            // bukan
                                                                                                            // floor
                                                        if (millis > 0 && minutesDuration == 0)
                                                                minutesDuration = 1;

                                                        // 4) RunHour row pertama (pakai RPM baseline untuk decide
                                                        // running / not)
                                                        prevPortRpm = previousInfo.getPortRpm();
                                                        prevStarBoardRpm = previousInfo.getStarboardRpm();

                                                        int totalRpmFirst = prevPortRpm + prevStarBoardRpm;
                                                        if (totalRpmFirst == 0) {
                                                                // tidak running
                                                                prevRunMinutes = 0;
                                                                vesselInfo.setFormattedRunHour("00:00");

                                                                // tapi kalau kamu mau tetap hitung saat mainEngineTime
                                                                // ada (sesuai pola kamu):
                                                                if (vesselInfo.getMainEngineTime() > 0
                                                                                && minutesDuration > 0) {
                                                                        prevRunMinutes += (int) minutesDuration;
                                                                        vesselInfo.setFormattedRunHour(
                                                                                        DurationFormatUtils
                                                                                                        .formatDuration((long) prevRunMinutes
                                                                                                                        * 60
                                                                                                                        * 1000,
                                                                                                                        "HH:mm"));
                                                                }
                                                        } else {
                                                                // running
                                                                prevRunMinutes = (minutesDuration > 0)
                                                                                ? (int) minutesDuration
                                                                                : 0;
                                                                vesselInfo.setFormattedRunHour(
                                                                                DurationFormatUtils.formatDuration(
                                                                                                (long) prevRunMinutes
                                                                                                                * 60
                                                                                                                * 1000,
                                                                                                "HH:mm"));
                                                        }

                                                        totalRunHour += prevRunMinutes;

                                                        // move state ke current row
                                                        prevTime = vesselInfo.getSentDate();
                                                        prevPortRpm = vesselInfo.getPortRpm();
                                                        prevStarBoardRpm = vesselInfo.getStarboardRpm();

                                                } else {
                                                        // ==== fallback lama kalau baseline tidak ada ====
                                                        vesselInfo.setConsumptionCalculationResult(0);
                                                        vesselInfo.setFuelRefill(0);
                                                        consumptionCalculationResult = vesselInfo.getConsumption();
                                                        totalConsumptionCalculationResult = 0;
                                                        fuelRefill = vesselInfo.getParameterA();
                                                        totalFuelRefill = vesselInfo.getFuelRefill();
                                                        prevFuel = 0;
                                                        prevTime = vesselInfo.getSentDate();
                                                        vesselInfo.setFormattedRunHour("00:00");
                                                        prevRunMinutes = 0;
                                                        prevPortRpm = vesselInfo.getPortRpm();
                                                        prevStarBoardRpm = vesselInfo.getStarboardRpm();
                                                }
                                        } else {
                                                currentTime = vesselInfo.getSentDate();
                                                boolean longRefuelGap = isRefuelGapTooLong(prevTime, currentTime);
                                                int refuelDiff = calcRefuelDiff(
                                                                vesselInfo.getParameterA(), fuelRefill);
                                                if (longRefuelGap) {
                                                        refuelDiff = 0;
                                                }
                                                vesselInfo.setConsumption(
                                                                vesselInfo.getConsumption() - refuelDiff);
                                                consumption = vesselInfo.getConsumption();
                                                vesselInfo.setConsumptionCalculationResult(
                                                                consumptionCalculationResult - consumption + prevFuel);
                                                vesselInfo.setFuelRefill(refuelDiff);
                                                totalFuelRefill += refuelDiff;
                                                consumptionCalculationResult = vesselInfo.getConsumption();
                                                prevFuel = refuelDiff;
                                                fuelRefill = vesselInfo.getParameterA();
                                                Duration duration = new Duration(
                                                                Util.timestampToDateTime(prevTime),
                                                                Util.timestampToDateTime(currentTime));

                                                long millis = duration.getMillis();
                                                double hours = millis / 3600000d;
                                                hoursSincePrev = hours;

                                                if (hours > 0d) {
                                                        consumptionPerHour = (double) vesselInfo
                                                                        .getConsumptionCalculationResult() / hours;
                                                } else {
                                                        consumptionPerHour = 0d;
                                                }
                                                vesselInfo.setConsumptionPerHour(consumptionPerHour);

                                                // OPTIONAL (kalau runhour mau ikut “2 menit” bukan 1 menit):
                                                long minutesDuration = Math.round(millis / 60000d);
                                                if (millis > 0 && minutesDuration == 0)
                                                        minutesDuration = 1;

                                                totalRpm = prevPortRpm + prevStarBoardRpm;
                                                if (totalRpm == 0) {
                                                        vesselInfo.setFormattedRunHour("00:00");
                                                        prevRunMinutes = 0;

                                                        if (vesselInfo.getMainEngineTime() > 0) {
                                                                Duration tempDuration2 = new Duration(
                                                                                Util.timestampToDateTime(prevTime),
                                                                                Util.timestampToDateTime(currentTime));
                                                                prevRunMinutes += (int) tempDuration2
                                                                                .getStandardMinutes();
                                                                vesselInfo.setFormattedRunHour(
                                                                                DurationFormatUtils.formatDuration(
                                                                                                (long) prevRunMinutes
                                                                                                                * 60
                                                                                                                * 1000,
                                                                                                "HH:mm"));
                                                        }
                                                } else {
                                                        DateTime tempPrevTime = Util.timestampToDateTime(prevTime)
                                                                        .plusMinutes(prevRunMinutes);
                                                        Duration tempDuration = new Duration(
                                                                        tempPrevTime,
                                                                        Util.timestampToDateTime(currentTime));

                                                        Duration tempDuration2 = new Duration(
                                                                        Util.timestampToDateTime(prevTime),
                                                                        Util.timestampToDateTime(currentTime));
                                                        prevRunMinutes += (int) tempDuration2.getStandardMinutes();
                                                        vesselInfo.setFormattedRunHour(
                                                                        DurationFormatUtils.formatDuration(
                                                                                        (long) prevRunMinutes * 60
                                                                                                        * 1000,
                                                                                        "HH:mm"));
                                                }

                                                totalRunHour += prevRunMinutes;
                                                prevTime = vesselInfo.getSentDate();
                                                prevPortRpm = vesselInfo.getPortRpm();
                                                prevStarBoardRpm = vesselInfo.getStarboardRpm();
                                        }

                                        // FINAL: TotalCons = (F2-EIC diff) - (F3-EOC diff)
                                        int totalConsFromBC = calcTotalConsFromBC(vesselInfo, previousInfo);
                                        vesselInfo.setConsumptionCalculationResult(totalConsFromBC);
                                        totalConsumptionCalculationResult += totalConsFromBC;
                                        if (hoursSincePrev > 0d) {
                                                vesselInfo.setConsumptionPerHour(
                                                                (double) totalConsFromBC / hoursSincePrev);
                                        } else {
                                                vesselInfo.setConsumptionPerHour(0d);
                                        }

                                        // ===== Konsumsi Port / Stbd per hour =====
                                        double consumptionPortPerHour = 0;
                                        if (vesselInfo.getConsumptionCalculationResult() > 0) {
                                                consumptionPortPerHour = (double) vesselInfo
                                                                .getConsumptionCalculationResult()
                                                                / ((double) vesselInfo.getPortRunHour()
                                                                                + (double) vesselInfo
                                                                                                .getStarboardRunHour())
                                                                * (double) vesselInfo.getPortRunHour();
                                        }
                                        if (Double.isNaN(consumptionPortPerHour)) {
                                                vesselInfo.setConsumptionPortPerHour(0);
                                                totalConsumptionPortPerHour += 0;
                                        } else {
                                                vesselInfo.setConsumptionPortPerHour(consumptionPortPerHour);
                                                totalConsumptionPortPerHour += consumptionPortPerHour;
                                        }

                                        double consumptionStarboardPerHour = 0;
                                        if (vesselInfo.getConsumptionCalculationResult() > 0) {
                                                consumptionStarboardPerHour = (double) vesselInfo
                                                                .getConsumptionCalculationResult()
                                                                / ((double) vesselInfo.getPortRunHour()
                                                                                + (double) vesselInfo
                                                                                                .getStarboardRunHour())
                                                                * (double) vesselInfo.getStarboardRunHour();
                                        }
                                        if (Double.isNaN(consumptionStarboardPerHour)) {
                                                vesselInfo.setConsumptionStarboardPerHour(0);
                                                totalConsumptionStarboardPerHour += 0;
                                        } else {
                                                vesselInfo.setConsumptionStarboardPerHour(consumptionStarboardPerHour);
                                                totalConsumptionStarboardPerHour += consumptionStarboardPerHour;
                                        }

                                        // ===== AE1 / AE2 consumption per hour =====
                                        double AE1ConsumptionPerHour = ((double) vesselInfo.getAE1RunHour() / 60)
                                                        * 43.65;
                                        double AE2ConsumptionPerHour = ((double) vesselInfo.getAE2RunHour() / 60)
                                                        * 38.4;
                                        vesselInfo.setAE1ConsumptionPerHour(AE1ConsumptionPerHour);
                                        totalConsumptionAE1PerHour += AE1ConsumptionPerHour;
                                        vesselInfo.setAE2ConsumptionPerHour(AE2ConsumptionPerHour);
                                        totalConsumptionAE2PerHour += AE2ConsumptionPerHour;

                                        // ===== Koreksi khusus sender tertentu =====
                                        if (vesselInfo.getSender().equalsIgnoreCase("VESSEL_SENDER_EMAIL_PLACEHOLDER")) {
                                                if (vesselInfo.getPortRunHour() > 0
                                                                && vesselInfo.getStarboardRunHour() > 0) {
                                                        vesselInfo.setConsumptionPortPerHour(
                                                                        vesselInfo.getConsumptionPortPerHour()
                                                                                        + Double.parseDouble("1"));
                                                        vesselInfo.setConsumptionStarboardPerHour(
                                                                        vesselInfo.getConsumptionStarboardPerHour()
                                                                                        + Double.parseDouble("1"));
                                                        vesselInfo.setConsumptionCalculationResult(
                                                                        vesselInfo.getConsumptionCalculationResult()
                                                                                        + 2);
                                                } else if (vesselInfo.getPortRunHour() > 0
                                                                && vesselInfo.getStarboardRunHour() == 0) {
                                                        vesselInfo.setConsumptionPortPerHour(
                                                                        vesselInfo.getConsumptionPortPerHour()
                                                                                        + Double.parseDouble("2"));
                                                        vesselInfo.setConsumptionStarboardPerHour(
                                                                        vesselInfo.getConsumptionStarboardPerHour()
                                                                                        + Double.parseDouble("0"));
                                                        vesselInfo.setConsumptionCalculationResult(
                                                                        vesselInfo.getConsumptionCalculationResult()
                                                                                        + 2);
                                                } else if (vesselInfo.getPortRunHour() == 0
                                                                && vesselInfo.getStarboardRunHour() > 0) {
                                                        vesselInfo.setConsumptionPortPerHour(
                                                                        vesselInfo.getConsumptionPortPerHour()
                                                                                        + Double.parseDouble("0"));
                                                        vesselInfo.setConsumptionStarboardPerHour(
                                                                        vesselInfo.getConsumptionStarboardPerHour()
                                                                                        + Double.parseDouble("2"));
                                                        vesselInfo.setConsumptionCalculationResult(
                                                                        vesselInfo.getConsumptionCalculationResult()
                                                                                        + 2);
                                                } else {
                                                        vesselInfo.setConsumptionPortPerHour(0);
                                                        vesselInfo.setConsumptionStarboardPerHour(0);
                                                        vesselInfo.setConsumptionCalculationResult(0);
                                                }
                                        }

                                        double totalFuelUsed = vesselInfo.getConsumptionPortPerHour()
                                                        + vesselInfo.getConsumptionStarboardPerHour()
                                                        + AE1ConsumptionPerHour
                                                        + AE2ConsumptionPerHour;
                                        vesselInfo.setTotalFuelUsed(totalFuelUsed);

                                        if (vesselInfo.getPortRunHour() > 0) {
                                                numberOfCalculatedPortRunHour++;
                                                totalPortRunHour += vesselInfo.getPortRunHour();
                                        }
                                        if (vesselInfo.getStarboardRunHour() > 0) {
                                                numberOfCalculatedStarboardRunHour++;
                                                totalStarboardRunHour += vesselInfo.getStarboardRunHour();
                                        }

                                        // ===== 1) Sent date -> LocalDate =====
                                        LocalDate sentLocalDate = null;
                                        if (vesselInfo.getSentDate() != null) {
                                                sentLocalDate = vesselInfo.getSentDate()
                                                                .toInstant()
                                                                .atZone(ZoneId.systemDefault())
                                                                .toLocalDate();
                                        }

                                        // ===== 2) Mode kalkulasi genset (RAW vs DIFF) =====
                                        final LocalDate cutoff = vesselInfo.getCalcGensetCutoff();

                                        // Formula 2 ON/OFF cukup dari flag
                                        final boolean isFormula2 = vesselInfo.isUseSafeDiffErh();

                                        // Jika cutoff diisi: Formula 2 mulai dari tanggal cutoff (inclusive)
                                        // Jika cutoff null: langsung aktif
                                        final boolean afterCutoff = (sentLocalDate != null)
                                                        && (cutoff == null || !sentLocalDate.isBefore(cutoff)); // >=
                                                                                                                // cutoff

                                        final boolean useFormula2 = isFormula2 && afterCutoff;

                                        // ===== 3) Hitung & format run hour genset/main engine =====
                                        if (useFormula2) {
                                                final VesselInfo current = vesselInfoList.get(i);
                                                final VesselInfo previous = previousInfo;

                                                if (previous == null) {
                                                        current.setFormattedMainEngineTime("00:00");
                                                        current.setFormattedGenset1RunHour("00:00");
                                                        current.setFormattedGenset2RunHour("00:00");
                                                        current.setFormattedGenset3RunHour("00:00");
                                                        current.setFormattedGenset4RunHour("00:00");
                                                        current.setFormattedGenset5RunHour("00:00");
                                                        current.setFormattedBoilerRunTime("00:00");
                                                        current.setFormattedMdo("00:00");
                                                        current.setFormattedHfo("00:00");

                                                        mainEngineTime = 0;
                                                        genset1Time = genset2Time = genset3Time = genset4Time = genset5Time = 0;
                                                        boilerRunTimeMinutes = 0L;
                                                        mdoMinutes = 0L;
                                                        hfoMinutes = 0L;
                                                } else {
                                                        mainEngineTime = normalizeCounterMinutesForDisplay(
                                                                        calcMainEngineTimeMinutes(
                                                                                        current.getMainEngineTime(),
                                                                                        previous.getMainEngineTime()));
                                                        genset1Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset1Time(),
                                                                                        previous.getGenset1Time()));
                                                        genset2Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset2Time(),
                                                                                        previous.getGenset2Time()));
                                                        genset3Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset3Time(),
                                                                                        previous.getGenset3Time()));
                                                        genset4Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset4Time(),
                                                                                        previous.getGenset4Time()));
                                                        genset5Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset5Time(),
                                                                                        previous.getGenset5Time()));

                                                        boilerRunTimeMinutes = safeDiff(current.getBoilerRunTime(),
                                                                        previous.getBoilerRunTime());
                                                        mdoMinutes = safeDiff(current.getMdo(), previous.getMdo());
                                                        hfoMinutes = safeDiff(current.getHfo(), previous.getHfo());

                                                        current.setFormattedMainEngineTime(formatHHmm(mainEngineTime));
                                                        current.setFormattedGenset1RunHour(genset1Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset1Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset2RunHour(genset2Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset2Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset3RunHour(genset3Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset3Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset4RunHour(genset4Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset4Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset5RunHour(genset5Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset5Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");

                                                        current.setFormattedBoilerRunTime(boilerRunTimeMinutes > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        boilerRunTimeMinutes * 60L
                                                                                                        * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedMdo(mdoMinutes > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        mdoMinutes * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedHfo(hfoMinutes > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        hfoMinutes * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                }
                                        } else {
                                                // FORMULA 1 (RAW)
                                                mainEngineTime = vesselInfo.getMainEngineTime();
                                                genset1Time = vesselInfo.getGenset1Time();
                                                genset2Time = vesselInfo.getGenset2Time();
                                                genset3Time = vesselInfo.getGenset3Time();
                                                genset4Time = vesselInfo.getGenset4Time();
                                                genset5Time = vesselInfo.getGenset5Time();

                                                boilerRunTimeMinutes = vesselInfo.getBoilerRunTime();
                                                mdoMinutes = vesselInfo.getMdo();
                                                hfoMinutes = vesselInfo.getHfo();

                                                vesselInfo.setFormattedMainEngineTime(formatHHmm(mainEngineTime));
                                                vesselInfo.setFormattedGenset1RunHour(
                                                                genset1Time > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                genset1Time * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedGenset2RunHour(
                                                                genset2Time > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                genset2Time * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedGenset3RunHour(
                                                                genset3Time > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                genset3Time * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedGenset4RunHour(
                                                                genset4Time > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                genset4Time * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedGenset5RunHour(
                                                                genset5Time > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                genset5Time * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedBoilerRunTime(
                                                                boilerRunTimeMinutes > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                boilerRunTimeMinutes
                                                                                                                * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedMdo(
                                                                mdoMinutes > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                mdoMinutes * 60L * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedHfo(
                                                                hfoMinutes > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                hfoMinutes * 60L * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                        }

                                        if (shouldZeroMainEngineTimeAtEngineOff(vesselInfo, previousInfo)) {
                                                mainEngineTime = 0;
                                                vesselInfo.setFormattedMainEngineTime("00:00");
                                        }

                                        // ===== 4) Akumulasi total waktu (pakai *Time di atas) =====
                                        if (mainEngineTime > 0)
                                                totalMainEngineTime += mainEngineTime;
                                        if (genset1Time > 0)
                                                totalGenset1Time += genset1Time;
                                        if (genset2Time > 0)
                                                totalGenset2Time += genset2Time;
                                        if (genset3Time > 0)
                                                totalGenset3Time += genset3Time;
                                        if (genset4Time > 0)
                                                totalGenset4Time += genset4Time;
                                        if (genset5Time > 0)
                                                totalGenset5Time += genset5Time;
                                        if (boilerRunTimeMinutes > 0)
                                                totalBoilerRunTime += boilerRunTimeMinutes;
                                        if (mdoMinutes > 0)
                                                totalMdo += mdoMinutes;
                                        if (hfoMinutes > 0)
                                                totalHfo += hfoMinutes;

                                        // ===== 5) ERH/GRH/Shaft Running Hours (safeDiff jika flag aktif) =====
                                        if (useFormula2) {
                                                final VesselInfo current = vesselInfoList.get(i);
                                                final VesselInfo previous = previousInfo;

                                                if (previous == null) {
                                                        current.setFormattedPortERH("00:00");
                                                        current.setFormattedStbdERH("00:00");
                                                        current.setFormattedPortGRH("00:00");
                                                        current.setFormattedStbdGRH("00:00");
                                                        current.setFormattedPortShaftRunningHours("00:00");
                                                        current.setFormattedStbdShaftRunningHours("00:00");

                                                        portERH = stbdERH = portGRH = stbdGRH = portShaftRunningHours = stbdShaftRunningHours = 0;
                                                } else {
                                                        portERH = resolveCounterMinutes(current.getPortERH(),
                                                                        previous.getPortERH(), true);
                                                        stbdERH = resolveCounterMinutes(current.getStbdERH(),
                                                                        previous.getStbdERH(), true);
                                                        portERH = applyEngineRunHoursByRpm(portERH,
                                                                        current.getPortRpm(), hasAnyRpmSignal);
                                                        stbdERH = applyEngineRunHoursByRpm(stbdERH,
                                                                        current.getStarboardRpm(), hasAnyRpmSignal);
                                                        portGRH = resolveCounterMinutes(current.getPortGRH(),
                                                                        previous.getPortGRH(), true);
                                                        stbdGRH = resolveCounterMinutes(current.getStbdGRH(),
                                                                        previous.getStbdGRH(), true);
                                                        portShaftRunningHours = resolveCounterMinutes(
                                                                        current.getPortShaftRunningHours(),
                                                                        previous.getPortShaftRunningHours(), true);
                                                        stbdShaftRunningHours = resolveCounterMinutes(
                                                                        current.getStbdShaftRunningHours(),
                                                                        previous.getStbdShaftRunningHours(), true);

                                                        current.setFormattedPortERH(formatHHmm(portERH));
                                                        current.setFormattedStbdERH(formatHHmm(stbdERH));
                                                        current.setFormattedPortGRH(formatHHmm(portGRH));
                                                        current.setFormattedStbdGRH(formatHHmm(stbdGRH));
                                                        current.setFormattedPortShaftRunningHours(
                                                                        formatHHmm(portShaftRunningHours));
                                                        current.setFormattedStbdShaftRunningHours(
                                                                        formatHHmm(stbdShaftRunningHours));
                                                }
                                        } else {
                                                // mode lama
                                                if (!hasAnyRpmSignal) {
                                                        if (previousInfo == null) {
                                                                portERH = 0;
                                                                stbdERH = 0;
                                                        } else {
                                                                portERH = resolveCounterMinutes(vesselInfo.getPortERH(),
                                                                                previousInfo.getPortERH(), true);
                                                                stbdERH = resolveCounterMinutes(vesselInfo.getStbdERH(),
                                                                                previousInfo.getStbdERH(), true);
                                                        }
                                                } else {
                                                        portERH = resolveCounterMinutes(vesselInfo.getPortERH(), 0,
                                                                        false);
                                                        stbdERH = resolveCounterMinutes(vesselInfo.getStbdERH(), 0,
                                                                        false);
                                                }
                                                portERH = applyEngineRunHoursByRpm(portERH, vesselInfo.getPortRpm(),
                                                                hasAnyRpmSignal);
                                                stbdERH = applyEngineRunHoursByRpm(stbdERH,
                                                                vesselInfo.getStarboardRpm(), hasAnyRpmSignal);
                                                portGRH = resolveCounterMinutes(vesselInfo.getPortGRH(), 0, false);
                                                stbdGRH = resolveCounterMinutes(vesselInfo.getStbdGRH(), 0, false);
                                                portShaftRunningHours = resolveCounterMinutes(
                                                                vesselInfo.getPortShaftRunningHours(), 0, false);
                                                stbdShaftRunningHours = resolveCounterMinutes(
                                                                vesselInfo.getStbdShaftRunningHours(), 0, false);

                                                vesselInfo.setFormattedPortERH(formatHHmm(portERH));
                                                vesselInfo.setFormattedStbdERH(formatHHmm(stbdERH));
                                                vesselInfo.setFormattedPortGRH(formatHHmm(portGRH));
                                                vesselInfo.setFormattedStbdGRH(formatHHmm(stbdGRH));
                                                vesselInfo.setFormattedPortShaftRunningHours(
                                                                formatHHmm(portShaftRunningHours));
                                                vesselInfo.setFormattedStbdShaftRunningHours(
                                                                formatHHmm(stbdShaftRunningHours));
                                        }

                                        if (shouldZeroEngineAndShaftHoursAtEngineOff(vesselInfo, previousInfo)) {
                                                portERH = 0;
                                                stbdERH = 0;
                                                portShaftRunningHours = 0;
                                                stbdShaftRunningHours = 0;
                                                vesselInfo.setFormattedPortERH("00:00");
                                                vesselInfo.setFormattedStbdERH("00:00");
                                                vesselInfo.setFormattedPortShaftRunningHours("00:00");
                                                vesselInfo.setFormattedStbdShaftRunningHours("00:00");
                                        }

                                        // ===== 6) RPM Shaft: null/+0 tidak dihitung untuk average =====
                                        portShaft = vesselInfo.getPortShaftRPM();
                                        stbdShaft = vesselInfo.getStbdShaftRPM();
                                        vesselInfo.setFormattedPortShaftRPM(formatHHmm(portShaft));
                                        vesselInfo.setFormattedStbdShaftRPM(formatHHmm(stbdShaft));

                                        // 7) Akumulasi total lain (hanya jika > 0)
                                        if (portERH > 0)
                                                totalPortERH += portERH;
                                        if (portGRH > 0)
                                                totalPortGRH += portGRH;
                                        if (stbdERH > 0)
                                                totalStbdERH += stbdERH;
                                        if (stbdGRH > 0)
                                                totalStbdGRH += stbdGRH;

                                        if (portShaft > 0) {
                                                totalPortShaftRPM += portShaft;
                                                numberOfCalculatedPortShaftRPM++;
                                        }
                                        if (stbdShaft > 0) {
                                                totalStbdShaftRPM += stbdShaft;
                                                numberOfCalculatedStbdShaftRPM++;
                                        }
                                        if (portShaftRunningHours > 0)
                                                totalPortShaftRunningHours += portShaftRunningHours;
                                        if (stbdShaftRunningHours > 0)
                                                totalStbdShaftRunningHours += stbdShaftRunningHours;
                                }

                                // ===== Setelah loop: hitung total & average =====
                                double totalMainEngineConsumption = 0;
                                double totalPortConsumption = 0;
                                double totalStarboardConsumption = 0;
                                double totalAE1Consumption = 0;
                                double totalAE2Consumption = 0;

                                for (VesselInfo vesselInfo : vesselInfoList) {
                                        if (vesselInfo.getSender().equalsIgnoreCase("VESSEL_SENDER_EMAIL_PLACEHOLDER")) {
                                                vesselInfo.setConsumptionPortPerHour(
                                                                vesselInfo.getConsumptionPortPerHour()
                                                                                * vesselInfo.getPortConsumptionCoefficient());
                                                vesselInfo.setConsumptionStarboardPerHour(
                                                                vesselInfo.getConsumptionStarboardPerHour()
                                                                                * vesselInfo.getStarboardConsumptionCoefficient());
                                                vesselInfo.setAE1ConsumptionPerHour(
                                                                vesselInfo.getAE1ConsumptionPerHour()
                                                                                * vesselInfo.getAE1ConsumptionCoefficient());
                                                vesselInfo.setAE2ConsumptionPerHour(
                                                                vesselInfo.getAE2ConsumptionPerHour()
                                                                                * vesselInfo.getAE2ConsumptionCoefficient());

                                                double tempConsumptionCalculationResult = vesselInfo
                                                                .getConsumptionPortPerHour()
                                                                + vesselInfo.getConsumptionStarboardPerHour();
                                                vesselInfo.setConsumptionCalculationResult(
                                                                (int) Math.round(tempConsumptionCalculationResult));
                                                vesselInfo.setTotalFuelUsed(
                                                                vesselInfo.getConsumptionPortPerHour()
                                                                                + vesselInfo.getConsumptionStarboardPerHour()
                                                                                + vesselInfo.getAE1ConsumptionPerHour()
                                                                                + vesselInfo.getAE2ConsumptionPerHour());

                                                totalMainEngineConsumption += tempConsumptionCalculationResult;
                                                totalPortConsumption += vesselInfo.getConsumptionPortPerHour();
                                                totalStarboardConsumption += vesselInfo
                                                                .getConsumptionStarboardPerHour();
                                                totalAE1Consumption += vesselInfo.getAE1ConsumptionPerHour();
                                                totalAE2Consumption += vesselInfo.getAE2ConsumptionPerHour();
                                        }
                                }

                                if (numberOfCalculatedPortRpm > 0) {
                                        avgPortRpm = totalPortRpm / numberOfCalculatedPortRpm;
                                }
                                if (numberOfCalculatedStarboardRpm > 0) {
                                        avgStarboardRpm = totalStarboardRpm / numberOfCalculatedStarboardRpm;
                                }
                                if (numberOfCalculatedSpeed > 0) {
                                        avgSpeed = totalSpeed / numberOfCalculatedSpeed;
                                } else {
                                        avgSpeed = 0;
                                }

                                avgPortPitch = totalPortPitch / counter;
                                avgStarboardPitch = totalStarboardPitch / counter;
                                avgPortShaft = totalPortShaft / counter;
                                avgStarboardShaft = totalStarboardShaft / counter;
                                avgRunHour = totalRunHour / counter;

                                // Average RPM Shaft pakai hanya baris dengan RPM > 0
                                double avgPortShaftRPM = 0.0;
                                double avgStbdShaftRPM = 0.0;
                                if (numberOfCalculatedPortShaftRPM > 0) {
                                        avgPortShaftRPM = (double) totalPortShaftRPM / numberOfCalculatedPortShaftRPM;
                                }
                                if (numberOfCalculatedStbdShaftRPM > 0) {
                                        avgStbdShaftRPM = (double) totalStbdShaftRPM / numberOfCalculatedStbdShaftRPM;
                                }

                                // ===== Summary =====
                                Map<String, Object> summary = new HashMap<>();
                                summary.put("consumption", String.valueOf(totalConsumptionCalculationResult));
                                if (vesselInfoList.size() > 0) {
                                        VesselInfo vesselInfo = vesselInfoList.get(0);
                                        if (vesselInfo.getSender().equalsIgnoreCase("VESSEL_SENDER_EMAIL_PLACEHOLDER")) {
                                                summary.put("consumption",
                                                                String.valueOf(totalMainEngineConsumption));
                                        }
                                }

                                summary.put("avgPortRpm", avgPortRpm);
                                summary.put("avgStarboardRpm", avgStarboardRpm);
                                summary.put("avgSpeed", avgSpeed);
                                summary.put("totalFuelRefill", totalFuelRefill);
                                summary.put("avgPortPitch", avgPortPitch);
                                summary.put("avgStarboardPitch", avgStarboardPitch);
                                summary.put("avgPortShaft", avgPortShaft);
                                summary.put("avgStarboardShaft", avgStarboardShaft);
                                int avgMainEngineRpmValue = 0;
                                if (numberOfCalculatedMainEngineRpm > 0) {
                                        avgMainEngineRpmValue = (int) Math.round(
                                                        avgMainEngineRpm / numberOfCalculatedMainEngineRpm);
                                }
                                summary.put("avgMainEngineRpm", avgMainEngineRpmValue);
                                summary.put("avgRunHour",
                                                avgRunHour > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (avgRunHour * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalRunHour",
                                                totalRunHour > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalRunHour * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalMainEngineTime",
                                                totalMainEngineTime > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalMainEngineTime * 60L
                                                                                                * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalGenset1Time", totalGenset1Time);
                                summary.put("totalGenset2Time", totalGenset2Time);
                                summary.put("totalGenset3Time", totalGenset3Time);
                                summary.put("totalGenset4Time", totalGenset4Time);
                                summary.put("totalGenset5Time", totalGenset5Time);
                                summary.put("formattedTotalGenset1RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset1Time * 60 * 1000, "HH:mm"));
                                summary.put("formattedTotalGenset2RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset2Time * 60 * 1000, "HH:mm"));
                                summary.put("formattedTotalGenset3RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset3Time * 60 * 1000, "HH:mm"));
                                summary.put("formattedTotalGenset4RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset4Time * 60 * 1000, "HH:mm"));
                                summary.put("formattedTotalGenset5RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset5Time * 60 * 1000, "HH:mm"));
                                summary.put("totalMdo",
                                                totalMdo > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalMdo * 60L * 1000), "HH:mm")
                                                                : "00:00");
                                summary.put("totalHfo",
                                                totalHfo > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalHfo * 60L * 1000), "HH:mm")
                                                                : "00:00");
                                summary.put("avgBostrPump", avgBostrPump / counter);
                                summary.put("avgEngineIn", avgEngineIn / counter);
                                summary.put("avgDailyTank1", avgDailyTank1 / counter);
                                summary.put("avgDailyTank2", avgDailyTank2 / counter);
                                summary.put("totalBoilerRunTime",
                                                totalBoilerRunTime > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalBoilerRunTime * 60L
                                                                                                * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalPortRunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalPortRunHour * 60 * 1000, "HH:mm"));
                                summary.put("totalStarboardRunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalStarboardRunHour * 60 * 1000, "HH:mm"));
                                summary.put("totalConsumptionPortPerHour", totalPortConsumption);
                                summary.put("totalConsumptionStarboardPerHour", totalStarboardConsumption);
                                summary.put("totalAE1RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalAE1RunHour * 60 * 1000, "HH:mm"));
                                summary.put("totalAE2RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalAE2RunHour * 60 * 1000, "HH:mm"));
                                summary.put("totalConsumptionAE1PerHour", totalAE1Consumption);
                                summary.put("totalConsumptionAE2PerHour", totalAE2Consumption);
                                summary.put("totalConsumption",
                                                totalPortConsumption + totalStarboardConsumption
                                                                + totalAE1Consumption + totalAE2Consumption);
                                summary.put("totalPortERH",
                                                totalPortERH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalPortERH * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalPortGRH",
                                                totalPortGRH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalPortGRH * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalStbdERH",
                                                totalStbdERH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalStbdERH * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalStbdGRH",
                                                totalStbdGRH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalStbdGRH * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");

                                summary.put("totalPortShaftRPM", totalPortShaftRPM);
                                summary.put("totalStbdShaftRPM", totalStbdShaftRPM);

                                summary.put("averagePortShaftRPM",
                                                String.valueOf((int) Math.round(avgPortShaftRPM)));
                                summary.put("averageStbdShaftRPM",
                                                String.valueOf((int) Math.round(avgStbdShaftRPM)));

                                summary.put("totalPortShaftRunningHours",
                                                totalPortShaftRunningHours > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalPortShaftRunningHours * 60L
                                                                                                * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalStbdShaftRunningHours",
                                                totalStbdShaftRunningHours > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalStbdShaftRunningHours * 60L
                                                                                                * 1000),
                                                                                "HH:mm")
                                                                : "00:00");

                                jsonObject.put("success", true);
                                jsonObject.put("data", new JSONArray(vesselInfoList));
                                jsonObject.put("summary", summary);

                                List<VesselLocation> vesselLocationList = this.vesselMapper
                                                .selectFirstAndLastLocation(map);
                                jsonObject.put("vesselLocation", new JSONArray(vesselLocationList));

                                List<VesselLocation> vesselRouteLocationList = this.vesselMapper.selectLocation(map);
                                jsonObject.put("vesselRouteLocation", new JSONArray(vesselRouteLocationList));
                        } else {
                                jsonObject.put("success", true);
                                jsonObject.put("data", new JSONArray());
                                jsonObject.put("summary", JSONObject.NULL);
                                jsonObject.put("vesselLocation", new JSONArray());
                        }
                } catch (Exception e) {
                        jsonObject = Util.exceptionToJSONObject(e);
                }
                return jsonObject;
        }

        public JSONObject selectVesselMap(Map<String, Object> map) {
                JSONObject jsonObject = new JSONObject();
                List<VesselLocation> vesselLocationList = this.vesselMapper.selectFirstAndLastLocation(map);
                jsonObject.put("vesselLocation", new JSONArray(vesselLocationList));
                List<VesselLocation> vesselRouteLocationList = this.vesselMapper.selectLocation(map);
                jsonObject.put("vesselRouteLocation", new JSONArray(vesselRouteLocationList));
                return jsonObject;
        }

        public JSONObject selectShipInfo(Map<String, Object> map) {
                JSONObject jsonObject = new JSONObject();

                String vesselEmail = (String) map.get("vessel_email");
                String categoryGsm = null;

                Map<String, Object> parameterMap = new HashMap<>();
                parameterMap.put("email", vesselEmail);
                List<Vessel> vesselList = this.vesselMapper.searchVesselEmail(parameterMap);

                for (Vessel vessel : vesselList) {
                        categoryGsm = vessel.getCategoryGsm();
                }

                map.put("categoryGsm", categoryGsm);

                try {
                        List<VesselInfo> vesselInfoList = this.vesselMapper.selectVesselInfo(map);

                        // baseline = 1 data terakhir sebelum range report dipilih
                        VesselInfo baseline = null;
                        try {
                                baseline = this.vesselMapper.selectVesselInfoBaseline(map);
                        } catch (Exception ex) {
                                baseline = null; // fallback aman
                        }

                        if (vesselInfoList.size() > 0) {
                                PeriodFormatter fmt = new PeriodFormatterBuilder()
                                                .printZeroAlways()
                                                .minimumPrintedDigits(2)
                                                .appendHours()
                                                .appendSeparator(":")
                                                .printZeroAlways()
                                                .minimumPrintedDigits(2)
                                                .appendMinutes()
                                                .toFormatter();

                                int counter = 0;
                                int consumptionCalculationResult = 0;
                                int totalConsumptionCalculationResult = 0;
                                int consumption;
                                int fuelRefill = 0;
                                int totalFuelRefill = 0;
                                int totalPortRpm = 0;
                                int avgPortRpm = 0;
                                int totalStarboardRpm = 0;
                                int avgStarboardRpm = 0;
                                double totalSpeed = 0.0;
                                double avgSpeed = 0.0;
                                String shipName = "";
                                int numberOfCalculatedPortRpm = 0;
                                int numberOfCalculatedStarboardRpm = 0;
                                int numberOfCalculatedSpeed = 0;
                                int prevFuel = 0;
                                Timestamp prevTime = null;
                                Timestamp currentTime;
                                double consumptionPerHour;
                                int totalRpm;
                                int prevRunMinutes = 0;
                                int runMinutes;
                                int prevPortRpm = 0;
                                int prevStarBoardRpm = 0;
                                double totalPortPitch = 0.0;
                                double avgPortPitch = 0.0;
                                double totalStarboardPitch = 0.0;
                                double avgStarboardPitch = 0.0;
                                double totalPortShaft = 0.0;
                                double avgPortShaft = 0.0;
                                double totalStarboardShaft = 0.0;
                                double avgStarboardShaft = 0.0;
                                int prevEngineInletConsume = 0;
                                int prevEngineOutletConsume = 0;
                                double avgMainEngineRpm = 0;
                                int numberOfCalculatedMainEngineRpm = 0;
                                int totalMainEngineTime = 0;
                                long mainEngineTime = 0;
                                long genset1Time = 0;
                                long genset2Time = 0;
                                long genset3Time = 0;
                                long genset4Time = 0;
                                long genset5Time = 0;
                                int totalGenset1Time = 0;
                                int totalGenset2Time = 0;
                                int totalGenset3Time = 0;
                                int totalGenset4Time = 0;
                                int totalGenset5Time = 0;
                                long boilerRunTimeMinutes = 0L;
                                long mdoMinutes = 0L;
                                long hfoMinutes = 0L;
                                int totalMdo = 0;
                                int totalHfo = 0;
                                double avgBostrPump = 0;
                                double avgEngineIn = 0;
                                double avgDailyTank1 = 0;
                                double avgDailyTank2 = 0;
                                int totalBoilerRunTime = 0;
                                int numberOfCalculatedPortRunHour = 0;
                                int totalPortRunHour = 0;
                                int avgPortRunHour = 0;
                                int numberOfCalculatedStarboardRunHour = 0;
                                int totalStarboardRunHour = 0;
                                int avgStarboardRunHour = 0;
                                double totalConsumptionPortPerHour = 0;
                                double totalConsumptionStarboardPerHour = 0;
                                int totalAE1RunHour = 0;
                                int totalAE2RunHour = 0;
                                double totalConsumptionAE1PerHour = 0;
                                double totalConsumptionAE2PerHour = 0;
                                int portProgressiveRunHour = 0;
                                int starboardProgressiveRunHour = 0;
                                int AE1ProgressiveRunHour = 0;
                                int AE2ProgressiveRunHour = 0;
                                long portERH = 0;
                                long stbdERH = 0;
                                long portGRH = 0;
                                long stbdGRH = 0;
                                long portShaft = 0;
                                long stbdShaft = 0;
                                long portShaftRunningHours = 0;
                                long stbdShaftRunningHours = 0;
                                double totalPortERH = 0;
                                double totalPortGRH = 0;
                                double totalStbdERH = 0;
                                double totalStbdGRH = 0;
                                long totalPortShaftRPM = 0;
                                long totalStbdShaftRPM = 0;
                                int numberOfCalculatedPortShaftRPM = 0;
                                int numberOfCalculatedStbdShaftRPM = 0;
                                double totalPortShaftRunningHours = 0;
                                double totalStbdShaftRunningHours = 0;
                                double avgRunHour = 0;
                                double totalRunHour = 0;

                                final boolean hasAnyRpmSignal = vesselInfoList.stream()
                                                .anyMatch(v -> v.getPortRpm() > 0 || v.getStarboardRpm() > 0);

                                for (int i = 0; i < vesselInfoList.size(); i++) {
                                        counter++;
                                        VesselInfo vesselInfo = vesselInfoList.get(i);
                                        double hoursSincePrev = 0d;

                                        // previous record: kalau i==0 pakai baseline, kalau i>0 pakai row sebelumnya di
                                        // list
                                        VesselInfo previousInfo = (i == 0) ? baseline : vesselInfoList.get(i - 1);

                                        // ===== Run hour port/stbd (parameter D) =====
                                        int portRunHour = resolveEngineRunHourFromCounter(vesselInfo, previousInfo, true);
                                        vesselInfo.setPortRunHour(portRunHour);
                                        if (portRunHour == 0) {
                                                portProgressiveRunHour = 0;
                                        } else {
                                                portProgressiveRunHour += portRunHour;
                                        }

                                        int starboardRunHour = resolveEngineRunHourFromCounter(vesselInfo, previousInfo,
                                                        false);
                                        vesselInfo.setStarboardRunHour(starboardRunHour);
                                        if (starboardRunHour == 0) {
                                                starboardProgressiveRunHour = 0;
                                        } else {
                                                starboardProgressiveRunHour += starboardRunHour;
                                        }

                                        vesselInfo.setFormattedPortRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getPortRunHour() * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setFormattedStarboardRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getStarboardRunHour() * 60
                                                                                        * 1000,
                                                                        "HH:mm"));

                                        vesselInfo.setPortProgressiveRunHour(portProgressiveRunHour);
                                        vesselInfo.setFormattedPortProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getPortProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setStarboardProgressiveRunHour(starboardProgressiveRunHour);
                                        vesselInfo.setFormattedStarboardProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo
                                                                                        .getStarboardProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));

                                        // ===== AE run hour (parameter G) =====
                                        if (vesselInfo.getParameterG() != null
                                                        && !vesselInfo.getParameterG().equalsIgnoreCase("")
                                                        && vesselInfo.getParameterG().length() == 8) {
                                                vesselInfo.setAE1RunHour(
                                                                Util.stringToInt(vesselInfo.getParameterG().substring(0,
                                                                                4)));
                                                vesselInfo.setAE2RunHour(
                                                                Util.stringToInt(vesselInfo.getParameterG().substring(4,
                                                                                8)));

                                                if (Util.stringToInt(vesselInfo.getParameterG().substring(0, 4)) == 0) {
                                                        AE1ProgressiveRunHour = 0;
                                                } else {
                                                        AE1ProgressiveRunHour = AE1ProgressiveRunHour
                                                                        + vesselInfo.getAE1RunHour();
                                                }

                                                if (Util.stringToInt(vesselInfo.getParameterG().substring(4, 8)) == 0) {
                                                        AE2ProgressiveRunHour = 0;
                                                } else {
                                                        AE2ProgressiveRunHour = AE2ProgressiveRunHour
                                                                        + vesselInfo.getAE2RunHour();
                                                }
                                        } else {
                                                AE1ProgressiveRunHour = 0;
                                                AE2ProgressiveRunHour = 0;
                                                vesselInfo.setAE1RunHour(0);
                                                vesselInfo.setAE2RunHour(0);
                                        }

                                        totalAE1RunHour += vesselInfo.getAE1RunHour();
                                        totalAE2RunHour += vesselInfo.getAE2RunHour();

                                        vesselInfo.setFormattedAE1RunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE1RunHour() * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setFormattedAE2RunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE2RunHour() * 60 * 1000,
                                                                        "HH:mm"));

                                        vesselInfo.setAE1ProgressiveRunHour(AE1ProgressiveRunHour);
                                        vesselInfo.setFormattedAE1ProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE1ProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));
                                        vesselInfo.setAE2ProgressiveRunHour(AE2ProgressiveRunHour);
                                        vesselInfo.setFormattedAE2ProgressiveRunHour(
                                                        DurationFormatUtils.formatDuration(
                                                                        (long) vesselInfo.getAE2ProgressiveRunHour()
                                                                                        * 60 * 1000,
                                                                        "HH:mm"));

                                        // ===== akumulasi pitch/shaft/rpm/dll =====
                                        totalPortPitch += vesselInfo.getPortPitch();
                                        totalStarboardPitch += vesselInfo.getStarboardPitch();
                                        totalPortShaft += vesselInfo.getPortShaft();
                                        totalStarboardShaft += vesselInfo.getStarboardShaft();
                                        if (vesselInfo.getMainEngineRpm() > 0) {
                                                avgMainEngineRpm += vesselInfo.getMainEngineRpm();
                                                numberOfCalculatedMainEngineRpm++;
                                        }
                                        avgBostrPump += vesselInfo.getBostrPump();
                                        avgEngineIn += vesselInfo.getEngineIn();
                                        avgDailyTank1 += vesselInfo.getDailyTank1();
                                        avgDailyTank2 += vesselInfo.getDailyTank2();

                                        if (vesselInfo.getPortRpm() > 0) {
                                                numberOfCalculatedPortRpm++;
                                                totalPortRpm += vesselInfo.getPortRpm();
                                        }
                                        if (vesselInfo.getStarboardRpm() > 0) {
                                                numberOfCalculatedStarboardRpm++;
                                                totalStarboardRpm += vesselInfo.getStarboardRpm();
                                        }

                                        // ==== Average SPEED: skip null / "" / "null" / <= 0.40 ====
                                        String spd = vesselInfo.getSpeed();
                                        if (spd != null && !spd.trim().isEmpty()
                                                        && !spd.trim().equalsIgnoreCase("null")) {
                                                try {
                                                        double speed = Double.parseDouble(spd.trim().replace(",", ".")); // jaga-jaga
                                                                                                                         // kalau
                                                                                                                         // ada
                                                                                                                         // "0,45"
                                                        if (speed > 0.40d) {
                                                                numberOfCalculatedSpeed++;
                                                                totalSpeed += speed;
                                                        }
                                                } catch (Exception ex) {
                                                        // skip kalau format tidak valid
                                                }
                                        }

                                        int prevB = (previousInfo != null) ? previousInfo.getParameterB() : 0;
                                        int prevC = (previousInfo != null) ? previousInfo.getParameterC() : 0;

                                        vesselInfo.setEngineInletConsume(
                                                        safeDiffInt(vesselInfo.getParameterB(), prevB));
                                        vesselInfo.setEngineOutletConsume(
                                                        safeDiffInt(vesselInfo.getParameterC(), prevC));

                                        // keep tracker kalau masih dipakai di tempat lain (optional aman)
                                        prevEngineInletConsume = vesselInfo.getParameterB();
                                        prevEngineOutletConsume = vesselInfo.getParameterC();

                                        // ============================================================
                                        // PERBAIKAN INTI:
                                        // UI TotalCons baca consumptionCalculationResult.
                                        // Untuk SAT/Stratos (yang memang TotalCons harus = (B diff) - (C diff)),
                                        // kita set ulang consumptionCalculationResult agar konsisten dengan F2/F3.
                                        // ============================================================
                                        final String senderStr = String.valueOf(vesselInfo.getSender());
                                        final String reportTypeStr = String.valueOf(vesselInfo.getReportType());

                                        final boolean hasBC = vesselInfo.getParameterB() > 0
                                                        && vesselInfo.getParameterC() > 0;
                                        final boolean isStratos = senderStr.toLowerCase().contains("stratosmobile.net");
                                        final boolean isSat = "SAT".equalsIgnoreCase(reportTypeStr);

                                        // (opsional) kalau kamu mau lebih ketat, bisa tambah condition categoryGsm
                                        // tertentu.
                                        final boolean wantTotalFromBC = hasBC && (isSat || isStratos);

                                        if (i == 0) {
                                                jsonObject.put("reportType", vesselInfo.getReportType());
                                                jsonObject.put("vesselName", vesselInfo.getName());

                                                // ==== NEW: kalau baseline ada, row pertama dihitung dari baseline ====
                                                if (previousInfo != null) {

                                                        // 1) Fuel refill row pertama = max(currA - baselineA, 0)
                                                        int baselineA = previousInfo.getParameterA();
                                                        int refillFirst = calcRefuelDiff(
                                                                        vesselInfo.getParameterA(), baselineA);
                                                        if (isRefuelGapTooLong(previousInfo.getSentDate(),
                                                                        vesselInfo.getSentDate())) {
                                                                refillFirst = 0;
                                                        }
                                                        vesselInfo.setFuelRefill(refillFirst);
                                                        totalFuelRefill += refillFirst;

                                                        // 2) Consumption row pertama (ikut pola perhitungan existing
                                                        // kamu)
                                                        // baseline consumption dianggap sebagai
                                                        // "consumptionCalculationResult" sebelumnya
                                                        consumptionCalculationResult = previousInfo.getConsumption();

                                                        // sama seperti branch else: kurangi efek refill
                                                        vesselInfo.setConsumption(
                                                                        vesselInfo.getConsumption() - refillFirst);
                                                        int consumptionNow = vesselInfo.getConsumption();

                                                        // hitung actual first row
                                                        int firstCalc = consumptionCalculationResult - consumptionNow
                                                                        + refillFirst;
                                                        vesselInfo.setConsumptionCalculationResult(firstCalc);

                                                        // update state supaya row ke-2 dst tetap pakai logic lama
                                                        fuelRefill = vesselInfo.getParameterA();
                                                        prevFuel = refillFirst;

                                                        // 3) Interval waktu untuk row pertama (baseline time -> current
                                                        // time)
                                                        prevTime = previousInfo.getSentDate();
                                                        currentTime = vesselInfo.getSentDate();

                                                        Duration duration = new Duration(
                                                                        Util.timestampToDateTime(prevTime),
                                                                        Util.timestampToDateTime(currentTime));

                                                        long millis = duration.getMillis();
                                                        double hours = millis / 3600000d; // ms -> jam
                                                        hoursSincePrev = hours;

                                                        if (hours > 0d) {
                                                                consumptionPerHour = (double) firstCalc / hours;
                                                        } else {
                                                                consumptionPerHour = 0d;
                                                        }
                                                        vesselInfo.setConsumptionPerHour(consumptionPerHour);

                                                        // OPTIONAL (kalau runhour mau ikut “2 menit” bukan 1 menit):
                                                        long minutesDuration = Math.round(millis / 60000d); // rounding,
                                                                                                            // bukan
                                                                                                            // floor
                                                        if (millis > 0 && minutesDuration == 0)
                                                                minutesDuration = 1;

                                                        // 4) RunHour row pertama (pakai RPM baseline untuk decide
                                                        // running / not)
                                                        prevPortRpm = previousInfo.getPortRpm();
                                                        prevStarBoardRpm = previousInfo.getStarboardRpm();

                                                        int totalRpmFirst = prevPortRpm + prevStarBoardRpm;
                                                        if (totalRpmFirst == 0) {
                                                                // tidak running
                                                                prevRunMinutes = 0;
                                                                vesselInfo.setFormattedRunHour("00:00");

                                                                // tapi kalau kamu mau tetap hitung saat mainEngineTime
                                                                // ada (sesuai pola kamu):
                                                                if (vesselInfo.getMainEngineTime() > 0
                                                                                && minutesDuration > 0) {
                                                                        prevRunMinutes += (int) minutesDuration;
                                                                        vesselInfo.setFormattedRunHour(
                                                                                        DurationFormatUtils
                                                                                                        .formatDuration((long) prevRunMinutes
                                                                                                                        * 60
                                                                                                                        * 1000,
                                                                                                                        "HH:mm"));
                                                                }
                                                        } else {
                                                                // running
                                                                prevRunMinutes = (minutesDuration > 0)
                                                                                ? (int) minutesDuration
                                                                                : 0;
                                                                vesselInfo.setFormattedRunHour(
                                                                                DurationFormatUtils.formatDuration(
                                                                                                (long) prevRunMinutes
                                                                                                                * 60
                                                                                                                * 1000,
                                                                                                "HH:mm"));
                                                        }

                                                        totalRunHour += prevRunMinutes;

                                                        // move state ke current row
                                                        prevTime = vesselInfo.getSentDate();
                                                        prevPortRpm = vesselInfo.getPortRpm();
                                                        prevStarBoardRpm = vesselInfo.getStarboardRpm();

                                                } else {
                                                        // ==== fallback lama kalau baseline tidak ada ====
                                                        vesselInfo.setConsumptionCalculationResult(0);
                                                        vesselInfo.setFuelRefill(0);
                                                        consumptionCalculationResult = vesselInfo.getConsumption();
                                                        totalConsumptionCalculationResult = 0;
                                                        fuelRefill = vesselInfo.getParameterA();
                                                        totalFuelRefill = vesselInfo.getFuelRefill();
                                                        prevFuel = 0;
                                                        prevTime = vesselInfo.getSentDate();
                                                        vesselInfo.setFormattedRunHour("00:00");
                                                        prevRunMinutes = 0;
                                                        prevPortRpm = vesselInfo.getPortRpm();
                                                        prevStarBoardRpm = vesselInfo.getStarboardRpm();
                                                }
                                        } else {
                                                currentTime = vesselInfo.getSentDate();
                                                boolean longRefuelGap = isRefuelGapTooLong(prevTime, currentTime);
                                                int refuelDiff = calcRefuelDiff(
                                                                vesselInfo.getParameterA(), fuelRefill);
                                                if (longRefuelGap) {
                                                        refuelDiff = 0;
                                                }
                                                vesselInfo.setConsumption(
                                                                vesselInfo.getConsumption() - refuelDiff);
                                                consumption = vesselInfo.getConsumption();
                                                vesselInfo.setConsumptionCalculationResult(
                                                                consumptionCalculationResult - consumption + prevFuel);
                                                vesselInfo.setFuelRefill(refuelDiff);
                                                totalFuelRefill += refuelDiff;
                                                consumptionCalculationResult = vesselInfo.getConsumption();
                                                prevFuel = refuelDiff;
                                                fuelRefill = vesselInfo.getParameterA();
                                                Duration duration = new Duration(
                                                                Util.timestampToDateTime(prevTime),
                                                                Util.timestampToDateTime(currentTime));

                                                long millis = duration.getMillis();
                                                double hours = millis / 3600000d;
                                                hoursSincePrev = hours;

                                                if (hours > 0d) {
                                                        consumptionPerHour = (double) vesselInfo
                                                                        .getConsumptionCalculationResult() / hours;
                                                } else {
                                                        consumptionPerHour = 0d;
                                                }
                                                vesselInfo.setConsumptionPerHour(consumptionPerHour);

                                                // OPTIONAL (kalau runhour mau ikut “2 menit” bukan 1 menit):
                                                long minutesDuration = Math.round(millis / 60000d);
                                                if (millis > 0 && minutesDuration == 0)
                                                        minutesDuration = 1;

                                                totalRpm = prevPortRpm + prevStarBoardRpm;
                                                if (totalRpm == 0) {
                                                        vesselInfo.setFormattedRunHour("00:00");
                                                        prevRunMinutes = 0;

                                                        if (vesselInfo.getMainEngineTime() > 0) {
                                                                Duration tempDuration2 = new Duration(
                                                                                Util.timestampToDateTime(prevTime),
                                                                                Util.timestampToDateTime(currentTime));
                                                                prevRunMinutes += (int) tempDuration2
                                                                                .getStandardMinutes();
                                                                vesselInfo.setFormattedRunHour(
                                                                                DurationFormatUtils.formatDuration(
                                                                                                (long) prevRunMinutes
                                                                                                                * 60
                                                                                                                * 1000,
                                                                                                "HH:mm"));
                                                        }
                                                } else {
                                                        DateTime tempPrevTime = Util.timestampToDateTime(prevTime)
                                                                        .plusMinutes(prevRunMinutes);
                                                        Duration tempDuration = new Duration(
                                                                        tempPrevTime,
                                                                        Util.timestampToDateTime(currentTime));

                                                        Duration tempDuration2 = new Duration(
                                                                        Util.timestampToDateTime(prevTime),
                                                                        Util.timestampToDateTime(currentTime));
                                                        prevRunMinutes += (int) tempDuration2.getStandardMinutes();
                                                        vesselInfo.setFormattedRunHour(
                                                                        DurationFormatUtils.formatDuration(
                                                                                        (long) prevRunMinutes * 60
                                                                                                        * 1000,
                                                                                        "HH:mm"));
                                                }

                                                totalRunHour += prevRunMinutes;
                                                prevTime = vesselInfo.getSentDate();
                                                prevPortRpm = vesselInfo.getPortRpm();
                                                prevStarBoardRpm = vesselInfo.getStarboardRpm();
                                        }

                                        // FINAL: TotalCons = (F2-EIC diff) - (F3-EOC diff)
                                        int totalConsFromBC = calcTotalConsFromBC(vesselInfo, previousInfo);
                                        vesselInfo.setConsumptionCalculationResult(totalConsFromBC);
                                        totalConsumptionCalculationResult += totalConsFromBC;
                                        if (hoursSincePrev > 0d) {
                                                vesselInfo.setConsumptionPerHour(
                                                                (double) totalConsFromBC / hoursSincePrev);
                                        } else {
                                                vesselInfo.setConsumptionPerHour(0d);
                                        }

                                        double consumptionPortPerHour = 0;
                                        if (vesselInfo.getConsumptionCalculationResult() > 0) {
                                                consumptionPortPerHour = (double) vesselInfo
                                                                .getConsumptionCalculationResult()
                                                                / ((double) vesselInfo.getPortRunHour()
                                                                                + (double) vesselInfo
                                                                                                .getStarboardRunHour())
                                                                * (double) vesselInfo.getPortRunHour();
                                        }
                                        if (Double.isNaN(consumptionPortPerHour)) {
                                                vesselInfo.setConsumptionPortPerHour(0);
                                                totalConsumptionPortPerHour += 0;
                                        } else {
                                                vesselInfo.setConsumptionPortPerHour(consumptionPortPerHour);
                                                totalConsumptionPortPerHour += consumptionPortPerHour;
                                        }

                                        double consumptionStarboardPerHour = 0;
                                        if (vesselInfo.getConsumptionCalculationResult() > 0) {
                                                consumptionStarboardPerHour = (double) vesselInfo
                                                                .getConsumptionCalculationResult()
                                                                / ((double) vesselInfo.getPortRunHour()
                                                                                + (double) vesselInfo
                                                                                                .getStarboardRunHour())
                                                                * (double) vesselInfo.getStarboardRunHour();
                                        }
                                        if (Double.isNaN(consumptionStarboardPerHour)) {
                                                vesselInfo.setConsumptionStarboardPerHour(0);
                                                totalConsumptionStarboardPerHour += 0;
                                        } else {
                                                vesselInfo.setConsumptionStarboardPerHour(consumptionStarboardPerHour);
                                                totalConsumptionStarboardPerHour += consumptionStarboardPerHour;
                                        }

                                        double AE1ConsumptionPerHour = ((double) vesselInfo.getAE1RunHour() / 60)
                                                        * 43.65;
                                        double AE2ConsumptionPerHour = ((double) vesselInfo.getAE2RunHour() / 60)
                                                        * 38.4;
                                        vesselInfo.setAE1ConsumptionPerHour(AE1ConsumptionPerHour);
                                        totalConsumptionAE1PerHour += AE1ConsumptionPerHour;
                                        vesselInfo.setAE2ConsumptionPerHour(AE2ConsumptionPerHour);
                                        totalConsumptionAE2PerHour += AE2ConsumptionPerHour;

                                        if (vesselInfo.getSender().equalsIgnoreCase("VESSEL_SENDER_EMAIL_PLACEHOLDER")) {
                                                if (vesselInfo.getPortRunHour() > 0
                                                                && vesselInfo.getStarboardRunHour() > 0) {
                                                        vesselInfo.setConsumptionPortPerHour(
                                                                        vesselInfo.getConsumptionPortPerHour()
                                                                                        + Double.parseDouble("1"));
                                                        vesselInfo.setConsumptionStarboardPerHour(
                                                                        vesselInfo.getConsumptionStarboardPerHour()
                                                                                        + Double.parseDouble("1"));
                                                        vesselInfo.setConsumptionCalculationResult(
                                                                        vesselInfo.getConsumptionCalculationResult()
                                                                                        + 2);
                                                } else if (vesselInfo.getPortRunHour() > 0
                                                                && vesselInfo.getStarboardRunHour() == 0) {
                                                        vesselInfo.setConsumptionPortPerHour(
                                                                        vesselInfo.getConsumptionPortPerHour()
                                                                                        + Double.parseDouble("2"));
                                                        vesselInfo.setConsumptionStarboardPerHour(
                                                                        vesselInfo.getConsumptionStarboardPerHour()
                                                                                        + Double.parseDouble("0"));
                                                        vesselInfo.setConsumptionCalculationResult(
                                                                        vesselInfo.getConsumptionCalculationResult()
                                                                                        + 2);
                                                } else if (vesselInfo.getPortRunHour() == 0
                                                                && vesselInfo.getStarboardRunHour() > 0) {
                                                        vesselInfo.setConsumptionPortPerHour(
                                                                        vesselInfo.getConsumptionPortPerHour()
                                                                                        + Double.parseDouble("0"));
                                                        vesselInfo.setConsumptionStarboardPerHour(
                                                                        vesselInfo.getConsumptionStarboardPerHour()
                                                                                        + Double.parseDouble("2"));
                                                        vesselInfo.setConsumptionCalculationResult(
                                                                        vesselInfo.getConsumptionCalculationResult()
                                                                                        + 2);
                                                } else {
                                                        vesselInfo.setConsumptionPortPerHour(0);
                                                        vesselInfo.setConsumptionStarboardPerHour(0);
                                                        vesselInfo.setConsumptionCalculationResult(0);
                                                }
                                        }

                                        double totalFuelUsed = vesselInfo.getConsumptionPortPerHour()
                                                        + vesselInfo.getConsumptionStarboardPerHour()
                                                        + AE1ConsumptionPerHour
                                                        + AE2ConsumptionPerHour;
                                        vesselInfo.setTotalFuelUsed(totalFuelUsed);

                                        if (vesselInfo.getPortRunHour() > 0) {
                                                numberOfCalculatedPortRunHour++;
                                                totalPortRunHour += vesselInfo.getPortRunHour();
                                        }
                                        if (vesselInfo.getStarboardRunHour() > 0) {
                                                numberOfCalculatedStarboardRunHour++;
                                                totalStarboardRunHour += vesselInfo.getStarboardRunHour();
                                        }

                                        // 1) sent date -> LocalDate
                                        LocalDate sentLocalDate = null;
                                        if (vesselInfo.getSentDate() != null) {
                                                sentLocalDate = vesselInfo.getSentDate()
                                                                .toInstant()
                                                                .atZone(ZoneId.systemDefault())
                                                                .toLocalDate();
                                        }

                                        // 2) mode kalkulasi genset (RAW vs DIFF)
                                        final LocalDate cutoff = vesselInfo.getCalcGensetCutoff();

                                        // Formula 2 ON/OFF cukup dari flag
                                        final boolean isFormula2 = vesselInfo.isUseSafeDiffErh();

                                        // Jika cutoff diisi: Formula 2 mulai dari tanggal cutoff (inclusive)
                                        // Jika cutoff null: langsung aktif
                                        final boolean afterCutoff = (sentLocalDate != null)
                                                        && (cutoff == null || !sentLocalDate.isBefore(cutoff)); // >=
                                                                                                                // cutoff

                                        final boolean useFormula2 = isFormula2 && afterCutoff;

                                        // 3) hitung & format run hour genset / main engine
                                        if (useFormula2) {
                                                final VesselInfo current = vesselInfoList.get(i);
                                                final VesselInfo previous = previousInfo;

                                                if (previous == null) {
                                                        current.setFormattedMainEngineTime("00:00");
                                                        current.setFormattedGenset1RunHour("00:00");
                                                        current.setFormattedGenset2RunHour("00:00");
                                                        current.setFormattedGenset3RunHour("00:00");
                                                        current.setFormattedGenset4RunHour("00:00");
                                                        current.setFormattedGenset5RunHour("00:00");
                                                        current.setFormattedBoilerRunTime("00:00");
                                                        current.setFormattedMdo("00:00");
                                                        current.setFormattedHfo("00:00");

                                                        mainEngineTime = 0;
                                                        genset1Time = genset2Time = genset3Time = genset4Time = genset5Time = 0;
                                                        boilerRunTimeMinutes = 0L;
                                                        mdoMinutes = 0L;
                                                        hfoMinutes = 0L;
                                                } else {
                                                        mainEngineTime = normalizeCounterMinutesForDisplay(
                                                                        calcMainEngineTimeMinutes(
                                                                                        current.getMainEngineTime(),
                                                                                        previous.getMainEngineTime()));
                                                        genset1Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset1Time(),
                                                                                        previous.getGenset1Time()));
                                                        genset2Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset2Time(),
                                                                                        previous.getGenset2Time()));
                                                        genset3Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset3Time(),
                                                                                        previous.getGenset3Time()));
                                                        genset4Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset4Time(),
                                                                                        previous.getGenset4Time()));
                                                        genset5Time = normalizeCounterMinutesForDisplay(
                                                                        safeDiff(current.getGenset5Time(),
                                                                                        previous.getGenset5Time()));

                                                        boilerRunTimeMinutes = safeDiff(current.getBoilerRunTime(),
                                                                        previous.getBoilerRunTime());
                                                        mdoMinutes = safeDiff(current.getMdo(), previous.getMdo());
                                                        hfoMinutes = safeDiff(current.getHfo(), previous.getHfo());

                                                        current.setFormattedMainEngineTime(formatHHmm(mainEngineTime));
                                                        current.setFormattedGenset1RunHour(genset1Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset1Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset2RunHour(genset2Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset2Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset3RunHour(genset3Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset3Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset4RunHour(genset4Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset4Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedGenset5RunHour(genset5Time > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        genset5Time * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");

                                                        current.setFormattedBoilerRunTime(boilerRunTimeMinutes > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        boilerRunTimeMinutes * 60L
                                                                                                        * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedMdo(mdoMinutes > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        mdoMinutes * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                        current.setFormattedHfo(hfoMinutes > 0
                                                                        ? DurationFormatUtils.formatDuration(
                                                                                        hfoMinutes * 60L * 1000,
                                                                                        "HH:mm")
                                                                        : "00:00");
                                                }
                                        } else {
                                                // FORMULA 1 (RAW)
                                                mainEngineTime = vesselInfo.getMainEngineTime();
                                                genset1Time = vesselInfo.getGenset1Time();
                                                genset2Time = vesselInfo.getGenset2Time();
                                                genset3Time = vesselInfo.getGenset3Time();
                                                genset4Time = vesselInfo.getGenset4Time();
                                                genset5Time = vesselInfo.getGenset5Time();

                                                boilerRunTimeMinutes = vesselInfo.getBoilerRunTime();
                                                mdoMinutes = vesselInfo.getMdo();
                                                hfoMinutes = vesselInfo.getHfo();

                                                vesselInfo.setFormattedMainEngineTime(formatHHmm(mainEngineTime));
                                                vesselInfo.setFormattedGenset1RunHour(
                                                                genset1Time > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                genset1Time * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedGenset2RunHour(
                                                                genset2Time > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                genset2Time * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedGenset3RunHour(
                                                                genset3Time > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                genset3Time * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedGenset4RunHour(
                                                                genset4Time > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                genset4Time * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedGenset5RunHour(
                                                                genset5Time > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                genset5Time * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedBoilerRunTime(
                                                                boilerRunTimeMinutes > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                boilerRunTimeMinutes
                                                                                                                * 60L
                                                                                                                * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedMdo(
                                                                mdoMinutes > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                mdoMinutes * 60L * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                                vesselInfo.setFormattedHfo(
                                                                hfoMinutes > 0
                                                                                ? DurationFormatUtils.formatDuration(
                                                                                                hfoMinutes * 60L * 1000,
                                                                                                "HH:mm")
                                                                                : "00:00");
                                        }

                                        if (shouldZeroMainEngineTimeAtEngineOff(vesselInfo, previousInfo)) {
                                                mainEngineTime = 0;
                                                vesselInfo.setFormattedMainEngineTime("00:00");
                                        }

                                        // 4) akumulasi total waktu (*Time)
                                        if (mainEngineTime > 0)
                                                totalMainEngineTime += mainEngineTime;
                                        if (genset1Time > 0)
                                                totalGenset1Time += genset1Time;
                                        if (genset2Time > 0)
                                                totalGenset2Time += genset2Time;
                                        if (genset3Time > 0)
                                                totalGenset3Time += genset3Time;
                                        if (genset4Time > 0)
                                                totalGenset4Time += genset4Time;
                                        if (genset5Time > 0)
                                                totalGenset5Time += genset5Time;
                                        if (boilerRunTimeMinutes > 0)
                                                totalBoilerRunTime += boilerRunTimeMinutes;
                                        if (mdoMinutes > 0)
                                                totalMdo += mdoMinutes;
                                        if (hfoMinutes > 0)
                                                totalHfo += hfoMinutes;

                                        // 5) ERH/GRH/Shaft Running Hours (safeDiff jika flag aktif)
                                        if (useFormula2) {
                                                final VesselInfo current = vesselInfoList.get(i);
                                                final VesselInfo previous = previousInfo;

                                                if (previous == null) {
                                                        current.setFormattedPortERH("00:00");
                                                        current.setFormattedStbdERH("00:00");
                                                        current.setFormattedPortGRH("00:00");
                                                        current.setFormattedStbdGRH("00:00");
                                                        current.setFormattedPortShaftRunningHours("00:00");
                                                        current.setFormattedStbdShaftRunningHours("00:00");

                                                        portERH = stbdERH = portGRH = stbdGRH = portShaftRunningHours = stbdShaftRunningHours = 0;
                                                } else {
                                                        portERH = resolveCounterMinutes(current.getPortERH(),
                                                                        previous.getPortERH(), true);
                                                        stbdERH = resolveCounterMinutes(current.getStbdERH(),
                                                                        previous.getStbdERH(), true);
                                                        portERH = applyEngineRunHoursByRpm(portERH,
                                                                        current.getPortRpm(), hasAnyRpmSignal);
                                                        stbdERH = applyEngineRunHoursByRpm(stbdERH,
                                                                        current.getStarboardRpm(), hasAnyRpmSignal);
                                                        portGRH = resolveCounterMinutes(current.getPortGRH(),
                                                                        previous.getPortGRH(), true);
                                                        stbdGRH = resolveCounterMinutes(current.getStbdGRH(),
                                                                        previous.getStbdGRH(), true);
                                                        portShaftRunningHours = resolveCounterMinutes(
                                                                        current.getPortShaftRunningHours(),
                                                                        previous.getPortShaftRunningHours(), true);
                                                        stbdShaftRunningHours = resolveCounterMinutes(
                                                                        current.getStbdShaftRunningHours(),
                                                                        previous.getStbdShaftRunningHours(), true);

                                                        current.setFormattedPortERH(formatHHmm(portERH));
                                                        current.setFormattedStbdERH(formatHHmm(stbdERH));
                                                        current.setFormattedPortGRH(formatHHmm(portGRH));
                                                        current.setFormattedStbdGRH(formatHHmm(stbdGRH));
                                                        current.setFormattedPortShaftRunningHours(
                                                                        formatHHmm(portShaftRunningHours));
                                                        current.setFormattedStbdShaftRunningHours(
                                                                        formatHHmm(stbdShaftRunningHours));
                                                }
                                        } else {
                                                if (!hasAnyRpmSignal) {
                                                        if (previousInfo == null) {
                                                                portERH = 0;
                                                                stbdERH = 0;
                                                        } else {
                                                                portERH = resolveCounterMinutes(vesselInfo.getPortERH(),
                                                                                previousInfo.getPortERH(), true);
                                                                stbdERH = resolveCounterMinutes(vesselInfo.getStbdERH(),
                                                                                previousInfo.getStbdERH(), true);
                                                        }
                                                } else {
                                                        portERH = resolveCounterMinutes(vesselInfo.getPortERH(), 0,
                                                                        false);
                                                        stbdERH = resolveCounterMinutes(vesselInfo.getStbdERH(), 0,
                                                                        false);
                                                }
                                                portERH = applyEngineRunHoursByRpm(portERH, vesselInfo.getPortRpm(),
                                                                hasAnyRpmSignal);
                                                stbdERH = applyEngineRunHoursByRpm(stbdERH,
                                                                vesselInfo.getStarboardRpm(), hasAnyRpmSignal);
                                                portGRH = resolveCounterMinutes(vesselInfo.getPortGRH(), 0, false);
                                                stbdGRH = resolveCounterMinutes(vesselInfo.getStbdGRH(), 0, false);
                                                portShaftRunningHours = resolveCounterMinutes(
                                                                vesselInfo.getPortShaftRunningHours(), 0, false);
                                                stbdShaftRunningHours = resolveCounterMinutes(
                                                                vesselInfo.getStbdShaftRunningHours(), 0, false);

                                                vesselInfo.setFormattedPortERH(formatHHmm(portERH));
                                                vesselInfo.setFormattedStbdERH(formatHHmm(stbdERH));
                                                vesselInfo.setFormattedPortGRH(formatHHmm(portGRH));
                                                vesselInfo.setFormattedStbdGRH(formatHHmm(stbdGRH));
                                                vesselInfo.setFormattedPortShaftRunningHours(
                                                                formatHHmm(portShaftRunningHours));
                                                vesselInfo.setFormattedStbdShaftRunningHours(
                                                                formatHHmm(stbdShaftRunningHours));
                                        }

                                        if (shouldZeroEngineAndShaftHoursAtEngineOff(vesselInfo, previousInfo)) {
                                                portERH = 0;
                                                stbdERH = 0;
                                                portShaftRunningHours = 0;
                                                stbdShaftRunningHours = 0;
                                                vesselInfo.setFormattedPortERH("00:00");
                                                vesselInfo.setFormattedStbdERH("00:00");
                                                vesselInfo.setFormattedPortShaftRunningHours("00:00");
                                                vesselInfo.setFormattedStbdShaftRunningHours("00:00");
                                        }

                                        // 6) RPM Shaft: tetap format, tapi average nanti hanya pakai yang > 0
                                        portShaft = vesselInfo.getPortShaftRPM();
                                        stbdShaft = vesselInfo.getStbdShaftRPM();
                                        vesselInfo.setFormattedPortShaftRPM(formatHHmm(portShaft));
                                        vesselInfo.setFormattedStbdShaftRPM(formatHHmm(stbdShaft));

                                        // 7) akumulasi total lain (hanya jika > 0)
                                        if (portERH > 0)
                                                totalPortERH += portERH;
                                        if (portGRH > 0)
                                                totalPortGRH += portGRH;
                                        if (stbdERH > 0)
                                                totalStbdERH += stbdERH;
                                        if (stbdGRH > 0)
                                                totalStbdGRH += stbdGRH;
                                        if (portShaft > 0) {
                                                totalPortShaftRPM += portShaft;
                                                numberOfCalculatedPortShaftRPM++;
                                        }
                                        if (stbdShaft > 0) {
                                                totalStbdShaftRPM += stbdShaft;
                                                numberOfCalculatedStbdShaftRPM++;
                                        }
                                        if (portShaftRunningHours > 0)
                                                totalPortShaftRunningHours += portShaftRunningHours;
                                        if (stbdShaftRunningHours > 0)
                                                totalStbdShaftRunningHours += stbdShaftRunningHours;
                                }

                                // ===== setelah loop: perhitungan konsumsi total =====
                                double totalMainEngineConsumption = 0;
                                double totalPortConsumption = 0;
                                double totalStarboardConsumption = 0;
                                double totalAE1Consumption = 0;
                                double totalAE2Consumption = 0;

                                for (VesselInfo vesselInfo : vesselInfoList) {
                                        if (vesselInfo.getSender().equalsIgnoreCase("VESSEL_SENDER_EMAIL_PLACEHOLDER")) {
                                                vesselInfo.setConsumptionPortPerHour(
                                                                vesselInfo.getConsumptionPortPerHour()
                                                                                * vesselInfo.getPortConsumptionCoefficient());
                                                vesselInfo.setConsumptionStarboardPerHour(
                                                                vesselInfo.getConsumptionStarboardPerHour()
                                                                                * vesselInfo.getStarboardConsumptionCoefficient());
                                                vesselInfo.setAE1ConsumptionPerHour(
                                                                vesselInfo.getAE1ConsumptionPerHour()
                                                                                * vesselInfo.getAE1ConsumptionCoefficient());
                                                vesselInfo.setAE2ConsumptionPerHour(
                                                                vesselInfo.getAE2ConsumptionPerHour()
                                                                                * vesselInfo.getAE2ConsumptionCoefficient());

                                                double tempConsumptionCalculationResult = vesselInfo
                                                                .getConsumptionPortPerHour()
                                                                + vesselInfo.getConsumptionStarboardPerHour();
                                                vesselInfo.setConsumptionCalculationResult(
                                                                (int) Math.round(tempConsumptionCalculationResult));
                                                vesselInfo.setTotalFuelUsed(
                                                                vesselInfo.getConsumptionPortPerHour()
                                                                                + vesselInfo.getConsumptionStarboardPerHour()
                                                                                + vesselInfo.getAE1ConsumptionPerHour()
                                                                                + vesselInfo.getAE2ConsumptionPerHour());

                                                totalMainEngineConsumption += tempConsumptionCalculationResult;
                                                totalPortConsumption += vesselInfo.getConsumptionPortPerHour();
                                                totalStarboardConsumption += vesselInfo
                                                                .getConsumptionStarboardPerHour();
                                                totalAE1Consumption += vesselInfo.getAE1ConsumptionPerHour();
                                                totalAE2Consumption += vesselInfo.getAE2ConsumptionPerHour();
                                        }
                                }

                                // ===== rata-rata RPM / speed / pitch / shaft / runHour =====
                                if (numberOfCalculatedPortRpm > 0) {
                                        avgPortRpm = totalPortRpm / numberOfCalculatedPortRpm;
                                }
                                if (numberOfCalculatedStarboardRpm > 0) {
                                        avgStarboardRpm = totalStarboardRpm / numberOfCalculatedStarboardRpm;
                                }
                                if (numberOfCalculatedSpeed > 0) {
                                        avgSpeed = totalSpeed / numberOfCalculatedSpeed;
                                } else {
                                        avgSpeed = 0;
                                }

                                avgPortPitch = totalPortPitch / counter;
                                avgStarboardPitch = totalStarboardPitch / counter;
                                avgPortShaft = totalPortShaft / counter;
                                avgStarboardShaft = totalStarboardShaft / counter;
                                avgRunHour = totalRunHour / counter;

                                double avgPortShaftRPM = 0.0;
                                double avgStbdShaftRPM = 0.0;
                                if (numberOfCalculatedPortShaftRPM > 0) {
                                        avgPortShaftRPM = (double) totalPortShaftRPM / numberOfCalculatedPortShaftRPM;
                                }
                                if (numberOfCalculatedStbdShaftRPM > 0) {
                                        avgStbdShaftRPM = (double) totalStbdShaftRPM / numberOfCalculatedStbdShaftRPM;
                                }

                                // ===== summary =====
                                Map<String, Object> summary = new HashMap<>();
                                summary.put("consumption", String.valueOf(totalConsumptionCalculationResult));
                                if (vesselInfoList.size() > 0) {
                                        VesselInfo vesselInfo = vesselInfoList.get(0);
                                        if (vesselInfo.getSender().equalsIgnoreCase("VESSEL_SENDER_EMAIL_PLACEHOLDER")) {
                                                summary.put("consumption",
                                                                String.valueOf(totalMainEngineConsumption));
                                        }
                                }
                                summary.put("avgPortRpm", avgPortRpm);
                                summary.put("avgStarboardRpm", avgStarboardRpm);
                                summary.put("avgSpeed", avgSpeed);

                                summary.put("totalFuelRefill", totalFuelRefill);
                                summary.put("avgPortPitch", avgPortPitch);
                                summary.put("avgStarboardPitch", avgStarboardPitch);
                                summary.put("avgPortShaft", avgPortShaft);
                                summary.put("avgStarboardShaft", avgStarboardShaft);
                                int avgMainEngineRpmValue = 0;
                                if (numberOfCalculatedMainEngineRpm > 0) {
                                        avgMainEngineRpmValue = (int) Math.round(
                                                        avgMainEngineRpm / numberOfCalculatedMainEngineRpm);
                                }
                                summary.put("avgMainEngineRpm", avgMainEngineRpmValue);
                                summary.put("avgRunHour",
                                                avgRunHour > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (avgRunHour * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalRunHour",
                                                totalRunHour > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalRunHour * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalMainEngineTime",
                                                totalMainEngineTime > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalMainEngineTime * 60L
                                                                                                * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalGenset1Time", totalGenset1Time);
                                summary.put("totalGenset2Time", totalGenset2Time);
                                summary.put("totalGenset3Time", totalGenset3Time);
                                summary.put("totalGenset4Time", totalGenset4Time);
                                summary.put("totalGenset5Time", totalGenset5Time);
                                summary.put("formattedTotalGenset1RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset1Time * 60 * 1000, "HH:mm"));
                                summary.put("formattedTotalGenset2RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset2Time * 60 * 1000, "HH:mm"));
                                summary.put("formattedTotalGenset3RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset3Time * 60 * 1000, "HH:mm"));
                                summary.put("formattedTotalGenset4RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset4Time * 60 * 1000, "HH:mm"));
                                summary.put("formattedTotalGenset5RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalGenset5Time * 60 * 1000, "HH:mm"));
                                summary.put("totalMdo",
                                                totalMdo > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalMdo * 60L * 1000), "HH:mm")
                                                                : "00:00");
                                summary.put("totalHfo",
                                                totalHfo > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalHfo * 60L * 1000), "HH:mm")
                                                                : "00:00");
                                summary.put("avgBostrPump", avgBostrPump / counter);
                                summary.put("avgEngineIn", avgEngineIn / counter);
                                summary.put("avgDailyTank1", avgDailyTank1 / counter);
                                summary.put("avgDailyTank2", avgDailyTank2 / counter);
                                summary.put("totalBoilerRunTime",
                                                totalBoilerRunTime > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalBoilerRunTime * 60L
                                                                                                * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalPortRunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalPortRunHour * 60 * 1000, "HH:mm"));
                                summary.put("totalStarboardRunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalStarboardRunHour * 60 * 1000, "HH:mm"));
                                summary.put("totalConsumptionPortPerHour", totalPortConsumption);
                                summary.put("totalConsumptionStarboardPerHour", totalStarboardConsumption);
                                summary.put("totalAE1RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalAE1RunHour * 60 * 1000, "HH:mm"));
                                summary.put("totalAE2RunHour",
                                                DurationFormatUtils.formatDuration(
                                                                (long) totalAE2RunHour * 60 * 1000, "HH:mm"));
                                summary.put("totalConsumptionAE1PerHour", totalAE1Consumption);
                                summary.put("totalConsumptionAE2PerHour", totalAE2Consumption);
                                summary.put("totalConsumption",
                                                totalPortConsumption + totalStarboardConsumption
                                                                + totalAE1Consumption + totalAE2Consumption);
                                summary.put("totalPortERH",
                                                totalPortERH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalPortERH * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalPortGRH",
                                                totalPortGRH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalPortGRH * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalStbdERH",
                                                totalStbdERH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalStbdERH * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalStbdGRH",
                                                totalStbdGRH > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalStbdGRH * 60L * 1000),
                                                                                "HH:mm")
                                                                : "00:00");

                                summary.put("totalPortShaftRPM", totalPortShaftRPM);
                                summary.put("totalStbdShaftRPM", totalStbdShaftRPM);

                                summary.put("averagePortShaftRPM",
                                                String.valueOf((int) Math.round(avgPortShaftRPM)));
                                summary.put("averageStbdShaftRPM",
                                                String.valueOf((int) Math.round(avgStbdShaftRPM)));

                                summary.put("totalPortShaftRunningHours",
                                                totalPortShaftRunningHours > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalPortShaftRunningHours * 60L
                                                                                                * 1000),
                                                                                "HH:mm")
                                                                : "00:00");
                                summary.put("totalStbdShaftRunningHours",
                                                totalStbdShaftRunningHours > 0
                                                                ? DurationFormatUtils.formatDuration(
                                                                                (long) (totalStbdShaftRunningHours * 60L
                                                                                                * 1000),
                                                                                "HH:mm")
                                                                : "00:00");

                                // data: hanya 1 record sesuai message_id
                                JSONArray dataArray = new JSONArray();
                                for (VesselInfo vesselInfo : vesselInfoList) {
                                        if (vesselInfo.getEmailId().equals(map.get("message_id"))) {
                                                dataArray.put(new JSONObject(vesselInfo));
                                        }
                                }

                                jsonObject.put("success", true);
                                if (dataArray.length() > 0) {
                                        jsonObject.put("data", dataArray.getJSONObject(0));
                                } else {
                                        jsonObject.put("data", JSONObject.NULL);
                                }
                                jsonObject.put("summary", summary);
                        } else {
                                jsonObject.put("success", true);
                                jsonObject.put("data", new JSONArray());
                                jsonObject.put("summary", JSONObject.NULL);
                        }
                } catch (Exception e) {
                        jsonObject = Util.exceptionToJSONObject(e);
                }
                return jsonObject;
        }

        private static final int MAX_ME_COUNTER = 9_999;
        private static final long REFUEL_GAP_MILLIS = 24L * 60 * 60 * 1000;

        private long safeDiff(int current, int previous) {
                if (current < 0 || previous < 0) {
                        return 0L;
                }

                if (current >= previous) {
                        return (long) current - previous;
                }

                long wrapMax = resolveWrapMax(current, previous);
                if (wrapMax <= 0L) {
                        return 0L;
                }

                return (wrapMax - previous) + current;
        }

        private long resolveCounterMinutes(int current, int previous, boolean useDiffMode) {
                if (current <= 0) {
                        return 0L;
                }
                if (useDiffMode) {
                        if (previous < 0) {
                                return 0L;
                        }
                        return safeDiff(current, previous);
                }
                return current;
        }

        private int resolveEngineRunHourFromCounter(VesselInfo current, VesselInfo previous, boolean isPortSide) {
                int currentCounter = isPortSide ? current.getPortERH() : current.getStbdERH();
                if (currentCounter <= 0) {
                        currentCounter = extractCounterFromParameterD(current.getParameterD(), isPortSide);
                }

                if (previous == null) {
                        return 0;
                }

                int previousCounter = isPortSide ? previous.getPortERH() : previous.getStbdERH();
                if (previousCounter <= 0) {
                        previousCounter = extractCounterFromParameterD(previous.getParameterD(), isPortSide);
                }

                int sideDiff = (int) resolveCounterMinutes(currentCounter, previousCounter, true);
                if (sideDiff > 0) {
                        return sideDiff;
                }

                int currentPortCounter = current.getPortERH() > 0 ? current.getPortERH()
                                : extractCounterFromParameterD(current.getParameterD(), true);
                int prevPortCounter = previous.getPortERH() > 0 ? previous.getPortERH()
                                : extractCounterFromParameterD(previous.getParameterD(), true);
                int currentStbdCounter = current.getStbdERH() > 0 ? current.getStbdERH()
                                : extractCounterFromParameterD(current.getParameterD(), false);
                int prevStbdCounter = previous.getStbdERH() > 0 ? previous.getStbdERH()
                                : extractCounterFromParameterD(previous.getParameterD(), false);

                int portDiff = (int) resolveCounterMinutes(currentPortCounter, prevPortCounter, true);
                int stbdDiff = (int) resolveCounterMinutes(currentStbdCounter, prevStbdCounter, true);
                if (portDiff > 0 || stbdDiff > 0) {
                        return 0;
                }

                int totalConsFromBC = calcTotalConsFromBC(current, previous);
                if (totalConsFromBC <= 0 || current.getSentDate() == null || previous.getSentDate() == null) {
                        return 0;
                }

                Duration duration = new Duration(
                                Util.timestampToDateTime(previous.getSentDate()),
                                Util.timestampToDateTime(current.getSentDate()));
                long minutes = duration.getStandardMinutes();
                if (minutes <= 0L) {
                        return 0;
                }

                long distributedMinutes = Math.max(1L, minutes / 2L);
                return (int) distributedMinutes;
        }

        private int extractCounterFromParameterD(String parameterD, boolean isPortSide) {
                if (parameterD == null || parameterD.trim().isEmpty()) {
                        return 0;
                }

                String raw = parameterD.trim();
                String[] parts = raw.contains(":") ? raw.split(":") : null;
                String value = null;

                if (parts != null) {
                        if (isPortSide && parts.length > 0) {
                                value = parts[0];
                        } else if (!isPortSide && parts.length > 1) {
                                value = parts[1];
                        }
                } else {
                        switch (raw.length()) {
                                case 12:
                                        value = isPortSide ? raw.substring(0, 6) : raw.substring(6, 12);
                                        break;
                                case 8:
                                        value = isPortSide ? raw.substring(0, 4) : raw.substring(4, 8);
                                        break;
                                case 6:
                                        value = isPortSide ? raw.substring(0, 3) : raw.substring(3, 6);
                                        break;
                                case 4:
                                        value = isPortSide ? raw : null;
                                        break;
                                default:
                                        value = null;
                                        break;
                        }
                }

                if (value == null || value.trim().isEmpty()) {
                        return 0;
                }

                try {
                        return Integer.parseInt(value.trim());
                } catch (NumberFormatException ex) {
                        return 0;
                }
        }

        private long applyEngineRunHoursByRpm(long engineRunHoursMinutes, int rpm, boolean hasAnyRpmSignal) {
                return engineRunHoursMinutes;
        }

        private String formatHHmm(long totalMinutes) {
                if (totalMinutes <= 0)
                        return "00:00";

                long h = totalMinutes / 60;
                long m = totalMinutes % 60;
                return String.format("%02d:%02d", h, m);
                // return DurationFormatUtils.formatDuration(totalMinutes * 60L * 1000,
                // "HH:mm"); // tidak wrap 24 jam
        }

        private static int safeDiffInt(int current, int previous) {
                int diff = current - previous;
                return (diff >= 0) ? diff : 0;
        }

        private int calcRefuelDiff(int current, int previous) {
                int diff = current - previous;
                return (diff > 0) ? diff : 0;
        }

        private boolean isRefuelGapTooLong(Timestamp previousTime, Timestamp currentTime) {
                if (previousTime == null || currentTime == null) {
                        return false;
                }
                Duration duration = new Duration(
                                Util.timestampToDateTime(previousTime),
                                Util.timestampToDateTime(currentTime));
                return duration.getMillis() >= REFUEL_GAP_MILLIS;
        }

        private long calcMainEngineTimeMinutes(int current, int previous) {
                if (current < 0 || previous < 0) {
                        return 0L;
                }

                if (current >= previous) {
                        int diff = current - previous;
                        return (diff > 0) ? diff : 0L;
                }

                int diff = MAX_ME_COUNTER - previous + current;
                if (diff <= 0) {
                        return 0L;
                }
                return diff;
        }

        private long resolveWrapMax(int current, int previous) {
                int reference = Math.max(current, previous);
                if (reference <= 9_999) {
                        return 9_999L;
                }
                if (reference <= 99_999) {
                        return 99_999L;
                }
                if (reference <= 999_999) {
                        return 999_999L;
                }
                return 0L;
        }

        private int calcTotalConsFromBC(VesselInfo current, VesselInfo previous) {
                if (current == null)
                        return 0;

                int curB = current.getParameterB();
                int curC = current.getParameterC();

                // wajib punya B & C
                if (curB <= 0 || curC <= 0)
                        return 0;

                int prevB = (previous != null) ? previous.getParameterB() : 0;
                int prevC = (previous != null) ? previous.getParameterC() : 0;

                int eicDiff = safeDiffInt(curB, prevB); // F2-EIC diff
                int eocDiff = safeDiffInt(curC, prevC); // F3-EOC diff

                int total = eicDiff - eocDiff;
                return total;
        }

        private long normalizeToUserDecimalHm(long minutesRaw) {
                if (minutesRaw <= 0L) {
                        return 0L;
                }

                java.math.BigDecimal hourDecimal = java.math.BigDecimal.valueOf(minutesRaw)
                                .divide(java.math.BigDecimal.valueOf(60), 2, java.math.RoundingMode.DOWN);
                int hours = hourDecimal.intValue();
                int minutes = hourDecimal.remainder(java.math.BigDecimal.ONE)
                                .movePointRight(2)
                                .intValue();

                if (minutes >= 60) {
                        minutes -= 60;
                        hours += 1;
                }
                return (long) hours * 60L + minutes;
        }

        private long normalizeCounterMinutesForDisplay(long minutesRaw) {
                if (minutesRaw <= 0L) {
                        return 0L;
                }
                // Konversi "desimal user" hanya untuk loncatan besar.
                if (minutesRaw >= 480L) {
                        return normalizeToUserDecimalHm(minutesRaw);
                }
                return minutesRaw;
        }

        private boolean shouldZeroMainEngineTimeAtEngineOff(VesselInfo current, VesselInfo previous) {
                if (current == null || previous == null) {
                        return false;
                }

                boolean currentOff = current.getMainEngineRpm() <= 0
                                && current.getPortRpm() <= 0
                                && current.getStarboardRpm() <= 0;

                boolean previousOn = previous.getMainEngineRpm() > 0
                                || previous.getPortRpm() > 0
                                || previous.getStarboardRpm() > 0;

                return currentOff && previousOn;
        }

        private boolean shouldZeroEngineAndShaftHoursAtEngineOff(VesselInfo current, VesselInfo previous) {
                if (current == null || previous == null) {
                        return false;
                }

                boolean currentOff = current.getMainEngineRpm() <= 0
                                && current.getPortRpm() <= 0
                                && current.getStarboardRpm() <= 0;

                boolean previousOn = previous.getMainEngineRpm() > 0
                                || previous.getPortRpm() > 0
                                || previous.getStarboardRpm() > 0;

                // Jika ada konsumsi aktual saat idle, tetap izinkan run hour tampil.
                boolean noConsumptionSignal = calcTotalConsFromBC(current, previous) <= 0;

                return currentOff && previousOn && noConsumptionSignal;
        }

}
