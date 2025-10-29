package com.y11i.springcommddd.iam.application.service;

import com.y11i.springcommddd.iam.api.MemberMapper;
import com.y11i.springcommddd.iam.application.port.in.FindMemberUseCase;
import com.y11i.springcommddd.iam.application.port.in.ManageProfileUseCase;
import com.y11i.springcommddd.iam.application.port.in.RegisterMemberUseCase;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService implements RegisterMemberUseCase, ManageProfileUseCase, FindMemberUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberDTO register(RegisterMemberUseCase.Command command) {
        String encoded = passwordEncoder.encode(command.rawPassword());
        Member member = Member.register(command.email(), command.displayName(), encoded);
        Member saved = saveMemberPort.save(member);
        return MemberMapper.toMemberDTO(saved);
    }

    @Override
    @Transactional
    public MemberDTO rename(ManageProfileUseCase.RenameCommand cmd) {
        Member m = loadMemberPort.loadById(new MemberId(cmd.memberId()))
                .orElseThrow(); // 도메인 예외/공통 NotFound로 매핑
        m.rename(cmd.displayName());
        Member saved = saveMemberPort.save(m);
        return MemberMapper.toMemberDTO(saved);
    }

    @Override
    @Transactional
    public MemberDTO changeEmail(ManageProfileUseCase.ChangeEmailCommand command) {
        Member member = loadMemberPort.loadById(new MemberId(command.memberId()))
                .orElseThrow();
        member.changeEmail(command.email());
        Member saved = saveMemberPort.save(member);
        return MemberMapper.toMemberDTO(saved);
    }

    @Override
    @Transactional
    public MemberDTO changePassword(ManageProfileUseCase.ChangePasswordCommand command) {
        Member member = loadMemberPort.loadById(new MemberId(command.memberId()))
                .orElseThrow();
        if (!passwordEncoder.matches(command.currentPassword(), member.passwordHash().encoded())) {
            throw new BadCredentialsException("current password does not match");
        }
        member.setNewPassword(passwordEncoder.encode(command.rawPassword()));
        Member saved = saveMemberPort.save(member);
        return MemberMapper.toMemberDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MemberDTO> findByEmail(String email) {
        return loadMemberPort.loadByEmail(new Email(email)).map(MemberMapper::toMemberDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MemberDTO> findById(UUID memberId) {
        return loadMemberPort.loadById(new MemberId(memberId)).map(MemberMapper::toMemberDTO);
    }
}
