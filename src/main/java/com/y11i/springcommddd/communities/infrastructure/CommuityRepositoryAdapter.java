package com.y11i.springcommddd.communities.infrastructure;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.domain.CommunityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class CommuityRepositoryAdapter implements CommunityRepository {

    private final JpaCommunityRepository jpaCommunityRepository;

    public CommuityRepositoryAdapter(JpaCommunityRepository jpaCommunityRepository) {
        this.jpaCommunityRepository = jpaCommunityRepository;
    }

    /**
     * @param c Community to save
     * @return Returns saved Community
     */
    @Override
    @Transactional
    public Community save(Community c) {
        return jpaCommunityRepository.save(c);
    }

    /**
     * @param id Id to find Communities by
     * @return Returns Community matching parameter
     */
    @Override
    @Transactional
    public Optional<Community> findById(CommunityId id) {
        return jpaCommunityRepository.findById(id);
    }

    /**
     * @param key A nameKey to find communities by
     * @return A community matching parameter
     */
    @Override
    @Transactional
    public Optional<Community> findByCommunityNameKey(CommunityNameKey key) {
        return jpaCommunityRepository.findByCommunityNameKey(key);
    }

    /**
     * @return All the Communities as list
     */
    @Override
    @Transactional
    public List<Community> findAll() {
        return jpaCommunityRepository.findAll();
    }
}
