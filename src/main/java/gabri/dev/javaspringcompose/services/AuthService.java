package gabri.dev.javaspringcompose.services;

import gabri.dev.javaspringcompose.dtos.auth.RegisterRequestDto;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface AuthService {
    void signup(RegisterRequestDto user, MultipartFile file) throws MessagingException;

    void verificarCuenta(String token);
}
