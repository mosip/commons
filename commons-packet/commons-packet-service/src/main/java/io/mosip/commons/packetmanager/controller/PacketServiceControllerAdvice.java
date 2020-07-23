package io.mosip.commons.packetmanager.controller;

import io.mosip.commons.packet.exception.FieldNameNotFoundException;
import io.mosip.commons.packet.exception.NoAvailableProviderException;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class PacketServiceControllerAdvice {

    @ExceptionHandler(FieldNameNotFoundException.class)
    public ResponseEntity fieldNameNotFoundException(FieldNameNotFoundException e) {
        return buildPrintApiExceptionResponse((Exception) e);
    }

    @ExceptionHandler(NoAvailableProviderException.class)
    public ResponseEntity noAvailableProviderException(NoAvailableProviderException e) {
        return buildPrintApiExceptionResponse((Exception) e);
    }

    private ResponseEntity buildPrintApiExceptionResponse(Exception e) {

        ResponseWrapper<String> response = new ResponseWrapper<>();
        if (e instanceof BaseCheckedException) {
            List<String> errorCodes = ((BaseCheckedException) e).getCodes();
            List<String> errorTexts = ((BaseCheckedException) e).getErrorTexts();

            List<ServiceError> errors = errorTexts.parallelStream()
                    .map(errMsg -> new ServiceError(errorCodes.get(errorTexts.indexOf(errMsg)), errMsg)).distinct()
                    .collect(Collectors.toList());
            response.setErrors(errors);
        }
        if (e instanceof BaseUncheckedException) {
            List<String> errorCodes = ((BaseUncheckedException) e).getCodes();
            List<String> errorTexts = ((BaseUncheckedException) e).getErrorTexts();

            List<ServiceError> errors = errorTexts.parallelStream()
                    .map(errMsg -> new ServiceError(errorCodes.get(errorTexts.indexOf(errMsg)), errMsg)).distinct()
                    .collect(Collectors.toList());
            response.setErrors(errors);
        }
        response.setResponsetime(DateUtils.getUTCCurrentDateTime());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }
}
