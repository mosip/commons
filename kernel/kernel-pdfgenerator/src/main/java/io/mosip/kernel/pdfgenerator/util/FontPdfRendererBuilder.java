package io.mosip.kernel.pdfgenerator.util;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FontPdfRendererBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FontPdfRendererBuilder.class);

    private static PdfRendererBuilder builderInstance;

    private FontPdfRendererBuilder() {
        // Private constructor to enforce singleton
    }

    public static synchronized PdfRendererBuilder getBuilder() throws IOException {
        if (builderInstance == null) {
            builderInstance = new PdfRendererBuilder();
            initializeFonts(builderInstance);
        }
        return builderInstance;
    }

    private static void initializeFonts(PdfRendererBuilder builder) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/pdf-generator/*.ttf");

        if (resources.length == 0) {
            LOGGER.info("Font family not found taking default font");
        }else {

            File tempFontDir = Files.createTempDirectory("loaded-fonts").toFile();
            tempFontDir.deleteOnExit();

            for (Resource resource : resources) {
                try (InputStream fontStream = resource.getInputStream()) {
                    File tempFontFile = new File(tempFontDir, resource.getFilename());
                    Files.copy(fontStream, tempFontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    LOGGER.info("Loading font: {}", tempFontFile.getAbsolutePath());
                    builder.useFont(tempFontFile, resource.getFilename().replace(".ttf", ""));
                }
            }
        }
    }
}
