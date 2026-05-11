package com.olivelive.livecontrol.session.controller;

import com.olivelive.livecontrol.session.dto.AttachabilityResponse;
import com.olivelive.livecontrol.session.service.LiveSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/live-sessions")
@RequiredArgsConstructor
public class InternalLiveSessionController {

    private final LiveSessionService service;

    @GetMapping("/{id}/attachability")
    public AttachabilityResponse getAttachability(@PathVariable String id) {
        return service.getAttachability(id);
    }
}
