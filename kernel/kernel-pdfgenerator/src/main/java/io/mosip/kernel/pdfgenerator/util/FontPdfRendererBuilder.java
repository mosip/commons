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
import java.util.Objects;

public class FontPdfRendererBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FontPdfRendererBuilder.class);

    private static final String CLASS_PATH = "classpath";

    private static PdfRendererBuilder builderInstance;

    private FontPdfRendererBuilder() {
        // Private constructor to enforce singleton
    }

    public static synchronized PdfRendererBuilder getBuilder(String ttfFilePath) throws IOException {
        if (builderInstance == null) {
            builderInstance = new PdfRendererBuilder();
            initializeFonts(builderInstance,ttfFilePath);
        }
        return builderInstance;
    }

    private static void initializeFonts(PdfRendererBuilder builder,String ttfFilePath) throws IOException {
        File tempFontDir = Files.createTempDirectory("loaded-fonts").toFile();
        tempFontDir.deleteOnExit();
        if(ttfFilePath.contains(CLASS_PATH)) {
            // Load fonts from classpath
            loadFontsFromClasspath(builder, ttfFilePath, tempFontDir);
        }else {
            // Load fonts from external directory
            loadFontsFromExternalDirectory(builder, ttfFilePath, tempFontDir);
        }
    }
    /**
     * Load fonts from classpath
     */
    private static void loadFontsFromClasspath(PdfRendererBuilder builder, String classpathTtfPath, File tempFontDir) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(classpathTtfPath);
            if (resources.length == 0) {
                LOGGER.info("No fonts found in classpath at {}", classpathTtfPath);
                return;
            }
            for (Resource resource : resources) {
                try (InputStream fontStream = resource.getInputStream()) {
                    File tempFontFile = new File(tempFontDir, Objects.requireNonNull(resource.getFilename()));
                    Files.copy(fontStream, tempFontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Loaded font from classpath: {}", tempFontFile.getAbsolutePath());
                    builder.useFont(tempFontFile, tempFontFile.getName().replace(".ttf", ""));
                }
            }
        }catch (Exception e) {
            LOGGER.error("Failed to load fonts from classpath: {}. Reason: {}", classpathTtfPath, e.getMessage());
        }
    }

    /**
     * Load fonts from external directory
     */
    private static void loadFontsFromExternalDirectory(PdfRendererBuilder builder, String externalTtfDir, File tempFontDir) throws IOException {
        try {
            File fontDir = new File(externalTtfDir);
            File[] fontFiles = fontDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttf"));
            if (fontFiles == null || fontFiles.length == 0) {
                LOGGER.info("No TTF fonts found in external directory: {}", externalTtfDir);
                return;
            }
            for (File fontFile : fontFiles) {
                File tempFontFile = new File(tempFontDir, fontFile.getName());
                Files.copy(fontFile.toPath(), tempFontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Loaded font from external directory: {}", tempFontFile.getAbsolutePath());
                builder.useFont(tempFontFile, tempFontFile.getName().replace(".ttf", ""));
            }
        }catch (Exception e){
            LOGGER.error("Failed to load fonts from external font directory: {}. Reason: {}", externalTtfDir, e.getMessage());
        }
    }

}
