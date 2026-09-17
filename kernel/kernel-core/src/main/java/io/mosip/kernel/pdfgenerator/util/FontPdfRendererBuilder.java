package io.mosip.kernel.pdfgenerator.util;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Singleton OpenHTMLToPDF {@link PdfRendererBuilder} with TTF fonts registered.
 * <p>
 * {@link #getBuilder(String)} copies fonts from a classpath glob or filesystem
 * directory into an owner-restricted temp folder and registers each {@code .ttf}
 * with the builder.
 * </p>
 */
public class FontPdfRendererBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FontPdfRendererBuilder.class);

    private static final String CLASS_PATH = "classpath";

    private static PdfRendererBuilder builderInstance;

    /**
     * Prevents instantiation; use {@link #getBuilder(String)}.
     */
    private FontPdfRendererBuilder() {
        // Private constructor to enforce singleton
    }

    /**
     * Returns the process-wide builder, initializing fonts on first call.
     *
     * @param ttfFilePath classpath glob (contains {@code classpath}) or filesystem directory of {@code .ttf} files
     * @return shared {@link PdfRendererBuilder}
     * @throws IOException if the temp font directory cannot be created
     */
    public static synchronized PdfRendererBuilder getBuilder(String ttfFilePath) throws IOException {
        if (builderInstance == null) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            initializeFonts(builder,ttfFilePath);
            builderInstance = builder;
        }
        return builderInstance;
    }

    /**
     * Copies TTF files from classpath or filesystem into {@code tempFontDir} and registers them.
     *
     * @param builder     builder to register fonts on
     * @param ttfFilePath classpath glob or filesystem directory
     * @throws IOException if the temp directory cannot be created
     */
    private static void initializeFonts(PdfRendererBuilder builder,String ttfFilePath) throws IOException {
        File tempFontDir = createOwnerRestrictedTempDirectory("loaded-fonts");
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
     * Loads {@code .ttf} resources matching {@code classpathTtfPath} into {@code tempFontDir}.
     *
     * @param builder          builder to register fonts on
     * @param classpathTtfPath Spring resource glob such as {@code classpath:/pdf-generator/*.ttf}
     * @param tempFontDir      directory to copy font files into
     * @throws IOException unused checked signature
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
     * Loads {@code .ttf} files from {@code externalTtfDir} into {@code tempFontDir}.
     *
     * @param builder        builder to register fonts on
     * @param externalTtfDir filesystem directory containing {@code .ttf} files
     * @param tempFontDir    directory to copy font files into
     * @throws IOException unused checked signature
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

    /**
     * Creates a directory only the process owner can read, write, or traverse.
     * POSIX systems set {@code rwx------} atomically at creation so there is no
     * window in the shared system temp directory. Other systems use a private
     * parent under the user home (not the world-writable system temp).
     *
     * @param prefix directory name prefix
     * @return owner-restricted directory
     * @throws IOException if the directory cannot be created
     */
    static File createOwnerRestrictedTempDirectory(String prefix) throws IOException {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            FileAttribute<Set<PosixFilePermission>> ownerOnly =
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
            return Files.createTempDirectory(prefix, ownerOnly).toFile();
        }
        Path privateBase = Files.createDirectories(Path.of(System.getProperty("user.home"), ".mosip", "tmp"));
        File dir = Files.createDirectory(privateBase.resolve(prefix + "-" + UUID.randomUUID())).toFile();
        // NTFS does not map owner-only bits the same way as POSIX; user.home is already per-user.
        boolean ownerOnly = dir.setReadable(true, true)
                & dir.setWritable(true, true)
                & dir.setExecutable(true, true);
        if (!ownerOnly) {
            LOGGER.debug("Owner-only permission bits were not applied on {}", dir.getAbsolutePath());
        }
        return dir;
    }

}
