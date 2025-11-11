package com.y11i.springcommddd.iam.application.service;

import com.y11i.springcommddd.iam.application.port.in.EmailVerificationUseCase;
import com.y11i.springcommddd.iam.application.port.out.EmailVerificationTokenPort;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.MailPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * <h2>이메일 인증 관리 서비스</h2>
 *
 * <p>
 * {@link com.y11i.springcommddd.iam.application.port.in.EmailVerificationUseCase}를 구현하며,
 * 회원가입 및 이메일 변경 시의 인증 흐름을 담당합니다.
 * </p>
 *
 * <h3>주요 기능</h3>
 * <ul>
 *     <li>회원가입 시 이메일 인증 토큰 발급 및 이메일 전송</li>
 *     <li>회원가입 인증 토큰 검증 및 회원 상태 업데이트</li>
 *     <li>이메일 변경 요청 시 새 이메일로 인증 토큰 발급</li>
 *     <li>이메일 변경 토큰 검증 및 실제 이메일 주소 변경</li>
 * </ul>
 *
 * <p>
 * 토큰은 TTL(기본 24시간) 동안 유효하며, Redis 등 단기 저장소를 통해 발급/검증됩니다.
 * 실제 이메일 발송은 {@link com.y11i.springcommddd.iam.application.port.out.MailPort}를 통해 수행됩니다.
 * </p>
 *
 * <h3>사용 포트</h3>
 * <ul>
 *     <li>{@link com.y11i.springcommddd.iam.application.port.out.LoadMemberPort}</li>
 *     <li>{@link com.y11i.springcommddd.iam.application.port.out.SaveMemberPort}</li>
 *     <li>{@link com.y11i.springcommddd.iam.application.port.out.EmailVerificationTokenPort}</li>
 *     <li>{@link com.y11i.springcommddd.iam.application.port.out.MailPort}</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService implements EmailVerificationUseCase {

    private static final Duration SIGNUP_TTL = Duration.ofHours(24);
    private static final Duration CHANGE_TTL = Duration.ofHours(24);

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final EmailVerificationTokenPort emailVerificationTokenPort;
    private final MailPort mailPort;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public void requestForSignup(UUID memberId, String email) {
        Optional<Member> maybe = loadMemberPort.loadById(new MemberId(memberId));
        if (maybe.isEmpty()) return; // 보안상 조용히 종료

        Member member = maybe.get();
        // 멱등: 이미 인증되었으면 조용히 성공
        if(member.emailVerified()) return;

        String token = emailVerificationTokenPort.issueForSignup(member.memberId().id(), SIGNUP_TTL);
        String subject = "[SpringComm] Please Verify your Email";
        String body = "Click this link to verify your Email: \n"
                + "http://localhost:8080/api/email-verify/signup/confirm?token=" +  token + "\n"
                + "You have 24 hours. OR ELSE";
        mailPort.send(email, subject, body);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void confirmSignup(String token) {
        UUID memberId;
        try {
            memberId = emailVerificationTokenPort.consumeForSignup(token);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }

        Member member = loadMemberPort.loadById(new MemberId(memberId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));
        if (!member.emailVerified()) {
            member.markEmailVerified();
            saveMemberPort.save(member);
        }
        // 이미 인증된 경우도 멱등 처리: 204/200 모두 가능. 예외 없이 성공 종료.
    }

}
