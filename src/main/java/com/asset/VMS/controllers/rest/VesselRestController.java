package com.asset.VMS.controllers.rest;

import com.asset.VMS.domain.Account;
import com.asset.VMS.domain.Vessel;
import com.asset.VMS.domain.VesselLocation;
import com.asset.VMS.service.VesselService;
import com.asset.VMS.util.Util;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest/vessel")
public class VesselRestController {
    private static final Logger logger = LogManager.getLogger(AccountRestController.class);

    private final VesselService vesselService;

    @Autowired
    public VesselRestController(VesselService vesselService) {
        this.vesselService = vesselService;
    }

    @PostMapping(value = "/position")
    @ResponseBody
    public ResponseEntity<?> position(HttpServletRequest request) {
        Account account = (Account) request.getSession().getAttribute("account");
        List<VesselLocation> vesselLocationList = this.vesselService.selectLastLocation(account);
        JSONObject geoJson = new JSONObject();
        if (vesselLocationList != null && vesselLocationList.size() > 0) {
            geoJson.put("type", "FeatureCollection");
            int counter = 0;
            JSONArray features = new JSONArray();
            for (VesselLocation vesselLocation : vesselLocationList) {
                counter++;
                JSONArray coordinate = new JSONArray();
                coordinate.put(vesselLocation.getLongitude());
                coordinate.put(vesselLocation.getLatitude());

                JSONObject geometry = new JSONObject();
                geometry.put("type", "Point");
                geometry.put("coordinates", coordinate);

                JSONObject feature = new JSONObject();
                feature.put("type", "Feature");
                feature.put("id", counter);
                feature.put("geometry", geometry);
                feature.put("properties", vesselLocation);
                features.put(feature);
            }
            geoJson.put("features", features);
        }
        return new ResponseEntity<Object>(geoJson.toMap(), HttpStatus.OK);
    }

    @PostMapping(value = "/routes")
    @ResponseBody
    public ResponseEntity<?> routes(@RequestBody JsonNode jsonNode, HttpServletRequest request) {
        List<VesselLocation> vesselLocationList = this.vesselService.selectLocation(Util.jsonNodeToMap(jsonNode));
        JSONObject result = new JSONObject();
        if (vesselLocationList != null && vesselLocationList.size() > 0) {

            result.put("type", "FeatureCollection");
            int counter = 0;
            JSONArray features = new JSONArray();
            for (VesselLocation vesselLocation : vesselLocationList) {
                counter++;
                JSONArray coordinate = new JSONArray();
                coordinate.put(vesselLocation.getLongitude());
                coordinate.put(vesselLocation.getLatitude());

                JSONObject geometry = new JSONObject();
                geometry.put("type", "Point");
                geometry.put("coordinates", coordinate);

                JSONObject feature = new JSONObject();
                feature.put("type", "Feature");
                feature.put("id", counter);
                feature.put("geometry", geometry);
                feature.put("properties", vesselLocation);
                features.put(feature);
            }
            result.put("features", features);

            result.put("success", true);
            result.put("routes", vesselLocationList);
        } else {
            result.put("success", false);
            result.put("routes", new ArrayList<>());
        }
        return new ResponseEntity<Object>(result.toMap(), HttpStatus.OK);
    }

    @PostMapping(value = "/read")
    @ResponseBody
    public ResponseEntity<?> read(@RequestBody JsonNode jsonNode, HttpServletRequest request) {
        JSONObject result;
        if (request.getSession().getAttribute("account") == null) {
            result = Util.sendForbiddenMessage();
        } else {
            Map<String, Object> parameter = Util.jsonNodeToMap(jsonNode);
            parameter.put("paging", 1);
            parameter.put("search", "%" + parameter.get("search") + "%");
            result = this.vesselService.searchVessel(parameter);
        }
        return new ResponseEntity<Object>(result.toMap(), HttpStatus.OK);
    }

    @PostMapping(value = "/create")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody JsonNode jsonNode, HttpServletRequest request) {
        JSONObject result;
        if (request.getSession().getAttribute("account") == null) {
            result = Util.sendForbiddenMessage();
        } else {
            Map<String, Object> vesselMap = Util.jsonNodeToMap(jsonNode);

            Vessel vessel = new Vessel();
            vessel.setEmail(vesselMap.get("vessel_email").toString());
            vessel.setName(vesselMap.get("vessel_name").toString());
            vessel.setMaxPortRpm(Util.stringToInt(vesselMap.get("max_port_rpm").toString()));
            vessel.setMaxStarboardRpm(Util.stringToInt(vesselMap.get("max_starboard_rpm").toString()));
            vessel.setDeleted(0);

            // report_type tetap seperti sekarang (bukan formula 2)
            vessel.setReportType(Util.stringToInt(vesselMap.get("report_type").toString()));

            vessel.setEmailTrackingAccount(vesselMap.get("email_tracking_account").toString());
            vessel.setPasswordTrackingAccount(vesselMap.get("password_tracking_account").toString());
            vessel.setTrackingProtocol(vesselMap.get("tracking_protocol").toString());
            vessel.setTrackingHost(vesselMap.get("tracking_host").toString());
            vessel.setTrackingPort(vesselMap.get("tracking_port").toString());
            vessel.setDataType(Util.stringToInt(vesselMap.get("data_type").toString()));
            vessel.setCategoryGsm(vesselMap.get("category_gsm").toString());

            // === NEW: FORMULA CONFIG ===
            vessel.setCalcGensetMode(parseMode(vesselMap.get("calc_genset_mode")));
            vessel.setCalcGensetCutoff(parseCutoff(vesselMap.get("calc_genset_cutoff")));
            vessel.setUseSafeDiffErh(parseBooleanFlexible(vesselMap.get("use_safe_diff_erh")));

            result = this.vesselService.createVessel(vessel);
        }
        return new ResponseEntity<Object>(result.toMap(), HttpStatus.OK);
    }

    @PostMapping(value = "/update")
    @ResponseBody
    public ResponseEntity<?> update(@RequestBody JsonNode jsonNode, HttpServletRequest request) {
        JSONObject result;
        if (request.getSession().getAttribute("account") == null) {
            result = Util.sendForbiddenMessage();
        } else {
            Map<String, Object> vesselMap = Util.jsonNodeToMap(jsonNode);

            Vessel vessel = new Vessel();
            vessel.setEmail(vesselMap.get("vessel_email").toString());
            vessel.setEmailOld(vesselMap.get("vessel_email_old").toString());
            vessel.setName(vesselMap.get("vessel_name").toString());
            vessel.setMaxPortRpm(Integer.parseInt(vesselMap.get("max_port_rpm").toString()));
            vessel.setMaxStarboardRpm(Integer.parseInt(vesselMap.get("max_starboard_rpm").toString()));
            vessel.setDeleted(0);

            // report_type tetap seperti sekarang (bukan formula 2)
            vessel.setReportType(Integer.parseInt(vesselMap.get("report_type").toString()));

            vessel.setEmailTrackingAccount(vesselMap.get("email_tracking_account").toString());
            vessel.setPasswordTrackingAccount(vesselMap.get("password_tracking_account").toString());
            vessel.setTrackingProtocol(vesselMap.get("tracking_protocol").toString());
            vessel.setTrackingHost(vesselMap.get("tracking_host").toString());
            vessel.setTrackingPort(vesselMap.get("tracking_port").toString());
            vessel.setDataType(Integer.parseInt(vesselMap.get("data_type").toString()));
            vessel.setCategoryGsm(vesselMap.get("category_gsm").toString());

            // === NEW: FORMULA CONFIG ===
            vessel.setCalcGensetMode(parseMode(vesselMap.get("calc_genset_mode")));
            vessel.setCalcGensetCutoff(parseCutoff(vesselMap.get("calc_genset_cutoff")));
            vessel.setUseSafeDiffErh(parseBooleanFlexible(vesselMap.get("use_safe_diff_erh")));

            result = this.vesselService.updateVessel(vessel);
        }
        return new ResponseEntity<Object>(result.toMap(), HttpStatus.OK);
    }

    @PostMapping(value = "/delete")
    @ResponseBody
    public ResponseEntity<?> delete(@RequestBody Vessel vessel, HttpServletRequest request) {
        JSONObject result;
        if (request.getSession().getAttribute("account") == null) {
            result = Util.sendForbiddenMessage();
        } else {
            vessel.setEmailOld(vessel.getEmail());
            result = this.vesselService.updateVessel(vessel);
        }
        return new ResponseEntity<Object>(result.toMap(), HttpStatus.OK);
    }

    @PostMapping("/shipinfo")
    @ResponseBody
    public ResponseEntity<?> shipInfo(@RequestBody JsonNode jsonNode, HttpServletRequest request) {
        Account loggedAccount = (Account) request.getSession().getAttribute("account");
        JSONObject jsonObject = new JSONObject();
        if (loggedAccount != null) {
            Map<String, Object> jsonNodeMap = Util.jsonNodeToMap(jsonNode);
            jsonObject = this.vesselService.selectShipInfo(jsonNodeMap);
        } else {
            jsonObject.put("success", false);
            jsonObject.put("code", "forbidden");
            jsonObject.put("message", "Invalid username or password.");
        }
        return new ResponseEntity<Object>(jsonObject.toMap(), HttpStatus.OK);
    }

    // =========================
    // Helpers untuk formula config
    // =========================
    private String parseMode(Object v) {
        if (v == null)
            return null;
        String s = v.toString().trim();
        if (s.isEmpty())
            return null;
        s = s.toUpperCase();
        // biar aman, cuma izinkan RAW/DIFF
        if (!"RAW".equals(s) && !"DIFF".equals(s))
            return null;
        return s;
    }

    private LocalDate parseCutoff(Object v) {
        if (v == null)
            return null;
        String s = v.toString().trim();
        if (s.isEmpty())
            return null;
        try {
            // input type="date" biasanya yyyy-MM-dd
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid calc_genset_cutoff: {}", s);
            return null;
        }
    }

    private boolean parseBooleanFlexible(Object v) {
        if (v == null)
            return false;
        if (v instanceof Boolean)
            return (Boolean) v;
        if (v instanceof Number)
            return ((Number) v).intValue() != 0;

        String s = v.toString().trim().toLowerCase();
        return "1".equals(s) || "true".equals(s) || "yes".equals(s) || "on".equals(s);
    }
}
