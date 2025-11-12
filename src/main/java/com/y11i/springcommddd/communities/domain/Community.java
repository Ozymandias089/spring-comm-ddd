package com.y11i.springcommddd.communities.domain;

import com.y11i.springcommddd.communities.domain.exception.CommunityArchivedModificationNotAllowed;
import com.y11i.springcommddd.communities.domain.exception.CommunityStatusTransitionNotAllowed;
import com.y11i.springcommddd.shared.domain.AggregateRoot;
import com.y11i.springcommddd.shared.domain.ImageUrl;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * 커뮤니티(Community) 애그리게잇 루트.
 *
 * <p><b>개요</b><br>
 * 커뮤니티의 식별자, 표시명, 이름 키(고유 접근 키), 상태(활성/보관)를 관리하는 도메인 모델입니다.
 * 이름 키는 URL/검색용의 정상화된 값으로, 테이블에서 유니크 제약을 가집니다.
 * </p>
 *
 * <p><b>영속성/테이블</b></p>
 * <ul>
 *   <li>테이블: {@code communities}</li>
 *   <li>유니크 제약: {@code uk_communities_name_key} ({@code name_key})</li>
 *   <li>감사 필드: {@link #createdAt}, {@link #updatedAt}</li>
 *   <li>낙관적 락 버전: {@link #version}</li>
 * </ul>
 *
 * <p><b>불변식/규칙</b></p>
 * <ul>
 *   <li>{@link CommunityName}은 null/공백/최대 길이(100)를 검증</li>
 *   <li>{@link CommunityNameKey}는 정규화/패턴 검증(소문자, 숫자, 밑줄, 3~32자)</li>
 *   <li>보관(ARCHIVED) 상태에서는 이름 변경 불가</li>
 * </ul>
 */
@Entity
@Table(name = "communities",
        uniqueConstraints = @UniqueConstraint(name="uk_communities_name_key", columnNames={"name_key"}))
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class Community implements AggregateRoot {

    // -----------------------------------------------------
    // 식별자/상태/감사 필드
    // -----------------------------------------------------

    @EmbeddedId
    private CommunityId communityId;

    @Embedded
    @AttributeOverride(name="url", column=@Column(name="profile_image_url", length=1024))
    private ImageUrl profileImage;

    @Embedded
    @AttributeOverride(name="url", column=@Column(name="banner_image_url", length=1024))
    private ImageUrl bannerImage;

    @Embedded
    private CommunityName communityName;

    @Embedded
    private CommunityNameKey communityNameKey;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false, length=20)
    private CommunityStatus status;

    @CreatedDate
    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @Version
    private long version;

    /** JPA 기본 생성자. 외부에서 직접 호출하지 않습니다. */
    protected Community() {}

    private Community(CommunityName communityName) {
        this.communityId = CommunityId.newId();
        this.communityName = Objects.requireNonNull(communityName);
        this.communityNameKey = new CommunityNameKey(communityName.value());
        this.status = CommunityStatus.ACTIVE;
    }

    // -----------------------------------------------------
    // 생성 섹션 (정적 팩토리)
    // -----------------------------------------------------

    /**
     * 새 커뮤니티를 생성합니다.
     *
     * @param communityName 표시명 문자열 (검증됨)
     * @return 새 {@link Community} 인스턴스
     * @throws IllegalArgumentException 이름이 null/공백/과다 길이인 경우
     */
    public static Community create(String communityName) {
        return new Community(new CommunityName(communityName));
    }

    // -----------------------------------------------------
    // 도메인 동작 섹션 (이름 변경/보관/복구)
    // -----------------------------------------------------

    /**
     * 보관되지 않은 커뮤니티의 표시명을 변경합니다.
     *
     * <p><b>전제조건</b>: 상태가 {@link CommunityStatus#ARCHIVED}가 아니어야 합니다.</p>
     * <p><b>부작용</b>: {@link #communityName}, {@link #communityNameKey}가 새 값으로 갱신됩니다.</p>
     *
     * @param newCommunityName 새 표시명 문자열
     * @throws IllegalStateException 보관 상태에서 이름을 변경하려는 경우
     * @throws IllegalArgumentException 새 이름이 null/공백/과다 길이인 경우
     */
    public void rename(String newCommunityName) {
        ensureNotArchived();
        this.communityName = new CommunityName(newCommunityName);
        this.communityNameKey = new CommunityNameKey(newCommunityName);
    }

    /**
     * 커뮤니티를 보관(Archive) 상태로 전환합니다. (Soft delete의 성격)
     *
     * <p><b>부작용</b>: 상태가 {@link CommunityStatus#ARCHIVED}로 변경됩니다.</p>
     */
    public void archive() {
        this.status = CommunityStatus.ARCHIVED;
    }

    /**
     * 보관 상태의 커뮤니티를 복구합니다.
     *
     * <p><b>전제조건</b>: 현재 상태가 {@link CommunityStatus#ARCHIVED}이어야 합니다.</p>
     * <p><b>부작용</b>: 상태가 {@link CommunityStatus#ACTIVE}로 변경됩니다.</p>
     *
     * @throws CommunityStatusTransitionNotAllowed 보관 상태가 아닌 경우
     */
    public void restore() {
        if (status != CommunityStatus.ARCHIVED) throw new CommunityStatusTransitionNotAllowed("Only ARCHIVED can be restored");
        this.status = CommunityStatus.ACTIVE;
    }

    /**
     * 프로필 사진을 변경합니다.
     *
     * <p><b>전제조건</b>: 삭제 상태가 아니어야 합니다.</p>
     * <p><b>부작용</b>: {@link #profileImage}가 갱신됩니다.</p>
     *
     * @param url 새 프로필 사진 URL
     * @throws IllegalStateException 삭제된 {@link #Community}에 대한 변경시도
     * @throws IllegalStateException URL 형식 위반
     */
    public void changeProfileImage(String url) {
        ensureNotArchived();
        this.profileImage = (url == null ? null : new ImageUrl(url));
    }

    /**
     * 배너 사진을 변경합니다.
     *
     * <p><b>전제조건</b>: 삭제 상태가 아니어야 합니다.</p>
     * <p><b>부작용</b>: {@link #bannerImage}가 갱신됩니다.</p>
     *
     * @param url 새 배너 사진 URL
     * @throws IllegalStateException 삭제된 {@link #Community}에 대한 변경시도
     * @throws IllegalStateException URL 형식 위반
     */
    public void changeBannerImage(String url) {
        ensureNotArchived();
        this.bannerImage = (url == null ? null : new ImageUrl(url));
    }

    // -----------------------------------------------------
    // 내부 검증 섹션
    // -----------------------------------------------------

    /**
     * 보관 상태가 아님을 보장합니다.
     *
     * @throws IllegalStateException 보관 상태인 경우
     */
    private void ensureNotArchived(){
        if (status == CommunityStatus.ARCHIVED) throw new CommunityArchivedModificationNotAllowed("Archived community cannot be modified");
    }

    // -----------------------------------------------------
    // 접근자 섹션 (읽기 전용)
    // -----------------------------------------------------

    /** 커뮤니티 식별자 */
    public CommunityId communityId(){ return communityId; }
    /** 커뮤니티 프로필 사진 URL */
    public ImageUrl profileImage(){ return profileImage; }
    /** 커뮤니티 배너 사진 URL */
    public ImageUrl bannerImage(){ return bannerImage; }
    /** 표시명 */
    public CommunityName communityName(){ return communityName; }
    /** 이름 키(정규화, 고유) */
    public CommunityNameKey nameKey(){ return communityNameKey; }
    /** 상태(활성/보관) */
    public CommunityStatus status(){ return status; }
    /** 생성 시각(감사) */
    public Instant createdAt(){ return createdAt; }
    /** 수정 시각(감사) */
    public Instant updatedAt(){ return updatedAt; }
    /** 낙관적 락 버전 */
    public long version(){ return version; }
}
