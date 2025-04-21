package gabri.dev.javaspringcompose.security;

import gabri.dev.javaspringcompose.entities.UserEntity;
import gabri.dev.javaspringcompose.exceptions.SoundtribeUserException;
import gabri.dev.javaspringcompose.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.security.*;
import java.security.cert.Certificate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JwtProviderTest {

    @InjectMocks
    private JwtProvider jwtProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeyStore keyStore;

    @Mock
    private PrivateKey privateKey;

    @Mock
    private PublicKey publicKey;

    @Mock
    private Certificate certificate;

    private final String username = "testUser";
    private final String email = "test@example.com";
    private final String role = "ROLE_USER";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Inyectamos el keystore manualmente
        jwtProvider = spy(new JwtProvider());
        jwtProvider.repository = userRepository;
        jwtProvider.init(); // no hace nada útil en test, pero dejamos la llamada
        doReturn(privateKey).when(jwtProvider).getPrivateKey();
        doReturn(publicKey).when(jwtProvider).getPublicKey();
    }


    @Test
    void testGenerateToken_userNotFound_throwsException() {
        User user = new User(username, "password", List.of());
        var auth = new UsernamePasswordAuthenticationToken(user, null, List.of());

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(SoundtribeUserException.class, () -> jwtProvider.generateToken(auth));
    }

    @Test
    void testValidateToken_invalidToken_returnsFalse() {
        String invalidToken = "this.is.invalid";

        // simulate invalid parsing by throwing exception
        doThrow(new io.jsonwebtoken.JwtException("Invalid")).when(jwtProvider).getPublicKey();

        boolean isValid = jwtProvider.validateToken(invalidToken);
        assertFalse(isValid);
    }
}
