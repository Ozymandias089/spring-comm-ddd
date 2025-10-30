package com.y11i.springcommddd.iam.api.support;

import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class AuthenticatedMemberPrincipal implements UserDetails {
    @Getter
    private final MemberId memberId;
    private final String email;
    private final Set<? extends GrantedAuthority> authorities;
    private final String passwordHash;

    public AuthenticatedMemberPrincipal(MemberId memberId,
                                        String email,
                                        Set<? extends GrantedAuthority> authorities,
                                        String passwordHash) {
        this.memberId = memberId;
        this.email = email;
        this.authorities = authorities;
        this.passwordHash = passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

}
