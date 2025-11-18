package com.y11i.springcommddd.votes.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.votes.application.port.in.VotePostUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
public class VotePostController {

    private final VotePostUseCase votePostUseCase;

    // ----------------------------------------------------------------------
    // 추천 (UPVOTE)
    // ----------------------------------------------------------------------

    /**
     * 게시글에 대한 추천(Upvote)을 토글한다.
     *
     * <p>규칙:</p>
     * <ul>
     *     <li>현재 투표 없음(0)  → +1</li>
     *     <li>현재 비추천(-1)  → +1</li>
     *     <li>현재 추천(+1)    → 0 (취소)</li>
     * </ul>
     */
    @PostMapping(path = "/{postId}/vote/up")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upvote(
            @AuthenticatedMember MemberId voterId,
            @PathVariable String postId
    ) {
        votePostUseCase.upvote(PostId.objectify(postId), voterId);
    }

    // ----------------------------------------------------------------------
    // 비추천 (DOWNVOTE)
    // ----------------------------------------------------------------------

    /**
     * 게시글에 대한 비추천(Downvote)을 토글한다.
     *
     * <p>규칙:</p>
     * <ul>
     *     <li>현재 투표 없음(0)  → -1</li>
     *     <li>현재 추천(+1)    → -1</li>
     *     <li>현재 비추천(-1)  → 0 (취소)</li>
     * </ul>
     */
    @PostMapping(path = "/{postId}/vote/down")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void downvote(
            @AuthenticatedMember MemberId voterId,
            @PathVariable String postId
    ) {
        votePostUseCase.downvote(PostId.objectify(postId), voterId);
    }

    // ----------------------------------------------------------------------
    // 투표 취소 (CANCEL)
    // ----------------------------------------------------------------------

    /**
     * 게시글에 대한 나의 투표를 명시적으로 취소한다.
     *
     * <p>
     * 존재하는 투표가 있으면 삭제되고, 없으면 아무 동작도 하지 않는다.
     * </p>
     */
    @DeleteMapping(path = "/{postId}/vote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelVote(
            @AuthenticatedMember MemberId voterId,
            @PathVariable String postId
    ) {
        votePostUseCase.cancelVote(PostId.objectify(postId), voterId);
    }
}
