package com.y11i.springcommddd.comments.application.service;

import com.y11i.springcommddd.comments.application.port.in.ListCommentUseCase;
import com.y11i.springcommddd.comments.application.port.out.LoadAuthorForCommentPort;
import com.y11i.springcommddd.comments.application.port.out.LoadPostForCommentPort;
import com.y11i.springcommddd.comments.application.port.out.QueryCommentPort;
import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentStatus;
import com.y11i.springcommddd.comments.dto.internal.CommentSummaryDTO;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFound;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.exception.PostNotFound;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import com.y11i.springcommddd.votes.domain.CommentVoteRepository;
import com.y11i.springcommddd.votes.domain.MyCommentVote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ListCommentService implements ListCommentUseCase {
    private final QueryCommentPort queryCommentPort;
    private final LoadPostForCommentPort loadPostForCommentPort;
    private final LoadAuthorForCommentPort loadAuthorForCommentPort;
    private final CommentVoteRepository commentVoteRepository;

    @Override
    public PageResultDTO<CommentSummaryDTO> listComment(Query q) {
        int page = Math.max(q.page(), 0);
        int size = q.size() <= 0 ? 20 : q.size();
        Sort sort = resolveSort(q.sort());
        Pageable pageable = PageRequest.of(page, size, sort);

        // 1) 게시글 존재 여부 검증
        Post post = loadPostForCommentPort.loadById(q.postId())
                .orElseThrow(() -> new PostNotFound("Post not found: " + q.postId().stringify()));

        log.debug("Listing comments for post {} (parent={}, viewer={}, sort={}, page={}, size={})",
                post.postId().stringify(),
                q.parentCommentId() != null ? q.parentCommentId().stringify() : "ROOT",
                q.viewerId() != null ? q.viewerId().stringify() : "ANONYMOUS",
                q.sort(),
                page,
                size
        );

        // 2) 루트 vs 대댓글 분기
        Page<Comment> commentPage;
        if (q.parentCommentId() == null) {
            commentPage = queryCommentPort.findRootComments(q.postId(), pageable);
        } else {
            commentPage = queryCommentPort.findReplies(q.postId(), q.parentCommentId(), pageable);
        }

        List<Comment> comments = commentPage.getContent();
        if (comments.isEmpty()) {
            return new PageResultDTO<>(
                    List.of(),
                    commentPage.getNumber(),
                    commentPage.getSize(),
                    commentPage.getTotalElements(),
                    commentPage.getTotalPages(),
                    commentPage.hasNext()
            );
        }

        // 3) "나의 투표값" 맵 조회
        Map<CommentIdWrapper, Integer> myVotesMap = resolveMyVotesMap(q.viewerId(), comments);

        // 4) 작성자 캐시
        Map<MemberId, Member> authorCache = new HashMap<>();

        // 5) Comment → CommentSummaryDTO 매핑
        List<CommentSummaryDTO> content = comments.stream()
                .map(c -> toSummaryDTO(c, post, q.viewerId(), myVotesMap, authorCache))
                .toList();

        return new PageResultDTO<>(
                content,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.hasNext()
        );
    }

    // ----------------------------------------------------
    // 내부 유틸
    // ----------------------------------------------------

    /** 정렬 기준 변환 (지금은 "new" → createdAt DESC 정도만 지원) */
    private Sort resolveSort(String rawSort) {
        String sort = (rawSort == null || rawSort.isBlank())
                ? "new"
                : rawSort.toLowerCase();

        return switch (sort) {
            case "new", "recent" -> Sort.by(Sort.Direction.DESC, "createdAt");
            // case "top" -> Sort.by(Sort.Direction.DESC, "score"); // 나중에 필요하면 추가
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    /**
     * 현재 로그인 유저의 "댓글 투표값"을 한 번에 조회하여 Map으로 반환.
     */
    private Map<CommentIdWrapper, Integer> resolveMyVotesMap(MemberId viewerId, List<Comment> comments) {
        if (viewerId == null || comments.isEmpty()) return Map.of();

        List<CommentIdWrapper> ids = comments.stream()
                .map(c -> new CommentIdWrapper(c.commentId()))
                .toList();

        List<MyCommentVote> votes = commentVoteRepository.findMyVotesByCommentIds(
                viewerId,
                comments.stream().map(Comment::commentId).toList()
        );

        return votes.stream()
                .collect(Collectors.toMap(
                        v -> new CommentIdWrapper(v.id()),
                        MyCommentVote::value
                ));
    }

    /**
     * Comment → CommentSummaryDTO 매핑.
     */
    private CommentSummaryDTO toSummaryDTO(
            Comment c,
            Post post,
            MemberId viewerId,
            Map<CommentIdWrapper, Integer> myVotesMap,
            Map<MemberId, Member> authorCache
    ) {
        Member author = resolveAuthor(c.authorId(), authorCache);

        boolean mine = viewerId != null && viewerId.equals(author.memberId());
        boolean deleted = (c.status() == CommentStatus.DELETED);
        String body = deleted ? null : c.body().value();

        Integer myVote = myVotesMap.getOrDefault(new CommentIdWrapper(c.commentId()), null);

        return CommentSummaryDTO.builder()
                .commentId(c.commentId().stringify())
                .postId(c.postId().stringify())
                .parentCommentId(c.parentId() != null ? c.parentId().stringify() : null)
                .depth(c.depth())
                .authorId(author.memberId().stringify())
                .authorDisplayName(author.displayName().value())
                .mine(mine)
                .deleted(deleted)
                .edited(c.edited()) // 도메인에 추가한 플래그 사용
                .body(body)
                .upCount(c.upCount())
                .downCount(c.downCount())
                .score(c.score())
                .myVote(myVote)
                .createdAt(c.createdAt())
                .updatedAt(c.updatedAt())
                .children(List.of()) // lazy loading → 항상 비움
                .build();
    }

    private Member resolveAuthor(MemberId authorId, Map<MemberId, Member> cache) {
        Member cached = cache.get(authorId);
        if (cached != null) return cached;

        Member loaded = loadAuthorForCommentPort.loadById(authorId)
                .orElseThrow(() -> new MemberNotFound("Member not found: " + authorId.stringify()));
        cache.put(authorId, loaded);
        return loaded;
    }

    /**
     * CommentId 를 Map key에서 쓰기 위한 래퍼 (equals/hashCode 명시).
     * record CommentId 자체에 equals/hashCode가 이미 구현되어 있다면 이 래퍼는 생략 가능.
     */
    private record CommentIdWrapper(com.y11i.springcommddd.comments.domain.CommentId id) {}
}
