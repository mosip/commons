package io.mosip.commons.packetmanager.controller;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class PacketServiceControllerAdvice {

    @ExceptionHandler(BaseCheckedException.class)
    public ResponseEntity baseCheckedException(BaseCheckedException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(BaseUncheckedException.class)
    public ResponseEntity baseUnCheckedException(BaseUncheckedException e) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse(e.getErrorCode(), e.getMessage()));
    }

    private ResponseWrapper<ServiceError> getResponse(String errorCode, String message) {
        ResponseWrapper<ServiceError> response = new ResponseWrapper<>();
        ServiceError serviceError = new ServiceError(errorCode, message);
        List<ServiceError> errors = new ArrayList();
        errors.add(serviceError);
        response.setErrors(errors);
        return response;
    }
}
