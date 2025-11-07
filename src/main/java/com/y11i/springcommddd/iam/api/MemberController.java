package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMemberPrincipal;
import com.y11i.springcommddd.iam.api.support.MemberMapper;
import com.y11i.springcommddd.iam.application.port.in.FindMemberUseCase;
import com.y11i.springcommddd.iam.application.port.in.RegisterMemberUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import com.y11i.springcommddd.iam.dto.request.*;
import com.y11i.springcommddd.iam.dto.response.LoginResponseDTO;
import com.y11i.springcommddd.iam.dto.response.RegisterResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * 회원 가입 / 로그인 / 로그아웃 등 인증 관련 엔드포인트.
 *
 * <p>
 * 이 컨트롤러는 로그인 세션 기반(SecurityContext + HttpSession) 인증 모델을 사용한다.
 * 로그인 성공 시 JSESSIONID 세션이 생성되며,
 * 이후 요청은 세션 쿠키를 통해 인증 상태를 유지한다.
 * </p>
 *
 * <p>
 * 주요 엔드포인트:
 * </p>
 * <ul>
 *     <li><b>POST /api/register</b> - 신규 회원 가입</li>
 *     <li><b>POST /api/login</b> - 로그인 및 세션 발급</li>
 *     <li><b>POST /api/logout</b> - 현재 세션 로그아웃</li>
 * </ul>
 *
 * <p>
 * 로그인 이후 SecurityContext의 principal은
 * {@code AuthenticatedMemberPrincipal}이며,
 * 여기에 서비스 내부에서 사용하는 불변 식별자 {@code MemberId}가 들어 있다.
 * (이제 이메일이 아니라 MemberId가 주 식별자 역할을 한다)
 * </p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class MemberController {
    private final RegisterMemberUseCase registerMemberUseCase;
    private final AuthenticationManager authenticationManager;
    private final FindMemberUseCase findMemberUseCase;

    /**
     * 신규 회원 가입을 처리한다.
     *
     * <p>
     * 전달된 email / displayName / password(raw)로
     * {@link RegisterMemberUseCase}를 호출하여 실제 회원을 생성한다.
     * 비밀번호는 서비스 계층에서 해시 처리되어 저장되며,
     * 평문 비밀번호는 영속화되지 않는다.
     * </p>
     *
     * <p>
     * 응답은 201 Created 이고,
     * Location 헤더는 새로 생성된 회원 리소스의 URI(/api/members/{id} 형태)를 가리킨다.
     * Body는 현재 회원 상태(예: memberId 등)를 담은 {@link RegisterResponseDTO}다.
     * </p>
     */
    @PostMapping(path="/register", consumes="application/json", produces="application/json")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDto) {
        RegisterMemberUseCase.Command command =
                new RegisterMemberUseCase.Command(requestDto.getEmail(), requestDto.getDisplayName(), requestDto.getPassword());

        MemberDTO memberDTO = registerMemberUseCase.register(command);
        RegisterResponseDTO body = MemberMapper.toRegisterResponseDTO(memberDTO);

        URI location = URI.create("/api/members/" + body.getMemberId());
        return ResponseEntity.created(location).body(body);
    }

    /**
     * 로그인 시도 후, 세션을 수립한다.
     *
     * <p>
     * 1) 사용자가 이메일/비밀번호 자격 증명을 보낸다.<br/>
     * 2) {@link AuthenticationManager}를 통해 인증한다.<br/>
     * 3) 인증 성공 시 SecurityContext에 Authentication을 저장하고,
     *    HttpSession(JSESSIONID)을 생성한다.<br/>
     * 4) 실제 응답 바디는 현재 사용자 정보를 담은 {@link LoginResponseDTO}.
     * </p>
     *
     * <p>
     * 인증된 principal은 {@link AuthenticatedMemberPrincipal}이며,
     * 우리는 여기서 {@code MemberId}를 꺼내 내부 조회에 사용한다.
     * 즉, 더 이상 이메일을 식별자로 삼지 않는다.
     * </p>
     *
     * <p>
     * 성공 시 200 OK와 사용자 정보를 반환한다.
     * (원한다면 204 No Content를 선택할 수도 있지만, 현재는 200 + JSON 바디를 리턴한다.)
     * </p>
     */
    @PostMapping(path="/login", consumes="application/json", produces="application/json")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDto, HttpServletRequest request) {
        System.out.println(">>> ENTER /api/login");
        // 1. 사용자 자격 증명으로 Authentication 시도
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(),
                        requestDto.getPassword()
                );

        Authentication authentication = authenticationManager.authenticate(token);
        System.out.println("DEBUG principal class = " + authentication.getPrincipal().getClass());

        // 2. SecurityContext에 인증 결과 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 세션 발급 (상태ful 로그인 전략 유지)
        request.getSession(true);

        // 4. principal에서 memberId를 직접 꺼낸다
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthenticatedMemberPrincipal p)) throw new IllegalStateException("Unexpected principal type");

        MemberId memberId = p.getMemberId();

        // 5. 이제 이메일이 아니라 안정적인 ID로 조회
        MemberDTO memberDTO = findMemberUseCase.findById(memberId.id()).orElseThrow();

        // 6. 응답 DTO 구성
        LoginResponseDTO body = MemberMapper.toLoginResponseDTO(memberDTO);
        // 로그인은 200 OK가 자연스럽다. (원하면 204 No Content도 가능)
        return ResponseEntity.ok(body);
    }

    /**
     * 현재 세션을 무효화(로그아웃)한다.
     *
     * <p>
     * 클라이언트는 세션 쿠키(JSESSIONID)를 가지고 있고,
     * 이 엔드포인트를 호출하면 해당 세션이 invalidate되어 인증 상태가 해제된다.
     * SecurityContext도 초기화된다.
     * </p>
     *
     * <p>
     * 성공 시 204 No Content.
     * </p>
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    // dev 환경용 간단 피드
    @GetMapping("/csrf")
    public java.util.Map<String, String> csrf(HttpServletRequest req) {
        var token = (org.springframework.security.web.csrf.CsrfToken) req.getAttribute("_csrf");
        // 토큰 강제 생성/접근
        var value = token.getToken();
        // CookieCsrfTokenRepository가 XSRF-TOKEN 쿠키를 내려주고,
        // 디버깅용으로 바디에도 토큰을 보여줍니다.
        return java.util.Map.of(
                "headerName", token.getHeaderName(),
                "parameterName", token.getParameterName(),
                "token", value
        );
    }

}
