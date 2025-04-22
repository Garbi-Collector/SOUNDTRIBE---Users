package gabri.dev.javaspringcompose.services;

import gabri.dev.javaspringcompose.dtos.auth.ChangePasswordRequestDto;
import gabri.dev.javaspringcompose.dtos.auth.JwtLoginResponseDto;
import gabri.dev.javaspringcompose.dtos.auth.LoginRequestDto;
import gabri.dev.javaspringcompose.dtos.auth.RegisterRequestDto;
import gabri.dev.javaspringcompose.dtos.user.PerfilUsuarioDto;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
}
