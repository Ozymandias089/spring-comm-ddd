package com.y11i.springcommddd.iam.application.service;

import com.y11i.springcommddd.iam.api.support.MemberMapper;
import com.y11i.springcommddd.iam.application.port.in.ManageProfileUseCase;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

/**
 * <h2>사용자 프로필 관리 서비스</h2>
 *
 * <p>
 * {@link com.y11i.springcommddd.iam.application.port.in.ManageProfileUseCase}를 구현하며,
 * 로그인된 사용자가 자신의 프로필 정보를 수정할 수 있도록 지원합니다.
 * </p>
 *
 * <h3>주요 기능</h3>
 * <ul>
 *     <li>표시명(닉네임) 변경</li>
 *     <li>이메일 변경 (별도 인증 플로우와 연동 가능)</li>
 *     <li>비밀번호 변경 (현재 비밀번호 검증 포함)</li>
 *     <li>프로필 / 배너 이미지 변경</li>
 * </ul>
 *
 * <p>
 * 각 변경 후 최신 {@link com.y11i.springcommddd.iam.dto.MemberDTO}를 반환하며,
 * 도메인 모델에서 검증 가능한 제약(예: 표시명 형식, 비밀번호 정책 등)을 위반하면 예외를 발생시킵니다.
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
public class MyPageService implements ManageProfileUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final PasswordEncoder passwordEncoder;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MemberDTO rename(RenameCommand cmd) {
        Member m = loadMemberPort.loadById(new MemberId(cmd.memberId()))
                .orElseThrow(() -> {
                    System.err.println("[rename] member not found: " + cmd.memberId());
                    return new NoSuchElementException("member not found");
                }); //도메인 예외/공통 NotFound로 매핑
        m.rename(cmd.displayName());
        Member saved = saveMemberPort.save(m);
        return MemberMapper.toMemberDTO(saved);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MemberDTO changeEmail(ChangeEmailCommand cmd) {
        Member member = loadMemberPort.loadById(new MemberId(cmd.memberId()))
                .orElseThrow();
        member.changeEmail(cmd.email());
        Member saved = saveMemberPort.save(member);
        return MemberMapper.toMemberDTO(saved);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MemberDTO changePassword(ChangePasswordCommand cmd) {
        Member member = loadMemberPort.loadById(new MemberId(cmd.memberId()))
                .orElseThrow();
        if (!passwordEncoder.matches(cmd.currentPassword(), member.passwordHash().encoded())) {
            throw new BadCredentialsException("current password does not match");
        }
        member.setNewPassword(passwordEncoder.encode(cmd.rawPassword()));
        Member saved = saveMemberPort.save(member);
        return MemberMapper.toMemberDTO(saved);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MemberDTO changeProfileImage(ChangeProfileImageCommand command) {
        Member member = loadMemberPort.loadById(new MemberId(command.memberId()))
                .orElseThrow();
        member.changeProfileImage(command.profileImageUrl());
        Member saved = saveMemberPort.save(member);
        return MemberMapper.toMemberDTO(saved);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MemberDTO changeBannerImage(ChangeBannerImageCommand command) {
        Member member = loadMemberPort.loadById(new MemberId(command.memberId())).orElseThrow();
        member.changeBannerImage(command.bannerImageUrl());
        Member saved = saveMemberPort.save(member);
        return MemberMapper.toMemberDTO(saved);
    }
}
