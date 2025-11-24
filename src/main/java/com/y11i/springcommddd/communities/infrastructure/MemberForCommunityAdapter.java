package com.y11i.springcommddd.communities.infrastructure;

import com.y11i.springcommddd.communities.application.port.out.LoadMemberForCommunityPort;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberForCommunityAdapter implements LoadMemberForCommunityPort {
    private final LoadMemberPort loadMemberPort;

    @Override
    public Optional<Member> loadById(MemberId memberId) {
        return loadMemberPort.loadById(memberId);
    }
}
