package com.y11i.springcommddd.posts.application.service;

import com.y11i.springcommddd.communities.application.port.internal.CommunityAuthorization;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.exception.CommunityNotFound;
import com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase;
import com.y11i.springcommddd.posts.application.port.out.*;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostType;
import com.y11i.springcommddd.posts.media.model.AssetMeta;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 게시글 초안(DRAFT) 생성 유스케이스의 애플리케이션 서비스 구현체.
 *
 * <p><b>책임</b></p>
 * <ul>
 *     <li>TEXT / LINK / MEDIA 게시글의 초안을 생성하여 저장</li>
 *     <li>커뮤니티 ID / 작성자 ID 유효성 검증</li>
 *     <li>MEDIA 게시글의 자산(PostAsset) 저장</li>
 * </ul>
 *
 * <p>
 * 게시글의 실제 생성 규칙(상태 = DRAFT 부여, type 결정)은
 * 도메인 애그리게잇 {@link Post} 의 정적 팩토리 메서드가 담당한다.
 * </p>
 *
 * <p>
 * 이 서비스는 트랜잭션 경계를 관리하며, DDD의 애플리케이션 계층 책임에 따라
 * <b>도메인 객체 생성 → 영속화 포트(savePort) → 외부 시스템(스토리지) 연결 로직 연결</b>
 * 의 흐름을 담당한다.
 * </p>
 *
 * @see com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase
 * @see Post
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatePostDraftService implements CreatePostDraftUseCase {
    private final SavePostPort savePostPort;
    private final SavePostAssetsPort savePostAssetsPort;
    private final LoadCommunityForPostPort loadCommunityForPostPort;
    private final PostAssetFactory postAssetFactory;
    private final CheckCommunityBanPort checkCommunityBanPort;
    private final CommunityAuthorization communityAuthorization;

    @Override
    @Transactional
    public PostId createDraft(CreateDraftCommand cmd) {
        // 1. 기본 검증
        validateCommunity(cmd.communityId());
        communityAuthorization.requireActiveVerifiedMember(cmd.authorId());

        // 2. 밴 체크 (커뮤니티에서 작성 가능한지)
        checkCommunityBanPort.ensureNotBanned(cmd.communityId(), cmd.authorId());

        // 3. 타입별 도메인 Post 생성
        Post post = switch (cmd.type()) {
            case TEXT ->
                    Post.createText(
                            cmd.communityId(),
                            cmd.authorId(),
                            cmd.title(),
                            cmd.content()
                    );
            case LINK ->
                    Post.createLink(
                            cmd.communityId(),
                            cmd.authorId(),
                            cmd.title(),
                            cmd.link()
                    );
            case MEDIA ->
                    Post.createMedia(
                            cmd.communityId(),
                            cmd.authorId(),
                            cmd.title(),
                            cmd.content()
                    );
        };

        // 4. Post 저장
        Post savedPost = savePostPort.save(post);
        PostId postId = savedPost.postId();

        // 5. MEDIA 타입이면 자산 저장
        if (cmd.type() == PostType.MEDIA) {
            List<AssetMeta> metas = cmd.assets();
            if (metas != null && !metas.isEmpty()) {
                List<PostAsset> assets = metas.stream()
                        .map(meta -> postAssetFactory.fromMeta(postId, meta))
                        .toList();
                savePostAssetsPort.saveAll(assets);
            }
        }

        return postId;
    }

    // ----------------------------------------------------------------------
    // 검증
    // ----------------------------------------------------------------------

    /**
     * 커뮤니티 존재 여부 검증.
     *
     * @param communityId 커뮤니티 ID
     * @throws CommunityNotFound 존재하지 않으면 발생
     */
    private void validateCommunity(CommunityId communityId) {
        loadCommunityForPostPort.loadById(communityId).orElseThrow(() -> new CommunityNotFound("Community not found"));
    }
}
