package soundtribe.soundtribeusers.services;


import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import soundtribe.soundtribeusers.dtos.auth.ChangePasswordRequestDto;
import soundtribe.soundtribeusers.dtos.auth.JwtLoginResponseDto;
import soundtribe.soundtribeusers.dtos.auth.LoginRequestDto;
import soundtribe.soundtribeusers.dtos.auth.RegisterRequestDto;
import soundtribe.soundtribeusers.dtos.user.PerfilUsuarioDto;

import java.util.List;

@Service
public interface AuthService {
    void signup(RegisterRequestDto user, MultipartFile file) throws MessagingException;
    JwtLoginResponseDto login(LoginRequestDto user) ;

    void verificarCuenta(String token);
    boolean usernameExists(String username);
    List<PerfilUsuarioDto> obtenerUsuariosHabilitados();
    void cambiarPassword(String token, ChangePasswordRequestDto request);

    boolean emailExists(String email);

    void checkAndStoreImageIfMissing(String imageName);

    void crearUsuariosPorDefecto();
}
