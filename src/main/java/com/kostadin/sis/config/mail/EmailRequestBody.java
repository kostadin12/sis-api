package com.kostadin.sis.config.mail;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class EmailRequestBody {
    private String sendTo;
    private String sentFrom;
    private String replyTo;
    private String cc;
    private String subject;
    private String bodyContentType;
    private String body;
    private List<Object> attachments;
}
