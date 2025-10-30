package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.application.port.in.EmailVerificationUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.request.EmailChangeRequestDTO;
import com.y11i.springcommddd.iam.dto.request.EmailVerificationConfirmRequestDTO;
import com.y11i.springcommddd.iam.dto.request.EmailVerificationSignupRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequestMapping("/api/email-verify")
public class EmailVerificationController {
    private final EmailVerificationUseCase emailVerificationUseCase;

    /**
     * (미구현 placeholder)
     * 가입 이메일 인증 토큰을 재발송 요청하는 엔드포인트.
     *
     * <p>
     * 현재는 501(Not Implemented)로 응답하며,
     * "로그인 없이 이메일만 주고 다시 보내달라" 식의 플로우를 지원하려면
     * 이메일 → 회원 식별자 검색 로직을 별도로 노출해야 한다.
     * </p>
     *
     * <p>
     * 보안적으로는, 임의 이메일을 넣었을 때 존재 여부를 유추할 수 없게 해야 한다.
     * 즉 실제 구현을 할 경우에도 정보 누출(계정 존재 여부 확인)을 피해야 한다.
     * </p>
     */
    @PostMapping("/signup/request/me")
    public ResponseEntity<Void> requestForSignup(@Valid @RequestBody EmailVerificationSignupRequestDTO requestDTO) {
        // 이메일만으로 재전송을 허용하려면, 이메일→멤버 조회 유스케이스를 써서 memberId를 찾아 호출하면 됨.
        // 하지만 여기선 간단하게 회원이 로그인 상태에서 내 주소로 재발송하는 API를 권장.
        return ResponseEntity.status(501).build();
    }

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
    @PostMapping("/signup/confirm")
    public ResponseEntity<Void> confirmSignUp(@Valid @RequestBody EmailVerificationConfirmRequestDTO requestDTO) {
        emailVerificationUseCase.confirmSignup(requestDTO.getToken());
        return ResponseEntity.noContent().build();
    }

    /**
     * (로그인 필요) 사용자가 자신의 이메일 주소를 새 값으로 바꾸려 할 때,
     * 새 이메일 주소로 변경 확인 토큰을 발송하도록 요청한다.
     *
     * <p>
     * 즉시 이메일이 바뀌지는 않고, /change/confirm에서 토큰을 제출해야
     * 실제 이메일 변경이 확정된다.
     * </p>
     *
     * <p>
     * 성공 시 202 Accepted.
     * </p>
     */
    @PostMapping("/change/request")
    public ResponseEntity<Void> requestForChange(@AuthenticatedMember MemberId memberId,
                                                 @Valid @RequestBody EmailChangeRequestDTO requestDTO) {
        emailVerificationUseCase.requestForChange(memberId.id(), requestDTO.getEmail());
        return ResponseEntity.accepted().build();
    }

    /**
     * 이메일 변경 토큰을 검증/소비하여,
     * 사용자의 이메일 주소를 실제로 새 값으로 갱신한다.
     *
     * <p>
     * 사용자는 새 이메일로 받은 토큰을 제출한다.
     * 인증 상태가 아닐 수도 있음을 고려할 수 있다.
     * </p>
     *
     * <p>
     * 성공 시 204 No Content.
     * </p>
     */
    @PostMapping("/change/confirm")
    public ResponseEntity<Void> confirmChange(@Valid @RequestBody EmailVerificationConfirmRequestDTO requestDTO) {
        emailVerificationUseCase.confirmChange(requestDTO.getToken());
        return ResponseEntity.noContent().build();
    }
}
