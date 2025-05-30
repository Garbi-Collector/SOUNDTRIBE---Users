package soundtribe.soundtribeusers.services.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.ClassPathResource;
import soundtribe.soundtribeusers.entities.FotoEntity;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserMiniOException;
import soundtribe.soundtribeusers.models.FotoModel;
import soundtribe.soundtribeusers.models.enums.FileType;
import soundtribe.soundtribeusers.repositories.FotoRepository;
import soundtribe.soundtribeusers.services.MinioService;
import io.minio.*;
import io.minio.errors.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    public void removeFoto(FotoEntity currentPhoto) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // Eliminar de MinIO
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(currentPhoto.getFileUrl())
                        .build()
        );

        // Eliminar de la base de datos
        repository.deleteById(currentPhoto.getId());
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

    @Override
    public void uploadDefaultImagesIfNotExist() {
        try {
            String[] defaultImages = {"perfilstandar.png", "ADMIN.png"};

            for (String imageName : defaultImages) {
                // Verificar si la imagen ya existe en MinIO
                try {
                    minioClient.statObject(
                            StatObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(imageName)
                                    .build()
                    );
                    System.out.println("La imagen '" + imageName + "' ya existe en MinIO.");
                } catch (Exception e) {
                    // La imagen no existe, cargarla desde los recursos
                    ClassPathResource resource = new ClassPathResource("assets/" + imageName);

                    if (resource.exists()) {
                        try (InputStream inputStream = resource.getInputStream()) {
                            // Detectar el tipo de contenido
                            String contentType = determineContentType(imageName);

                            // Subir el archivo a MinIO
                            minioClient.putObject(
                                    PutObjectArgs.builder()
                                            .bucket(bucketName)
                                            .object(imageName)
                                            .stream(inputStream, resource.contentLength(), -1)
                                            .contentType(contentType)
                                            .build()
                            );

                            System.out.println("Imagen por defecto '" + imageName + "' cargada en MinIO.");
                        }
                    } else {
                        System.err.println("No se encontró el archivo de imagen '" + imageName + "' en los recursos.");
                    }
                }
            }
        } catch (Exception e) {
            throw new SoundtribeUserMiniOException("Error al cargar imágenes por defecto: " + e.getMessage());
        }
    }

    private String determineContentType(String fileName) {
        if (fileName.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "application/octet-stream";
        }
    }






}

