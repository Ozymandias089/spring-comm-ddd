package com.y11i.springcommddd.communities.api;

import com.y11i.springcommddd.communities.application.port.in.ManageCommunityStatusUseCase;
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
    private final ManageCommunityStatusUseCase manageCommunityStatusUseCase;

    @PatchMapping("/{communityId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateCommunity(@AuthenticatedMember MemberId memberId, @PathVariable String communityId) {
        CommunityId cid = manageCommunityStatusUseCase.activateCommunity(
                new ManageCommunityStatusUseCase.ActivateCommunityCommand(
                        memberId,
                        CommunityId.objectify(communityId)
                )
        );

        log.info("Activate community with id: {}", cid);
    }

    @PatchMapping("/{communityId}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveCommunity(@PathVariable String communityId, @AuthenticatedMember MemberId actorId) {
        CommunityId cid = manageCommunityStatusUseCase.archiveCommunity(
                new ManageCommunityStatusUseCase.ArchiveCommunityCommand(actorId, CommunityId.objectify(communityId))
        );
        log.info("Archived community with id: {}", cid);
    }

    @PatchMapping("/{communityId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreCommunity(@PathVariable String communityId, @AuthenticatedMember MemberId actorId) {
        CommunityId cid = manageCommunityStatusUseCase.restoreCommunity(
                new ManageCommunityStatusUseCase.RestoreCommunityCommand(
                        actorId,
                        CommunityId.objectify(communityId)
                )
        );
        log.info("Restored community with id: {}", cid);
    }
}
