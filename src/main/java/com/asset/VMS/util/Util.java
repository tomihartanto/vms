package com.asset.VMS.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class Util {
    public static String FORMAT_1 = "yyyy-MM-dd HH:mm:ss";

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte aData : data) {
            int halfByte = (aData >>> 4) & 0x0F;
            int twoHalf = 0;
            do {
                if (halfByte <= 9)
                    buf.append((char) ('0' + halfByte));
                else
                    buf.append((char) ('a' + (halfByte - 10)));
                halfByte = aData & 0x0F;
            } while (twoHalf++ < 1);
        }
        return buf.toString();
    }

    public static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        byte[] md5hash;
        md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
        md5hash = md.digest();
        return convertToHex(md5hash);
    }

    public static Map<String, Object> jsonNodeToMap(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(jsonNode, new TypeReference<>() {
        });
    }

    public static JSONObject exceptionToJSONObject(Exception e) throws JSONException {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", false);
        jsonObject.put("code", "Exception");
        jsonObject.put("message", errors.toString());
        jsonObject.put("draw", 1);
        jsonObject.put("recordsTotal", 0);
        jsonObject.put("recordsFiltered", 0);
        jsonObject.put("data", new JSONArray());

        return jsonObject;
    }

    public static JSONObject sendCreateOrUpdateFailedMessage() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", false);
        jsonObject.put("code", "General Error");
        jsonObject.put("message", "General error occurred. Please contact support.");
        return jsonObject;
    }

    public static JSONObject sendForbiddenMessage() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", false);
        jsonObject.put("code", "Forbidden");
        jsonObject.put("message", "Invalid username or password.");
        return jsonObject;
    }

    public static JSONObject sendEmptyJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        jsonObject.put("code", "valid");
        jsonObject.put("message", "valid");
        jsonObject.put("draw", 1);
        jsonObject.put("recordsTotal", 0);
        jsonObject.put("recordsFiltered", 0);
        jsonObject.put("data", new JSONObject());

        return jsonObject;
    }

    public static JSONObject resultToJSONObject(int recordsTotal, int recordsFiltered, JSONArray jsonArray) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("recordsTotal", recordsTotal);
        jsonObject.put("recordsFiltered", recordsFiltered);
        jsonObject.put("data", jsonArray);
        jsonObject.put("message", "valid");
        jsonObject.put("success", true);
        return jsonObject;
    }

    public static DateTime timestampToDateTime(Timestamp timestamp) {
        Long value = timestamp.getTime();
        TimeZone timeZone = TimeZone.getDefault();
        long offset = timeZone.getOffset(value);
        if (offset < 0) {
            value -= offset;
        } else {
            value += offset;
        }
        return new DateTime(value);
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String removeLadingZero(String textString) {
        return StringUtils.stripStart(textString, "0").equals("") ? "0" : StringUtils.stripStart(textString, "0");
    }

    public static String mapObjectToString(Object mapObject) {
        String returnString = null;
        if (mapObject != null) {
            returnString = mapObject.toString();
        }
        return returnString;
    }

    public static Timestamp stringToTimestamp(String strDate, String format) {
        try {
            DateFormat formatter = new SimpleDateFormat(format);
            // you can change format of date
            Date date = formatter.parse(strDate);

            return new Timestamp(date.getTime());
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return null;
        }
    }

    public static String changeDateFormat(String strDate, String oldFormat, String newFormat) {
        try {
            DateFormat formatter = new SimpleDateFormat(oldFormat);
            // you can change format of date
            Date date = formatter.parse(strDate);

            return new SimpleDateFormat(newFormat).format(date.getTime());
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return null;
        }
    }

    public static int stringToInt(String string) {
        int res;
        try {
            res = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            res = 0;
        }
        return res;
    }
}
