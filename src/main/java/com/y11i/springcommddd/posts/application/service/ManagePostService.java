package com.y11i.springcommddd.posts.application.service;

import com.y11i.springcommddd.communities.moderators.domain.CommunityModeratorRepository;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberRole;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFoundException;
import com.y11i.springcommddd.posts.application.port.in.ManagePostUseCase;
import com.y11i.springcommddd.posts.application.port.out.LoadAuthorForPostPort;
import com.y11i.springcommddd.posts.application.port.out.LoadPostAssetsPort;
import com.y11i.springcommddd.posts.application.port.out.LoadPostPort;
import com.y11i.springcommddd.posts.application.port.out.SavePostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostKind;
import com.y11i.springcommddd.posts.domain.exception.PostNotFound;
import com.y11i.springcommddd.posts.dto.internal.PostDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagePostService implements ManagePostUseCase {
    private final LoadPostPort loadPostPort;
    private final LoadPostAssetsPort loadPostAssetsPort;
    private final SavePostPort savePostPort;
    private final LoadAuthorForPostPort loadAuthorForPostPort;
    private final CommunityModeratorRepository communityModeratorRepository;

    private enum PostPermissionAction {
        PUBLISH,
        ARCHIVE,
        RESTORE,
        EDIT   // rename, rewrite, replaceLink, variant 조작 등
    }

    /**
     * DRAFT → PUBLISHED
     *
     * @param cmd {PostId postId, MemberId actorId}
     */
    @Override
    @Transactional
    public PostDTO publish(PublishPostCommand cmd) {
        // 1. 게시 대상 로드
        Post publishTarget = loadPostPort.loadById(cmd.postId()).orElseThrow(() -> new PostNotFound(cmd.postId().id().toString()));
        // 2. 권한 검증: 작성자만 publish 가능
        ensurePermission(publishTarget, cmd.actorId(), PostPermissionAction.PUBLISH);
        // 3. MEDIA 타입이면 자산 최소 1개 검증
        if (publishTarget.kind() == PostKind.MEDIA) {
            var assets = loadPostAssetsPort.loadByPostId(publishTarget.postId());
            if (assets == null || assets.isEmpty()) {
                // TODO: 필요하면 전용 예외 타입 정의 (e.g. MediaAssetsRequiredForPublish)
                throw new IllegalStateException("MEDIA post requires at least one asset to publish");
            }
        }

        // 4. 도메인 동작 호출 (상태 전이 + publishedAt 설정)
        publishTarget.publish();
        // 5. 변경된 애그리게잇 저장
        Post saved = savePostPort.save(publishTarget);
        // 6. Post → PostDTO 매핑 (임시. 나중에 매퍼로 분리 가능)
        return toPostDTO(saved);
    }

    /**
     * PUBLISHED → ARCHIVED (보관)
     *
     * @param cmd {PostId postId, MemberId actorId}
     */
    @Override
    @Transactional
    public PostDTO archive(ArchivePostCommand cmd) {
        // 1. 보관 대상 로드
        Post archiveTarget = loadPostPort.loadById(cmd.postId()).orElseThrow(() -> new PostNotFound(cmd.postId().id().toString()));
        // 2. 권한 검증: 작성자, 어드민, 모더레이터(community 도메인의 서브로 관리함.)
        ensurePermission(archiveTarget, cmd.actorId(), PostPermissionAction.ARCHIVE);
        // 3. 상태 변경
        archiveTarget.archive();
        // 4. 저장
        Post saved = savePostPort.save(archiveTarget);
        // 5. 반환
        return toPostDTO(saved);
    }

    /**
     * ARCHIVED → PUBLISHED (복구). 어드민과 모더레이터만 접근할 수 있게 하려고 함.
     *
     * @param cmd {PostId postId, MemberId actorId}
     */
    @Override
    @Transactional
    public PostDTO restore(RestorePostCommand cmd) {
        // 1. 복구 대상 로드
        Post restoreTarget = loadPostPort.loadById(cmd.postId()).orElseThrow(() -> new PostNotFound(cmd.postId().id().toString()));
        // 2. 권한 검증 - ADMIN, 모더레이터
        ensurePermission(restoreTarget, cmd.actorId(), PostPermissionAction.RESTORE);
        // 3. 복구
        restoreTarget.restore();
        Post saved = savePostPort.save(restoreTarget);
        // 4. 반환
        return toPostDTO(saved);
    }

    /**
     * 텍스트 게시글 수정.
     * - title / content 둘 중 하나 혹은 둘 다 수정 가능
     * - null인 필드는 수정하지 않음
     */
    @Override
    public PostDTO editTextPost(EditTextPostCommand cmd) {
        Post saved = executeWithPermission(
                cmd.postId(),
                cmd.actorId(),
                PostPermissionAction.EDIT,  // 수정은 작성자만
                post -> {
                    if (post.kind() != PostKind.TEXT) {
                        throw new IllegalStateException("editTextPost can be used only for TEXT posts");
                    }

                    if (cmd.newTitle() != null) {
                        post.rename(cmd.newTitle());
                    }
                    if (cmd.newContent() != null) {
                        post.rewrite(cmd.newContent());
                    }
                }
        );
        return toPostDTO(saved);
    }

    /**
     * 링크 게시글 수정.
     * - title만 수정 가능
     * - 링크(URL)는 강한 불변성 정책으로 인해 수정 불가
     */
    @Override
    public PostDTO editLinkPost(EditLinkPostCommand cmd) {
        Post saved = executeWithPermission(
                cmd.postId(),
                cmd.actorId(),
                PostPermissionAction.EDIT,
                post -> {
                    if (post.kind() != PostKind.LINK) {
                        throw new IllegalStateException("editLinkPost can be used only for LINK posts");
                    }

                    if (cmd.newTitle() != null) {
                        post.rename(cmd.newTitle());
                    }

                    // 링크 URL은 불변 → 도메인 메서드 replaceLink(...)는 여기서 사용하지 않음
                }
        );
        return toPostDTO(saved);
    }

    /**
     * 미디어 게시글 수정.
     * - title 수정 가능
     * - content는 미디어 게시글의 캡션/본문으로 사용된다고 가정하고 수정 허용
     * - 미디어 파일 자체(src/variants)는 수정 불가
     */
    @Override
    public PostDTO editMediaPost(EditMediaPostCommand cmd) {
        Post saved = executeWithPermission(
                cmd.postId(),
                cmd.actorId(),
                PostPermissionAction.EDIT,
                post -> {
                    if (post.kind() != PostKind.LINK) {
                        throw new IllegalStateException("editLinkPost can be used only for LINK posts");
                    }

                    if (cmd.newTitle() != null) {
                        post.rename(cmd.newTitle());
                    }

                    // 링크 URL은 불변 → 도메인 메서드 replaceLink(...)는 여기서 사용하지 않음
                }
        );
        return toPostDTO(saved);
    }

    /**
     * 게시글에 대한 액터의 권한을 검사한다.
     * <p>
     * 규칙:
     *  - PUBLISH: 작성자만
     *  - ARCHIVE: 작성자 OR 모더레이터 OR ADMIN
     *  - RESTORE: 모더레이터 OR ADMIN
     *  - EDIT   : 작성자만
     */
    private void ensurePermission(Post post, MemberId actorId, PostPermissionAction action) {
        boolean isAuthor = post.authorId().equals(actorId);

        // 멤버 정보, 롤, 모더레이터 여부
        var memberOpt = loadAuthorForPostPort.loadById(actorId);
        if (memberOpt.isEmpty()) {
            // 계정 자체가 없으면 권한 이전에 없는 유저
            throw new MemberNotFoundException(actorId);
        }
        var member = memberOpt.get();

        boolean isAdmin = member.roles().contains(MemberRole.ADMIN);
        boolean isModerator = communityModeratorRepository
                .existsByCommunityIdAndMemberId(post.communityId(), actorId);

        boolean allowed = switch (action) {
            case PUBLISH -> isAuthor;                            // 오직 작성자만
            case ARCHIVE -> isAuthor || isAdmin || isModerator;  // 셋 다 허용
            case RESTORE -> isAdmin || isModerator;              // 작성자 단독 불가
            case EDIT    -> isAuthor;                            // 수정은 작성자만
        };

        if (!allowed) {
            throw new AccessDeniedException("Not allowed to " + action + " this post");
        }
    }

    /**
     * 공통 템플릿:
     *  1) Post 로드
     *  2) 권한 체크
     *  3) mutator로 도메인 동작 수행
     *  4) 저장 후 Post 반환
     */
    private Post executeWithPermission(
            PostId postId,
            MemberId actorId,
            PostPermissionAction action,
            Consumer<Post> mutator
    ) {
        Post target = loadPostPort.loadById(postId)
                .orElseThrow(() -> new PostNotFound(postId.id().toString()));

        ensurePermission(target, actorId, action);

        mutator.accept(target);

        return savePostPort.save(target);
    }

    private PostDTO toPostDTO(Post post) {
        return PostDTO.builder()
                .postId(post.postId())
                .communityId(post.communityId())
                .authorId(post.authorId())
                .title(post.title() != null ? post.title().value() : null)
                .content(post.content() != null ? post.content().value() : null)
                .link(post.linkUrl() != null ? post.linkUrl().value() : null)
                .postType(post.kind())
                .status(post.status())
                .createdAt(post.createdAt())
                .updatedAt(post.updatedAt())
                // TODO: 자산/점수/댓글 수 등은 아직 여기서 계산 X → 나중에 확장
                .assets(Collections.emptyList())
                .build();
    }

}
