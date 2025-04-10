package gabri.dev.javaspringcompose.models;

import gabri.dev.javaspringcompose.models.enums.FileType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
public class FotoModel {
    private Long id;

    private String fileName;

    private FileType fileType;

    private String fileUrl;

    private LocalDateTime createdAt;
}
