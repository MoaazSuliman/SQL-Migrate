package com.moaaz.migration.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;

public class CheckSumCalculator {

    private static final String CHECK_SUM_ALGORITHM = "SHA-256";

    public static String calc(String path) {
        try (InputStream inputStream = getInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance(CHECK_SUM_ALGORITHM);
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            throw new RuntimeException("Error calculating checksum for: " + path, e);
        }
    }

    private static InputStream getInputStream(String path) throws IOException {
        // First try as classpath resource
        InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path.replace("\\", "/"));

        if (is != null) return is;

        // Fallback to filesystem
        return Files.newInputStream(Paths.get(path));
    }
}
