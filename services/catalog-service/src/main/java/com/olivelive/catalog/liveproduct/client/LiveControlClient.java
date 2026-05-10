package com.olivelive.catalog.liveproduct.client;

/**
 * Contract for validating live session state with live-control-service.
 * catalog-service does not own session state — it delegates validation here.
 */
public interface LiveControlClient {

    /**
     * Verifies that the session exists and can accept product attachments.
     *
     * @throws com.olivelive.catalog.common.exception.CatalogException LIVE_SESSION_NOT_FOUND if session does not exist
     * @throws com.olivelive.catalog.common.exception.CatalogException LIVE_SESSION_NOT_ATTACHABLE if session state disallows attachment
     */
    void validateSessionAttachable(String liveSessionId);
}
