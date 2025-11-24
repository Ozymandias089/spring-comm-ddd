package com.y11i.springcommddd.communities.api;

import com.y11i.springcommddd.communities.application.port.in.GetCommunityDetailsUseCase;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.dto.response.CommunityDetailsResponseDTO;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GetCommunityDetailsController {
    private final GetCommunityDetailsUseCase getCommunityDetailsUseCase;

    @GetMapping(path = "/c/{nameKey}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public CommunityDetailsResponseDTO getCommunityDetails(@PathVariable("nameKey") @Pattern(regexp = "^[a-z0-9_]{3,32}$") String nameKey) {
        log.debug("Entered GetCommunityDetailsController getCommunityDetails for {}", nameKey);
        return getCommunityDetailsUseCase.getCommunityDetails(new CommunityNameKey(nameKey));
    }
}
