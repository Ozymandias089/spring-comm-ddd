package com.y11i.springcommddd.posts.media.infrastructure;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import com.y11i.springcommddd.posts.media.domain.PostAssetId;
import com.y11i.springcommddd.posts.media.domain.ProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaPostAssetRepository extends JpaRepository<PostAsset, PostAssetId> {
    List<PostAsset> findByPostIdOrderByDisplayOrder(PostId postId);

    Optional<PostAsset> findFirstByPostIdOrderByDisplayOrderAsc(PostId postId);

    long countByPostId(PostId postId);
    boolean existsByPostIdAndDisplayOrder(PostId postId, int displayOrder);

    @Query("select max(a.displayOrder) from PostAsset a where a.postId = :postId")
    Optional<Integer> findMaxDisplayOrder(@Param("postId") PostId postId);

    @Query("select a from PostAsset a where a.postId = :postId and a.processingStatus = :status order by a.displayOrder asc")
    List<PostAsset> findByPostIdAndStatus(@Param("postId") PostId postId, @Param("status") ProcessingStatus status);

    @Query("select a from PostAsset a where a.postId = :postId")
    Page<PostAsset> findPageByPostId(@Param("postId") PostId postId, Pageable pageable);

    // variants(ElementCollection) 조인
    @Query("""
      select case when count(v)>0 then true else false end
      from PostAsset a join a.variants v
      where a.postId = :postId and v.name = :variantName
    """)
    boolean existsVariantByName(@Param("postId") PostId postId, @Param("variantName") String variantName);

    @Query("""
      select a from PostAsset a join a.variants v
      where a.postId = :postId and v.name = :variantName
      order by a.displayOrder asc
    """)
    List<PostAsset> findByPostIdAndVariantName(@Param("postId") PostId postId, @Param("variantName") String variantName);

    // 재정렬 벌크
    @Modifying
    @Query("update PostAsset a set a.displayOrder = a.displayOrder + 1 where a.postId = :postId and a.displayOrder >= :fromOrder")
    int shiftRightFromOrder(@Param("postId") PostId postId, @Param("fromOrder") int fromOrder);

    @Modifying
    @Query("update PostAsset a set a.displayOrder = a.displayOrder - 1 where a.postId = :postId and a.displayOrder > :removedOrder")
    int shiftLeftAfterOrder(@Param("postId") PostId postId, @Param("removedOrder") int removedOrder);

    @Modifying
    int deleteByPostId(PostId postId);
}
