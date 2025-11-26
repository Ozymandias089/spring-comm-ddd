package com.y11i.springcommddd.communities.bans.api;

import com.y11i.springcommddd.communities.bans.application.port.in.ManageBanUseCase;
import com.y11i.springcommddd.communities.bans.dto.request.BanMemberRequestDTO;
import com.y11i.springcommddd.communities.bans.dto.response.CommunityBanCreateResponseDTO;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/c/{nameKey}/bans")
@RequiredArgsConstructor
public class ManageBanController {
    private final ManageBanUseCase manageBanUseCase;

    /**
     * 멤버 밴 추가
     * POST /api/c/{nameKey}/bans
     * <p>
     * body:
     * {
     *   "targetMemberId": "uuid-string",
     *   "durationSeconds": 604800,
     *   "reason": "스팸 링크 반복 게시"
     * }
     * <p>
     * durationSeconds 가 null이면 영구 밴
     */
    @PostMapping
    public ResponseEntity<CommunityBanCreateResponseDTO> banMember(
            @PathVariable("nameKey") String nameKey,
            @RequestBody BanMemberRequestDTO request,
            @AuthenticatedMember MemberId actorId
    ) {
        Duration duration = null;
        if (request.durationSeconds() != null) {
            duration = Duration.ofSeconds(request.durationSeconds());
        }

        var cmd = new ManageBanUseCase.BanMemberCommand(
                actorId,
                new CommunityNameKey(nameKey),
                MemberId.objectify(request.targetMemberId()),
                duration,
                request.reason()
        );

        var banId = manageBanUseCase.banMember(cmd);

        var response = CommunityBanCreateResponseDTO.builder()
                .banId(banId.stringify())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 멤버 밴 해제
     * DELETE /api/c/{nameKey}/bans/{memberId}
     */
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> unbanMember(
            @PathVariable("nameKey") String nameKey,
            @PathVariable("memberId") String memberId,
            @AuthenticatedMember MemberId actorId
    ) {
        var cmd = new ManageBanUseCase.UnbanMemberCommand(
                actorId,
                new CommunityNameKey(nameKey),
                MemberId.objectify(memberId)
        );

        manageBanUseCase.unbanMember(cmd);

        return ResponseEntity.noContent().build();
    }
}
