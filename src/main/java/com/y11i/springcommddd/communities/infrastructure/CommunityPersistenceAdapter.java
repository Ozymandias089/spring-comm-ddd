package com.y11i.springcommddd.communities.infrastructure;

import com.y11i.springcommddd.communities.application.port.out.LoadCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.SaveCommunityPort;
import com.y11i.springcommddd.communities.domain.*;
import com.y11i.springcommddd.communities.domain.exception.InvalidCommunityNameKey;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            return communityRepository.save(community);
        } catch (DataIntegrityViolationException ex) {
            // name_key 유니크 제약 위반인지 검사 (에러 메시지 / 원인에 따라 분기)
            throw new InvalidCommunityNameKey("Community name key already exists");
        }
    }
}
