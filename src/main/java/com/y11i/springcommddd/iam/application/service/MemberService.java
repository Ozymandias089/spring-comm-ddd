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

/**
 * <h2>회원 등록 및 조회 서비스</h2>
 *
 * <p>
 * {@link com.y11i.springcommddd.iam.application.port.in.RegisterMemberUseCase}와
 * {@link com.y11i.springcommddd.iam.application.port.in.FindMemberUseCase}를 구현하는
 * 애플리케이션 서비스 계층 클래스입니다.
 * </p>
 *
 * <h3>주요 역할</h3>
 * <ul>
 *     <li>신규 회원 등록 (비밀번호 해싱 포함)</li>
 *     <li>회원 ID 또는 이메일 기반 회원 조회</li>
 * </ul>
 *
 * <p>
 * 회원 등록 시 비밀번호는 반드시 {@link org.springframework.security.crypto.password.PasswordEncoder}
 * 를 통해 해시되어 저장되며, 평문 비밀번호는 저장하지 않습니다.
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
public class MemberService implements RegisterMemberUseCase, FindMemberUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final PasswordEncoder passwordEncoder;

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Optional<MemberDTO> findById(UUID memberId) {
        return loadMemberPort.loadById(new MemberId(memberId)).map(MemberMapper::toMemberDTO);
    }
}
