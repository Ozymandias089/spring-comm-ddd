package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMemberPrincipal;
import com.y11i.springcommddd.iam.application.port.in.SessionManagementUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.SessionDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 현재 로그인한 사용자의 "활성 세션"을 관리하는 엔드포인트.
 *
 * <p>
 * 여기서 말하는 세션은 Spring Session이 관리하는 서버 세션(JSESSIONID 등)이며,
 * 같은 계정으로 여러 브라우저/기기에서 로그인했을 경우 각각 별도 세션으로 존재한다.
 * </p>
 *
 * <p>
 * 이 컨트롤러는 두 가지 기능을 제공한다:
 * </p>
 * <ul>
 *     <li><b>GET /api/sessions</b> - 내가 가진 모든 활성 세션 목록 조회</li>
 *     <li><b>DELETE /api/sessions/{sessionId}</b> - 특정 세션을 강제로 종료(로그아웃)</li>
 * </ul>
 *
 * <p>
 * 세션 소유자는 {@code MemberId}로 식별된다.
 * SecurityContext 안의 principal(AuthenticatedMemberPrincipal)에서 안전하게 MemberId를 꺼내며,
 * 클라이언트는 임의의 userId를 보낼 수 없다.
 * </p>
 *
 * <p>
 * 세션 강제 종료는 "다른 기기에서 로그인된 나 자신"을 끊을 때 쓸 수 있다.
 * </p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
@Validated
public class SessionsController {

    private final SessionManagementUseCase sessionManagementUseCase;

    /**
     * 현재 로그인한 사용자의 "활성 세션"을 관리하는 엔드포인트.
     *
     * <p>
     * 여기서 말하는 세션은 Spring Session이 관리하는 서버 세션(JSESSIONID 등)이며,
     * 같은 계정으로 여러 브라우저/기기에서 로그인했을 경우 각각 별도 세션으로 존재한다.
     * </p>
     *
     * <p>
     * 이 컨트롤러는 두 가지 기능을 제공한다:
     * </p>
     * <ul>
     *     <li><b>GET /api/sessions</b> - 내가 가진 모든 활성 세션 목록 조회</li>
     *     <li><b>DELETE /api/sessions/{sessionId}</b> - 특정 세션을 강제로 종료(로그아웃)</li>
     * </ul>
     *
     * <p>
     * 세션 소유자는 {@code MemberId}로 식별된다.
     * SecurityContext 안의 principal(AuthenticatedMemberPrincipal)에서 안전하게 MemberId를 꺼내며,
     * 클라이언트는 임의의 userId를 보낼 수 없다.
     * </p>
     *
     * <p>
     * 세션 강제 종료는 "다른 기기에서 로그인된 나 자신"을 끊을 때 쓸 수 있다.
     * </p>
     */
    @GetMapping
    public List<SessionDTO> list() {
        MemberId memberId = currentMemberId();
        return sessionManagementUseCase.listMySessions(memberId);
    }

    /**
     * 해당 sessionId를 가진 세션을 강제로 무효화한다.
     *
     * <p>
     * 요청자가 소유하지 않은 세션 ID인 경우에는 아무 일도 일어나지 않거나,
     * (정책에 따라) 404/403을 줄 수도 있다.
     * 현재 구현은 소유자가 아니면 조용히 무시하는 방식을 택할 수 있다.
     * </p>
     *
     * <p>
     * 성공 시 204 No Content.
     * </p>
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> revoke(@PathVariable @NotBlank String sessionId) {
        MemberId memberId = currentMemberId();
        sessionManagementUseCase.revokeSession(memberId, sessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 현재 SecurityContext로부터 안전하게 MemberId를 추출한다.
     *
     * <p>
     * 단순히 "요청 헤더로 넘어온 userId" 같은 값을 신뢰하는 대신,
     * 실제 인증된 principal(AuthenticatedMemberPrincipal) 내부의 MemberId만을 사용한다.
     * 이를 통해 임의 ID로 다른 사람 세션을 종료하는 공격을 막는다.
     * </p>
     *
     * <p>
     * 인증이 없거나 principal 타입이 예상과 다르면 401 Unauthorized를 발생시킨다.
     * </p>
     */
    private MemberId currentMemberId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Object principal = a.getPrincipal();
        if (!(principal instanceof AuthenticatedMemberPrincipal p)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid principal");
        }

        return p.getMemberId();
    }
}
