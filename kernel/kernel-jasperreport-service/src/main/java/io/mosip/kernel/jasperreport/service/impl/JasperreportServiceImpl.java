package io.mosip.kernel.jasperreport.service.impl;

import java.io.File;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import io.mosip.kernel.jasperreport.service.JasperreportService;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class JasperreportServiceImpl implements JasperreportService {
	
	@Autowired
	public ResourceLoader resourceLoader;
	
	public void generateReport() {
		try{	 
		    System.out.println("Generating PDF...");
	        String fileName = "hellojasper.jrxml";
	        ClassLoader classLoader = new JasperreportServiceImpl().getClass().getClassLoader();
	 
	        File file = new File(classLoader.getResource(fileName).getFile());
	        
		    JasperReport jasperReport = 
		    JasperCompileManager.compileReport(file.getPath());      
		    JasperPrint jasperPrint = 
		        JasperFillManager.fillReport(jasperReport, new HashMap(), new JREmptyDataSource());      
			        JasperExportManager.exportReportToPdfFile(
			        		jasperPrint, "HelloJasper.pdf");
			       	     
		    System.out.println("HelloJasper.pdf has been generated!");
		}
		catch (JRException e){
		    e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new JasperreportServiceImpl().generateReport();
	}
}
