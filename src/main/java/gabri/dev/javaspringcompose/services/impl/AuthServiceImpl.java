package gabri.dev.javaspringcompose.services.impl;

import gabri.dev.javaspringcompose.dtos.auth.JwtLoginResponseDto;
import gabri.dev.javaspringcompose.dtos.auth.LoginRequestDto;
import gabri.dev.javaspringcompose.dtos.auth.RegisterRequestDto;
import gabri.dev.javaspringcompose.dtos.notification.NotificationEmail;
import gabri.dev.javaspringcompose.dtos.user.PerfilUsuarioDto;
import gabri.dev.javaspringcompose.entities.FotoEntity;
import gabri.dev.javaspringcompose.entities.TokenEntity;
import gabri.dev.javaspringcompose.entities.UserEntity;
import gabri.dev.javaspringcompose.exceptions.SoundtribeUserException;
import gabri.dev.javaspringcompose.exceptions.SoundtribeUserTokenException;
import gabri.dev.javaspringcompose.repositories.TokenRepository;
import gabri.dev.javaspringcompose.repositories.UserRepository;
import gabri.dev.javaspringcompose.security.JwtProvider;
import gabri.dev.javaspringcompose.services.MinioService;
import gabri.dev.javaspringcompose.services.AuthService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider jwtProvider;


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
    public JwtLoginResponseDto login(LoginRequestDto loginRequestDto) {
        // 1. Buscar usuario por username o email
        Optional<UserEntity> optionalUser = repository.findByEmail(loginRequestDto.getEmail());

        if (optionalUser.isEmpty()) {
            throw new SoundtribeUserException("Usuario no encontrado");
        }

        UserEntity user = optionalUser.get();

        // 2. Verificar contraseña
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new SoundtribeUserException("Contraseña incorrecta");
        }

        // 3. Verificar si la cuenta está habilitada
        if (!user.isEnabled()) {
            throw new SoundtribeUserException("La cuenta no está verificada. Por favor revisá tu correo.");
        }

        // 4. Autenticar el usuario
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                loginRequestDto.getPassword()
        );

        Authentication authenticated = authenticationManager.authenticate(authentication); // <-- Importantísimo

        // 5. Generar JWT
        String token = jwtProvider.generateToken(authenticated);

        return new JwtLoginResponseDto(token, user.getUsername(), user.getEmail());
    }



    @Override
    public void verificarCuenta(String token) {
        Optional<TokenEntity> opToken = tokenRepository.findByToken(token);
        opToken.orElseThrow(()-> new SoundtribeUserTokenException("token invalido"));
        habilitarUsuario(opToken.get());
    }

    @Override
    public boolean emailExists(String email) {
        return repository.existsByEmail(email);
    }


    @Override
    public List<PerfilUsuarioDto> obtenerUsuariosHabilitados() {
        List<UserEntity> usuarios = repository.findAllByEnabledTrue();

        return usuarios.stream()
                .map(user -> new PerfilUsuarioDto(
                        user.getUsername(),
                        user.getFoto() != null ? generarUrlImagen(user.getFoto().getFileUrl()) : null
                ))
                .collect(Collectors.toList());
    }

    // Este método construye la URL completa para acceder a la imagen de MinIO
    private String generarUrlImagen(String fileUrl) {
        return "http://localhost:8080/auth/image/" + fileUrl; // o como tengas configurado tu controller para imágenes
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
