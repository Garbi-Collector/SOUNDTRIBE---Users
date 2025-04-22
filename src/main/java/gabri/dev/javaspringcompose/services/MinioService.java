package gabri.dev.javaspringcompose.services;

import gabri.dev.javaspringcompose.entities.FotoEntity;
import gabri.dev.javaspringcompose.models.FotoModel;
import io.minio.errors.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public interface MinioService {

    FotoModel upload (MultipartFile file);

    void removeFoto(FotoEntity currentPhoto) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    ResponseEntity<?> getImageById(Long id);
    ResponseEntity<?> getImageByFileName(String filename);
    Optional<FotoEntity> getStandardImage(String fileName);
}
