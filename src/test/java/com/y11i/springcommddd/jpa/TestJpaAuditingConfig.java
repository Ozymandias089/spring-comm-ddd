package com.y11i.springcommddd.jpa;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * &#064;DataJpaTest  슬라이스에서는 기본 애플리케이션의 @EnableJpaAuditing이 자동으로 안 올 수 있다.
 * 이 테스트 전용 구성으로 auditing(createdAt, updatedAt 등)을 활성화한다.
 */
@TestConfiguration
@EnableJpaAuditing
public class TestJpaAuditingConfig {

    // 보통은 아무 bean도 필요 없지만,
    // 커스텀 AuditorAware<?>가 필요한 경우 여기에 @Bean으로 넣을 수 있다.
    // 지금 Member 엔티티는 createdAt/updatedAt만 쓰고 있어서
    // AuditorAware<String> 같은 건 (아직) 필요 없으므로 비워도 된다.
}
