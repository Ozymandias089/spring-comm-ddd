package com.y11i.springcommddd.posts.application.service;

import com.y11i.springcommddd.posts.application.port.in.CreateAndPublishPostUseCase;
import com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase;
import com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase.*;
import com.y11i.springcommddd.posts.application.port.in.ManagePostUseCase;
import com.y11i.springcommddd.posts.application.port.in.ManagePostUseCase.*;
import com.y11i.springcommddd.posts.domain.PostId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글을 "바로 게재" 상태로 생성하는 애플리케이션 서비스 구현체.
 *
 * <p><b>역할</b></p>
 * <ul>
 *     <li>기존 초안 생성 유스케이스({@link CreatePostDraftUseCase}) 재사용</li>
 *     <li>기존 상태 전환 유스케이스({@link ManagePostUseCase}) 재사용</li>
 *     <li>두 유스케이스 호출을 하나의 트랜잭션 경계 안에서 오케스트레이션</li>
 * </ul>
 *
 * <p>
 * 도메인 규칙은 각 유스케이스/애그리게잇에 위임하고,
 * 이 서비스는 순서와 트랜잭션 경계만 책임진다.
 * </p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CreateAndPublishPostService implements CreateAndPublishPostUseCase {
    private final CreatePostDraftUseCase createPostDraftUseCase;
    private final ManagePostUseCase managePostUseCase;

    @Override
    public PostId createAndPublish(CreateAndPublishCommand cmd) {
        // 1) 초안 생성 (통합 Draft 유스케이스 재사용)
        PostId postId = createPostDraftUseCase.createDraft(
                new CreatePostDraftUseCase.CreateDraftCommand(
                        cmd.communityId(),
                        cmd.authorId(),
                        cmd.type(),
                        cmd.title(),
                        cmd.content(),
                        cmd.link(),
                        cmd.assets()
                )
        );

        // 2) 즉시 게시 (상태 전환 유스케이스 재사용)
        managePostUseCase.publish(
                new ManagePostUseCase.PublishPostCommand(
                        postId,
                        cmd.authorId()
                )
        );

        return postId;
    }
}
