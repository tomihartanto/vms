package com.asset.VMS.controllers.rest;

import com.asset.VMS.domain.Account;
import com.asset.VMS.service.AccountService;
import com.asset.VMS.service.VesselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountRestController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private VesselService vesselService;

    // Full implementation available upon request
    // Handles: user CRUD REST API, role management, vessel allocation
}
