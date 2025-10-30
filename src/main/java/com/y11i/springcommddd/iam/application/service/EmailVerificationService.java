package com.y11i.springcommddd.iam.application.service;

import com.y11i.springcommddd.iam.application.port.in.EmailVerificationUseCase;
import com.y11i.springcommddd.iam.application.port.out.EmailVerificationTokenPort;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.MailPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.domain.Email;
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
                + "https://app.springcomm.app/verify-your-email?token=" +  token + "\n"
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

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public void requestForChange(UUID memberId, String newEmail) {
        Optional<Member> maybe = loadMemberPort.loadById(new MemberId(memberId));
        if (maybe.isEmpty()) return;

        // newEmail 형식 1차 검증 (VO 수준으로 한 번 더 걸릴 예정)
        new Email(newEmail);

        String token = emailVerificationTokenPort.issueForChange(memberId, newEmail, CHANGE_TTL);
        String subject = "[SpringComm] 이메일 변경을 확인해 주세요";
        String body = "이 링크를 클릭하여 이메일 변경을 완료하세요:\n"
                + "https://app.springcomm.app/confirm-email-change?token=" + token + "\n"
                + "24시간 내에 유효합니다.";
        mailPort.send(newEmail, subject, body);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void confirmChange(String token) {
        EmailVerificationTokenPort.EmailChangePayload payload;
        try {
            payload = emailVerificationTokenPort.consumeForChange(token);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }

        Member member = loadMemberPort.loadById(new MemberId(payload.memberId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));

        // 실제 변경: 유니크 제약(uk_members_email) 위반 시 DataIntegrityViolationException → GlobalExceptionHandler가 409로 매핑하는 걸 권장
        member.changeEmail(payload.newEmail());
        // 이메일을 새로 바꿨다면, 정책에 따라 emailVerified를 유지할지 초기화할지 선택:
        //  - 이미 토큰 확인을 거쳤으므로 verified 유지가 자연스러움.
        saveMemberPort.save(member);
    }
}
