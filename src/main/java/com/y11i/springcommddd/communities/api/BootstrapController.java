package com.y11i.springcommddd.communities.api;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/community")
@RequiredArgsConstructor
public class BootstrapController {
    private final CommunityRepository communityRepository;

    @PostMapping("/bs")
    @Transactional
    public String bootstrap() {
        Community community = Community.create("DemoCommunity");
        communityRepository.save(community);
        return community.communityId().stringify();
    }
}
