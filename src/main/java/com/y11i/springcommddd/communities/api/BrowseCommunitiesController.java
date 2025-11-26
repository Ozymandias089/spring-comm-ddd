package com.y11i.springcommddd.communities.api;

import com.y11i.springcommddd.communities.application.port.in.BrowseCommunitiesUseCase;
import com.y11i.springcommddd.communities.domain.CommunityStatus;
import com.y11i.springcommddd.communities.dto.response.CommunityPageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BrowseCommunitiesController {
    private final BrowseCommunitiesUseCase browseCommunitiesUseCase;

    @GetMapping("/communities")
    public CommunityPageResponseDTO listCommunities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ACTIVE") String status,
            @RequestParam(name = "q", required = false) String keyword
    ) {
        return browseCommunitiesUseCase.browseCommunities(
                new BrowseCommunitiesUseCase.BrowseCommunitiesQuery(
                        page,
                        size,
                        CommunityStatus.valueOf(status.toUpperCase()),
                        keyword
                )
        );
    }
}
