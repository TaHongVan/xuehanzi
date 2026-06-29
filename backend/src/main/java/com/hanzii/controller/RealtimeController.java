package com.hanzii.controller;

import com.hanzii.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/realtime")
@RequiredArgsConstructor
public class RealtimeController {

    private final RealtimeService realtimeService;

    @PostMapping(
            value = "/session",
            consumes = {MediaType.TEXT_PLAIN_VALUE, "application/sdp"},
            produces = "application/sdp")
    public ResponseEntity<String> createSession(@RequestBody String sdpOffer) {
        return ResponseEntity.ok(realtimeService.createCall(sdpOffer));
    }
}
