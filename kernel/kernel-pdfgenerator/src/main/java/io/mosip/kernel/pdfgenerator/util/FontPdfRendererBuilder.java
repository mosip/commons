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
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;
import java.util.Set;

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

    private static Path createSecureTempDirectory() throws IOException {
        Path baseTemp = Path.of(System.getProperty("java.io.tmpdir"), "mosip-pdf-fonts");
        Files.createDirectories(baseTemp);
        FileAttribute<Set<PosixFilePermission>> attrs = getSecurePermissions();
        Path dir = Files.createTempDirectory(baseTemp, "fonts-", attrs);
        LOGGER.debug("Created secure temp font directory: {}", dir);
        return dir;
    }
    private static void initializeFonts(PdfRendererBuilder builder, String ttfFilePath) throws IOException {
        Path tempFontDir = createSecureTempDirectory();
        try {
            if (ttfFilePath.contains(CLASS_PATH)) {
                loadFontsFromClasspath(builder, ttfFilePath, tempFontDir);
            } else {
                loadFontsFromExternalDirectory(builder, ttfFilePath, tempFontDir);
            }
        } finally {
            // Always clean up immediately — don't rely on JVM exit
            deleteDirectoryRecursively(tempFontDir);
        }
    }
    private static FileAttribute<Set<PosixFilePermission>> getSecurePermissions() {
        try {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwx------");
            return PosixFilePermissions.asFileAttribute(perms);
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }

    private static void loadFontsFromClasspath(PdfRendererBuilder builder, String classpathTtfPath, Path tempFontDir) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(classpathTtfPath);
            if (resources.length == 0) {
                LOGGER.info("No fonts found in classpath: {}", classpathTtfPath);
                return;
            }
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null || !filename.toLowerCase().endsWith(".ttf")) {
                    continue;
                }
                Path target = tempFontDir.resolve(filename);
                try (InputStream is = resource.getInputStream()) {
                    Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
                    registerFont(builder, target);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load fonts from classpath: {}", classpathTtfPath, e);
        }
    }

    private static void loadFontsFromExternalDirectory(PdfRendererBuilder builder, String externalTtfDir, Path tempFontDir) {
        File dir = new File(externalTtfDir);
        if (!dir.isDirectory()) {
            LOGGER.warn("External font directory does not exist or is not a directory: {}", externalTtfDir);
            return;
        }
        File[] fontFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".ttf"));
        if (fontFiles == null || fontFiles.length == 0) {
            LOGGER.info("No TTF fonts found in external directory: {}", externalTtfDir);
            return;
        }
        for (File fontFile : fontFiles) {
            Path target = tempFontDir.resolve(fontFile.getName());
            try {
                Files.copy(fontFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                registerFont(builder, target);
            } catch (IOException e) {
                LOGGER.warn("Failed to copy font file: {}", fontFile.getName(), e);
            }
        }
    }

    private static void registerFont(PdfRendererBuilder builder, Path fontPath) {
        String fontName = fontPath.getFileName().toString().replaceAll("\\.tt[fF]$", "");
        try {
            builder.useFont(fontPath.toFile(), fontName);
            LOGGER.info("Successfully registered font: {} ({})", fontName, fontPath);
        } catch (Exception e) {
            LOGGER.warn("Failed to register font: {}", fontPath, e);
        }
    }

    private static void deleteDirectoryRecursively(Path path) {
        if (path == null || !Files.exists(path)) return;
        try (var stream = Files.walk(path)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            LOGGER.debug("Failed to delete: {}", file);
                        }
                    });
        } catch (IOException e) {
            LOGGER.warn("Incomplete cleanup of temp font directory: {}", path, e);
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
