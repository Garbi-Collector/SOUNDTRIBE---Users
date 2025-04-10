package gabri.dev.javaspringcompose.controllers;

import gabri.dev.javaspringcompose.models.FotoModel;
import gabri.dev.javaspringcompose.services.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/fotos")
@CrossOrigin(origins = "*") // Opcional: permite requests desde el frontend
public class FotoController {

    @Autowired
    private MinioService minioService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FotoModel> uploadFoto(@RequestParam("file") MultipartFile file) {
        FotoModel fotoModel = minioService.upload(file);
        return ResponseEntity.ok(fotoModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFotoById(@PathVariable Long id) {
        return minioService.getImageById(id);
    }
}
