package soundtribe.soundtribeusers.controllers;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soundtribe.soundtribeusers.entities.UserEntity;
import soundtribe.soundtribeusers.models.enums.Rol;
import soundtribe.soundtribeusers.repositories.UserRepository;
import soundtribe.soundtribeusers.security.JwtProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/jwt")
public class JwtTokenController {


    private static final Logger logger = LoggerFactory.getLogger(JwtTokenController.class);

    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Authorization header faltante o malformado");
            return ResponseEntity.badRequest().body(Map.of("error", "Authorization header faltante o malformado"));
        }
        logger.info("Token recibido: {}", authorizationHeader);

        String token = authorizationHeader.substring(7); // Sacamos el "Bearer "


        logger.info("Token recibido: {}", token);

        if (token.isBlank()) {
            logger.warn("Token faltante");
            return ResponseEntity.badRequest().body(Map.of("error", "Token faltante"));
        }

        logger.info("Token recibido: {}", token);
        boolean isValid = jwtProvider.validateToken(token);
        logger.info("¿Token válido?: {}", isValid);

        if (!isValid) {
            logger.warn("Token inválido");
            return ResponseEntity.status(401).body(Map.of("valid", false, "message", "Token inválido"));
        }

        Claims claims = jwtProvider.getAllClaimsFromToken(token);
        logger.info("Claims extraídos: {}", claims);

        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        logger.info("Username extraído: {}", username);
        logger.info("Role extraído: {}", role);

        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("Usuario no encontrado: {}", username);
            return ResponseEntity.status(404).body(Map.of("valid", false, "message", "Usuario no encontrado"));
        }

        UserEntity user = userOpt.get();
        logger.info("Usuario encontrado: {}", user);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", role);
        response.put("slug", user.getSlug());

        response.put("isAdmin", Rol.ADMIN.equals(user.getRol()));
        response.put("isArtista", Rol.ARTISTA.equals(user.getRol()));
        response.put("isOyente", Rol.OYENTE.equals(user.getRol()));

        logger.info("Respuesta construida: {}", response);

        return ResponseEntity.ok(response);
    }

}
