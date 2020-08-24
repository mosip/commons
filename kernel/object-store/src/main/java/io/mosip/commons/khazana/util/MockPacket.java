package io.mosip.commons.khazana.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MockPacket {

    private static final String DIRECTORY = "C:\\Users\\M1045447\\Desktop\\decryptor\\packets";

    public static InputStream getMockPacket(String id, String packetName) {
        File file = new File("C:\\Users\\M1045447\\Downloads\\10001100770000320200720092256.zip");
        InputStream data = null;
        try {
            data = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStream sourceFolderInputStream = data != null?unzipAndGetFile(data, id + "_" + packetName) : null;

        return sourceFolderInputStream;
    }

    private static InputStream unzipAndGetFile(InputStream packetStream, String file) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean flag = false;
        byte[] buffer = new byte[2048];
        try (ZipInputStream zis = new ZipInputStream(packetStream)) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
                if (FilenameUtils.equals(fileNameWithOutExt, file, true, IOCase.INSENSITIVE)) {
                    int len;
                    flag = true;
                    while ((len = zis.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    break;
                }
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                packetStream.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (flag) {
            return new ByteArrayInputStream(out.toByteArray());
        }

        return null;
    }

    public static boolean putMock(String containerName, String objectName, InputStream data) {
        File file = new File(DIRECTORY + "\\" + containerName);
        if (!file.exists())
            file.mkdir();
        File f = new File(file.getPath() + "\\" + objectName + ".zip");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ZipOutputStream zipOS = new ZipOutputStream(fos);
            ZipEntry zipEntry = new ZipEntry(f.getPath()); zipOS.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = data.read(bytes)) >= 0) {
                zipOS.write(bytes, 0, length);
            }
            zipOS.closeEntry();
            data.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
