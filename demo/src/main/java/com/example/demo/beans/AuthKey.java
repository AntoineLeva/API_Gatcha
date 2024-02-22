package com.example.demo.beans;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Document
public class AuthKey {
    @Id
    private String token;
    private String usernameAccount;
    private LocalDateTime expirationDateTime;

    public AuthKey(String token, String usernameAccount, LocalDateTime expirationDateTime) {
        this.token = token;
        this.usernameAccount = usernameAccount;
        this.expirationDateTime = expirationDateTime;
    }

    public boolean TokenIsExprirated(LocalDateTime expirationDateTime) {
        return (this.expirationDateTime.isBefore(expirationDateTime));
    }

    public LocalDateTime refreshExpirationDateTime() {
        LocalDateTime expiratedDateTime = LocalDateTime.now().plusHours(1);
        this.expirationDateTime = expiratedDateTime;
        return this.expirationDateTime;
    }
}
