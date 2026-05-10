package com.olivelive.livecontrol.session.controller;

import com.olivelive.livecontrol.session.dto.CreateSessionRequest;
import com.olivelive.livecontrol.session.dto.LiveSessionResponse;
import com.olivelive.livecontrol.session.dto.SessionStatusResponse;
import com.olivelive.livecontrol.session.service.LiveSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/live-sessions")
@RequiredArgsConstructor
public class LiveSessionController {

    private final LiveSessionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LiveSessionResponse create(@Valid @RequestBody CreateSessionRequest request) {
        return service.createSession(request);
    }

    @GetMapping("/{id}")
    public LiveSessionResponse get(@PathVariable String id) {
        return service.getSession(id);
    }

    @PostMapping("/{id}/go-live")
    public SessionStatusResponse goLive(@PathVariable String id) {
        return service.goLive(id);
    }

    @PostMapping("/{id}/end")
    public SessionStatusResponse end(@PathVariable String id) {
        return service.end(id);
    }
}
