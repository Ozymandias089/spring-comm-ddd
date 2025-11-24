package com.y11i.springcommddd.communities.api;

import com.y11i.springcommddd.communities.application.port.in.CreateCommunityUseCase;
import com.y11i.springcommddd.communities.dto.request.CommunityCreateRequestDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityCreateResponseDTO;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/communities")
public class CommunityCreationController {
    private final CreateCommunityUseCase createCommunityUseCase;

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityCreateResponseDTO createCommunity(
            @AuthenticatedMember MemberId actorId,
            @Valid @RequestBody CommunityCreateRequestDTO requestDTO
    ){
        return createCommunityUseCase.createCommunity(
                new CreateCommunityUseCase.CreateCommunityCommand(
                        actorId,
                        requestDTO.name(),
                        requestDTO.description(),
                        requestDTO.rules()
                )
        );
    }
}
