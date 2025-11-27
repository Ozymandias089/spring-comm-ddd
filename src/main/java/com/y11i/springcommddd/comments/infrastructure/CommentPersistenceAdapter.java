package com.y11i.springcommddd.comments.infrastructure;

import com.y11i.springcommddd.comments.application.port.out.*;
import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.comments.domain.CommentRepository;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberRepository;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements LoadCommentPort, SaveCommentPort, LoadPostForCommentPort, LoadAuthorForCommentPort, QueryCommentPort {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Override
    public Optional<Comment> loadById(CommentId id) {
        return commentRepository.findById(id);
    }

    @Override
    public Optional<Post> loadById(PostId postId) {
        return postRepository.findById(postId);
    }

    @Override
    @Transactional
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    /**
     * 특정 게시글의 루트 댓글(부모 없음) 목록 조회.
     *
     * @param postId 게시글ID
     * @param pageable 페이지 객체
     */
    @Override
    public Page<Comment> findRootComments(PostId postId, Pageable pageable) {
        return commentRepository.findRootsByPostId(postId, pageable);
    }

    /**
     * 특정 부모 댓글의 자식 댓글 목록 조회.
     *
     * <p>lazy loading 이므로, 페이징/정렬을 허용한다.</p>
     *
     * @param postId 게시글 ID
     * @param parentId 부모 ID
     * @param pageable 페이징 객체
     */
    @Override
    public Page<Comment> findReplies(PostId postId, CommentId parentId, Pageable pageable) {
        // 1) 부모 기준 전체 자식 댓글 로드
        List<Comment> allReplies = commentRepository.findByParentId(parentId);

        if (allReplies.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2) 인메모리 페이징 계산
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int total = allReplies.size();

        int fromIndex = page * size;
        if (fromIndex >= total) {
            // 요청한 페이지가 범위를 벗어나면 빈 페이지 반환
            return new PageImpl<>(List.of(), pageable, total);
        }

        int toIndex = Math.min(fromIndex + size, total);
        List<Comment> pageContent = allReplies.subList(fromIndex, toIndex);

        // 3) PageImpl로 래핑
        return new PageImpl<>(pageContent, pageable, total);
    }

    @Override
    public Optional<Member> loadById(MemberId memberId) {
        return memberRepository.findById(memberId);
    }
}
