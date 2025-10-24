package com.y11i.springcommddd.iam.domain;

/**
 * 회원 상태.
 *
 * <ul>
 *   <li>{@link #ACTIVE}: 활성 상태</li>
 *   <li>{@link #SUSPENDED}: 일시 정지</li>
 *   <li>{@link #DELETED}: 삭제(소프트 삭제)</li>
 * </ul>
 */
public enum MemberStatus {
    ACTIVE,
    SUSPENDED,
    DELETED
}
