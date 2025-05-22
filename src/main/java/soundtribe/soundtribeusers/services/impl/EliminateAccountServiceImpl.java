package soundtribe.soundtribeusers.services.impl;

import io.minio.errors.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import soundtribe.soundtribeusers.entities.UserEntity;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserException;
import soundtribe.soundtribeusers.external_APIS.EliminateAccountFromMicroservice;
import soundtribe.soundtribeusers.repositories.FollowerFollowedRepository;
import soundtribe.soundtribeusers.repositories.FotoRepository;
import soundtribe.soundtribeusers.repositories.TokenRepository;
import soundtribe.soundtribeusers.repositories.UserRepository;
import soundtribe.soundtribeusers.security.JwtProvider;
import soundtribe.soundtribeusers.services.EliminateAccountService;
import soundtribe.soundtribeusers.services.MinioService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class EliminateAccountServiceImpl implements EliminateAccountService {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private FollowerFollowedRepository followedRepository;

    @Autowired
    private FotoRepository fotoRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MinioService minioService;
    @Autowired
    private EliminateAccountFromMicroservice eliminateAccountFromMicroservice;

    @Async
    @Transactional
    @Override
    public void eliminarCuenta(String jwt) {
        String email = jwtProvider.getEmailFromToken(jwt);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SoundtribeUserException("Usuario no encontrado"));

        // 1. Eliminar datos en microservicio de donaciones
        try {
            eliminateAccountFromMicroservice.eliminarDonacionesDelUsuario(jwt);
        } catch (Exception e) {
            System.err.println("Error eliminando donaciones: " + e.getMessage());
        }

        // 2. Eliminar datos en microservicio de notificaciones
        try {
            eliminateAccountFromMicroservice.eliminarNotificacionesDelUsuario(jwt);
        } catch (Exception e) {
            System.err.println("Error eliminando notificaciones: " + e.getMessage());
        }

        // 3. Eliminar datos en microservicio de música
        try {
            eliminateAccountFromMicroservice.eliminarMusicaDelUsuario(jwt);
        } catch (Exception e) {
            System.err.println("Error eliminando música: " + e.getMessage());
        }

        // 4. Eliminar datos locales

        // 4.1 Eliminar relaciones de seguimiento donde es follower o seguido
        followedRepository.deleteAllByFollowerOrFollowed(user, user);

        // 4.2 Eliminar token si existe
        tokenRepository.deleteByUser(user);

        // 4.3 Eliminar foto si no es estándar
        if (user.getFoto() != null) {
            String nombreFoto = user.getFoto().getFileName();

            if (!nombreFoto.equalsIgnoreCase("perfilstandar.png") && !nombreFoto.equalsIgnoreCase("ADMIN.png")) {
                try {
                    minioService.removeFoto(user.getFoto());
                    fotoRepository.delete(user.getFoto());
                } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                         NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException |
                         XmlParserException | InternalException e) {
                    throw new RuntimeException("Error eliminando foto en MinIO: " + e.getMessage(), e);
                }
            }
        }

        // 4.4 Eliminar usuario
        userRepository.delete(user);
    }

}
