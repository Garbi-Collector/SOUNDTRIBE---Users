package soundtribe.soundtribeusers.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import soundtribe.soundtribeusers.models.enums.FileType;

import java.time.LocalDateTime;

@Data
public class FotoModel {
    private Long id;

    private String fileName;

    private FileType fileType;

    private String fileUrl;

    private LocalDateTime createdAt;
}
