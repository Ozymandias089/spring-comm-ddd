package com.y11i.springcommddd.comments.infrastructure;

import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.comments.domain.CommentRepository;
import com.y11i.springcommddd.posts.domain.PostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * {@link CommentRepository}의 인프라스트럭처 계층 구현체.
 * <p>
 * 이 클래스는 도메인 계층의 {@link CommentRepository}를 JPA 기반 구현체로
 * 연결(어댑팅)하여 실제 데이터베이스 접근을 수행합니다.
 * <br>
 * 내부적으로 {@link JpaCommentRepository}를 위임받아 사용합니다.
 * </p>
 *
 * <p><b>특징:</b></p>
 * <ul>
 *     <li>도메인 계층에서는 {@link CommentRepository} 인터페이스만 의존</li>
 *     <li>JPA 구현 세부사항은 인프라 계층에 격리됨</li>
 *     <li>{@code @Transactional(readOnly = true)} 기본 적용</li>
 *     <li>쓰기 작업(저장)은 별도 트랜잭션으로 처리</li>
 * </ul>
 *
 * @author y11
 */
@Repository
@Transactional(readOnly = true)
public class CommentRepositoryAdapter implements CommentRepository {

    private final JpaCommentRepository jpa;

    /** {@inheritDoc} */
    public CommentRepositoryAdapter(JpaCommentRepository jpa) {
        this.jpa = jpa;
    }

    /** {@inheritDoc} */
    @Override @Transactional
    public Comment save(Comment c) { return jpa.save(c); }

    /** {@inheritDoc} */
    @Override
    public Optional<Comment> findById(CommentId id) { return jpa.findById(id); }

    /** {@inheritDoc} */
    @Override
    public Page<Comment> findRootsByPostId(PostId postId, Pageable pageable) {
        return jpa.findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(postId, pageable);
    }

    /** {@inheritDoc} */
    @Override
    public List<Comment> findByParentId(CommentId parentId) {
        return jpa.findByParentIdOrderByCreatedAtAsc(parentId);
    }

    /** {@inheritDoc} */
    @Override
    public Page<Comment> findByPostId(PostId postId, Pageable pageable) {
        return jpa.findByPostId(postId, pageable);
    }
}
