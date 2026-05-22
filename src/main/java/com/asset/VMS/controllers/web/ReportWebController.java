package com.asset.VMS.controllers.web;

import com.asset.VMS.service.VesselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/report")
public class ReportWebController {

    @Autowired
    private VesselService vesselService;

    // Full implementation available upon request
    // Handles: fuel monitoring report, daily report pages, PDF/XLSX export via JasperReports
}
