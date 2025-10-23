package com.y11i.springcommddd.communities.infrastructure;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaCommunityRepository extends JpaRepository<Community, CommunityId> {
    Optional<Community> findByCommunityNameKey(CommunityNameKey key);
}
