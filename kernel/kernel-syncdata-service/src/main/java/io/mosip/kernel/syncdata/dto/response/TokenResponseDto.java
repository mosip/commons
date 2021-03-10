package io.mosip.kernel.syncdata.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenResponseDto {

    private String token;
    private String refreshToken;
    private long expiryTime;
    private long refreshExpiryTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
}
