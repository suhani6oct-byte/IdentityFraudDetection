package com.fivewheels.fraud.controller;

import com.fivewheels.fraud.model.IdentityRequest;
import com.fivewheels.fraud.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class VerifyController {

    private final VerificationService verificationService;

    public VerifyController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    /** Send an identity, get back APPROVE / REVIEW / BLOCK with every check's reason. */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestBody IdentityRequest request,
                                                      HttpServletRequest http) {
        // Never trust the client for these two: the server sees the real connection.
        request.setIpAddress(http.getRemoteAddr());
        request.setUserAgent(http.getHeader("User-Agent"));

        return ResponseEntity.ok(verificationService.verify(request));
    }
}
