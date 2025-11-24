package com.y11i.springcommddd.config;

import com.y11i.springcommddd.iam.infrastructure.MemberAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final MemberAuthProvider memberAuthProvider;

    /**
     * Spring Security 필터 체인을 정의합니다.
     *
     * @param http {@link HttpSecurity} 설정 객체
     * @return 구성된 {@link SecurityFilterChain} 인스턴스
     * @throws Exception 설정 과정에서 오류 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                // 1) 커스텀 AuthenticationProvider를 정의합니다.
                .authenticationProvider(memberAuthProvider)
                .securityContext(sc -> sc.requireExplicitSave(false))

                // 2) CSRF: 쿠키 기반 토큰(프론트엔드가 X-XSRF-TOKEN 헤더로 돌려보내야 함)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // CSRF 예외 API 목록: 회원가입, 로그인, 로그아웃, 패스워드 변경요청
                        .ignoringRequestMatchers(
                                "/api/register",
                                "/api/login",
                                "/api/logout",
                                "/api/password-reset",
                                "/api/password-reset/confirm",
                                "/api/c/**",
                                "/api/posts/community/bs"
                        )
                )

                // 3) 세션: 상태 유지(세션 생성 허용)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 4) 권한 규칙
                .authorizeHttpRequests(reg -> reg
                        // 인증/세션 관련
                        .requestMatchers("/api/auth/**").permitAll()
                        // 회원 가입은 익명 허용
                        .requestMatchers(HttpMethod.POST,  "/api/register").permitAll()
                        .requestMatchers("/api/login", "/api/logout").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/csrf").permitAll()
                        .requestMatchers("/api/email-verify/signup/confirm").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/my-page").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/my-page/display-name").authenticated()
                        .requestMatchers("/api/sessions/**").authenticated()
                        .requestMatchers("/api/password-reset/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // (옵션) 헬스체크 등
                        // .requestMatchers("/actuator/health").permitAll()
                        // --- 게시글 관련 API ---

                        // 초안 생성 (TEXT / LINK / MEDIA) → 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/posts/drafts/**").authenticated()

                        // 피드 / 커뮤니티 피드 → 인증 불필요
                        .requestMatchers(HttpMethod.GET, "/api/posts/feed").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/communities/*/posts").permitAll()

                        // 게시글 상세 조회 → 인증 불필요 (GET /api/posts/{postId})
                        .requestMatchers(HttpMethod.GET, "/api/posts/*").permitAll()

                        // 게시글 상태 변경 (publish/archive/restore) → 인증 필요
                        .requestMatchers(HttpMethod.POST,
                                "/api/posts/*/publish",
                                "/api/posts/*/archive",
                                "/api/posts/*/restore"
                        ).authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/draft/*/delete").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/posts/draft/*/edit").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/posts/draft/*/publish").authenticated()

                        // 게시글 수정 (PATCH /api/posts/{postId}) → 인증 필요
                        .requestMatchers(HttpMethod.PATCH, "/api/posts/*/edit").authenticated()

                        // 투표 (up / down / cancel) → 인증 필요
                        .requestMatchers(HttpMethod.POST,
                                "/api/posts/*/vote/up",
                                "/api/posts/*/vote/down"
                        ).authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/*/vote").authenticated()

                        // 커뮤니티 API
                        .requestMatchers(HttpMethod.POST, "/api/communities/create").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/communities/*/activate").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/c/**").permitAll()
                        // 테스트용 부트스트랩 API → 인증 불필요
                        .requestMatchers(HttpMethod.POST, "/api/posts/community/bs").permitAll()

                        .anyRequest().authenticated()
                )

                // 5) 폼/HTTP Basic 사용 안 함(세션은 우리가 컨트롤러에서 생성)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 6) CORS(필요 시)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    // 컨트롤러에서 AuthenticationManager를 주입받아 사용
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
        return new ProviderManager(memberAuthProvider);
    }

    // 필요하면 CORS 열어주기(프론트엔드 도메인으로 제한 권장)
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*")); // 운영에선 정확히 지정!
        cfg.setAllowedMethods(List.of("GET","POST","PATCH","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-XSRF-TOKEN"));
        cfg.setAllowCredentials(true);

        var src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
