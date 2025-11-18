package com.y11i.springcommddd.posts.media.domain;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.domain.exception.InvalidDisplayOrder;
import com.y11i.springcommddd.posts.media.domain.exception.InvalidMediaMetadata;
import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.*;

/**
 * 게시글 자산(PostAsset) 애그리게잇 루트.
 *
 * <p><b>개요</b><br>
 * 게시글({@link PostId})에 첨부된 이미지/영상과 같은 미디어 자산을 표현합니다.
 * 자산 타입, 표시 순서, 원본/썸네일 URL, 메타데이터(크기/길이), 캡션/ALT 등을 관리합니다.
 * </p>
 *
 * <p><b>테이블/제약</b></p>
 * <ul>
 *   <li>테이블: {@code post_assets}</li>
 *   <li>유니크 제약: (post_id, display_order) — 동일 게시글 내 표시 순서 중복 금지</li>
 *   <li>인덱스: (post_id, display_order)</li>
 * </ul>
 *
 * <p><b>주의</b><br>
 * 자산의 추가/삭제/순서변경 시, 운영 정책(예: 게시글 보관 상태 금지)은 서비스 계층에서 보장합니다.
 * </p>
 */
@Entity
@Table(
        name = "post_assets",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_assets_post_order", columnNames = {"post_id", "display_order"}),
        indexes = @Index(name = "ix_post_assets_post_order", columnList = "post_id, display_order")
)
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class PostAsset implements AggregateRoot {

    @EmbeddedId
    private PostAssetId postAssetId;

    @Embedded
    @AttributeOverride(name = "id",
            column = @Column(name = "post_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false))
    private PostId postId;

    @Column(name = "size_bytes")
    private Long sizeBytes; // null 허용

    @Column(name = "original_filename", length = 255)
    private String originalFilename; // null 허용

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    /** 동일 게시글 내 표시 순서(0부터, 중복 금지) */
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    /** 원본/대표 URL */
    @Embedded
    @AttributeOverride(name = "url", column = @Column(name = "src_url", nullable = false, length = 1024))
    private Url srcUrl;

    @Column(name = "mime_type", length = 255)
    private String mimeType;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    /** 영상 길이(초) — VIDEO에만 사용 */
    @Column(name = "duration_sec")
    private Integer durationSec;

    /** 접근성(이미지 ALT) */
    @Column(name = "alt_text", length = 255)
    private String altText;

    /** 자막/캡션(선택) */
    @Column(name = "caption", length = 255)
    private String caption;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private ProcessingStatus processingStatus = ProcessingStatus.READY;

    @Column(name = "processing_error", length = 512)
    private String processingError;

    @ElementCollection
    @CollectionTable(
            name = "post_asset_variants",
            joinColumns = @JoinColumn(name = "post_asset_id", nullable = false)
    )
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "variant_name", nullable = false, length = 64)),
            @AttributeOverride(name = "url.url", column = @Column(name = "variant_url", nullable = false, length = 1024)),
            @AttributeOverride(name = "mimeType", column = @Column(name = "variant_mime_type", length = 255)),
            @AttributeOverride(name = "width", column = @Column(name = "variant_width")),
            @AttributeOverride(name = "height", column = @Column(name = "variant_height"))
    })
    // (post_asset_id, variant_name) 유니크/인덱스는 DDL로 추가 권장
    private List<MediaVariant> variants = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected PostAsset() {}

    public PostAsset (
            PostId postId,
            Long sizeBytes,
            String originalFilename,
            MediaType mediaType,
            int displayOrder,
            Url srcUrl,
            String mimeType,
            Integer width,
            Integer height,
            Integer durationSec,
            String altText,
            String caption
    ) {
        this.postAssetId = PostAssetId.newId();
        this.postId = Objects.requireNonNull(postId);
        this.sizeBytes = sizeBytes;
        this.originalFilename = originalFilename;
        this.mediaType = Objects.requireNonNull(mediaType);
        if (displayOrder < 0) throw new IllegalArgumentException("displayOrder must be >= 0");
        this.displayOrder = displayOrder;
        this.srcUrl = Objects.requireNonNull(srcUrl);
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.durationSec = durationSec;
        this.altText = altText;
        this.caption = caption;

        validateMeta(this.mediaType, width, height, durationSec);
    }

    // -----------------------------------------------------
    // 정적 팩토리 (생성 섹션)
    // -----------------------------------------------------

    /**
     * 이미지 자산을 생성합니다.
     *
     * @param postId        게시글 식별자
     * @param displayOrder  표시 순서(0부터)
     * @param srcUrl        원본/대표 URL
     * @param mimeType      MIME 타입(예: image/jpeg)
     * @param width         가로(px)
     * @param height        세로(px)
     * @param altText       접근성 대체 텍스트(옵션)
     * @param caption       캡션(옵션)
     */
    public static PostAsset image (
            PostId postId,
            Long sizeBytes,
            String originalFilename,
            int displayOrder,
            String srcUrl,
            String mimeType,
            Integer width,
            Integer height,
            String altText,
            String caption
    ) {
        return new PostAsset(
                postId,
                sizeBytes,
                originalFilename,
                MediaType.IMAGE,
                displayOrder,
                new Url(srcUrl),
                mimeType,
                width,
                height,
                null,
                altText,
                caption
        );
    }

    /**
     * 동영상 자산을 생성합니다.
     *
     * @param postId        게시글 식별자
     * @param displayOrder  표시 순서(0부터)
     * @param srcUrl        원본/대표 URL
     * @param mimeType      MIME 타입(예: video/mp4)
     * @param width         가로(px)
     * @param height        세로(px)
     * @param durationSec   길이(초)
     * @param caption       캡션(옵션)
     */
    public static PostAsset video (
            PostId postId,
            Long sizeBytes,
            String originalFilename,
            int displayOrder,
            String srcUrl,
            String mimeType,
            Integer width,
            Integer height,
            Integer durationSec,
            String altText,
            String caption
    ) {
        return new PostAsset(
                postId,
                sizeBytes,
                originalFilename,
                MediaType.VIDEO,
                displayOrder,
                new Url(srcUrl),
                mimeType,
                width,
                height,
                durationSec,
                altText,
                caption
        );
    }

    // -----------------------------------------------------
    // 도메인 동작 (수정 섹션)
    // -----------------------------------------------------

    /** 표시 순서를 변경합니다(동일 게시글 내에서만 의미). */
    public void changeDisplayOrder(int newOrder) {
        if (newOrder < 0) throw new InvalidDisplayOrder(newOrder);
        this.displayOrder = newOrder;
    }

    /** 원본/대표 URL을 교체합니다. */
    public void changeSrcUrl(String newSrcUrl) { this.srcUrl = new Url(newSrcUrl); }

    /** 캡션/ALT를 갱신합니다(널 허용). */
    public void changeTexts(String newAltText, String newCaption) {
        this.altText = newAltText;
        this.caption = newCaption;
    }

    /** 미디어 메타데이터를 갱신합니다. (VIDEO의 길이, 공통 width/height 등) */
    public void changeMeta(Integer width, Integer height, Integer durationSec, String mimeType) {
        validateMeta(this.mediaType, width, height, durationSec);
        this.width = width;
        this.height = height;
        this.durationSec = durationSec;
        this.mimeType = mimeType;
    }

    public void changeFileInfo(Long sizeBytes, String originalFilename, String mimeType) {
        if (sizeBytes != null && sizeBytes < 0) throw new InvalidMediaMetadata("sizeBytes must be >= 0");
        this.sizeBytes = sizeBytes;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
    }

    /* === Variant 조작 === */

    /** upsert: 이름 기준으로 있으면 교체, 없으면 추가 */
    public void upsertVariant(String name, String url, String mimeType, Integer width, Integer height) {
        removeVariant(name); // 있으면 제거
        variants.add(new MediaVariant(name, new Url(url), mimeType, width, height));
    }

    /** 이름으로 제거 (없으면 noop) */
    public void removeVariant(String name) {
        variants.removeIf(v -> v.name().equals(name));
    }

    /** 단일 조회(옵션) */
    public Optional<MediaVariant> getVariant(String name) {
        return variants.stream().filter(v -> v.name().equals(name)).findFirst();
    }

    /** 일괄 교체(드래그 앤 드롭 이후 전체 세트 교체 등) */
    public void replaceVariants(Collection<MediaVariant> newVariants) {
        // 중복 이름 방지
        var names = new HashSet<String>();
        for (var v : newVariants) {
            if (!names.add(v.name()))
                throw new InvalidMediaMetadata("Duplicate variant name: " + v.name());
        }
        this.variants.clear();
        this.variants.addAll(newVariants);
    }

    /** === [신규] 가공 상태 전이 === */

    public void markProcessing() {
        this.processingStatus = ProcessingStatus.PROCESSING;
        this.processingError = null;
    }

    public void markReady() {
        this.processingStatus = ProcessingStatus.READY;
        this.processingError = null;
    }

    public void markFailed(String errorMessage) {
        this.processingStatus = ProcessingStatus.FAILED;
        this.processingError = (errorMessage != null && errorMessage.length() > 512)
                ? errorMessage.substring(0, 512) : errorMessage;
    }

    // --- 내부 검증

    private void validateMeta(MediaType type, Integer width, Integer height, Integer durationSec) {
        if (width != null && width < 0)  throw new InvalidMediaMetadata("width must be >= 0");
        if (height != null && height < 0) throw new InvalidMediaMetadata("height must be >= 0");

        if (type == MediaType.VIDEO) {
            if (durationSec == null || durationSec < 0)
                throw new InvalidMediaMetadata("durationSec must be provided and >= 0 for VIDEO");
            if (mimeType != null && !mimeType.startsWith("video/"))
                throw new InvalidMediaMetadata("mimeType must start with video/ for VIDEO");
        } else { // IMAGE
            if (durationSec != null)
                throw new InvalidMediaMetadata("durationSec must be null for non-VIDEO media");
            if (mimeType != null && !mimeType.startsWith("image/"))
                throw new InvalidMediaMetadata("mimeType must start with image/ for IMAGE");
        }
    }

    // -----------------------------------------------------
    // 접근자 (읽기 전용)
    // -----------------------------------------------------

    public PostAssetId postAssetId() { return postAssetId; }
    public PostId postId() { return postId; }
    public MediaType mediaType() { return mediaType; }
    public int displayOrder() { return displayOrder; }
    public Url srcUrl() { return srcUrl; }
    public String mimeType() { return mimeType; }
    public Integer width() { return width; }
    public Integer height() { return height; }
    public Integer durationSec() { return durationSec; }
    public String altText() { return altText; }
    public String caption() { return caption; }
    public ProcessingStatus processingStatus() { return processingStatus; }
    public String processingError() { return processingError; }
    public List<MediaVariant> variants() { return Collections.unmodifiableList(variants); }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public long version() { return version; }
}
