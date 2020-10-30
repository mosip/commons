package io.mosip.kernel.keymanagerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Certificate Info class
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateInfo<T> {
        
	private String alias;

    private T certificate;
    
}