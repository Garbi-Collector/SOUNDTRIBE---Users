package gabri.dev.javaspringcompose.services.impl;

import gabri.dev.javaspringcompose.dtos.userExperience.GetAll;
import gabri.dev.javaspringcompose.dtos.userExperience.UserDescription;
import gabri.dev.javaspringcompose.dtos.userExperience.UserGet;
import gabri.dev.javaspringcompose.entities.FollowerFollowedEntity;
import gabri.dev.javaspringcompose.entities.UserEntity;
import gabri.dev.javaspringcompose.exceptions.SoundtribeUserException;
import gabri.dev.javaspringcompose.repositories.FollowerFollowedRepository;
import gabri.dev.javaspringcompose.repositories.UserRepository;
import gabri.dev.javaspringcompose.security.JwtProvider;
import gabri.dev.javaspringcompose.services.UserExperienceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserExperienceServiceImpl implements UserExperienceService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowerFollowedRepository followedRepository;

    @Autowired
    private JwtProvider jwtProvider;



    //      info para rellenar


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




    // ---------- Métodos privados reutilizables ----------

    private UserGet mapToUserGet(UserEntity user) {
        return UserGet.builder()
                .id(user.getId())
                .username(user.getUsername())
                .urlFoto(user.getFoto().getFileUrl())
                .followersCount(followedRepository.countByFollowed(user))
                .slug(user.getSlug())
                .build();
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
}
