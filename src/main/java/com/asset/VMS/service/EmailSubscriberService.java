package com.asset.VMS.service;

import com.asset.VMS.mapper.AccountMapper;
import com.asset.VMS.mapper.VesselMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailSubscriberService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private VesselMapper vesselMapper;

    // Full implementation available upon request
    // Handles: daily report generation, email subscription delivery, JasperReports export
}
