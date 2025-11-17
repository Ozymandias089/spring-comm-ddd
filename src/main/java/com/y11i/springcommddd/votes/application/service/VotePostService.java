package com.y11i.springcommddd.votes.application.service;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.out.LoadPostPort;
import com.y11i.springcommddd.posts.application.port.out.SavePostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.exception.PostNotFound;
import com.y11i.springcommddd.votes.application.port.in.VotePostUseCase;
import com.y11i.springcommddd.votes.domain.PostVote;
import com.y11i.springcommddd.votes.domain.PostVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VotePostService implements VotePostUseCase {
    private final PostVoteRepository postVoteRepository;
    private final LoadPostPort loadPostPort;
    private final SavePostPort savePostPort;

    // ----------------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------------
    /**
     * 게시글에 추천(+1)을 표시한다.
     *
     * <p>기존 상태에 따른 결과</p>
     * <ul>
     *   <li>없음 → +1 생성</li>
     *   <li>-1 → +1 로 변경</li>
     *   <li>+1 → 취소(0)</li>
     * </ul>
     *  @param postId  게시글 ID
     *
     * @param voterId 투표자 ID
     */
    @Override
    @Transactional
    public void upvote(PostId postId, MemberId voterId) {
        applyVote(postId, voterId, +1);
    }

    /**
     * 게시글에 비추천(-1)을 표시한다.
     *
     * <p>기존 상태에 따른 결과</p>
     * <ul>
     *   <li>없음 → -1 생성</li>
     *   <li>+1 → -1로 변경</li>
     *   <li>-1 → 취소(0)</li>
     * </ul>
     *  @param postId  게시글 ID
     *
     * @param voterId 투표자 ID
     */
    @Override
    @Transactional
    public void downvote(PostId postId, MemberId voterId) {
        applyVote(postId, voterId, -1);
    }

    /**
     * 게시글에 대한 나의 투표를 취소한다.
     *
     * <p>
     * 존재하면 삭제되고, 없으면 아무 동작도 하지 않는다.
     * </p>
     *
     * @param postId  게시글 ID
     * @param voterId 투표자 ID
     */
    @Override
    @Transactional
    public void cancelVote(PostId postId, MemberId voterId) {
        applyVote(postId, voterId, 0);
    }

    // ----------------------------------------------------------------------
    // 내부 헬퍼
    // ----------------------------------------------------------------------
    /**
     * 투표를 적용한다.
     *
     * @param postId   대상 게시글 ID
     * @param voterId  투표자 ID
     * @param desired  의도한 방향: +1(up), -1(down), 0(cancel)
     */
    private void applyVote(PostId postId, MemberId voterId, int desired) {
        if (desired != -1 && desired != 0 && desired != 1) {
            throw new IllegalArgumentException("desired vote must be -1, 0, or 1");
        }

        // 1. 게시글 로드 (없으면 예외)
        Post post = loadPostPort.loadById(postId)
                .orElseThrow(() -> new PostNotFound(postId.stringify()));

        // Post의 status가 PUBLISHED인 경우에만 투표를 할 수 있다.
        post.ensureVotable();

        // 2. 기존 투표 조회
        Optional<PostVote> existingOpt = postVoteRepository.findByPostIdAndVoterId(postId, voterId);
        int oldValue = existingOpt.map(PostVote::value).orElse(0);

        // 3. 새 값 결정 (toggle 규칙 적용)
        int newValue = decideNewValue(oldValue, desired);

        // 4. PostVote 애그리게잇 처리 (생성/변경/삭제)
        if (oldValue == 0 && newValue != 0) {
            // 새 투표 생성
            PostVote vote = PostVote.cast(postId, voterId, newValue);
            postVoteRepository.save(vote);
        } else if (oldValue != 0 && newValue == 0) {
            // 투표 취소(삭제)
            existingOpt.ifPresent(postVoteRepository::delete);
        } else if (oldValue != 0 && newValue != 0) {
            // 방향 변경 (+1 ↔ -1)
            PostVote existing = existingOpt.orElseThrow(); // oldValue != 0이면 반드시 존재
            existing.setValue(newValue);
            postVoteRepository.save(existing);
        }
        // old == new == 0 인 경우: 아무 변화 없음

        // 5. 게시글 집계값 갱신
        post.applyVoteDelta(oldValue, newValue);

        // 6. 저장
        savePostPort.save(post);
    }

    /**
     * 기존 투표값과 사용자의 의도(desired)로부터 실제 적용될 newValue를 결정한다.
     *
     * <p>규칙:</p>
     * <ul>
     *     <li>desired = +1 (upvote):</li>
     *     <ul>
     *         <li>old = +1 → 0 (토글: 추천 취소)</li>
     *         <li>old = 0  → +1</li>
     *         <li>old = -1 → +1 (비추천 → 추천)</li>
     *     </ul>
     *     <li>desired = -1 (downvote):</li>
     *     <ul>
     *         <li>old = -1 → 0 (토글: 비추천 취소)</li>
     *         <li>old = 0  → -1</li>
     *         <li>old = +1 → -1 (추천 → 비추천)</li>
     *     </ul>
     *     <li>desired = 0 (cancel): 항상 0</li>
     * </ul>
     */
    private int decideNewValue(int oldValue, int desired) {
        if (desired == 0) {
            return 0; // 무조건 취소
        }

        if (desired == 1) {
            // upvote 요청
            return (oldValue == 1) ? 0 : 1;
        } else { // desired == -1
            // downvote 요청
            return (oldValue == -1) ? 0 : -1;
        }
    }
}
