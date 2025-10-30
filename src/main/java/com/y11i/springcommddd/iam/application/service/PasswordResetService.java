package com.y11i.springcommddd.iam.application.service;

import com.y11i.springcommddd.iam.application.port.in.PasswordResetUseCase;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.PasswordResetTokenPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;

/**
 * <h2>비밀번호 재설정(복구) 서비스</h2>
 *
 * <p>
 * {@link com.y11i.springcommddd.iam.application.port.in.PasswordResetUseCase}를 구현하며,
 * 비밀번호를 잊은 사용자가 재설정 요청 → 토큰 검증 → 새 비밀번호 설정까지의 흐름을 처리합니다.
 * </p>
 *
 * <h3>주요 역할</h3>
 * <ul>
 *     <li>비밀번호 재설정 토큰 발급 및 이메일 전송</li>
 *     <li>토큰 검증 및 회원 비밀번호 갱신</li>
 * </ul>
 *
 * <p>
 * 토큰은 {@link com.y11i.springcommddd.iam.application.port.out.PasswordResetTokenPort}를 통해 발급되며,
 * Redis 등 단기 저장소에서 TTL 기반으로 관리됩니다.
 * 실제 이메일 발송은 {@link com.y11i.springcommddd.iam.application.port.out.MailPort}를 통해 수행할 수 있습니다.
 * 비밀번호는 반드시 {@link org.springframework.security.crypto.password.PasswordEncoder}로 해싱되어 저장됩니다.
 * </p>
 *
 * <h3>사용 포트</h3>
 * <ul>
 *     <li>{@link com.y11i.springcommddd.iam.application.port.out.LoadMemberPort}</li>
 *     <li>{@link com.y11i.springcommddd.iam.application.port.out.SaveMemberPort}</li>
 *     <li>{@link com.y11i.springcommddd.iam.application.port.out.PasswordResetTokenPort}</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService implements PasswordResetUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final PasswordResetTokenPort passwordResetTokenPort;
    private final PasswordEncoder passwordEncoder;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public void request(String email) {
        Optional<Member> maybe = loadMemberPort.loadByEmail(new Email(email));
        if (maybe.isEmpty()) return; // Hide if it exists for Security Reason: Always 202 Accepted
        Member member = maybe.get();
        String token = passwordResetTokenPort.issueToken(member.memberId().id(), Duration.ofMinutes(5));
        // Set to use Actual emails in prod. it uses console logs in development
        log.info("Password reset token for {}: {}", email, token);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void confirm(String token, String newPassword) {
        try {
            var memberId = passwordResetTokenPort.consume(token); // 없거나 만료면 예외
            Member member = loadMemberPort.loadById(new MemberId(memberId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));
            member.setNewPassword(passwordEncoder.encode(newPassword));
            saveMemberPort.save(member);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }
    }
}
