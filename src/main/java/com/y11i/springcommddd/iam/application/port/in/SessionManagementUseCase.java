package com.y11i.springcommddd.iam.application.port.in;

import com.y11i.springcommddd.iam.dto.SessionDTO;

import java.util.List;

public interface SessionManagementUseCase {
    List<SessionDTO> listMySessions(String principalEmail);
    void revokeSession(String principalEmail, String sessionId);
}
