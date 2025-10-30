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

/**
 * <h2>관리자 계정 및 회원 상태 관리 서비스</h2>
 *
 * <p>
 * {@link com.y11i.springcommddd.iam.application.port.in.AdminMemberUseCase}를 구현하는
 * 애플리케이션 서비스 계층 클래스입니다.
 * </p>
 *
 * <h3>주요 역할</h3>
 * <ul>
 *     <li>회원에게 관리자 권한(ROLE_ADMIN 등)을 부여하거나 회수</li>
 *     <li>회원의 계정 상태(ACTIVE, SUSPENDED, DELETED 등)를 변경</li>
 *     <li>신규 관리자 계정을 직접 생성 (운영자 전용 플로우)</li>
 * </ul>
 *
 * <p>
 * 비즈니스 규칙에 따라 관리자만 접근할 수 있도록 상위 계층(API)에서 접근 제어를 수행하며,
 * 이 서비스는 도메인 모델을 조작하고 영속화하는 역할만 담당합니다.
 * </p>
 *
 * <h3>사용 포트</h3>
 * <ul>
 *     <li>{@link com.y11i.springcommddd.iam.application.port.out.LoadMemberPort}</li>
 *     <li>{@link com.y11i.springcommddd.iam.application.port.out.SaveMemberPort}</li>
 * </ul>
 */
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

    /** {@inheritDoc} */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void grantAdmin(GrantAdminCommand cmd) {
        Member member = loadMemberPort.loadById(new MemberId(cmd.targetMemberId()))
                .orElseThrow();
        member.grantRole(MemberRole.ADMIN);
        saveMemberPort.save(member);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
