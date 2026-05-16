package com.invitique.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
    // This class enables JPA Auditing for @CreatedDate and @LastModifiedDate
}
