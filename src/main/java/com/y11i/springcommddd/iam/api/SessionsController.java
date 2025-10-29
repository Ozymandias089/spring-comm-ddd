// src/main/java/com/y11i/springcommddd/iam/api/SessionsController.java
package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.application.port.in.SessionManagementUseCase;
import com.y11i.springcommddd.iam.dto.SessionDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
@Validated
public class SessionsController {

    private final SessionManagementUseCase sessionManagementUseCase;

    @GetMapping
    public List<SessionDTO> list() {
        String email = currentPrincipalEmail();
        return sessionManagementUseCase.listMySessions(email);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> revoke(@PathVariable @NotBlank String sessionId) {
        String email = currentPrincipalEmail();
        sessionManagementUseCase.revokeSession(email, sessionId);
        return ResponseEntity.noContent().build();
    }

    private String currentPrincipalEmail() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return a.getName(); // MemberAuthProvider가 email을 principal로 셋업
    }
}
