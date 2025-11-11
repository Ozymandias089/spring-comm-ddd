package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.application.port.in.EmailVerificationUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.request.EmailVerificationSignupRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * 이메일 인증/변경 관련 HTTP 엔드포인트.
 *
 * <p>
 * 가입 이메일 인증 플로우와, 로그인된 사용자의 이메일 변경 플로우를 다룬다.
 * 크게 두 가지 흐름이 있다:
 * </p>
 *
 * <ol>
 *   <li><b>회원가입 이메일 인증</b><br/>
 *       - 회원 가입 후, 제공된 이메일이 실제 소유자인지 확인하기 위한 토큰을 발급/전송한다.<br/>
 *       - 사용자는 해당 토큰을 제출함으로써 이메일을 "인증됨" 상태로 만든다.
 *   </li>
 *
 *   <li><b>이메일 변경 인증</b><br/>
 *       - 로그인된 사용자가 이메일을 새 값으로 변경하려는 경우,<br/>
 *         새 이메일 주소로 토큰을 발송하고, 토큰 제출 시 실제 이메일이 갱신된다.
 *   </li>
 * </ol>
 *
 * <p>
 * 이 컨트롤러는 인증 여부에 따라 엔드포인트가 나뉜다.
 * - 일부 요청은 로그인된 사용자(@AuthenticatedMember MemberId)만 호출 가능하다.
 * - 토큰 확인(confirm) 엔드포인트는 인증되지 않은 호출도 허용할 수 있다.
 *   (메일 안의 확인 링크 등)
 * </p>
 *
 * <p>
 * 응답으로는 보통 다음의 HTTP 상태 코드를 사용한다:
 * <ul>
 *   <li>202 Accepted: 토큰 발급/전송 요청이 수락되었음을 의미 (비동기 처리 가능)</li>
 *   <li>204 No Content: 검증/확정이 완료되었음을 의미</li>
 * </ul>
 * </p>
 */
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/email-verify")
public class EmailVerificationController {
    private final EmailVerificationUseCase emailVerificationUseCase;

    /**
     * (로그인 필요) 현재 로그인한 사용자의 이메일 주소로
     * 가입 인증(이메일 검증) 토큰을 발급/전송한다.
     *
     * <p>
     * 클라이언트는 이메일 주소를 함께 보내고,
     * 서버는 해당 계정(memberId)와 이메일의 조합으로 토큰을 발행한다.
     * 이후 사용자는 이메일로 전달된 토큰을 /signup/confirm 으로 제출해 인증을 마무리한다.
     * </p>
     *
     * <p>
     * 성공 시 202 Accepted를 내려주며, 실제 메일 발송 등은 백그라운드에서 처리 가능하다는 시그널이다.
     * </p>
     */
    @PostMapping("/signup/request")
    public ResponseEntity<Void> requestForSignupMe(@AuthenticatedMember MemberId memberId, @Valid @RequestBody EmailVerificationSignupRequestDTO requestDTO) {
        emailVerificationUseCase.requestForSignup(memberId.id(), requestDTO.getEmail());
        return ResponseEntity.accepted().build();
    }

    /**
     * 가입 인증 토큰을 검증하고,
     * 해당 계정의 이메일을 "인증됨" 상태로 변경한다.
     *
     * <p>
     * 일반적으로 사용자는 이메일에 포함된 토큰을 이 엔드포인트로 제출한다.
     * 인증(로그인) 상태가 아닐 수도 있다고 가정할 수 있다.
     * </p>
     *
     * <p>
     * 성공 시 204 No Content를 반환한다.
     * </p>
     */
    @GetMapping("/signup/confirm")
    public ResponseEntity<Void> confirmSignUp(@RequestParam("token") String token) {
        if (!StringUtils.hasText(token)) throw new ResponseStatusException(BAD_REQUEST, "token must not be blank");
        emailVerificationUseCase.confirmSignup(token);
        return ResponseEntity.noContent().build();
    }
}
