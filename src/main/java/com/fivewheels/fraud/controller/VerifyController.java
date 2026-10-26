package com.fivewheels.fraud.controller;

import com.fivewheels.fraud.model.IdentityRequest;
import com.fivewheels.fraud.service.VerificationService;
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

@PostMapping("/verify")
public ResponseEntity<Map<String, Object>> verify(
        @RequestBody IdentityRequest request) {

    return ResponseEntity.ok(verificationService.verify(request));
}


}
