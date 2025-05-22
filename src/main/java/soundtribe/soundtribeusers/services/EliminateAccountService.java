package soundtribe.soundtribeusers.services;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public interface EliminateAccountService {
    @Async
    @Transactional
    void eliminarCuenta(String jwt);
}
