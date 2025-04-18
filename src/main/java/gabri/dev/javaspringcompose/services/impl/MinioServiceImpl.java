package gabri.dev.javaspringcompose.services.impl;

import gabri.dev.javaspringcompose.entities.FotoEntity;
import gabri.dev.javaspringcompose.exceptions.SoundtribeUserMiniOException;
import gabri.dev.javaspringcompose.models.FotoModel;
import gabri.dev.javaspringcompose.models.enums.FileType;
import gabri.dev.javaspringcompose.repositories.FotoRepository;
import gabri.dev.javaspringcompose.services.MinioService;
import io.minio.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Service
public class MinioServiceImpl implements MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private FotoRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    @Transactional
    public FotoModel upload(MultipartFile file) {
        // 1. Validar tipo de archivo
        if (!FileType.PNG_IMAGE.isValid(file)) {
            throw new SoundtribeUserMiniOException("Archivo inválido: debe ser PNG de 98x98 y menos de 4MB");
        }

        // 2. Nombre único para el archivo
        String uniqueFileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        // 3. Subir a MinIO
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(FileType.PNG_IMAGE.getMimeType())
                            .build()
            );
        } catch (Exception e) {
            throw new SoundtribeUserMiniOException("Error al subir imagen a MinIO "+ e);
        }

        // 4. Guardar en base de datos
        FotoEntity fotoEntity = FotoEntity.builder()
                .fileName(file.getOriginalFilename())
                .fileType(FileType.PNG_IMAGE)
                .fileUrl(uniqueFileName)
                .build();

        FotoEntity fotoSaved = repository.save(fotoEntity);

        // 5. Devolver el modelo
        return modelMapper.map(fotoSaved, FotoModel.class);
    }

    @Override
    public ResponseEntity<?> getImageById(Long id) {
        Optional<FotoEntity> optionalFoto = repository.findById(id);
        if (optionalFoto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FotoEntity foto = optionalFoto.get();

        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(foto.getFileUrl())
                            .build()
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(foto.getFileType().getMimeType()))
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            throw new SoundtribeUserMiniOException("No se pudo obtener la imagen desde MinIO "+e);
        }
    }

    @Override
    public ResponseEntity<?> getImageByFileName(String filename) {
        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            throw new SoundtribeUserMiniOException("No se pudo obtener la imagen desde MinIO " + e);
        }
    }

    @Override
    public Optional<FotoEntity> getStandardImage(String fileName) {
        try {
            StatObjectResponse statResponse = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            if (statResponse != null) {
                FotoEntity fotoEntity = new FotoEntity();
                fotoEntity.setFileName(fileName);
                fotoEntity.setFileUrl(fileName);
                fotoEntity.setFileType(FileType.PNG_IMAGE);
                return Optional.of(fotoEntity);
            }
        } catch (Exception e) {
            throw new SoundtribeUserMiniOException("Error al verificar imagen estándar en MinIO: " + e.getMessage());
        }
        return Optional.empty();
    }
}

