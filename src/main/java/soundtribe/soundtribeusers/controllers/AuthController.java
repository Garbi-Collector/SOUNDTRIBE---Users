package soundtribe.soundtribeusers.controllers;

import soundtribe.soundtribeusers.dtos.auth.ChangePasswordRequestDto;
import soundtribe.soundtribeusers.dtos.auth.JwtLoginResponseDto;
import soundtribe.soundtribeusers.dtos.auth.LoginRequestDto;
import soundtribe.soundtribeusers.dtos.auth.RegisterRequestDto;
import soundtribe.soundtribeusers.dtos.user.PerfilUsuarioDto;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserException;
import soundtribe.soundtribeusers.services.AuthService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    //      AUTENTICACION
    /**
     * registra un nuevo usuario en la plataforma.
     */
    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    public ResponseEntity<String> signup(
            @RequestPart("user") RegisterRequestDto registerRequestDto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws MessagingException {

        if (registerRequestDto.getPassword() == null || registerRequestDto.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("La contraseña no puede ser nula o vacía");
        }

        try {
            authService.signup(registerRequestDto, file);
            return ResponseEntity.ok("El registro del usuario fue exitoso. Por favor, chequear el email para verificar la cuenta. En caso de no encontrar el email, verifique en su apartado de SPAM");
        } catch (SoundtribeUserException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * verifica la cuenta del usuario a partir del token de activacion.
     */
    @GetMapping("/accountVerification/{token}")
    public ResponseEntity<String> verificarCuenta(@PathVariable String token){
        authService.verificarCuenta(token);
        return new ResponseEntity<>("La cuenta se ha activado Exitosamente", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            JwtLoginResponseDto response = authService.login(loginRequestDto);
            return ResponseEntity.ok(response);
        } catch (SoundtribeUserException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    //      METODOS EUXILIARES

    @GetMapping("/email-exists")
    public ResponseEntity<Boolean> emailExists(@RequestParam("email") String email) {
        boolean exists = authService.emailExists(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/username-exists")
    public ResponseEntity<Boolean> usernameExists(@RequestParam("username") String username) {
        boolean exists = authService.usernameExists(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/usuarios-habilitados")
    public ResponseEntity<List<PerfilUsuarioDto>> obtenerUsuariosHabilitados() {
        List<PerfilUsuarioDto> usuarios = authService.obtenerUsuariosHabilitados();
        return ResponseEntity.ok(usuarios);
    }








    //      CAMBIAR DATA

    @PutMapping("/change-password")
    public ResponseEntity<String> cambiarPassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequestDto request) {

        try {
            // Quitar el prefijo "Bearer " del token
            String token = authHeader.replace("Bearer ", "");
            authService.cambiarPassword(token, request);
            return ResponseEntity.ok("Contraseña actualizada correctamente");
        } catch (SoundtribeUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PostMapping("/is-my-password")
    public ResponseEntity<Boolean> isMyPassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        String token = authHeader.replace("Bearer ", "");
        String password = body.get("password");

        boolean result = authService.isMyPassword(token, password);
        return ResponseEntity.ok(result);
    }

}
