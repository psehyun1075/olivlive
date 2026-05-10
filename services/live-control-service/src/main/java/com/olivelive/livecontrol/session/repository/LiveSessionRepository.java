package com.olivelive.livecontrol.session.repository;

import com.olivelive.livecontrol.session.domain.LiveSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveSessionRepository extends JpaRepository<LiveSession, String> {
}
