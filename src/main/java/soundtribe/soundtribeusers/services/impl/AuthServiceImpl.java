package soundtribe.soundtribeusers.services.impl;

import soundtribe.soundtribeusers.dtos.auth.ChangePasswordRequestDto;
import soundtribe.soundtribeusers.dtos.auth.JwtLoginResponseDto;
import soundtribe.soundtribeusers.dtos.auth.LoginRequestDto;
import soundtribe.soundtribeusers.dtos.auth.RegisterRequestDto;
import soundtribe.soundtribeusers.dtos.notification.NotificationEmail;
import soundtribe.soundtribeusers.dtos.user.PerfilUsuarioDto;
import soundtribe.soundtribeusers.entities.FotoEntity;
import soundtribe.soundtribeusers.entities.TokenEntity;
import soundtribe.soundtribeusers.entities.UserEntity;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserException;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserMiniOException;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserTokenException;
import soundtribe.soundtribeusers.external_APIS.RandomWordService;
import soundtribe.soundtribeusers.models.enums.Rol;
import soundtribe.soundtribeusers.repositories.FotoRepository;
import soundtribe.soundtribeusers.repositories.TokenRepository;
import soundtribe.soundtribeusers.repositories.UserRepository;
import soundtribe.soundtribeusers.security.JwtProvider;
import soundtribe.soundtribeusers.services.MinioService;
import soundtribe.soundtribeusers.services.AuthService;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${app.back.url}")
    private String backUrl;
    @Value("${app.front.url}")
    private String frontUrl;


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
    private RandomWordService randomWordService;

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
                .mensaje(frontUrl + "/verificar-cuenta/" + token)
                .build());


        String slug = generarSlugUnico();

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

    @Override
    public void cambiarPassword(String token, ChangePasswordRequestDto request) {
        String email = jwtProvider.getEmailFromToken(token);

        UserEntity user = repository.findByEmail(email)
                .orElseThrow(() -> new SoundtribeUserException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new SoundtribeUserException("La contraseña actual es incorrecta");
        }

        // Cambiar la contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
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




    @Override
    public void checkAndStoreImageIfMissing(String imageName) {
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


    @Override
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
                    .slug(generarSlugUnico())
                    .build();

            repository.save(usuario);
            System.out.println("Usuario " + rol.name() + " creado: " + username);
        } else {
            System.out.println("Usuario con email " + email + " o username " + username + " ya existe, no se crea uno nuevo.");
        }
    }

    public String generarSlugUnico() {
        String slug;
        do {
            slug = generarSlug();
        } while (repository.existsBySlug(slug));
        return slug;
    }


    private String generarSlug() {

        String palabra1 = randomWordService.obtenerPalabraAleatoria();
        String palabra2 = randomWordService.obtenerPalabraAleatoria();

        // Paso 2: Generar un número aleatorio de 4 dígitos
        int numeroRandom = (int) (Math.random() * 10000); // Genera un número entre 0 y 9999
        String numeroRandomStr = String.format("%04d", numeroRandom); // Asegura que sea de 4 dígitos

        // Paso 3: Concatenar las palabras y el número aleatorio

        return palabra1 + "-" + palabra2 + "-" + numeroRandomStr;
    }

}