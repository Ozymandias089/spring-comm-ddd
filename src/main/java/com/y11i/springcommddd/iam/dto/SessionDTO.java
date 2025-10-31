package com.y11i.springcommddd.iam.dto;

import lombok.Builder;
import lombok.Getter;

public class SessionDTO {
    @Getter private String sessionId;
    @Getter private String creationTime;
    @Getter private String lastAccessedTime;
    @Getter private int maxInactiveIntervalSeconds;

    @Builder
    public SessionDTO(String sessionId, String creationTime, String lastAccessedTime, int maxInactiveIntervalSeconds) {
        this.sessionId = sessionId;
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveIntervalSeconds = maxInactiveIntervalSeconds;
    }
}
