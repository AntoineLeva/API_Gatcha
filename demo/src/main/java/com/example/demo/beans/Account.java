package com.example.demo.beans;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document
public class Account {
    @Id
    private String username;
    private String password;
    private String pseudo;

    public Account(String username, String password, String pseudo) {
        this.username = username;
        this.password = password;
        this.pseudo = pseudo;
    }

    @Override
    public String toString() {
        return "---------------------------------------------\n"+
                "Compte : " + pseudo + "\n" +
                "┕-- Nom d'utilisateur: " + username + "\n" +
                "┕-- Mot de passe: " + password + "\n" +
                "---------------------------------------------";
    }
}
