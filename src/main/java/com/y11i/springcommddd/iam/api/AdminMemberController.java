package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.application.port.in.AdminMemberUseCase;
import com.y11i.springcommddd.iam.dto.request.admin.CreateAdminRequestDTO;
import com.y11i.springcommddd.iam.dto.request.admin.GrantAdminRequestDTO;
import com.y11i.springcommddd.iam.dto.request.admin.RevokeAdminRequestDTO;
import com.y11i.springcommddd.iam.dto.request.admin.SetStatusRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@Validated
public class AdminMemberController {
    private final AdminMemberUseCase adminMemberUseCase;

    @PostMapping("/grant-admin")
    public ResponseEntity<Void> grantAdmin(@Valid @RequestBody GrantAdminRequestDTO req) {
        adminMemberUseCase.grantAdmin(new AdminMemberUseCase.GrantAdminCommand(req.getMemberId()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/revoke-admin")
    public ResponseEntity<Void> revokeAdmin(@Valid @RequestBody RevokeAdminRequestDTO req) {
        adminMemberUseCase.revokeAdmin(new AdminMemberUseCase.RevokeAdminCommand(req.getMemberId()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/set-status")
    public ResponseEntity<Void> setStatus(@Valid @RequestBody SetStatusRequestDTO req) {
        adminMemberUseCase.setStatus(new AdminMemberUseCase.SetStatusCommand(req.getMemberId(), req.getStatus()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-admin")
    public ResponseEntity<Void> createAdmin(@Valid @RequestBody CreateAdminRequestDTO req) {
        var id = adminMemberUseCase.createAdminAccount(
                new AdminMemberUseCase.CreateAdminCommand(req.getEmail(), req.getDisplayName(), req.getPassword())
        );
        return ResponseEntity.created(URI.create("/api/admin/members/" + id)).build();
    }
}
