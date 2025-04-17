package gabri.dev.javaspringcompose.services.impl;
import gabri.dev.javaspringcompose.entities.UserEntity;
import gabri.dev.javaspringcompose.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword()) // contraseña ya está encriptada
                .roles(userEntity.getRol().name()) // convierte el rol enum a string
                .build();
    }

}