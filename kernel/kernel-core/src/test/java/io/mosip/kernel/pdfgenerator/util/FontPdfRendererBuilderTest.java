package io.mosip.kernel.pdfgenerator.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.Test;

public class FontPdfRendererBuilderTest {

	@Test
	public void createOwnerRestrictedTempDirectory_isOwnerOnlyOnPosix() throws IOException {
		File dir = FontPdfRendererBuilder.createOwnerRestrictedTempDirectory("loaded-fonts");
		try {
			assertTrue(dir.isDirectory());
			if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
				assertEquals(PosixFilePermissions.fromString("rwx------"),
						Files.getPosixFilePermissions(dir.toPath()));
			}
		} finally {
			Files.deleteIfExists(dir.toPath());
		}
	}

	@Test
	public void getBuilder_initializesFontsWithoutError() throws IOException {
		assertNotNull(FontPdfRendererBuilder.getBuilder("classpath:/pdf-generator/*.ttf"));
	}
}
