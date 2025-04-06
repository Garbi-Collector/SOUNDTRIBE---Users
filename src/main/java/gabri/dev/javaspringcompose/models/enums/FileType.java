package gabri.dev.javaspringcompose.models.enums;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public enum FileType {

    PNG_IMAGE("image/png", 4 * 1024 * 1024, 98, 98); // 4 MB, 98x98 px

    private final String mimeType;
    private final long maxSizeBytes;
    private final int expectedWidth;
    private final int expectedHeight;

    FileType(String mimeType, long maxSizeBytes, int expectedWidth, int expectedHeight) {
        this.mimeType = mimeType;
        this.maxSizeBytes = maxSizeBytes;
        this.expectedWidth = expectedWidth;
        this.expectedHeight = expectedHeight;
    }

    public boolean isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;

        if (!file.getContentType().equalsIgnoreCase(this.mimeType)) return false;
        if (file.getSize() > this.maxSizeBytes) return false;

        if (this.mimeType.startsWith("image/")) {
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image == null) return false;

                return image.getWidth() == this.expectedWidth && image.getHeight() == this.expectedHeight;
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public int getExpectedWidth() {
        return expectedWidth;
    }

    public int getExpectedHeight() {
        return expectedHeight;
    }
}
