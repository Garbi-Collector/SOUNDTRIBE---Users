package gabri.dev.javaspringcompose.services.impl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // A modo de prueba, usuario hardcodeado
        if (username.equals("admin")) {
            return new User("admin", "$2a$10$KbQi...tuHashBCrypt...", Collections.emptyList());
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }
}