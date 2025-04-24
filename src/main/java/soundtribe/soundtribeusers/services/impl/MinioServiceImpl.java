package soundtribe.soundtribeusers.services.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            String[] defaultImages = {"ADMIN.png", "perfilstandar.png"};
            Path assetsPath = Paths.get("src/main/resources/assets");

            for (String imageName : defaultImages) {
                if (repository.existsByFileName(imageName)) {
                    System.out.println("La imagen ya existe en la base de datos: " + imageName);
                    continue;
                }

                Path imagePath = assetsPath.resolve(imageName);
                if (!Files.exists(imagePath)) {
                    System.out.println("No se encontró la imagen: " + imageName);
                    continue;
                }

                byte[] imageBytes = Files.readAllBytes(imagePath);
                InputStream inputStream = Files.newInputStream(imagePath);

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(imageName)
                                .stream(inputStream, imageBytes.length, -1)
                                .contentType(FileType.PNG_IMAGE.getMimeType())
                                .build()
                );

                FotoEntity fotoEntity = FotoEntity.builder()
                        .fileName(imageName)
                        .fileType(FileType.PNG_IMAGE)
                        .fileUrl(imageName) // El nombre en MinIO será igual
                        .build();

                repository.save(fotoEntity);

                System.out.println("Imagen subida y guardada: " + imageName);
            }
        } catch (Exception e) {
            throw new SoundtribeUserMiniOException("Error al subir imágenes por defecto: " + e.getMessage());
        }
    }


    @Override
    public void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
    
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                System.out.println("Bucket creado: " + bucketName);
            } else {
                System.out.println("El bucket ya existe: " + bucketName);
            }
        } catch (Exception e) {
            throw new SoundtribeUserMiniOException("Error al verificar/crear el bucket: " + e.getMessage());
        }
    }
    


}

