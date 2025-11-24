package com.y11i.springcommddd.communities.api;

import com.y11i.springcommddd.communities.application.port.in.ActivateCommunityUseCase;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/communities")
public class CommunityAdminController {
    private final ActivateCommunityUseCase activateCommunityUseCase;

    @PatchMapping("/{communityId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateCommunity(@AuthenticatedMember MemberId memberId, @PathVariable String communityId) {
        CommunityId cid = activateCommunityUseCase.activateCommunity(
                new ActivateCommunityUseCase.ActivateCommunityCommand(
                        memberId,
                        CommunityId.objectify(communityId)
                )
        );

        log.debug("Activate community with id: {}", cid);
    }
}
