package com.y11i.springcommddd.shared.domain;

/**
 * 도메인 모델의 애그리게잇 루트(Aggregate Root)를 표시하기 위한 마커 인터페이스.
 * <p>
 * 애그리게잇 루트는 해당 애그리게잇 내의 엔티티 및 값 객체의 불변성을 보장하며,
 * 외부에서는 루트를 통해서만 하위 객체들에 접근할 수 있습니다.
 * </p>
 *
 * <p><b>역할:</b></p>
 * <ul>
 *     <li>애그리게잇의 진입점으로 작동</li>
 *     <li>애그리게잇 전체의 일관성을 책임짐</li>
 *     <li>{@link DomainEntity}의 확장으로 모든 도메인 엔티티의 공통 조상 역할 수행</li>
 * </ul>
 *
 * <p>
 * 구현 클래스 예시: {@code Member}, {@code Post}, {@code Community}, {@code Comment} 등
 * </p>
 */
public interface AggregateRoot extends DomainEntity {}
