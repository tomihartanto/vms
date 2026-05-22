package com.asset.VMS.service;

import com.asset.VMS.domain.EmailConfig;
import com.asset.VMS.domain.VesselInbox;
import com.asset.VMS.mapper.VesselMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailDownloaderService {

    @Autowired
    private VesselMapper vesselMapper;

    // Full implementation available upon request
    // Handles: scheduled IMAP email download, multipart parsing, SAT/GSM detection
}
