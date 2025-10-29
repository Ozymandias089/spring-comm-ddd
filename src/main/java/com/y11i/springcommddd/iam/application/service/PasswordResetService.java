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

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService implements PasswordResetUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final PasswordResetTokenPort passwordResetTokenPort;
    private final PasswordEncoder passwordEncoder;


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
