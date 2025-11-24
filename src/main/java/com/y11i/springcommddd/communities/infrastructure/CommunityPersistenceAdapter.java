package com.y11i.springcommddd.communities.infrastructure;

import com.y11i.springcommddd.communities.application.port.out.LoadCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.SaveCommunityPort;
import com.y11i.springcommddd.communities.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPersistenceAdapter implements SaveCommunityPort, LoadCommunityPort {
    private final CommunityRepository communityRepository;

    @Override
    public Optional<Community> loadById(CommunityId communityId) {
        return communityRepository.findById(communityId);
    }

    @Override
    public Optional<Community> loadByNameKey(CommunityNameKey communityNameKey) {
        return communityRepository.findByCommunityNameKey(communityNameKey);
    }

    @Override
    public Optional<Community> loadByName(CommunityName name) {
        return communityRepository.findByCommunityName(name);
    }

    @Override
    @Transactional
    public Community save(Community community) {
        return communityRepository.save(community);
    }
}
