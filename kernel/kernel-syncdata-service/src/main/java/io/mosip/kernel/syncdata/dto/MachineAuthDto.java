package io.mosip.kernel.syncdata.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MachineAuthDto {

    private String userId;
    private String password;
    private String authType; // NEW / OTP / REFRESH
    private String refreshToken;
    private String otp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
}
