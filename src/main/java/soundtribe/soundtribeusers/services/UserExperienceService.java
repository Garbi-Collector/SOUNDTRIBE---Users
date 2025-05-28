package soundtribe.soundtribeusers.services;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import soundtribe.soundtribeusers.dtos.userExperience.GetAll;
import soundtribe.soundtribeusers.dtos.userExperience.UserDescription;
import soundtribe.soundtribeusers.dtos.userExperience.UserGet;
import soundtribe.soundtribeusers.entities.UserEntity;

import java.util.List;

@Service
public interface UserExperienceService {
    GetAll getAll();
    GetAll getAll(String jwt);
    UserDescription getDescription(Long id);
    void followUser(String jwt, Long idToFollow);

    UserDescription getDescriptionFromJwt(String jwt);

    void unfollowUser(String jwt, Long idToUnfollow);

    UserDescription getDescriptionBySlug(String slug);
    UserGet getUser(String jwt);

    boolean isFollowing(String jwt, Long followedId);

    List<UserGet> getFollowersFromJwt(String jwt);

    @Transactional
    void recuperarContraseña(String token);

    @Transactional
    void CambiarContraseña(String newPassword, String slugRecovery);

    @Transactional
    boolean isSlugRecoveryValid(String slug);

    @Transactional
    UserEntity getUserBySlugRecovery(String slug);

    @Transactional
    UserGet convertToUserGet(UserEntity user);

    @Transactional
    void cambiarFotoPerfil(String token, MultipartFile file);

    @Transactional
    void changeDescription(String jwt, String newDescription);

    @Transactional
    String changeSlug(String jwt, String firstWord, String secondWord, int number);

    boolean existFirstSlug(String firstSlugPart);

    boolean existSecondSlug(String secondSlugPart);

    boolean existNumberSlug(int number);

    List<UserGet> getMutualArtistFriends(String jwt);
}
