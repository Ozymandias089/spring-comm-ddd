package com.y11i.springcommddd.shared.domain;

/**
 * 도메인 엔티티(Entity)를 나타내는 기본 마커 인터페이스.
 * <p>
 * 모든 도메인 객체(엔티티, 애그리게잇 루트, 값 객체)가 공통으로 구현할 수 있는
 * 최상위 식별 타입입니다.
 * </p>
 *
 * <p><b>의도:</b></p>
 * <ul>
 *     <li>도메인 계층 내의 엔티티 타입 식별을 명확히 함</li>
 *     <li>일관된 타입 계층을 유지하여 도메인 모델 구조를 단순화</li>
 * </ul>
 *
 * <p>
 * 서브타입: {@link AggregateRoot}, {@link ValueObject}
 * </p>
 */
public interface DomainEntity {}
