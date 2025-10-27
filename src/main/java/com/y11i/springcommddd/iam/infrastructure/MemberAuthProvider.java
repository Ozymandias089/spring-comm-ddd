package com.y11i.springcommddd.iam.infrastructure;

import com.y11i.springcommddd.communities.moderators.domain.CommunityModeratorRepository;
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

@Component
public class MemberAuthProvider implements AuthenticationProvider {
    private final MemberRepository memberRepository;
    private final CommunityModeratorRepository communityModeratorRepository;
    private final PasswordEncoder passwordEncoder;

    MemberAuthProvider(MemberRepository memberRepository, CommunityModeratorRepository communityModeratorRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.communityModeratorRepository = communityModeratorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getPrincipal().toString();
        String rawPassword = authentication.getCredentials().toString();
        var member = memberRepository.findByEmail(new Email(email)).orElseThrow(
                () -> new BadCredentialsException("bad credentials")
        );

        if (member.status() == MemberStatus.DELETED) throw new DisabledException("member is deleted");
        if (member.status() == MemberStatus.SUSPENDED) throw new LockedException("member is Suspended");
        if (!passwordEncoder.matches(rawPassword, member.passwordHash().encoded())) throw new  BadCredentialsException("bad credentials");

        var authorities = new ArrayList<SimpleGrantedAuthority>();
        for(var r : member.roles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + r.name()));
        }

        communityModeratorRepository.findByMemberId(member.memberId()).forEach(moderator -> {
            authorities.add(new SimpleGrantedAuthority("COMMUNITY_MOD:" + moderator.communityId().id()));
        });

        return new UsernamePasswordAuthenticationToken(email, null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
