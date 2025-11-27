package com.y11i.springcommddd.comments.application.service;

import com.y11i.springcommddd.comments.application.port.in.CreateCommentUseCase;
import com.y11i.springcommddd.comments.application.port.out.LoadCommentPort;
import com.y11i.springcommddd.comments.application.port.out.LoadPostForCommentPort;
import com.y11i.springcommddd.comments.application.port.out.SaveCommentPort;
import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.comments.domain.exception.CommentNotFound;
import com.y11i.springcommddd.posts.application.port.out.CheckCommunityBanPort;
import com.y11i.springcommddd.posts.application.port.out.SavePostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.exception.PostNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CreateCommentService implements CreateCommentUseCase {
    private final LoadPostForCommentPort loadPostForCommentPort;
    private final LoadCommentPort loadCommentPort;
    private final SaveCommentPort saveCommentPort;
    private final CheckCommunityBanPort checkCommunityBanPort;
    private final SavePostPort savePostPort;

    /**
     * 새 댓글을 생성한다.
     *
     * @param cmd 작성 정보
     * @return 생성된 댓글 ID
     */
    @Override
    @Transactional
    public CommentId create(CreateCommentCommand cmd) {
        // 1. 게시글 로드 + 댓글 가능여부 검증
        Post post = loadPostForCommentPort.loadById(cmd.postId()).orElseThrow(() -> new PostNotFound("Post not found"));
        // 2. 게시글 상태 기반 댓글 가능여부 검증
        post.ensureCommentable();
        // 3. 커뮤니티 밴 여부 검증
        checkCommunityBanPort.ensureNotBanned(post.communityId(), cmd.authorId());
        // 4. 부모 댓글이 있으면 로드
        Comment parent = null;
        if (cmd.parentId() != null) {
            parent = loadCommentPort.loadById(cmd.parentId()).orElseThrow(() -> new CommentNotFound("Parent comment not found"));
        }
        // 5. 도메인 생성(루트 vs 대댓글)
        Comment comment = (parent == null)
                ? Comment.createRoot(cmd.postId(), cmd.authorId(), cmd.body())
                : Comment.replyTo(cmd.postId(), cmd.authorId(), parent, cmd.body());
        // 6. 댓글 저장
        Comment saved = saveCommentPort.save(comment);
        // 7. Post의 commentCount 갱신
        post.commentCountIncrement();
        savePostPort.save(post);

        log.info("Created comment {} on post {} by author {} (parent={})",
                saved.commentId().stringify(),
                saved.postId().stringify(),
                saved.authorId().stringify(),
                saved.parentId() != null ? saved.parentId().stringify() : "ROOT");

        return saved.commentId();
    }
}
