CREATE TABLE live_sessions (
  id               VARCHAR(32)  PRIMARY KEY,
  title            VARCHAR(200) NOT NULL,
  host_id          VARCHAR(32)  NOT NULL,
  status           VARCHAR(10)  NOT NULL DEFAULT 'READY',
  scheduled_at     TIMESTAMPTZ,
  started_at       TIMESTAMPTZ,
  ended_at         TIMESTAMPTZ,
  ivs_channel_arn  TEXT,
  ivs_playback_url TEXT,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

  CONSTRAINT live_sessions_status_check
    CHECK (status IN ('READY', 'LIVE', 'ENDED'))
);

CREATE INDEX idx_live_sessions_status     ON live_sessions (status);
CREATE INDEX idx_live_sessions_host       ON live_sessions (host_id);
CREATE INDEX idx_live_sessions_created_at ON live_sessions (created_at DESC);
