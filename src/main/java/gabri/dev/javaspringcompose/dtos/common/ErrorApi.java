package gabri.dev.javaspringcompose.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error API DTO class.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorApi {

    /**
     * Timestamp when the error occurred.
     */
    private String timestamp;

    /**
     * Error code number.
     */
    private Integer status;

    /**
     * Error Code name.
     */
    private String error;

    /**
     * Error Code description.
     */
    private String message;
}
