package com.pia.telekom.controller;

import com.pia.telekom.dto.SubscriptionRequest;
import com.pia.telekom.dto.SubscriptionResponse;
import com.pia.telekom.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/by-customer/{customerId}")
    public SubscriptionResponse getSubscriptionByCustomer(@PathVariable Integer customerId) {
        return subscriptionService.getSubscriptionByCustomer(customerId);
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse created = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}