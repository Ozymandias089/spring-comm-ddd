package com.y11i.springcommddd.comments.application.port.out;

import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.posts.domain.PostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QueryCommentPort {
    /**
     * 특정 게시글의 루트 댓글(부모 없음) 목록 조회.
     */
    Page<Comment> findRootComments(PostId postId, Pageable pageable);

    /**
     * 특정 부모 댓글의 자식 댓글 목록 조회.
     *
     * <p>lazy loading 이므로, 페이징/정렬을 허용한다.</p>
     */
    Page<Comment> findReplies(PostId postId, CommentId parentId, Pageable pageable);
}
