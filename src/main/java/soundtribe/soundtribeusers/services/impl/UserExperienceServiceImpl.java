package soundtribe.soundtribeusers.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import soundtribe.soundtribeusers.dtos.notis.NotificationPost;
import soundtribe.soundtribeusers.dtos.notis.NotificationType;
import soundtribe.soundtribeusers.dtos.userExperience.GetAll;
import soundtribe.soundtribeusers.dtos.userExperience.UserDescription;
import soundtribe.soundtribeusers.dtos.userExperience.UserGet;
import soundtribe.soundtribeusers.entities.FollowerFollowedEntity;
import soundtribe.soundtribeusers.entities.FotoEntity;
import soundtribe.soundtribeusers.entities.UserEntity;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserEmailException;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserException;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserNotFoundException;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserValidationException;
import soundtribe.soundtribeusers.external_APIS.NotificationService;
import soundtribe.soundtribeusers.external_APIS.RandomWordService;
import soundtribe.soundtribeusers.models.enums.Rol;
import soundtribe.soundtribeusers.repositories.FollowerFollowedRepository;
import soundtribe.soundtribeusers.repositories.FotoRepository;
import soundtribe.soundtribeusers.repositories.UserRepository;
import soundtribe.soundtribeusers.security.JwtProvider;
import soundtribe.soundtribeusers.services.MinioService;
import soundtribe.soundtribeusers.services.UserExperienceService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserExperienceServiceImpl implements UserExperienceService {



    @Value("${app.front.url}")
    private String frontUrl;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RandomWordService randomWordService;

    @Autowired
    private FollowerFollowedRepository followedRepository;

    @Autowired
    private FotoRepository fotoRepository;
    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    private MinioService minioService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private NotificationService notificationService;



    //      info para rellenar

    @Cacheable("getAllUsersCache")
    @Override
    public GetAll getAll() {
        List<UserGet> usersGets = userRepository.findAll()
                .stream()
                .map(this::mapToUserGet)
                .collect(Collectors.toList());

        return GetAll.builder()
                .usuarios(usersGets)
                .build();
    }

    @Cacheable("getAllUsersCache")
    @Override
    public GetAll getAll(String jwt) {
        String email = jwtProvider.getEmailFromToken(jwt);

        List<UserGet> usersGets = userRepository.findAll()
                .stream()
                .filter(user -> !user.getEmail().equals(email))
                .map(this::mapToUserGet)
                .collect(Collectors.toList());

        return GetAll.builder()
                .usuarios(usersGets)
                .build();
    }

    @Cacheable("userDescriptionCache")
    @Override
    public UserDescription getDescription(Long id) {
        UserEntity user = getUserByIdOrThrow(id);

        return UserDescription.builder()
                .id(user.getId())
                .username(user.getUsername())
                .description(user.getDescripcion())
                .rol(user.getRol().name())
                .urlimage(user.getFoto().getFileUrl())
                .createdAt(user.getCreatedAt()) // si lo usás
                .ArtistasSeguidos(getTop5ArtistsFollowedByUser(id))
                .slug(user.getSlug())
                .followersCount(followedRepository.countByFollowed(user))
                .followedsCount(followedRepository.countByFollower(user)) // <-- esta línea
                .build();
    }


    @Cacheable("userDescriptionCache")
    @Override
    public UserDescription getDescriptionFromJwt(String jwt) {
        String email = jwtProvider.getEmailFromToken(jwt);
        UserEntity user = getUserByEmailOrThrow(email);

        return UserDescription.builder()
                .id(user.getId())
                .username(user.getUsername())
                .description(user.getDescripcion())
                .rol(user.getRol().name())
                .urlimage(user.getFoto().getFileUrl())
                .createdAt(user.getCreatedAt()) // si lo usás
                .ArtistasSeguidos(getTop5ArtistsFollowedByUser(user.getId()))
                .slug(user.getSlug())
                .followersCount(followedRepository.countByFollowed(user))
                .followedsCount(followedRepository.countByFollower(user)) // <-- esta línea
                .build();
    }


    @Cacheable("userDescriptionCache")
    @Override
    public UserDescription getDescriptionBySlug(String slug) {
        UserEntity user = (UserEntity) userRepository.findBySlug(slug)
                .orElseThrow(() -> new SoundtribeUserException("Usuario con slug " + slug + " no encontrado"));

        return UserDescription.builder()
                .id(user.getId())
                .username(user.getUsername())
                .description(user.getDescripcion())
                .rol(user.getRol().name())
                .urlimage(user.getFoto().getFileUrl())
                .createdAt(user.getCreatedAt())
                .ArtistasSeguidos(getTop5ArtistsFollowedByUser(user.getId()))
                .followersCount(followedRepository.countByFollowed(user))
                .followedsCount(followedRepository.countByFollower(user))
                .build();
    }


    @Cacheable("userGetCache")
    @Override
    public UserGet getUser(String jwt) {
        String email = jwtProvider.getEmailFromToken(jwt);
        UserEntity user = getUserByEmailOrThrow(email);
        return mapToUserGet(user);
    }



    // Gestion de Seguidores


    @Override
    public void followUser(String jwt, Long idToFollow) {
        String followerEmail = jwtProvider.getEmailFromToken(jwt);
        UserEntity follower = getUserByEmailOrThrow(followerEmail);
        UserEntity followed = getUserByIdOrThrow(idToFollow);

        if (follower.getId().equals(followed.getId())) {
            throw new SoundtribeUserException("No puedes seguirte a ti mismo: " + follower.getEmail());
        }

        if (followedRepository.existsByFollowerAndFollowed(follower, followed)) {
            throw new SoundtribeUserException("Ya estás siguiendo a este usuario");
        }

        FollowerFollowedEntity relation = new FollowerFollowedEntity();
        relation.setFollower(follower);
        relation.setFollowed(followed);
        followedRepository.save(relation);

        // Crear una notificación para el usuario seguido
        notificationService.enviarNotificacion(
                jwt,
                NotificationPost.builder()
                        .receivers(List.of(followed.getId())) // 👈 El usuario que fue seguido la recibe
                        .type(NotificationType.FOLLOW)
                        .build()
        );
    }


    @Override
    public void unfollowUser(String jwt, Long idToUnfollow) {
        String followerEmail = jwtProvider.getEmailFromToken(jwt);
        UserEntity follower = getUserByEmailOrThrow(followerEmail);
        UserEntity followed = getUserByIdOrThrow(idToUnfollow);

        if (follower.getId().equals(followed.getId())) {
                throw new SoundtribeUserException("No puedes dejar de seguirte a ti mismo: " + follower.getEmail());
        }

        FollowerFollowedEntity relation = followedRepository.findByFollowerAndFollowed(follower, followed)
                .orElseThrow(() -> new SoundtribeUserException("No estás siguiendo a este usuario"));

        followedRepository.delete(relation);
    }

    @Override
    public boolean isFollowing(String jwt, Long idToCheck) {
        String email = jwtProvider.getEmailFromToken(jwt);
        UserEntity follower = getUserByEmailOrThrow(email);
        UserEntity followed = getUserByIdOrThrow(idToCheck);

        return followedRepository.existsByFollowerAndFollowed(follower, followed);
    }

    @Cacheable("ListuserGetCache")
    @Transactional
    @Override
    public List<UserGet> getFollowersFromJwt(String jwt) {
        String email = jwtProvider.getEmailFromToken(jwt);
        UserEntity user = getUserByEmailOrThrow(email);

        List<UserEntity> followers = followedRepository.findFollowersByFollowed(user);

        return followers.stream()
                .map(this::mapToUserGet)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void recuperarContraseña(String email) {
        // 1. Buscar al usuario o lanzar excepción si no existe
        UserEntity user = getUserByEmailOrThrow(email);

        // 2. Generar un slug único para recuperación
        String slugUnico = generarSlugUnico();
        user.setSlugRecovery(slugUnico);
        user.setSlugRecoveryDate(LocalDateTime.now());
        userRepository.save(user);

        // 3. Crear la URL para recuperación (adaptalo si tu URL de frontend cambia)
        String url = frontUrl + "/recovery-password/" + slugUnico;

        // 4. Enviar el correo con el link de recuperación
        try {
            emailService.enviarMailRecuperacionContraseña(user.getEmail(), url);
        } catch (Exception e) {
            throw new SoundtribeUserEmailException("No se pudo enviar el correo de recuperación: " + e.getMessage());
        }
    }


    @Transactional
    @Override
    public void CambiarContraseña(String newPassword, String slugRecovery){
        UserEntity userEntity = getUserBySlugRecovery(slugRecovery);

        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userEntity.setSlugRecovery(null);
        userEntity.setSlugRecoveryDate(null);
        userRepository.save(userEntity);
    }

    @Transactional
    @Override
    public boolean isSlugRecoveryValid(String slug) {
        Optional<UserEntity> optionalUser = userRepository.findBySlugRecovery(slug);

        if (optionalUser.isEmpty()) {
            return false;
        }

        UserEntity user = optionalUser.get();

        if (user.getSlugRecovery() == null || user.getSlugRecoveryDate() == null) {
            return false;
        }

        if (user.getSlugRecoveryDate().plusMinutes(30).isBefore(LocalDateTime.now())) {
            // Limpiar datos si expiró
            user.setSlugRecovery(null);
            user.setSlugRecoveryDate(null);
            userRepository.save(user);
            return false;
        }

        return true;
    }



    @Transactional
    @Override
    public UserEntity getUserBySlugRecovery(String slug) {
        UserEntity user = (UserEntity) userRepository.findBySlugRecovery(slug)
                .orElseThrow(() -> new SoundtribeUserNotFoundException("Slug no válido"));

        if (user.getSlugRecoveryDate() == null ||
                user.getSlugRecoveryDate().plusMinutes(30).isBefore(LocalDateTime.now())) {

            // Limpiar el slug si ya expiró
            user.setSlugRecovery(null);
            user.setSlugRecoveryDate(null);
            userRepository.save(user);

            throw new SoundtribeUserValidationException("El enlace de recuperación ha expirado.");
        }

        return user;
    }

    @Transactional
    @Override
    public UserGet convertToUserGet(UserEntity user) {
        if (user == null) {
            return null;
        }

        return UserGet.builder()
                .id(user.getId())
                .username(user.getUsername())
                .rol(user.getRol() != null ? user.getRol().name() : null)  // Convertir Enum a String
                .urlFoto(user.getFoto() != null ? user.getFoto().getFileUrl() : null)  // Asumiendo que FotoEntity tiene getUrl()
                .slug(user.getSlug())
                .followersCount(user.getSeguidores() != null ? (long) user.getSeguidores().size() : 0L)
                .build();
    }





    // ---------- Métodos privados reutilizables ----------


    public String generarSlugUnico() {
        String slug;
        do {
            slug = generarSlug();
        } while (userRepository.existsBySlugRecovery(slug)); // paréntesis corregidos
        return slug;
    }



    private String generarSlug() {

        String palabra1 = randomWordService.obtenerPalabraAleatoria();
        String palabra2 = randomWordService.obtenerPalabraAleatoria();

        // Paso 2: Generar un número aleatorio de 4 dígitos
        int numeroRandom = (int) (Math.random() * 10000); // Genera un número entre 0 y 9999
        String numeroRandomStr = String.format("%04d", numeroRandom); // Asegura que sea de 4 dígitos

        // Paso 3: Concatenar las palabras y el número aleatorio

        return palabra1 + "-" + palabra2 + "-" + numeroRandomStr;
    }


    private UserGet mapToUserGet(UserEntity user) {
        return UserGet.builder()
                .id(user.getId())
                .username(user.getUsername())
                .rol(user.getRol().name())
                .urlFoto(user.getFoto().getFileUrl())
                .followersCount(followedRepository.countByFollowed(user))
                .slug(user.getSlug())
                .build();
    }

    private String generarPasswordSegura(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&_-.";
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();

        for (int i = 0; i < longitud; i++) {
            sb.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return sb.toString();
    }


    private UserEntity getUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new SoundtribeUserException("El usuario con ID " + id + " no existe"));
    }

    private UserEntity getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SoundtribeUserException("Usuario con email " + email + " no encontrado"));
    }

    private List<UserGet> getTop5ArtistsFollowedByUser(Long userId) {
        Pageable topFive = PageRequest.of(0, 5);
        return followedRepository.findTop5FollowedArtistsByFollowerId(userId, topFive)
                .stream()
                .map(this::mapToUserGet)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void cambiarFotoPerfil(String token, MultipartFile file) {
        // 1. Obtener el email del token y buscar el usuario
        String email = jwtProvider.getEmailFromToken(token);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SoundtribeUserException("Usuario no encontrado"));

        // 2. Verificar el archivo
        if (file == null || file.isEmpty()) {
            throw new SoundtribeUserException("Debe proporcionar una imagen");
        }

        // 3. Obtener la foto de perfil actual
        FotoEntity currentPhoto = user.getFoto();
        boolean isStandardImage = false;

        // 4. Verificar si la foto actual es una imagen estándar
        if (currentPhoto != null) {
            isStandardImage = currentPhoto.getId() == 1 || currentPhoto.getId() == 2 ||
                    "perfilstandar.png".equals(currentPhoto.getFileName()) ||
                    "ADMIN.png".equals(currentPhoto.getFileName());
        }

        // 5. Subir la nueva imagen a MinIO
        FotoEntity newPhoto = modelMapper.map(minioService.upload(file), FotoEntity.class);

        // 6. Actualizar la referencia en el usuario
        user.setFoto(newPhoto);
        userRepository.save(user);

        // 7. Si la foto anterior no era estándar, eliminarla
        if (currentPhoto != null && !isStandardImage) {
            try {
                minioService.removeFoto(currentPhoto);
            } catch (Exception e) {
                // Log error but continue
                System.err.println("Error al eliminar la foto anterior: " + e.getMessage());
            }
        }
    }

    @Transactional
    @Override
    public void changeDescription(String jwt, String newDescription) {
        String email = jwtProvider.getEmailFromToken(jwt);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SoundtribeUserException("Usuario no encontrado"));

        // Update the description
        user.setDescripcion(newDescription);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public String changeSlug(String jwt, String firstWord, String secondWord, int number) {
        String email = jwtProvider.getEmailFromToken(jwt);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SoundtribeUserException("Usuario no encontrado"));

        String slug = generarSlugManual(firstWord, secondWord, number);

        if (userRepository.existsBySlug(slug)) {
            throw new SoundtribeUserException("El slug ya está en uso. Elegí otro");
        }

        user.setSlug(slug);
        userRepository.save(user);
        return slug;
    }


    private String generarSlugManual(String palabra1, String palabra2, int numero) {
        // Validación básica
        if (palabra1 == null || palabra2 == null ||
                palabra1.length() < 2 || palabra1.length() > 7 ||
                palabra2.length() < 2 || palabra2.length() > 7) {
            throw new IllegalArgumentException("Las palabras deben tener entre 2 y 7 caracteres");
        }

        if (numero < 0 || numero > 9999) {
            throw new IllegalArgumentException("El número debe estar entre 0000 y 9999");
        }

        String numeroStr = String.format("%04d", numero); // Siempre 4 cifras

        return palabra1 + "-" + palabra2 + "-#" + numeroStr;
    }


    @Override
    public boolean existFirstSlug(String firstSlugPart) {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                .map(UserEntity::getSlug)
                .filter(Objects::nonNull)
                .anyMatch(slug -> {
                    String[] parts = slug.split("-");
                    return parts.length >= 1 && parts[0].equals(firstSlugPart);
                });
    }

    @Override
    public boolean existSecondSlug(String secondSlugPart) {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                .map(UserEntity::getSlug)
                .filter(Objects::nonNull)
                .anyMatch(slug -> {
                    String[] parts = slug.split("-");
                    return parts.length >= 2 && parts[1].equals(secondSlugPart);
                });
    }

    @Override
    public boolean existNumberSlug(int number) {
        // Check if the number part of any slug matches the given number
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                .map(UserEntity::getSlug)
                .filter(Objects::nonNull)
                .anyMatch(slug -> {
                    String[] parts = slug.split("-");
                    if (parts.length >= 3) {
                        try {
                            int slugNumber = Integer.parseInt(parts[2]);
                            return slugNumber == number;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                    return false;
                });
    }


    @Override
    public List<UserGet> getMutualArtistFriends(String jwt) {
        String email = jwtProvider.getEmailFromToken(jwt);
        UserEntity currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Obtener los usuarios artistas que son amigos mutuos (se siguen entre sí)
        List<UserEntity> mutualArtistFriends = getMutualArtistFriendsForUser(currentUser);

        // Convertir a DTO
        return mutualArtistFriends.stream()
                .map(this::mapToUserGet)
                .collect(Collectors.toList());
    }

    private List<UserEntity> getMutualArtistFriendsForUser(UserEntity user) {
        // Obtener los usuarios a los que sigue el usuario actual
        List<UserEntity> following = followedRepository.findByFollower(user)
                .stream()
                .map(FollowerFollowedEntity::getFollowed)
                .collect(Collectors.toList());

        // Filtrar solo los artistas que también siguen al usuario actual (mutuo follow)
        return following.stream()
                .filter(followedUser ->
                        followedUser.getRol() == Rol.ARTISTA &&
                                followedRepository.existsByFollowerAndFollowed(followedUser, user))
                .collect(Collectors.toList());
    }
}
