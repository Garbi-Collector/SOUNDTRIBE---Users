package gabri.dev.javaspringcompose.services;

import gabri.dev.javaspringcompose.dtos.userExperience.GetAll;
import gabri.dev.javaspringcompose.dtos.userExperience.UserDescription;
import gabri.dev.javaspringcompose.dtos.userExperience.UserGet;
import org.springframework.stereotype.Service;

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
}
