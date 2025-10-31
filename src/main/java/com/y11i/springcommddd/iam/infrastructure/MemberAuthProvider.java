package com.y11i.springcommddd.iam.infrastructure;

import com.y11i.springcommddd.communities.moderators.domain.CommunityModeratorRepository;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMemberPrincipal;
import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.domain.MemberRepository;
import com.y11i.springcommddd.iam.domain.MemberStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;

/**
 * Spring Security {@link AuthenticationProvider} 구현체.
 *
 * <p><b>역할</b><br>
 * 이메일/패스워드 기반으로 사용자를 인증하고, 전역 역할(ROLE_*)과
 * 커뮤니티별 모더레이터 권한(예: {@code COMMUNITY_MOD:<communityId>})을 부여합니다.
 * </p>
 *
 * <p><b>권한 구성</b></p>
 * <ul>
 *   <li>전역 역할: {@code Member.roles()} → {@code ROLE_USER}, {@code ROLE_ADMIN} 등</li>
 *   <li>커뮤니티별: {@code COMMUNITY_MOD:<communityId>} 포맷으로 부여</li>
 * </ul>
 *
 * <p><b>예외 처리</b></p>
 * <ul>
 *   <li>자격 증명 불일치: {@link BadCredentialsException}</li>
 *   <li>정지/삭제 사용자: {@link LockedException},
 *       {@link DisabledException}</li>
 * </ul>
 *
 * <p><b>주의</b><br>
 * 인증 성공 시 {@link Authentication#getPrincipal()} 값으로 이메일을 사용합니다.
 * API 계층에서 도메인 식별자(MemberId)가 필요하면, 추가적인 UserDetails/Principal 매핑을 고려하세요.
 * </p>
 */
@Component
public class MemberAuthProvider implements AuthenticationProvider {
    private final MemberRepository memberRepository;
    private final CommunityModeratorRepository communityModeratorRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 생성자.
     *
     * @param memberRepository 회원 조회용 리포지토리
     * @param communityModeratorRepository 커뮤니티 모더레이터 조회용 리포지토리
     * @param passwordEncoder 비밀번호 검증용 인코더
     */
    public MemberAuthProvider(MemberRepository memberRepository, CommunityModeratorRepository communityModeratorRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.communityModeratorRepository = communityModeratorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 이메일/비밀번호로 인증을 수행합니다.
     *
     * @param authentication {@link UsernamePasswordAuthenticationToken} 형태의 인증 요청
     * @return 인증 성공 시 {@link UsernamePasswordAuthenticationToken}
     * @throws AuthenticationException 인증 실패 시 예외
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getPrincipal().toString();
        String rawPassword = authentication.getCredentials().toString();

        var member = memberRepository.findByEmail(new Email(email)).orElseThrow(
                () -> new BadCredentialsException("bad credentials")
        );

        if (member.status() == MemberStatus.DELETED) throw new DisabledException("member is deleted");
        // Allow logins on SUSPENDED status
        // if (member.status() == MemberStatus.SUSPENDED) throw new LockedException("member is Suspended");
        if (!passwordEncoder.matches(rawPassword, member.passwordHash().encoded())) throw new  BadCredentialsException("bad credentials");

        var authorities = new ArrayList<SimpleGrantedAuthority>();
        for(var r : member.roles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + r.name()));
        }

        // 파생 권한: 글/댓글 작성 가능여부
        boolean canPublish = (member.status() == MemberStatus.ACTIVE) && member.emailVerified();
        if (!canPublish) { authorities.add(new SimpleGrantedAuthority("CAN_PUBLISH")); }
        else { authorities.add(new SimpleGrantedAuthority("CANNOT_PUBLISH")); }

        communityModeratorRepository.findByMemberId(member.memberId()).forEach(moderator -> {
            authorities.add(new SimpleGrantedAuthority("COMMUNITY_MOD:" + moderator.communityId().id()));
        });

        var principal = new AuthenticatedMemberPrincipal(
                member.memberId(),
                member.email().value(),
                Set.copyOf(authorities),
                member.passwordHash().encoded()
        );

        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    /**
     * 이 Provider가 지원하는 인증 토큰 타입을 지정합니다.
     *
     * @param authentication 인증 토큰 타입
     * @return {@code UsernamePasswordAuthenticationToken} 지원 여부
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
