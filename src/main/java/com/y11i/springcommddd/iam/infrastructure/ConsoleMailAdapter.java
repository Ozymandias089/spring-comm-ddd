package com.y11i.springcommddd.iam.infrastructure;

import com.y11i.springcommddd.iam.application.port.out.MailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * NOTE: Temporary Implementation Which uses Console logs instead of Actually Sending an Email.<br>
 * <b>DO NOT USE IN PROD</b>
 */
@Slf4j
@Repository
public class ConsoleMailAdapter implements MailPort {
    @Override
    public void send(String to, String subject, String body) {
        log.info("[MAIL] to = {} subject = {} body = {}", to, subject, body);
    }
}
