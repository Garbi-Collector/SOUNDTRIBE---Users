package gabri.dev.javaspringcompose.services;

import gabri.dev.javaspringcompose.models.FotoModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface MinioService {

    FotoModel upload (MultipartFile file);
    ResponseEntity<?> getImageById(Long id);
    ResponseEntity<?> getImageByFileName(String filename);
}
