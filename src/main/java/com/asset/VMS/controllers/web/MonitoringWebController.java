package com.asset.VMS.controllers.web;

import com.asset.VMS.domain.Account;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.asset.VMS.service.EmailProcessorService;

@Controller
public class MonitoringWebController {

    private final EmailProcessorService emailProcessorService;

    public MonitoringWebController(EmailProcessorService emailProcessorService) {
        this.emailProcessorService = emailProcessorService;
    }

    @GetMapping({ "/monitoring" })
    public String index(Model model, HttpServletRequest request) {
        String returnString;
        if (request.getSession().getAttribute("account") == null) {
            returnString = "redirect:/login";
        } else {
            Account account = (Account) request.getSession().getAttribute("account");
            String vessels = new JSONArray(account.getVesselList()).toString();
            model.addAttribute("vessels", vessels);
            model.addAttribute("pageTitle", "Vessel Position");
            model.addAttribute("activeMenu", "vessel-position");
            model.addAttribute(account);

            returnString = "monitoring";
        }
        return returnString;
    }

    @GetMapping("/test")
    @ResponseBody
    public String testWindy() {
        Map<String, Object> result = emailProcessorService.getWindyForecastData();

        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Hasil Windy Forecast</h2>");
        sb.append("<ul>");

        for (Map.Entry<String, Object> entry : result.entrySet()) {
            sb.append(String.format("<li><strong>%s:</strong> %s</li>", entry.getKey(), entry.getValue()));
        }

        sb.append("</ul>");
        return sb.toString();
    }

    @GetMapping("/testgsm")
    @ResponseBody
    public String testGSM() {
        emailProcessorService.getGSMData();

        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Hasil Windy Forecast</h2>");
        sb.append("<ul>");

        // for (Map.Entry<String, Object> entry : result.entrySet()) {
        // sb.append(String.format("<li><strong>%s:</strong> %s</li>", entry.getKey(),
        // entry.getValue()));
        // }

        sb.append("</ul>");
        return sb.toString();
    }

}
