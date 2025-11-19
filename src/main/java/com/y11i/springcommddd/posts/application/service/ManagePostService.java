package com.y11i.springcommddd.posts.application.service;

import com.y11i.springcommddd.communities.moderators.domain.CommunityModeratorRepository;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberRole;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFound;
import com.y11i.springcommddd.posts.application.port.in.ManagePostUseCase;
import com.y11i.springcommddd.posts.application.port.out.LoadAuthorForPostPort;
import com.y11i.springcommddd.posts.application.port.out.LoadPostAssetsPort;
import com.y11i.springcommddd.posts.application.port.out.LoadPostPort;
import com.y11i.springcommddd.posts.application.port.out.SavePostPort;
import com.y11i.springcommddd.posts.domain.*;
import com.y11i.springcommddd.posts.domain.exception.PostNotFound;
import com.y11i.springcommddd.posts.domain.exception.PostStatusTransitionNotAllowed;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import com.y11i.springcommddd.posts.media.domain.PostAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 게시글(Post) 상태 관리 및 수정 기능을 제공하는 애플리케이션 서비스.
 *
 * <p><b>책임</b></p>
 * <ul>
 *     <li>초안(DRAFT) 게시글을 게시(PUBLISH) 상태로 전환</li>
 *     <li>게시(PUBLISHED) 게시글을 보관(ARCHIVED) 상태로 전환</li>
 *     <li>보관된(ARCHIVED) 게시글을 복구(PUBLISHED) 상태로 전환</li>
 *     <li>게시글의 제목/본문 수정</li>
 *     <li>액터(actor)의 권한(작성자/모더레이터/관리자)에 대한 검증</li>
 * </ul>
 *
 * <p><b>권한 모델</b></p>
 * <ul>
 *     <li>PUBLISH : <b>작성자만</b></li>
 *     <li>ARCHIVE : <b>작성자 / 모더레이터 / 관리자</b></li>
 *     <li>RESTORE : <b>모더레이터 / 관리자</b> (작성자는 불가)</li>
 *     <li>EDIT    : <b>작성자만</b></li>
 * </ul>
 *
 * <p>
 * 도메인 규칙과 권한 체크를 조합하여 게시글 상태 전이를 안전하게 수행합니다.
 * 상태 전이와 수정 자체는 <code>Post</code> 애그리게잇이 담당하며,<br>
 * 이 서비스는 트랜잭션 경계, 권한 검증, 로딩·저장 포트 연결을 수행합니다.
 * </p>
 *
 * @see com.y11i.springcommddd.posts.domain.Post
 * @see com.y11i.springcommddd.posts.application.port.in.ManagePostUseCase
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagePostService implements ManagePostUseCase {

    // == Port & Repository Dependencies ==

    private final LoadPostPort loadPostPort;
    private final LoadPostAssetsPort loadPostAssetsPort;
    private final SavePostPort savePostPort;
    private final LoadAuthorForPostPort loadAuthorForPostPort;
    private final CommunityModeratorRepository communityModeratorRepository;
    private final PostRepository postRepository;
    private final PostAssetRepository postAssetRepository;

    /**
     * 게시글 액션 구분(Enum).
     * 권한 체크에서 어떤 정책을 적용할지 결정하기 위해 사용된다.
     */
    private enum PostPermissionAction {
        PUBLISH,
        ARCHIVE,
        RESTORE,
        EDIT   // rename, rewrite, replaceLink, variant 조작 등
    }

    // ---------------------------------------------------------------------
    // 게시 (PUBLISH)
    // ---------------------------------------------------------------------

    /**
     * 게시글 초안(DRAFT)을 게시(PUBLISHED) 상태로 전환한다.
     *
     * <p><b>도메인 규칙</b></p>
     * <ul>
     *     <li>작성자만 게시할 수 있다.</li>
     *     <li>MEDIA 게시글은 최소 1개 이상의 PostAsset이 존재해야 한다.</li>
     *     <li>도메인 메서드 {@link Post#publish()} 호출로 상태 전이가 수행된다.</li>
     * </ul>
     *
     * @param cmd {@link PublishPostCommand} (postId, actorId)
     * @return 게시 완료된 게시글의 {@link PostId}
     *
     * @throws PostNotFound 게시글이 존재하지 않는 경우
     * @throws IllegalStateException MEDIA 게시글에 자산이 하나도 없는 경우
     * @throws AccessDeniedException 권한이 없는 사용자가 호출한 경우
     */
    @Override
    @Transactional
    public PostId publish(PublishPostCommand cmd) {
        // 1. 게시 대상 로드
        Post publishTarget = loadPostPort.loadById(cmd.postId()).orElseThrow(() -> new PostNotFound("Post not found"));
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

    // ---------------------------------------------------------------------
    // 보관 (ARCHIVE)
    // ---------------------------------------------------------------------

    /**
     * 게시(PUBLISHED) 상태의 게시글을 보관(ARCHIVED) 상태로 전환한다.
     *
     * <p><b>권한 규칙</b> : 작성자 / 모더레이터 / 관리자</p>
     *
     * @param cmd {@link ArchivePostCommand}
     * @return 보관된 게시글의 {@link PostId}
     *
     * @throws PostNotFound 존재하지 않는 게시글
     * @throws AccessDeniedException 권한 없는 사용자
     */
    @Override
    @Transactional
    public PostId archive(ArchivePostCommand cmd) {
        // 1. 보관 대상 로드
        Post archiveTarget = loadPostPort.loadById(cmd.postId()).orElseThrow(() -> new PostNotFound("Post not found"));
        // 2. 권한 검증: 작성자, 어드민, 모더레이터(community 도메인의 서브로 관리함.)
        ensurePermission(archiveTarget, cmd.actorId(), PostPermissionAction.ARCHIVE);
        // 3. 상태 변경
        archiveTarget.archive();
        // 4. 저장
        Post saved = savePostPort.save(archiveTarget);
        // 5. 반환
        return saved.postId();
    }

    // ---------------------------------------------------------------------
    // 복구 (RESTORE)
    // ---------------------------------------------------------------------

    /**
     * 보관(ARCHIVED) 상태의 게시글을 다시 게시(PUBLISHED) 상태로 복구한다.
     *
     * <p><b>권한 규칙</b> : 모더레이터 / 관리자 (작성자는 불가)</p>
     *
     * @param cmd {@link RestorePostCommand}
     * @return 복구된 게시글의 {@link PostId}
     *
     * @throws PostNotFound 존재하지 않는 게시글
     * @throws AccessDeniedException 권한 없는 사용자
     */
    @Override
    @Transactional
    public PostId restore(RestorePostCommand cmd) {
        // 1. 복구 대상 로드
        Post restoreTarget = loadPostPort.loadById(cmd.postId()).orElseThrow(() -> new PostNotFound("Post not found"));
        // 2. 권한 검증 - ADMIN, 모더레이터
        ensurePermission(restoreTarget, cmd.actorId(), PostPermissionAction.RESTORE);
        // 3. 복구
        restoreTarget.restore();
        Post saved = savePostPort.save(restoreTarget);
        // 4. 반환
        return saved.postId();
    }

    // ---------------------------------------------------------------------
    // 수정 (EDIT)
    // ---------------------------------------------------------------------

    /**
     * TEXT / LINK / MEDIA 게시글을 공통 규칙에 따라 수정한다.
     *
     * <p><b>도메인 정책</b></p>
     * <ul>
     *     <li><b>모든 타입</b>: 제목(title) 수정 가능</li>
     *     <li><b>TEXT / MEDIA</b>: 본문(content) 수정 가능</li>
     *     <li><b>LINK</b>: content 없음 → content 수정 불가(무시)</li>
     *     <li>미디어 파일(src, variants)은 강한 불변성 정책으로 수정 불가</li>
     * </ul>
     *
     * <p><b>권한 규칙</b> : 작성자만 수정 가능</p>
     *
     * @param cmd {@link EditPostCommand}
     * @return 수정된 게시글의 {@link PostId}
     *
     * @throws PostNotFound 게시글 존재 X
     * @throws AccessDeniedException 권한 없음
     */
    @Override
    @Transactional
    public PostId editPost(EditPostCommand cmd) {
        // 1. Post 로드
        Post target = loadPostPort.loadById(cmd.postId())
                .orElseThrow(() -> new PostNotFound("Post not found"));

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

    @Override
    @Transactional
    public void scrapDraft(ScrapDraftCommand cmd) {
        Post scrapTarget = loadPostPort.loadById(cmd.postId()).orElseThrow(() -> new PostNotFound("Post not found"));

        ensurePermission(scrapTarget, cmd.actorId(), PostPermissionAction.EDIT);

        if (scrapTarget.status() != PostStatus.DRAFT) throw new PostStatusTransitionNotAllowed("Only DRAFT posts can be scrapped");

        int deletedAssets = postAssetRepository.deleteAllByPostId(scrapTarget.postId());
        log.debug("ScrapDraft: postId={} deletedAssets={}", scrapTarget.postId().stringify(), deletedAssets);

        postRepository.delete(scrapTarget);
    }

    // ---------------------------------------------------------------------
    // 권한 검증
    // ---------------------------------------------------------------------

    /**
     * 액터(actor)의 게시글 액션 수행 권한을 검증한다.
     *
     * <p><b>검증 요소</b></p>
     * <ul>
     *     <li>작성자 여부</li>
     *     <li>관리자 여부</li>
     *     <li>커뮤니티 모더레이터 여부</li>
     * </ul>
     *
     * @param post     대상 게시글
     * @param actorId  액션 실행자
     * @param action   수행하려는 액션
     *
     * @throws MemberNotFound 액터(member)가 존재하지 않을 때
     * @throws AccessDeniedException 권한 부족
     */
    private void ensurePermission(Post post, MemberId actorId, PostPermissionAction action) {
        boolean isAuthor = post.authorId().equals(actorId);

        // 멤버 정보, 롤, 모더레이터 여부
        var memberOpt = loadAuthorForPostPort.loadById(actorId);
        if (memberOpt.isEmpty()) {
            // 계정 자체가 없으면 권한 이전에 없는 유저
            throw new MemberNotFound("Member not found");
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
            throw new PostStatusTransitionNotAllowed("Not allowed to " + action.toString().toLowerCase() + " this post");
        }
    }
}
