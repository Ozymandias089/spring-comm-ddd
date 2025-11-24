package com.y11i.springcommddd.communities.api;

import com.y11i.springcommddd.communities.application.port.in.CommunitySettingsUseCase;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.dto.request.ChangeDescriptionRequestDTO;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommunitySettingsController {
    private final CommunitySettingsUseCase communitySettingsUseCase;

    @PatchMapping(path = "/c/{nameKey}/description", consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeDescription(
            @AuthenticatedMember MemberId actorId,
            @Pattern(regexp = "^[a-z0-9_]{3,32}$") @PathVariable("nameKey") String nameKey,
            @Valid @RequestBody ChangeDescriptionRequestDTO dto
    ) {
        var communityId = communitySettingsUseCase.redescribe(new CommunitySettingsUseCase.RedescribeCommand(actorId, new CommunityNameKey(nameKey), dto.description()));
        log.info("Change description for community with id {}", communityId.stringify());
    }
}
