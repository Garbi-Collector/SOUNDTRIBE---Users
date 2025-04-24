package soundtribe.soundtribeusers.services;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import soundtribe.soundtribeusers.dtos.userExperience.GetAll;
import soundtribe.soundtribeusers.dtos.userExperience.UserDescription;
import soundtribe.soundtribeusers.dtos.userExperience.UserGet;

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

    @Transactional
    void cambiarFotoPerfil(String token, MultipartFile file);

    @Transactional
    void changeDescription(String jwt, String newDescription);

    @Transactional
    String changeSlug(String jwt, String firstWord, String secondWord, int number);

    boolean existFirstSlug(String firstSlugPart);

    boolean existSecondSlug(String secondSlugPart);

    boolean existNumberSlug(int number);
}
