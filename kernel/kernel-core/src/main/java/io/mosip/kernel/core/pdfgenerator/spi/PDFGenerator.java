package io.mosip.kernel.core.pdfgenerator.spi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.List;

import io.mosip.kernel.core.keymanager.model.CertificateEntry;
import io.mosip.kernel.core.pdfgenerator.model.Rectangle;

/**
 * Generates, merges, and digitally signs PDF documents.
 * <p>
 * Contract: implementations perform CPU-bound PDF rendering (and optionally
 * crypto) with no MOSIP HTTP. HTML/template inputs must be non-null. Call when
 * producing receipts, acknowledgements, or signed PDFs.
 * </p>
 *
 * @author Urvil Joshi
 * @author Uday Kumar
 * @author Neha
 * @since 1.0.0
 */
public interface PDFGenerator {
	/**
	 * Renders HTML from {@code htmlStream} into a PDF.
	 *
	 * @param htmlStream never-null HTML input
	 * @return never-null PDF bytes as an output stream
	 * @throws IOException when reading HTML or writing PDF fails
	 */
	public OutputStream generate(InputStream htmlStream) throws IOException;

	/**
	 * Renders HTML template text into a PDF.
	 *
	 * @param template never-null HTML string
	 * @return never-null PDF bytes as an output stream
	 * @throws IOException when writing PDF fails
	 */
	public OutputStream generate(String template) throws IOException;

	/**
	 * Renders a template file to a PDF on disk.
	 *
	 * @param templatePath   never-null, never-blank path to the HTML template
	 * @param outputFilePath never-null directory where the PDF is written
	 * @param outputFileName never-null file name without requiring an extension
	 * @throws IOException when reading the template or writing the PDF fails
	 */
	public void generate(String templatePath, String outputFilePath, String outputFileName) throws IOException;

	/**
	 * Renders HTML from {@code dataStream} using extra resources at {@code resourceLoc}.
	 *
	 * @param dataStream  never-null HTML input
	 * @param resourceLoc never-null path or URL prefix for CSS/images
	 * @return never-null PDF bytes as an output stream
	 * @throws IOException when reading HTML or writing PDF fails
	 */
	public OutputStream generate(InputStream dataStream, String resourceLoc) throws IOException;

	/**
	 * Combines raster images into a single PDF.
	 *
	 * @param bufferedImages never-null, never-empty list of images
	 * @return never-null PDF bytes
	 * @throws IOException when encoding the PDF fails
	 */
	public byte[] asPDF(List<BufferedImage> bufferedImages) throws IOException;

	/**
	 * Concatenates PDF files identified by {@code pdfLists}.
	 *
	 * @param pdfLists never-null list of PDF URLs
	 * @return never-null merged PDF bytes
	 * @throws IOException when downloading or merging PDFs fails
	 */
	public byte[] mergePDF(List<URL> pdfLists) throws IOException;

	/**
	 * Digitally signs {@code pdf} and optionally password-protects the result.
	 *
	 * @param pdf              never-null PDF bytes to sign
	 * @param rectangle        never-null visible signature box
	 * @param reason           signing reason; may be blank
	 * @param pageNumber       1-based page index for the signature rectangle
	 * @param provider         JCA provider; may be null to use the default
	 * @param certificateEntry never-null certificate and private key pair
	 * @param password         owner password; may be null to skip encryption
	 * @return never-null signed (and optionally encrypted) PDF stream
	 * @throws IOException              when reading or writing PDF bytes fails
	 * @throws GeneralSecurityException when signing or encryption fails
	 */
	OutputStream signAndEncryptPDF(byte[] pdf, Rectangle rectangle, String reason, int pageNumber, Provider provider,
			CertificateEntry<X509Certificate, PrivateKey> certificateEntry, String password)
			throws IOException, GeneralSecurityException;

}
