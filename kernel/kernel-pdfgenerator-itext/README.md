# Kernel Pdfgenerator Itext

## Overview
This api provide functions related to generation of PDF in MOSIP.

## Usage 
1. Exceptions to be handled while using this functionality:

- PDFGeneratorException
- IOException 

2. Maven Dependency
 
 ```
 <dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-pdfgenerator-itext</artifactId>
			<version>${project.version}</version>
 </dependency>
 ```
 
3. Usage Sample:
 
- Input processed Template as inputStream and generate OutputStream containing the resulting PDF
 
 ```
@Autowired
 PDFGenerator pdfGenerator;
 
     ClassLoader classLoader = getClass().getClassLoader();
	  String inputFile = classLoader.getResource("csshtml.html").getFile();
	  InputStream is = new FileInputStream(inputFile);
     OutputStream os = pdfGenerator.generate(inputStream);
  
 ```

- Input processed Template as String and generate OutputStream containing the resulting PDF
 
 ```
@Autowired
 PDFGenerator pdfGenerator;
 
       ClassLoader classLoader = getClass().getClassLoader();
		String inputFileName = classLoader.getResource("test.html").getFile();
		BufferedReader br = new BufferedReader(new FileReader(inputFileName));
		String line;
		StringBuilder template = new StringBuilder();
		while ((line = br.readLine()) != null) {
			template.append(line.trim());
		}
       OutputStream os = pdfGenerator.generate(template); 
 ```
 
- Generate PDF take processed template as file, output file path and output file name and generate the resulting PDF in given output path with the given output file name
 
 
 ```
 @Autowired
 PDFGenerator pdfGenerator;
 
        String outputPath = System.getProperty("user.dir");
		ClassLoader classLoader = getClass().getClassLoader();
		String inputFile = classLoader.getResource("textcontant.txt").getFile();
		String generatedPdfFileName = "textcontant";
        pdfGenerator.generate(templatePath,outpuFilePath,outputFileName);
       
 ```
 
- Input processed Template as inputStream, resource file path  and generate OutputStream containing the resulting PDF.
 
 ```
@Autowired
 PDFGenerator pdfGenerator;
 
      ClassLoader classLoader = getClass().getClassLoader();
		String inputFile = classLoader.getResource("responsive.html").getFile();
		File file = new File(inputFile);
		if (file.getParentFile().isDirectory()) {
			file = file.getParentFile();
		}
		String resourceLoc = file.getAbsolutePath();
		InputStream is = new FileInputStream(inputFile);
		ByteArrayOutputStream bos = (ByteArrayOutputStream) pdfGenerator.generate(is, resourceLoc);
  
 ```
 
- Input is list of BufferedImage and generate Byte Array containing the resulting PDF.
 
 ```
		@Autowired
 		PDFGenerator pdfGenerator;
 		
		BufferedImage bufferedImage;
		BufferedImage bufferedImage2;

		List<BufferedImage> bufferedImages = new ArrayList<>();
	
		URL url = PDFGeneratorTest.class.getResource("/Change.jpg");
		URL url2 = PDFGeneratorTest.class.getResource("/nelsonmandela1-2x.jpg");

		bufferedImage = ImageIO.read(url);
		bufferedImages.add(bufferedImage);
		bufferedImage2 = ImageIO.read(url2);
		bufferedImages.add(bufferedImage2);
		
		byte[] data = pdfGenerator.asPDF(bufferedImages);
  
 ```

- Input is list of URL of pdf files and generate Byte Array containing the resulting PDF.
 
```
		@Autowired
 		PDFGenerator pdfGenerator;

		List<URL> pdfFiles = new ArrayList<URL>(Arrays.asList(PDFGeneratorTest.class.getResource("/sample.pdf"),
				PDFGeneratorTest.class.getResource("/pdf-sample.pdf")));
		byte[] byteArray = pdfGenerator.mergePDF(pdfFiles);
  
 ```
 
- Create a Password Protected PDF.
 
```
@Autowired
PDFGenerator pdfGenerator;

StringBuilder htmlString = new StringBuilder();
htmlString.append("<html><body> This is HMTL to PDF conversion Example</body></html>");
InputStream htmlStream = new ByteArrayInputStream(htmlString.toString().getBytes());
ByteArrayOutputStream outputStream = (ByteArrayOutputStream) pdfGenerator.generate(htmlStream,"userpassword".getBytes());
File file = new File(filename);
FileUtils.writeByteArrayToFile(file, outputStream.toByteArray());
  
 ```





