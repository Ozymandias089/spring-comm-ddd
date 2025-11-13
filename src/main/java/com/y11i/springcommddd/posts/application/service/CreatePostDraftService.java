package com.y11i.springcommddd.posts.application.service;

import com.y11i.springcommddd.posts.application.port.in.CreatePostDraftUseCase;
import com.y11i.springcommddd.posts.domain.PostId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatePostDraftService implements CreatePostDraftUseCase {
    @Override
    public PostId createTextDraft(CreateTextDraftCommand cmd) {
        return null;
    }

    @Override
    public PostId createLinkDraft(CreateLinkDraftCommand cmd) {
        return null;
    }

    @Override
    public PostId createMediaDraft(CreateMediaDraftCommand cmd) {
        return null;
    }
}
