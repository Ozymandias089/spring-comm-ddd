package com.y11i.springcommddd.iam.application.service;

import com.y11i.springcommddd.iam.application.port.in.AdminMemberUseCase;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMemberService implements AdminMemberUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final PasswordEncoder passwordEncoder;

    /** 자기 자신에 대한 위험 작업 방지용 */
    private static void ensureNotSelf(UUID targetId) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String principalEmail = a != null ? a.getName() : null;
        if (principalEmail == null) return;
        // 필요 시 이메일→멤버 조회 포트로 자신 여부를 더 정확히 비교해도 됨
        // 여기서는 컨트롤러에서 target==me 비교를 하거나, 하단에서 memberId로 한번 더 확인하도록 확장 가능.
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void grantAdmin(GrantAdminCommand cmd) {
        Member member = loadMemberPort.loadById(new MemberId(cmd.targetMemberId()))
                .orElseThrow();
        member.grantRole(MemberRole.ADMIN);
        saveMemberPort.save(member);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void revokeAdmin(RevokeAdminCommand cmd) {
        Member member = loadMemberPort.loadById(new MemberId(cmd.targetMemberId()))
                .orElseThrow();
        // 자기 자신 ADMIN 회수 금지 (간단 가드)
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getName().equalsIgnoreCase(member.email().value())) {
            throw new IllegalStateException("cannot revoke your own ADMIN role");
        }
        member.revokeRole(MemberRole.ADMIN);
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void setStatus(SetStatusCommand cmd) {
        Member member = loadMemberPort.loadById(new MemberId(cmd.targetMemberId()))
                .orElseThrow();

        String status = cmd.status();
        if ("ACTIVE".equalsIgnoreCase(status)) {
            member.activate();
        } else if ("SUSPENDED".equalsIgnoreCase(status)) {
            member.suspend();
        } else if ("DELETED".equalsIgnoreCase(status)) {
            // 자기 자신 삭제 금지
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a != null && a.getName().equalsIgnoreCase(member.email().value())) {
                throw new IllegalStateException("cannot delete your own account");
            }
            member.markDeleted();
        } else {
            throw new IllegalArgumentException("unknown status: " + status);
        }
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UUID createAdminAccount(CreateAdminCommand cmd) {
        String encoded = passwordEncoder.encode(cmd.rawPassword());
        Member member = Member.register(cmd.email(), cmd.displayName(), encoded);
        member.grantRole(MemberRole.ADMIN);
        // 정책: 새 관리자도 이메일 인증이 필요하면 emailVerified=false 유지,
        // 운영 일괄 등록이라면 바로 markEmailVerified() 호출 가능
        saveMemberPort.save(member);
        return member.memberId().id();
    }
}
