package com.y11i.springcommddd.shared.domain;

/**
 * 도메인 모델의 값 객체(Value Object)를 표시하기 위한 마커 인터페이스.
 * <p>
 * 값 객체는 고유 식별자를 가지지 않으며, 속성의 조합으로만 동일성이 정의됩니다.
 * </p>
 *
 * <p><b>특징:</b></p>
 * <ul>
 *     <li>불변(Immutable)으로 설계되어야 함</li>
 *     <li>동등성 비교는 값 기반으로 수행</li>
 *     <li>{@link DomainEntity}의 하위 타입</li>
 * </ul>
 *
 * <p>
 * 구현 예시: {@code Email}, {@code DisplayName}, {@code PostId}, {@code Title}, {@code Content} 등
 * </p>
 */
public interface ValueObject extends DomainEntity {}
