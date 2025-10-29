package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.application.port.in.RegisterMemberUseCase;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import com.y11i.springcommddd.iam.dto.request.RegisterRequestDTO;
import com.y11i.springcommddd.iam.dto.response.RegisterResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class MemberController {
    private final RegisterMemberUseCase registerMemberUseCase;

    @PostMapping("register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDto) {
        RegisterMemberUseCase.Command command =
                new RegisterMemberUseCase.Command(requestDto.getEmail(), requestDto.getDisplayName(), requestDto.getPassword());

        MemberDTO memberDTO = registerMemberUseCase.register(command);
        RegisterResponseDTO body = MemberMapper.toRegisterResponseDTO(memberDTO);

        URI location = URI.create("/api/members/" + body.getMemberId());
        return ResponseEntity.created(location).body(body);
    }
}
