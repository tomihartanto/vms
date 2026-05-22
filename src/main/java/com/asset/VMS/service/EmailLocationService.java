package com.asset.VMS.service;

import com.asset.VMS.domain.Vessel;
import com.asset.VMS.domain.VesselLocation;
import com.asset.VMS.mapper.VesselMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailLocationService {

    @Autowired
    private VesselMapper vesselMapper;

    // Full implementation available upon request
    // Handles: location email processing, JSON attachment parsing, map image extraction
}
