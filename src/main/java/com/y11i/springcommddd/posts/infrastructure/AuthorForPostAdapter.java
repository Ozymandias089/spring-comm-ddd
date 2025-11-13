package com.y11i.springcommddd.posts.infrastructure;

import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.out.LoadAuthorForPostPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthorForPostAdapter implements LoadAuthorForPostPort {
    private final LoadMemberPort loadMemberPort;

    @Override
    public Optional<Member> loadById(MemberId authorId) {
        return loadMemberPort.loadById(authorId);
    }
}
