package com.y11i.springcommddd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security의 웹 보안 설정 클래스.
 * <p>
 * 기본적으로 CSRF를 비활성화하고, 모든 요청을 허용하도록 구성되어 있습니다.
 * 개발 단계 또는 API 서버 환경에서 보안 필터를 최소화할 때 유용합니다.
 * </p>
 *
 * <p><b>현재 설정 요약:</b></p>
 * <ul>
 *     <li>CSRF 보호 비활성화</li>
 *     <li>모든 HTTP 요청 허용</li>
 *     <li>기본 HTTP Basic 인증 활성화</li>
 * </ul>
 *
 * <p>
 * 운영 환경에서는 반드시 세부 보안 설정(인증, 인가 정책)을 추가하는 것이 권장됩니다.
 * </p>
 *
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see org.springframework.security.web.SecurityFilterChain
 */
@Configuration
public class WebSecurityConfig {

    /**
     * Spring Security 필터 체인을 정의합니다.
     *
     * @param http {@link HttpSecurity} 설정 객체
     * @return 구성된 {@link SecurityFilterChain} 인스턴스
     * @throws Exception 설정 과정에서 오류 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(reg -> reg.anyRequest().permitAll());
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
