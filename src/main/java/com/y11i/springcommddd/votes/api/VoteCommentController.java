package com.y11i.springcommddd.votes.api;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.votes.application.port.in.VoteCommentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Validated
public class VoteCommentController {
    private final VoteCommentUseCase voteCommentUseCase;

    @PostMapping(path = "/{commentId}/vote/up")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void voteUp(
            @AuthenticatedMember MemberId voterId,
            @PathVariable String commentId
    ){
        voteCommentUseCase.upvote(CommentId.objectify(commentId), voterId);
    }

    @PostMapping(path = "/{commentId}/vote/down")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void voteDown(
            @AuthenticatedMember MemberId voterId,
            @PathVariable String commentId
    ){
        voteCommentUseCase.downvote(CommentId.objectify(commentId), voterId);
    }

    @DeleteMapping(path = "/{commentId}/vote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelVote(
            @AuthenticatedMember MemberId voterId,
            @PathVariable String commentId
    ){
        voteCommentUseCase.cancelVote(CommentId.objectify(commentId), voterId);
    }
}
