package com.y11i.springcommddd.unit.posts;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostStatus;
import com.y11i.springcommddd.posts.domain.exception.ArchivedPostModificationNotAllowed;
import com.y11i.springcommddd.posts.domain.exception.PostStatusTransitionNotAllowed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class PostTest {

    // === Happy path: DRAFT → publish ===
//    @Test
//    @DisplayName("given DRAFT post, when publish, then status becomes PUBLISHED and publishedAt is set")
//    void givenDraft_whenPublish_thenStatusBecomesPublished() {
//        // Given
//        var post = Post.create(CommunityId.newId(), MemberId.newId(), "Hello", "world");
//
//        // When
//        post.publish();
//
//        // Then
//        assertThat(post.status()).isEqualTo(PostStatus.PUBLISHED);
//        assertThat(post.publishedAt()).isNotNull();
//    }

    // === Invalid: PUBLISHED → publish again ===
//    @Test
//    @DisplayName("given non-DRAFT post, when publish again, then PostStatusTransitionNotAllowed")
//    void givenArchive_whenRename_thenThrowArchiveModification() {
//        // Given
//        var post = Post.create(CommunityId.newId(), MemberId.newId(), "t", "c");
//        post.publish();
//
//        // When/Then
//        assertThatThrownBy(post::publish)
//                .isInstanceOf(PostStatusTransitionNotAllowed.class)
//                .hasMessageContaining("Only DRAFT status can be published");
//    }

    // === Invalid: ARCHIVED → rename ===
//    @Test
//    @DisplayName("given ARCHIVED post, when rename, then ArchivedPostModificationNotAllowed")
//    void givenArchive_whenRename_thenThrowArchivedPostModification() {
//        // Given
//        var post = Post.create(CommunityId.newId(), MemberId.newId(), "title", "body");
//        post.publish();
//        post.archive();
//
//        // When/Then
//        assertThatThrownBy(() -> post.rename("new Title"))
//                .isInstanceOf(ArchivedPostModificationNotAllowed.class)
//                .hasMessageContaining("rename");
//    }

    // === Invalid: ARCHIVED → rewrite ===
//    @Test
//    @DisplayName("given ARCHIVED post, when rewrite content, then ArchivedPostModificationNotAllowed")
//    void givenArchive_whenRewrite_thenThrowArchivedPostModification() {
//        // Given
//        var post = Post.create(CommunityId.newId(), MemberId.newId(), "title", "body");
//        post.publish();
//        post.archive();
//
//        // When/Then
//        assertThatThrownBy(() -> post.rewrite("new Body"))
//                .isInstanceOf(ArchivedPostModificationNotAllowed.class);
//    }

    // Happy path: rename/rewrite when not archived
//    @Test
//    @DisplayName("rename/rewrite allowed when not archived")
//    void renameAndRewriteWhenNotArchived() {
//        // Given
//        var post = Post.create(CommunityId.newId(), MemberId.newId(), "title", "body");
//        post.publish();
//
//        // When
//        post.rename("new Title");
//        post.rewrite("new Body");
//
//        // Then
//        assertThat(post.title().value()).isEqualTo("new Title");
//        assertThat(post.content().value()).isEqualTo("new Body");
//    }
}
