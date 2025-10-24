package com.y11i.springcommddd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Data JPA의 감사(Auditing) 기능을 활성화하는 설정 클래스.
 * <p>
 * 엔티티 클래스에 {@code @CreatedDate}, {@code @LastModifiedDate} 등의 애노테이션을 사용할 수 있게 해줍니다.
 * </p>
 *
 * <p><b>주요 기능:</b></p>
 * <ul>
 *     <li>엔티티 생성 시 자동으로 생성 시각 기록</li>
 *     <li>엔티티 수정 시 자동으로 갱신 시각 기록</li>
 * </ul>
 *
 * <p>
 * 예시:
 * <pre>
 * &#64;Entity
 * &#64;EntityListeners(AuditingEntityListener.class)
 * public class Post {
 *     &#64;CreatedDate
 *     private Instant createdAt;
 *
 *     &#64;LastModifiedDate
 *     private Instant updatedAt;
 * }
 * </pre>
 * </p>
 *
 * @see org.springframework.data.annotation.CreatedDate
 * @see org.springframework.data.annotation.LastModifiedDate
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {}
