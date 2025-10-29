package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.application.port.in.PasswordResetUseCase;
import com.y11i.springcommddd.iam.dto.request.PasswordResetConfirmRequestDTO;
import com.y11i.springcommddd.iam.dto.request.PasswordResetRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private final PasswordResetUseCase passwordResetUseCase;

    @PostMapping
    public ResponseEntity<Void> request(@Valid @RequestBody PasswordResetRequestDTO requestDto) {
        passwordResetUseCase.request(requestDto.getEmail());
        return ResponseEntity.accepted().build(); // 202
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@Valid @RequestBody PasswordResetConfirmRequestDTO requestDto) {
        passwordResetUseCase.confirm(requestDto.getToken(), requestDto.getNewPassword());
        return ResponseEntity.noContent().build(); // 204
    }
}
