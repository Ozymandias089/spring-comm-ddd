package com.y11i.springcommddd.iam.domain.exception;

import com.y11i.springcommddd.iam.domain.MemberId;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(MemberId id) {
        super("Member with id " + id.id().toString() + " not found");
    }
}
