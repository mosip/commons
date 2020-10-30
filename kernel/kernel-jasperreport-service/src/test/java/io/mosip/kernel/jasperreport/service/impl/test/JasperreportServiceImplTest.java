package io.mosip.kernel.jasperreport.service.impl.test;

import static org.junit.Assert.*;

import org.junit.Test;

import io.mosip.kernel.jasperreport.service.impl.JasperreportServiceImpl;

public class JasperreportServiceImplTest {

	@Test
	public void testGenerateReport() {
		new JasperreportServiceImpl().generateReport();
	}

}
