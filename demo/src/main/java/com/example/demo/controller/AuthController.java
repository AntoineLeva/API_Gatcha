package com.example.demo.controller;

import com.example.demo.beans.Account;
import com.example.demo.beans.AuthKey;
import com.example.demo.beans.Product;
import org.bson.codecs.jsr310.LocalDateTimeCodec;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final MongoTemplate mongoTemplate;

    public AuthController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostMapping("newAccount")
    public ResponseEntity<?> newAccount(@RequestBody Account account) {
        try {
            mongoTemplate.save(account);
            return ResponseEntity.status(HttpStatus.FOUND).body("Compte créé !\n"+account.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Impossible de créer un nouveau compte");
        }
    }

    @GetMapping("getAccounts")
    public ResponseEntity<?> getAccounts() {
        try {
            List<Account> accounts = mongoTemplate.findAll(Account.class);
            String rep = "Liste des comptes :\n";
            for (Account account : accounts) rep += account.toString()+"\n";
            return ResponseEntity.status(HttpStatus.OK).body(rep);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Impossible de visualiser la liste des comptes !");
        }
    }

    @GetMapping("login")
    public ResponseEntity<?> login(@RequestBody Account account) {
        try {
            Account verifiedAccount = mongoTemplate.findById(account.getUsername(), Account.class);
            if (verifiedAccount == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Compte inexistant.");
            if (!verifiedAccount.getPassword().equals(account.getPassword())) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mot de passe incorrect.");

            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            LocalDateTime currentDateTime = LocalDateTime.of(currentDate, currentTime);

            Query query = new Query();
            query.addCriteria(Criteria.where("usernameAccount").is(verifiedAccount.getUsername()));
            List<AuthKey> authKeys = mongoTemplate.find(query, AuthKey.class);

            AuthKey authKey = null;
            if (!authKeys.isEmpty()) {
                authKey = authKeys.get(0);
            }

            if ((authKey == null) || (authKey.TokenIsExprirated(currentDateTime))) {
                String key = verifiedAccount.getUsername() + "-" + currentDate.toString() + "-" + currentTime.toString();
                String token = this.encrypt(key, this.generateAESKey("gatcha"));

                authKey = new AuthKey(token, account.getUsername(), currentDateTime.plusHours(1));
                mongoTemplate.save(authKey);
            }

            return ResponseEntity.status(HttpStatus.OK).body("Connexion résussie !\nClé API: "+authKey.getToken());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Impossible de se connecter !\nErreur: "+e.toString());
        }
    }

    @GetMapping("tokenIsValid")
    public ResponseEntity<?> tokenIsValid(@RequestBody AuthKey authKey) {
        try {
            AuthKey verifiedAuthKey = mongoTemplate.findById(authKey.getToken(), AuthKey.class);
            if (verifiedAuthKey == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token invalide.");
            if (verifiedAuthKey.TokenIsExprirated(LocalDateTime.now())) {
                ResponseEntity<String> responseEntity = new ResponseEntity<>("Token expiré ! Authentifiez vous à nouveau.", HttpStatus.UNAUTHORIZED);
                return responseEntity;
            }

            verifiedAuthKey.refreshExpirationDateTime();

            return ResponseEntity.status(HttpStatus.OK).body("Token valide, nom d'utilisateur: "+verifiedAuthKey.getUsernameAccount());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token invalide !\nErreur: "+e.toString());
        }
    }

    public SecretKey generateAESKey(String secretKey) throws Exception {
        String salt = "unique_salt"; // Sel unique
        int iterations = 10000; // Nombre d'itérations
        int keyLength = 256; // Longueur de la clé en bits (256 bits pour AES)

        // Création du spécificateur de clé basé sur le mot de passe
        KeySpec keySpec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), iterations, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();

        // Retourner la clé secrète AES
        return new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String input, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

}
