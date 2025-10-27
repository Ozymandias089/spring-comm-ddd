package com.y11i.springcommddd.votes.infrastructure;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.votes.domain.PostVote;
import com.y11i.springcommddd.votes.domain.PostVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA 기반의 게시글 투표 리포지토리.
 * <p>
 * {@link PostVote} 엔티티를 데이터베이스에 영속화하며,
 * 게시글({@link PostId})과 투표자({@link MemberId})를 복합 키로 조회할 수 있는 기능을 제공합니다.
 * </p>
 *
 * <p><b>역할:</b></p>
 * <ul>
 *     <li>Spring Data JPA가 자동으로 구현체를 생성</li>
 *     <li>쿼리 메서드 파생(query derivation)을 통해 투표 단건 조회 제공</li>
 *     <li>{@link com.y11i.springcommddd.votes.infrastructure.PostVoteRepositoryAdapter}에서 사용됨</li>
 * </ul>
 *
 * @see JpaRepository
 * @see PostVote
 * @see PostVoteId
 * @see PostVoteRepositoryAdapter
 */
public interface JpaPostVoteRepository extends JpaRepository<PostVote, PostVoteId> {

    /**
     * 게시글 ID와 투표자 ID를 기준으로 투표를 조회합니다.
     *
     * @param postId  게시글 식별자
     * @param voterId 투표자 식별자
     * @return 일치하는 {@link PostVote}가 존재하면 반환, 없으면 비어 있음
     */
    Optional<PostVote> findByPostIdAndVoterId(PostId postId, MemberId voterId);

    /**
     * 현재 사용자(voterId)의 여러 게시글(postIds)에 대한 투표값을 배치로 조회.
     * - 성능/호환성을 위해 임베디드의 내부 UUID로 비교합니다.
     */
    @Query("""
        select v.postId.id as id, v.value as value
        from PostVote v
        where v.voterId.id = :voterId
          and v.postId.id in :postIds
    """)
    List<PostVoteEntry> findValuesByVoterAndPostIds(@Param("voterId") UUID voterId,
                                                    @Param("postIds") Collection<UUID> postIds);

    interface PostVoteEntry {
        UUID getId();   // post_id
        int getValue(); // -1 or +1
    }
}
