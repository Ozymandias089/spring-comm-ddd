package com.y11i.springcommddd.communities.domain;

import java.util.List;
import java.util.Optional;

public interface CommunityRepository {
    Community save(Community c);
    Optional<Community> findById(CommunityId id);
    Optional<Community> findByCommunityNameKey(CommunityNameKey key);
    List<Community> findAll();
}
