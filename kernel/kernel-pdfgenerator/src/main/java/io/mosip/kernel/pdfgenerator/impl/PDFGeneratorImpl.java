package io.mosip.kernel.pdfgenerator.impl;


import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.mosip.kernel.core.keymanager.model.CertificateEntry;
import io.mosip.kernel.core.pdfgenerator.exception.PDFGeneratorException;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.pdfgenerator.constant.PDFGeneratorExceptionCodeConstant;
import io.mosip.kernel.pdfgenerator.util.FontPdfRendererBuilder;
import io.mosip.kernel.pdfgenerator.util.SignatureHandler;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.*;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.security.auth.x500.X500Principal;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The PdfGeneratorImpl is the class you will use most when converting processed
 * Template to PDF. It contains a series of methods that accept processed
 * Template as a {@link String}, {@link File}, or {@link InputStream}, and
 * convert it to PDF in the form of an {@link OutputStream}, {@link File}
 *
 * @author Dhanendra Sahu
 *
 * @since 1.3.0
 *
 */
@Component
public class PDFGeneratorImpl implements PDFGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(PDFGeneratorImpl.class);

	private static final String OUTPUT_FILE_EXTENSION = ".pdf";

	@Value("${mosip.kernel.pdf_owner_password:\"\"}")
	private String pdfOwnerPassword;

	@Override
	public OutputStream generate(InputStream is) throws IOException {
		isValidInputStream(is);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			PdfRendererBuilder builder = FontPdfRendererBuilder.getBuilder();
			String wellFormedHtml = preprocessHtml(is);
			builder.withHtmlContent(wellFormedHtml, null); // Convert InputStream to String
			builder.toStream(os);
			builder.run();
		} catch (Exception e) {
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		return os;
	}

	private String preprocessHtml(InputStream is) throws IOException {
		// Specify UTF-8 encoding explicitly when reading the input stream
		String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);

		// Parse the HTML content with Jsoup
		Document document = Jsoup.parse(html);

		// Preserve non-Latin characters by setting the correct output encoding
		document.outputSettings()
				.syntax(Document.OutputSettings.Syntax.xml)
				.charset(StandardCharsets.UTF_8); // Ensure UTF-8 encoding for output

		return document.html(); // Returns well-formed XHTML with proper encoding
	}

	@Override
	public OutputStream generate(String template) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if(template.isEmpty()){
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.INPUTSTREAM_NULL_EMPTY_EXCEPTION.getErrorCode(),
					PDFGeneratorExceptionCodeConstant.INPUTSTREAM_NULL_EMPTY_EXCEPTION.getErrorMessage());
		}
		try {
			Document document = Jsoup.parse(template);
			document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
			PdfRendererBuilder builder = FontPdfRendererBuilder.getBuilder();
			builder.withHtmlContent(document.html(), null);
			builder.toStream(os);
			builder.run();
		}catch (Exception e) {
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		return os;
	}

	@Override
	public void generate(String templatePath, String outputFilePath, String outputFileName) throws IOException {
		File outputFile = new File(outputFilePath + outputFileName + OUTPUT_FILE_EXTENSION);
		try {


		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);

			try (BufferedReader reader = new BufferedReader(new FileReader(templatePath));
				 PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				contentStream.beginText();
				contentStream.setFont(PDType1Font.HELVETICA, 12);
				contentStream.newLineAtOffset(50, 750);

				String line;
				while ((line = reader.readLine()) != null) {
					contentStream.showText(line);
					contentStream.newLineAtOffset(0, -15); // Move down for the next line
				}

				contentStream.endText();
			}

			document.save(outputFile);
		}
		}catch (Exception e){
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
	}

	@Override
	public OutputStream generate(InputStream is, String resourceLoc) throws IOException {
		return generate(is); // Placeholder, as no specific resource handling is demonstrated in the existing implementation
	}

	@Override
	public byte[] asPDF(List<BufferedImage> bufferedImages) throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			 PDDocument document = new PDDocument()) {

			for (BufferedImage bufferedImage : bufferedImages) {
				PDPage page = new PDPage(PDRectangle.A4);
				document.addPage(page);

				PDImageXObject image = LosslessFactory.createFromImage(document, bufferedImage);

				try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
					contentStream.drawImage(image, 50, 100, 500, 700); // Example scaling
				}
			}

			document.save(byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		}
	}

	@Override
	public byte[] mergePDF(List<URL> pdfFiles) throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			PDFMergerUtility merger = new PDFMergerUtility();
			merger.setDestinationStream(byteArrayOutputStream);
			for (URL url : pdfFiles) {
				try (PDDocument tempDocument = PDDocument.load(url.openStream())) {
					merger.addSource(url.openStream());  // Add each file to the merger
				}
			}
			merger.mergeDocuments(null); // Merge all documents
			return byteArrayOutputStream.toByteArray();
		}
	}

	@Override
	public OutputStream signAndEncryptPDF(byte[] pdf, io.mosip.kernel.core.pdfgenerator.model.Rectangle rectangle,
										  String reason, int pageNumber, Provider provider,
										  CertificateEntry<X509Certificate, PrivateKey> certificateEntry, String password)
			throws IOException {
		OutputStream os = new ByteArrayOutputStream();
		ByteArrayOutputStream encryptedOS = new ByteArrayOutputStream();

		if (password !=null && !password.trim().isEmpty()) {
			try (PDDocument document = PDDocument.load(pdf)) {
				AccessPermission accessPermission = new AccessPermission();
				accessPermission.setCanPrint(true);
				accessPermission.setCanModify(true);
				accessPermission.setCanAssembleDocument(false);

				StandardProtectionPolicy protectionPolicy = new StandardProtectionPolicy(pdfOwnerPassword, password, accessPermission);
				protectionPolicy.setEncryptionKeyLength(128);

				document.protect(protectionPolicy);
				document.save(encryptedOS);
				pdf = encryptedOS.toByteArray();
			}
		}

		try (PDDocument document = PDDocument.load(pdf, password)){
			String signName = getCertificateCommonName(certificateEntry.getChain()[0].getSubjectX500Principal());
			PDSignature sign = new PDSignature();
			sign.setType(COSName.getPDFName("Sig"));
			sign.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
			sign.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
			sign.setName(signName);
			sign.setReason(reason);
			sign.setSignDate(Calendar.getInstance());

			SignatureOptions options = new SignatureOptions();
			options.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE);
			options.setPage(pageNumber);

			Rectangle2D rectangle2D = new Rectangle2D.Float(
					rectangle.getLlx(), rectangle.getLly(), rectangle.getUrx(), rectangle.getUry());

			PDRectangle rect = createSignatureRectangle(rectangle2D);
			options.setVisualSignature(createVisualSignatureTemplate(signName, reason,rect));

			document.addSignature(sign, new SignatureHandler(certificateEntry.getPrivateKey(), certificateEntry.getChain(), provider), options);
			document.saveIncremental(os);
			os.close();
			document.close();
			IOUtils.closeQuietly(options);
		} catch (IOException e) {
			LOGGER.error("While generating pdf exception occured {}",e.getCause());
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		return os;
	}
	private String getCertificateCommonName(X500Principal x500CertPrincipal) {
		X500Name x500Name = new X500Name(x500CertPrincipal.getName());
		RDN[] rdns = x500Name.getRDNs(BCStyle.CN);
		if (rdns.length == 0) {
			return "";
		}
		return IETFUtils.valueToString((rdns[0]).getFirst().getValue());
	}

	private PDRectangle createSignatureRectangle(Rectangle2D rectangle2D) {
		float x = (float) rectangle2D.getX();
		float y = (float) rectangle2D.getY();
		float width = (float) rectangle2D.getWidth();
		float height = (float) rectangle2D.getHeight();

		PDRectangle rect = new PDRectangle();
		rect.setLowerLeftX(x);
		rect.setUpperRightX(x+width);
		rect.setLowerLeftY(y);
		rect.setUpperRightY(y+height);

		return rect;
	}

	private static InputStream createVisualSignatureTemplate(String signName,String reason, PDRectangle rectangle)
			throws IOException {
		try (PDDocument doc = new PDDocument()) {
			PDAcroForm acroForm = new PDAcroForm(doc);
			doc.getDocumentCatalog().setAcroForm(acroForm);

			PDSignatureField signatureField = new PDSignatureField(acroForm);
			PDAnnotationWidget widget = signatureField.getWidgets().get(0);
			acroForm.setSignaturesExist(true);
			acroForm.setAppendOnly(true);
			acroForm.getFields().add(signatureField);

			widget.setRectangle(rectangle);

			PDStream stream = new PDStream(doc);
			PDFormXObject formXObject = new PDFormXObject(stream);
			PDResources resources = new PDResources();
			formXObject.setResources(resources);
			formXObject.setFormType(1);

			PDRectangle bbox = new PDRectangle(rectangle.getWidth(), rectangle.getHeight());
			formXObject.setBBox(bbox);

			PDAppearanceDictionary appearanceDictionary = new PDAppearanceDictionary();
			PDAppearanceStream appearanceStream = new PDAppearanceStream(formXObject.getCOSObject());
			appearanceDictionary.setNormalAppearance(appearanceStream);
			widget.setAppearance(appearanceDictionary);

			try (PDPageContentStream cs = new PDPageContentStream(doc, appearanceStream)) {
				PDFont font = PDType1Font.HELVETICA_BOLD;
				float fontSize = 10;
				float leading = 1.2f * fontSize;
				float width = rectangle.getWidth() - 10;

				cs.beginText();
				cs.setFont(font, fontSize);
				cs.setLeading(leading);
				cs.newLineAtOffset(5, rectangle.getHeight() - leading - 5);

				String signText = "Digitally signed " + signName +
						"\nDate: " + new java.text.SimpleDateFormat("yyyy.MM.DD HH:mm:ss z").format(Calendar.getInstance().getTime()) +
						"\nReason:"+ reason;

				List<String> lines = getWrappedLines(signText, font, fontSize, width);
				for (String line : lines) {
					cs.showText(line);
					cs.newLine();
				}
				cs.endText();
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			doc.save(baos);
			return new ByteArrayInputStream(baos.toByteArray());
		}
	}

	private static List<String> getWrappedLines(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
		// Split text by explicit newlines first
		String[] explicitLines = text.split("\n");
		List<String> lines = new ArrayList<>();

		for (String explicitLine : explicitLines) {
			// Split the explicit line by spaces, underscores, or hyphens
			String[] words = explicitLine.split("(?=[\\s_-])|(?<=[\\s_-])");
			StringBuilder currentLine = new StringBuilder();

			for (String word : words) {
				float wordWidth = font.getStringWidth(word) / 1000 * fontSize;
				float currentLineWidth = font.getStringWidth(currentLine.toString()) / 1000 * fontSize;

				if (currentLineWidth + wordWidth > maxWidth) {
					lines.add(currentLine.toString().trim());
					currentLine = new StringBuilder(word);
				} else {
					currentLine.append(word);
				}
			}
			// Add the remaining part of the current line
			if (currentLine.length() > 0) {
				lines.add(currentLine.toString().trim());
			}
		}

		return lines;
	}
	private void isValidInputStream(InputStream dataInputStream) {
		if (EmptyCheckUtils.isNullEmpty(dataInputStream)) {
			throw new PDFGeneratorException("PDF_GENERATOR_001", "Input stream is null or empty");
		}
	}
}
