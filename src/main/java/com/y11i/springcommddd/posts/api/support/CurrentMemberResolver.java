package com.y11i.springcommddd.posts.api.support;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMemberPrincipal;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor
public class CurrentMemberResolver {

    /**
     * 현재 로그인한 사용자의 MemberId를 조회한다.
     * 로그인하지 않은 경우 null을 반환한다.
     */
    public static MemberId resolveCurrentMemberIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedMemberPrincipal memberPrincipal) return memberPrincipal.getMemberId();

        return null;
    }
}
