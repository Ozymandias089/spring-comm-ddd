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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PostId publish(PublishPostCommand cmd) {
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
        return saved.postId();
    }

    /**
     * PUBLISHED → ARCHIVED (보관)
     *
     * @param cmd {PostId postId, MemberId actorId}
     */
    @Override
    @Transactional
    public PostId archive(ArchivePostCommand cmd) {
        // 1. 보관 대상 로드
        Post archiveTarget = loadPostPort.loadById(cmd.postId()).orElseThrow(() -> new PostNotFound(cmd.postId().id().toString()));
        // 2. 권한 검증: 작성자, 어드민, 모더레이터(community 도메인의 서브로 관리함.)
        ensurePermission(archiveTarget, cmd.actorId(), PostPermissionAction.ARCHIVE);
        // 3. 상태 변경
        archiveTarget.archive();
        // 4. 저장
        Post saved = savePostPort.save(archiveTarget);
        // 5. 반환
        return saved.postId();
    }

    /**
     * ARCHIVED → PUBLISHED (복구). 어드민과 모더레이터만 접근할 수 있게 하려고 함.
     *
     * @param cmd {PostId postId, MemberId actorId}
     */
    @Override
    @Transactional
    public PostId restore(RestorePostCommand cmd) {
        // 1. 복구 대상 로드
        Post restoreTarget = loadPostPort.loadById(cmd.postId()).orElseThrow(() -> new PostNotFound(cmd.postId().id().toString()));
        // 2. 권한 검증 - ADMIN, 모더레이터
        ensurePermission(restoreTarget, cmd.actorId(), PostPermissionAction.RESTORE);
        // 3. 복구
        restoreTarget.restore();
        Post saved = savePostPort.save(restoreTarget);
        // 4. 반환
        return saved.postId();
    }

    /**
     * TEXT / LINK / MEDIA 공용 수정.
     *  - LINK 가 아닌 경우(TEXT / MEDIA): content 수정 허용
     *  - 모든 타입: title 수정 허용
     */
    @Override
    @Transactional
    public PostId editPost(EditPostCommand cmd) {
        // 1. Post 로드
        Post target = loadPostPort.loadById(cmd.postId())
                .orElseThrow(() -> new PostNotFound(cmd.postId().id().toString()));

        // 2. 권한 검증 (작성자만 EDIT 가능)
        ensurePermission(target, cmd.actorId(), PostPermissionAction.EDIT);

        // 3. 내용 수정
        // content: LINK가 아닌 경우에만 허용
        if (cmd.newContent() != null && target.kind() != PostKind.LINK) target.rewrite(cmd.newContent());

        // title: 모든 타입에서 허용
        if (cmd.newTitle() != null) target.rename(cmd.newTitle());

        // 4. 저장
        Post saved = savePostPort.save(target);
        return saved.postId();
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
}
