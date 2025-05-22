package soundtribe.soundtribeusers.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soundtribe.soundtribeusers.services.EliminateAccountService;

@RestController
@RequestMapping("/eliminate/user")
@RequiredArgsConstructor
public class EliminateAccountController {

    private final EliminateAccountService eliminateAccountService;

    /**
     * Endpoint para eliminar toda la cuenta del usuario y sus datos relacionados.
     * Este proceso es asíncrono e incluye eliminación en microservicios de música, donaciones, notificaciones,
     * así como limpieza local (tokens, seguidores, foto personalizada y cuenta).
     *
     * @param authorizationHeader El header que contiene el token JWT (formato: "Bearer token")
     * @return 200 OK si se inició correctamente la eliminación
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("❌ Token JWT no proporcionado correctamente.");
        }

        String jwt = authorizationHeader.substring(7); // Quitar el "Bearer "
        eliminateAccountService.eliminarCuenta(jwt);

        return ResponseEntity.ok("✅ Se ha iniciado el proceso de eliminación de cuenta.");
    }
}
