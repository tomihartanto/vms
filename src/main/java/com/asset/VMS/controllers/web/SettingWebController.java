package com.asset.VMS.controllers.web;

import com.asset.VMS.mapper.VesselMapper;
import com.asset.VMS.service.VesselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/setting")
public class SettingWebController {

    @Autowired
    private VesselService vesselService;

    @Autowired
    private VesselMapper vesselMapper;

    // Full implementation available upon request
    // Handles: vessel configuration, data type settings, formula config
}
