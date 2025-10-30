package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.application.port.in.PasswordResetUseCase;
import com.y11i.springcommddd.iam.dto.request.PasswordResetConfirmRequestDTO;
import com.y11i.springcommddd.iam.dto.request.PasswordResetRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 비밀번호 분실/복구 플로우용 엔드포인트.
 *
 * <p>
 * 사용자는 이메일 주소를 제출해서 "비밀번호 재설정 토큰"을 발급받을 수 있고,
 * 이후 그 토큰과 새 비밀번호를 제출해서 계정 비밀번호를 초기화할 수 있다.
 * </p>
 *
 * <p>
 * 이 컨트롤러는 인증되지 않은 사용자(로그아웃 상태)도 호출할 수 있다.
 * 즉 "로그인을 못 하겠어요" 상황을 다룬다.
 * </p>
 *
 * <p>
 * 반환 코드 정책:
 * <ul>
 *   <li>202 Accepted: 토큰 발급 요청이 수락되었다 (메일 발송 등은 비동기적으로 처리 가능)</li>
 *   <li>204 No Content: 비밀번호 재설정이 완료되었다</li>
 * </ul>
 * </p>
 */
@RestController
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private final PasswordResetUseCase passwordResetUseCase;

    /**
     * 비밀번호 재설정 토큰 발급 요청.
     *
     * <p>
     * 사용자가 자신의 이메일을 제출하면,
     * 시스템은 해당 계정에 대해 재설정 토큰을 만들고 전달한다(예: 메일).
     * 이때 "그 이메일이 존재하는지 여부"를 외부에 노출하지 않도록 해야 한다.
     * 즉, 항상 202 Accepted를 주는 식으로 동작할 수 있다.
     * </p>
     *
     * <p>
     * 성공 시 202 Accepted.
     * </p>
     */
    @PostMapping
    public ResponseEntity<Void> request(@Valid @RequestBody PasswordResetRequestDTO requestDto) {
        passwordResetUseCase.request(requestDto.getEmail());
        return ResponseEntity.accepted().build(); // 202
    }

    /**
     * 재설정 토큰과 새 비밀번호를 제출하여 실제 비밀번호를 갱신한다.
     *
     * <p>
     * 토큰은 단일 사용으로 소모(consume)되어야 하며,
     * 유효 기간을 지난 토큰은 거부된다.
     * </p>
     *
     * <p>
     * 성공 시 204 No Content.
     * </p>
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@Valid @RequestBody PasswordResetConfirmRequestDTO requestDto) {
        passwordResetUseCase.confirm(requestDto.getToken(), requestDto.getNewPassword());
        return ResponseEntity.noContent().build(); // 204
    }
}
