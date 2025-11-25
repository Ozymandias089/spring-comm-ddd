package com.y11i.springcommddd.communities.api;

import com.y11i.springcommddd.communities.application.port.in.CommunityModeratorUseCase;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.dto.response.CommunityModeratorsResponseDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityPageResponseDTO;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommunityModeratorController {
    private final CommunityModeratorUseCase communityModeratorUseCase;

    @GetMapping("/me/mod-communities")
    @ResponseStatus(HttpStatus.OK)
    public CommunityPageResponseDTO listMyModeratedCommunities(
            @AuthenticatedMember MemberId actorId
    ) {
        return communityModeratorUseCase.listMyModeratedCommunities(
                new CommunityModeratorUseCase.ListMyModeratedCommunitiesQuery(actorId, 0, 0)
        );
    }

    @GetMapping(path = "/c/{nameKey}/moderators", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public CommunityModeratorsResponseDTO listModerators(
            @PathVariable("nameKey")
            @Pattern(regexp = "^[a-z0-9_]{3,32}$")
            String nameKey
    ) {
        return communityModeratorUseCase.listModerators(new CommunityNameKey(nameKey));
    }

    @PostMapping(path = "/c/{nameKey}/moderators/{targetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grantModerator(
            @AuthenticatedMember MemberId actorId,
            @PathVariable("nameKey") @Pattern(regexp = "^[a-z0-9_]{3,32}$") String nameKey,
            @PathVariable("targetId") String targetId
    ) {
        communityModeratorUseCase.grantModerator(
                new CommunityModeratorUseCase.GrantModeratorCommand(
                        actorId,
                        new CommunityNameKey(nameKey),
                        MemberId.objectify(targetId)
                )
        );
        log.info("Moderator has been granted for target Member {}", targetId);
    }

    @DeleteMapping(path = "/c/{nameKey}/moderators/{targetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeModerator(
            @AuthenticatedMember MemberId memberId,
            @PathVariable("nameKey") @Pattern(regexp = "^[a-z0-9_]{3,32}$") String nameKey,
            @PathVariable("targetId") String targetId
    ) {
        communityModeratorUseCase.revokeModerator(
                new CommunityModeratorUseCase.RevokeModeratorCommand(
                        memberId,
                        new CommunityNameKey(nameKey),
                        MemberId.objectify(targetId)
                )
        );
        log.info("Moderator has been revoked for target Member {}", targetId);
    }
}
