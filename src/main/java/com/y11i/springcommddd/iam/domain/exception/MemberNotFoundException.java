package com.y11i.springcommddd.iam.domain.exception;


public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String id) {
        super("Member with id " + id + " not found");
    }
}
