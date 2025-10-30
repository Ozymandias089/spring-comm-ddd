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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class MemberController {
    private final RegisterMemberUseCase registerMemberUseCase;
    private final AuthenticationManager authenticationManager;
    private final FindMemberUseCase findMemberUseCase;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDto) {
        RegisterMemberUseCase.Command command =
                new RegisterMemberUseCase.Command(requestDto.getEmail(), requestDto.getDisplayName(), requestDto.getPassword());

        MemberDTO memberDTO = registerMemberUseCase.register(command);
        RegisterResponseDTO body = MemberMapper.toRegisterResponseDTO(memberDTO);

        URI location = URI.create("/api/members/" + body.getMemberId());
        return ResponseEntity.created(location).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDto, HttpServletRequest request) {
        // 1. 사용자 자격 증명으로 Authentication 시도
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(),
                        requestDto.getPassword()
                );

        Authentication authentication = authenticationManager.authenticate(token);

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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
