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

@Service
@RequiredArgsConstructor
public class MyPageService implements ManageProfileUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberDTO rename(RenameCommand cmd) {
        Member m = loadMemberPort.loadById(new MemberId(cmd.memberId()))
                .orElseThrow(); // 도메인 예외/공통 NotFound로 매핑
        m.rename(cmd.displayName());
        Member saved = saveMemberPort.save(m);
        return MemberMapper.toMemberDTO(saved);
    }

    @Override
    @Transactional
    public MemberDTO changeEmail(ChangeEmailCommand cmd) {
        Member member = loadMemberPort.loadById(new MemberId(cmd.memberId()))
                .orElseThrow();
        member.changeEmail(cmd.email());
        Member saved = saveMemberPort.save(member);
        return MemberMapper.toMemberDTO(saved);
    }

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

    @Override
    @Transactional
    public MemberDTO changeProfileImage(ChangeProfileImageCommand command) {
        Member member = loadMemberPort.loadById(new MemberId(command.memberId()))
                .orElseThrow();
        member.changeProfileImage(command.profileImageUrl());
        Member saved = saveMemberPort.save(member);
        return MemberMapper.toMemberDTO(saved);
    }

    @Override
    @Transactional
    public MemberDTO changeBannerImage(ChangeBannerImageCommand command) {
        Member member = loadMemberPort.loadById(new MemberId(command.memberId())).orElseThrow();
        member.changeBannerImage(command.bannerImageUrl());
        Member saved = saveMemberPort.save(member);
        return MemberMapper.toMemberDTO(saved);
    }
}
