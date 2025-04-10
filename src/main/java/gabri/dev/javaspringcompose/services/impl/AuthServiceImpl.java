package gabri.dev.javaspringcompose.services.impl;

import gabri.dev.javaspringcompose.dtos.auth.RegisterRequestDto;
import gabri.dev.javaspringcompose.dtos.notification.NotificationEmail;
import gabri.dev.javaspringcompose.entities.FotoEntity;
import gabri.dev.javaspringcompose.entities.TokenEntity;
import gabri.dev.javaspringcompose.entities.UserEntity;
import gabri.dev.javaspringcompose.exceptions.SoundtribeUserException;
import gabri.dev.javaspringcompose.exceptions.SoundtribeUserTokenException;
import gabri.dev.javaspringcompose.models.FotoModel;
import gabri.dev.javaspringcompose.repositories.TokenRepository;
import gabri.dev.javaspringcompose.repositories.UserRepository;
import gabri.dev.javaspringcompose.services.MinioService;
import gabri.dev.javaspringcompose.services.AuthService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private MinioService minioService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Override
    public void signup(RegisterRequestDto user, MultipartFile file) throws MessagingException {

        //1. verificar si ya esta registrado
        if (repository.existsByEmail(user.getEmail())){
            throw new SoundtribeUserException("El email existe, esta cuenta existe");
        }
        if (repository.existsByUsername(user.getUsername())){
            throw new SoundtribeUserException("El username existe, esta cuenta existe");
        }

        //2. Guardar la imagen en la DB
        FotoEntity fileUploaded = null;
        if (file != null && !file.isEmpty()) {
            fileUploaded = modelMapper.map(minioService.upload(file), FotoEntity.class);
        }

        //3. Generar la entidad Usuario y asignarle una imagen como foto de perfil
        UserEntity userEntity = UserEntity.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .rol(user.getRol())
                .enabled(false)
                .foto(fileUploaded)
                .build();

        //4. guardar la entidad Usuario en la DB
        UserEntity userSaved = repository.save(userEntity);

        //5. Generar token para habilitar al usuario
        String token = generarTokenVerificador(userSaved);

        emailService.enviarMail(new NotificationEmail().builder()
                .asunto("Porfavor active su cuenta")
                .destinatario(userSaved.getEmail())
                .mensaje("http://localhost:8080/auth/accountVerification/"+token)
                .build());
    }

    @Override
    public void verificarCuenta(String token) {
        Optional<TokenEntity> opToken = tokenRepository.findByToken(token);
        opToken.orElseThrow(()-> new SoundtribeUserTokenException("token invalido"));
        habilitarUsuario(opToken.get());
    }

    @Transactional
    protected void habilitarUsuario(TokenEntity token) {
        String username = token.getUser().getUsername();
        UserEntity user = repository.findByUsername(username).orElseThrow(()-> new SoundtribeUserTokenException("usuario no encontrado: "+ username));
        user.setEnabled(true);
        userRepository.save(user);
    }

    private String generarTokenVerificador(UserEntity userSaved) {
        //1. Generar token  para el verificador de token
        String token = UUID.randomUUID().toString();
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setUser(userSaved);

        //2. Guardar entidad
        tokenRepository.save(tokenEntity);
        return token;
    }
}
