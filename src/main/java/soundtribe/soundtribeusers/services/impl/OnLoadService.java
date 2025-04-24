package soundtribe.soundtribeusers.services.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import soundtribe.soundtribeusers.configs.MinioConfig;
import soundtribe.soundtribeusers.services.AuthService;
import soundtribe.soundtribeusers.services.MinioService;

@Service
public class OnLoadService {

    @Autowired
    MinioConfig minioConfig;
    @Autowired
    MinioService minioService;
    @Autowired
    AuthService authService;

    @PostConstruct
    void OnInit(){
        //crea el bucket
        minioConfig.init();
        minioService.uploadDefaultImagesIfNotExist();
        authService.checkAndStoreImageIfMissing("perfilstandar.png");
        authService.checkAndStoreImageIfMissing("ADMIN.png");
        authService.crearUsuariosPorDefecto();
    }
}
