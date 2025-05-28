package soundtribe.soundtribeusers.controllers;

import soundtribe.soundtribeusers.dtos.user.PasswordChangeRequest;
import soundtribe.soundtribeusers.dtos.userExperience.GetAll;
import soundtribe.soundtribeusers.dtos.userExperience.UserDescription;
import soundtribe.soundtribeusers.dtos.userExperience.UserGet;
import soundtribe.soundtribeusers.entities.UserEntity;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserEmailException;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserException;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserNotFoundException;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserValidationException;
import soundtribe.soundtribeusers.services.UserExperienceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user")
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

    @DeleteMapping("/unfollow/{id}")
    public ResponseEntity<String> unfollowUser(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long idToUnfollow
    ) {
        String jwt = token.replace("Bearer ", "");
        userExperienceService.unfollowUser(jwt, idToUnfollow);
        return ResponseEntity.ok("Has dejado de seguir al usuario");
    }

    @GetMapping("/is-following/{id}")
    public ResponseEntity<Boolean> isFollowing(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long idToCheck
    ) {
        String jwt = token.replace("Bearer ", "");
        boolean result = userExperienceService.isFollowing(jwt, idToCheck);
        return ResponseEntity.ok(result);
    }

    // Nuevo endpoint para obtener amigos artistas (followers mutuos)
    @GetMapping("/mutual-artist-friends")
    public ResponseEntity<List<UserGet>> getMutualArtistFriends(
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "");
        return ResponseEntity.ok(userExperienceService.getMutualArtistFriends(jwt));
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserGet>> getFollowers(
            @RequestHeader("Authorization") String token
    ) {
        String cleanJwt = token.replace("Bearer ", "");
        List<UserGet> followers = userExperienceService.getFollowersFromJwt(cleanJwt);
        return ResponseEntity.ok(followers);
    }


    /**
     * RECOVERY
     */

    @PostMapping("/recuperar-password")
    public ResponseEntity<String> recuperarPassword(@RequestBody() String email) {
        try {
            userExperienceService.recuperarContraseña(email);
            return ResponseEntity.ok("Se ha enviado una nueva contraseña al correo electrónico. En caso de no encontrar el email, verifique en su apartado de SPAM");
        } catch (SoundtribeUserEmailException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al recuperar la contraseña.");
        }
    }


    @PutMapping("/cambiar-password")
    public ResponseEntity<String> cambiarPassword(@RequestBody PasswordChangeRequest request) {
        try {
            userExperienceService.CambiarContraseña(request.getNewPassword(), request.getSlugRecovery());
            return ResponseEntity.ok("Contraseña cambiada correctamente");
        } catch (SoundtribeUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar la contraseña: " + e.getMessage());
        }
    }



    @GetMapping("/validar-slug/{slugRecovery}")
    public ResponseEntity<?> validarSlugRecovery(@PathVariable String slugRecovery) {
        try {
            UserEntity user = userExperienceService.getUserBySlugRecovery(slugRecovery);
            return ResponseEntity.ok(user.getUsername());
        } catch (SoundtribeUserNotFoundException | SoundtribeUserValidationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al validar el enlace de recuperación: " + ex.getMessage());
        }
    }


    @GetMapping("/recuperar-password/validar/{slugRecovery}")
    public ResponseEntity<Boolean> isSlugRecoveryValid(@PathVariable String slugRecovery) {
        try {
            boolean isValid = userExperienceService.isSlugRecoveryValid(slugRecovery);
            System.out.println("recibimos este slug:" +slugRecovery);
            System.out.println("devolvemos esta respuesta: " +isValid);
            return ResponseEntity.ok(isValid);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(false);
        }
    }








    // Endpoint para obtener todos los usuarios (para exploración)
    @GetMapping("/all")
    public ResponseEntity<GetAll> getAllUsers() {
        return ResponseEntity.ok(userExperienceService.getAll());
    }


    /**
     * traer a todoss excepto el usuario autenticado
     * @param token
     * @return
     */
    @GetMapping("/all/jwt")
    public ResponseEntity<GetAll> getAllUsers(
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "");
        return ResponseEntity.ok(userExperienceService.getAll(jwt));
    }

    @GetMapping("/perfil/slug/{slug}")
    public ResponseEntity<UserDescription> getUserDescriptionBySlug(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(userExperienceService.getDescriptionBySlug(slug));
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



    @PutMapping("/change-description")
    public ResponseEntity<String> changeDescription(
            @RequestHeader("Authorization") String token,
            @RequestParam String newDescription
    ) {
        try {
            String jwt = token.replace("Bearer ", "");
            userExperienceService.changeDescription(jwt, newDescription);
            return ResponseEntity.ok("Descripción actualizada correctamente");
        } catch (SoundtribeUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar la descripción: " + e.getMessage());
        }
    }

//TODO          DE ACA EN ADELANTE NO ESTA IMPLEMENTADO EN FRONT



    @PostMapping(value = "/cambiar-foto", consumes = "multipart/form-data")
    public ResponseEntity<String> cambiarFotoPerfil(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart(value = "file") MultipartFile file) {

        try {
            // Quitar el prefijo "Bearer " del token
            String token = authHeader.replace("Bearer ", "");
            userExperienceService.cambiarFotoPerfil(token, file);
            return ResponseEntity.ok("Foto de perfil actualizada correctamente");
        } catch (SoundtribeUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar la foto de perfil: " + e.getMessage());
        }
    }


    @PostMapping("/generate-slug")
    public ResponseEntity<String> generateSlug(
            @RequestHeader("Authorization") String token,
            @RequestParam String firstWord,
            @RequestParam String secondWord,
            @RequestParam int number
    ) {
        try {
            String jwt = token.replace("Bearer ", "");
            String slug = userExperienceService.changeSlug(jwt,firstWord,secondWord,number);
            return ResponseEntity.ok(slug);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar slug: " + e.getMessage());
        }
    }


    @GetMapping("/check-first-slug/{firstPart}")
    public ResponseEntity<Boolean> checkFirstSlug(@PathVariable String firstPart) {
        boolean exists = userExperienceService.existFirstSlug(firstPart);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-second-slug/{secondPart}")
    public ResponseEntity<Boolean> checkSecondSlug(@PathVariable String secondPart) {
        boolean exists = userExperienceService.existSecondSlug(secondPart);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-number-slug/{number}")
    public ResponseEntity<Boolean> checkNumberSlug(@PathVariable int number) {
        boolean exists = userExperienceService.existNumberSlug(number);
        return ResponseEntity.ok(exists);
    }
}
