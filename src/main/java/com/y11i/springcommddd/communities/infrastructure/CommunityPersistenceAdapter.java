package com.y11i.springcommddd.communities.infrastructure;

import com.y11i.springcommddd.communities.application.port.out.BrowseCommunitiesPort;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.SaveCommunityPort;
import com.y11i.springcommddd.communities.domain.*;
import com.y11i.springcommddd.communities.domain.exception.InvalidCommunityNameKey;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPersistenceAdapter implements SaveCommunityPort, LoadCommunityPort, BrowseCommunitiesPort {
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

    /**
     * 주어진 상태의 커뮤니티를 페이지 단위로 조회합니다.
     *
     * @param status 필터링할 상태
     * @param page   0-based 페이지 번호
     * @param size   페이지 크기
     * @return 해당 페이지의 커뮤니티 목록
     */
    @Override
    public List<Community> loadByStatus(CommunityStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("communityNameKey.value").ascending());
        return communityRepository.findByStatus(status, pageable).getContent();
    }

    /**
     * 주어진 상태의 커뮤니티 총 개수를 반환합니다.
     *
     * @param status 필터링할 상태
     * @return 전체 개수
     */
    @Override
    public long countByStatus(CommunityStatus status) {
        return communityRepository.countByStatus(status);
    }
}
