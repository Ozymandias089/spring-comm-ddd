package com.y11i.springcommddd.communities.domain;

/**
 * 커뮤니티의 상태.
 *
 * <ul>
 *   <li>{@link #ACTIVE}: 활성</li>
 *   <li>{@link #ARCHIVED}: 보관(Soft delete 성격)</li>
 * </ul>
 */
public enum CommunityStatus {
    /** 생성 확인 대기 */
    PENDING,
    /** 활성 */
    ACTIVE,
    /** 보관 (Soft delete) */
    ARCHIVED
}
