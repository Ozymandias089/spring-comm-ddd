package com.y11i.springcommddd.iam.application.service;

import com.y11i.springcommddd.iam.api.support.MemberMapper;
import com.y11i.springcommddd.iam.application.port.in.FindMemberUseCase;
import com.y11i.springcommddd.iam.application.port.in.RegisterMemberUseCase;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService implements RegisterMemberUseCase, FindMemberUseCase {
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

    /** @deprecated {@inheritDoc} */
    @Deprecated
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
