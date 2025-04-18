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
import gabri.dev.javaspringcompose.exceptions.SoundtribeUserMiniOException;
import gabri.dev.javaspringcompose.exceptions.SoundtribeUserTokenException;
import gabri.dev.javaspringcompose.models.enums.Rol;
import gabri.dev.javaspringcompose.repositories.FotoRepository;
import gabri.dev.javaspringcompose.repositories.TokenRepository;
import gabri.dev.javaspringcompose.repositories.UserRepository;
import gabri.dev.javaspringcompose.security.JwtProvider;
import gabri.dev.javaspringcompose.services.MinioService;
import gabri.dev.javaspringcompose.services.AuthService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${app.back.url}")
    private String backUrl;


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

    @Autowired
    private FotoRepository fotoRepository;

    @Transactional
    @Override
    public void signup(RegisterRequestDto user, MultipartFile file) throws MessagingException {

        //1. verificar si ya esta registrado
        if (repository.existsByEmail(user.getEmail())) {
            throw new SoundtribeUserException("El email existe, esta cuenta existe");
        }
        if (repository.existsByUsername(user.getUsername())) {
            throw new SoundtribeUserException("El username existe, esta cuenta existe");
        }


        // 2. Obtener la imagen de perfil: personalizada o estándar
        FotoEntity fileUploaded;
        if (file != null && !file.isEmpty()) {
            fileUploaded = modelMapper.map(minioService.upload(file), FotoEntity.class);
        } else {
            String defaultImageName = user.getRol() == Rol.ADMIN ? "ADMIN.png" : "perfilstandar.png";
            fileUploaded = fotoRepository.findByFileName(defaultImageName)
                    .orElseThrow(() -> new SoundtribeUserMiniOException("Imagen por defecto no encontrada: " + defaultImageName));
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

        new NotificationEmail();
        emailService.enviarMail(NotificationEmail.builder()
                .asunto("Porfavor active su cuenta")
                .destinatario(userSaved.getEmail())
                .mensaje(backUrl + "auth/accountVerification/" + token)
                .build());


        String slug = generarSlug(user.getUsername());

        // Asignar el slug generado al usuario y guardar
        userSaved.setSlug(slug);
        repository.save(userSaved);
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
        opToken.orElseThrow(() -> new SoundtribeUserTokenException("token invalido"));
        habilitarUsuario(opToken.get());
    }

    @Override
    public boolean emailExists(String email) {
        return repository.existsByEmail(email);
    }
    @Override
    public boolean usernameExists(String username) {
        return repository.existsByUsername(username);
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

    // Este méto-do construye la URL completa para acceder a la imagen de MinIO
    private String generarUrlImagen(String fileUrl) {
        return backUrl + "auth/image/" + fileUrl; // o como tengas configurado tu controller para imágenes
    }


    @Transactional
    protected void habilitarUsuario(TokenEntity token) {
        String username = token.getUser().getUsername();
        UserEntity user = repository.findByUsername(username).orElseThrow(() -> new SoundtribeUserTokenException("usuario no encontrado: " + username));
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


    @PostConstruct
    public void checkAndStoreStandardImage() {
        checkAndStoreImageIfMissing("perfilstandar.png");
        checkAndStoreImageIfMissing("ADMIN.png");
        crearUsuariosPorDefecto();
    }

    private void checkAndStoreImageIfMissing(String imageName) {
        Optional<FotoEntity> fotoEntityOptional = fotoRepository.findByFileName(imageName);

        if (fotoEntityOptional.isEmpty()) {
            Optional<FotoEntity> image = minioService.getStandardImage(imageName); // Hacé el méto-do más flexible
            if (image.isPresent()) {
                fotoRepository.save(image.get());
                System.out.println("Imagen estándar '" + imageName + "' guardada en la base de datos.");
            } else {
                System.out.println("No se encontró la imagen '" + imageName + "' en MinIO.");
            }
        } else {
            System.out.println("La imagen '" + imageName + "' ya está en la base de datos.");
        }
    }


    public void crearUsuariosPorDefecto() {
        // Creación de usuarios con roles diferentes
        crearUsuarioPorRol("gabriel.scipioni21@gmail.com", "gabriel", Rol.ADMIN, "ADMIN.png", "el mascapito de esta red social");
        crearUsuarioPorRol("uwurulos@gmail.com", "uwurulos", Rol.ARTISTA, "perfilstandar.png", "Artista en SoundTribe");
        crearUsuarioPorRol("watashilittle@gmail.com", "watashi", Rol.OYENTE, "perfilstandar.png", "Oyente en SoundTribe");
    }


    public void crearUsuarioPorRol(String email, String username, Rol rol, String imagenFileName, String descripcion) {
        // Verificar si ya existe un usuario con el mismo email o username
        if (!repository.existsByEmail(email) && !repository.existsByUsername(username)) {
            FotoEntity imagen = fotoRepository.findByFileName(imagenFileName)
                    .orElseThrow(() -> new SoundtribeUserMiniOException("Imagen no encontrada: " + imagenFileName));

            UserEntity usuario = UserEntity.builder()
                    .email(email)
                    .username(username)
                    .password(passwordEncoder.encode("21082003"))  // Contraseña fija
                    .rol(rol)
                    .enabled(true)
                    .descripcion(descripcion)
                    .foto(imagen)
                    .slug(generarSlug(username))
                    .build();

            repository.save(usuario);
            System.out.println("Usuario " + rol.name() + " creado: " + username);
        } else {
            System.out.println("Usuario con email " + email + " o username " + username + " ya existe, no se crea uno nuevo.");
        }
    }

    private String generarSlug(String username) {
        // Paso 1: Limpiar el username, hacer minúsculas y reemplazar caracteres no alfanuméricos con guiones
        String slugBase = username.trim().toLowerCase().replaceAll("[^a-z0-9]", "-");

        // Paso 2: Crear un código único codificado (ejemplo: UUID + tiempo en milisegundos)
        String uniqueCode = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();

        // Paso 3: Codificar el código único en Base64 (sin caracteres especiales)
        String encodedCode = Base64.getUrlEncoder().withoutPadding().encodeToString(uniqueCode.getBytes(StandardCharsets.UTF_8));

        // Paso 4: Asegurarse de que la longitud sea al menos 18 caracteres
        if (encodedCode.length() < 18) {
            // Si la longitud es menor, ajustamos a 18 (esto es solo por seguridad)
            encodedCode = String.format("%-18s", encodedCode).replace(' ', '0');
        }

        // Paso 5: Concatenar el username con el código único codificado
        String slug = slugBase + "-" + encodedCode;

        return slug;
    }




}
