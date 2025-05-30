package soundtribe.soundtribeusers.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import soundtribe.soundtribeusers.entities.UserEntity;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserException;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserJWTException;
import soundtribe.soundtribeusers.repositories.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.jsonwebtoken.Jwts.parser;

@Service
public class JwtProvider {

    @Autowired
    UserRepository repository;

    private KeyStore keyStore;

    private static final String KEYSTORE_FILE = "/soundtribe.jks";     // nombre de tu archivo .jks
    private static final String KEYSTORE_PASSWORD = "secret";          // contraseña del keystore
    private static final String KEY_ALIAS = "soundtribe";              // alias configurado con keytool
    private static final String KEY_PASSWORD = "secret";               // contraseña de la clave

    @PostConstruct
    public void init() {
        try {
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream(KEYSTORE_FILE);
            keyStore.load(resourceAsStream, KEYSTORE_PASSWORD.toCharArray());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new SoundtribeUserJWTException("Error loading keystore for SoundTribe :" + e);
        }
    }

    public String generateToken(Authentication authentication) {
        User principal = (User) authentication.getPrincipal();

        // Buscar el usuario desde tu base de datos
        Optional<UserEntity> userOptional = repository.findByUsername(principal.getUsername());

        if (userOptional.isEmpty()) {
            throw new SoundtribeUserException("Usuario no encontrado al generar el token");
        }

        UserEntity user = userOptional.get();

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", principal.getAuthorities().stream()
                .findFirst().orElseThrow().getAuthority());
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .addClaims(claims)
                .signWith(getPrivateKey())
                .compact();
    }



    PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey(KEY_ALIAS, KEY_PASSWORD.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new SoundtribeUserJWTException("Error retrieving private key from keystore: "+e);
        }
    }

    PublicKey getPublicKey() {
        try {
            return keyStore.getCertificate(KEY_ALIAS).getPublicKey();
        } catch (KeyStoreException e) {
            throw new SoundtribeUserJWTException("Error retrieving public key from keystore: "+ e);
        }
    }

    public boolean validateToken(String jwt) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    .build()
                    .parseClaimsJws(jwt);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = parser()
                .setSigningKey(getPublicKey())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmailFromToken(String token) {
        return getAllClaimsFromToken(token).get("email", String.class);
    }

    public String getRoleFromToken(String token) {
        return getAllClaimsFromToken(token).get("role", String.class);
    }


}