package com.kostadin.sis.config.mail.template;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "EMAIL_TEMPLATES")
public class EmailTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String sentFrom;

    private String replyTo;

    private String contentType;

    private String templateId;

    private String body;

}
