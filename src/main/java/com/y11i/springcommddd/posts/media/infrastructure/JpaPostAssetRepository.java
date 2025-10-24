package com.y11i.springcommddd.posts.media.infrastructure;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import com.y11i.springcommddd.posts.media.domain.PostAssetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaPostAssetRepository extends JpaRepository<PostAsset, PostAssetId> {
    List<PostAsset> findByPostIdOrderByDisplayOrder(PostId postId);

    Optional<PostAsset> findFirstByPostIdOrderByDisplayOrderAsc(PostId postId);
}
