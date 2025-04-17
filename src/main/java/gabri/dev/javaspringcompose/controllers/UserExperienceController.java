package gabri.dev.javaspringcompose.controllers;

import gabri.dev.javaspringcompose.dtos.userExperience.GetAll;
import gabri.dev.javaspringcompose.dtos.userExperience.UserDescription;
import gabri.dev.javaspringcompose.dtos.userExperience.UserGet;
import gabri.dev.javaspringcompose.services.UserExperienceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserExperienceController {

    @Autowired
    private UserExperienceService userExperienceService;

    // Endpoint para seguir a un usuario
    @PostMapping("/follow/{id}")
    public ResponseEntity<String> followUser(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long idToFollow
    ) {
        String jwt = token.replace("Bearer ", "");
        userExperienceService.followUser(jwt, idToFollow);
        return ResponseEntity.ok("Seguido correctamente");
    }


    // Endpoint para obtener todos los usuarios (para exploración)
    @GetMapping("/all")
    public ResponseEntity<GetAll> getAllUsers() {
        return ResponseEntity.ok(userExperienceService.getAll());
    }

    // Endpoint para obtener la descripción de un usuario por ID
    @GetMapping("/perfil/{id}")
    public ResponseEntity<UserDescription> getUserDescription(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userExperienceService.getDescription(id));
    }

    // Endpoint para obtener la descripción de un usuario por jwt
    @GetMapping("/mi-perfil")
    public ResponseEntity<UserDescription> getUserDescriptionFromJwt(
            @RequestHeader("Authorization") String token
    ){
        return ResponseEntity.ok(userExperienceService.getDescriptionFromJwt(token));
    }


    // Endpoint para obtener la información del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<UserGet> getAuthenticatedUser(
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "");
        return ResponseEntity.ok(userExperienceService.getUser(jwt));
    }
}
