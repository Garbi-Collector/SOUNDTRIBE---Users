package soundtribe.soundtribeusers.models.enums;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public enum FileType {

    PNG_IMAGE("image/png", 4 * 1024 * 1024); // solo definimos tamaño máximo

    private final String mimeType;
    private final long maxSizeBytes;

    FileType(String mimeType, long maxSizeBytes) {
        this.mimeType = mimeType;
        this.maxSizeBytes = maxSizeBytes;
    }

    public boolean isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;

        if (!file.getContentType().equalsIgnoreCase(this.mimeType)) return false;
        if (file.getSize() > this.maxSizeBytes) return false;

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) return false;

            // Validar que sea cuadrada
            return image.getWidth() == image.getHeight();
        } catch (IOException e) {
            return false;
        }
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }
}
