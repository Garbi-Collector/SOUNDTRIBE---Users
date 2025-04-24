package soundtribe.soundtribeusers.services;

;
import io.minio.errors.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import soundtribe.soundtribeusers.entities.FotoEntity;
import soundtribe.soundtribeusers.models.FotoModel;

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

    void uploadDefaultImagesIfNotExist();

}
