package com.enty.payment.customer.storage;

import com.enty.payment.customer.service.CustomerApiException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalCustomerLogoStorage implements CustomerLogoStorage {
    private static final String PUBLIC_PREFIX = "/files/customer-logos/";
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");
    private final Path storageDirectory;

    public LocalCustomerLogoStorage(@Value("${app.storage.customer-logo-directory}") String directory) {
        this.storageDirectory = Path.of(directory).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initialize() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create customer logo directory", ex);
        }
    }

    @Override
    public String store(MultipartFile logo) {
        if (logo == null || logo.isEmpty()) return null;
        try {
            byte[] content = logo.getBytes();
            String extension = EXTENSIONS.get(detectType(content));
            if (extension == null) throw invalidFile("logo must be a JPEG, PNG, or WebP image");
            String fileName = UUID.randomUUID() + extension;
            Path target = storageDirectory.resolve(fileName).normalize();
            if (!target.getParent().equals(storageDirectory)) throw invalidFile("invalid logo file name");
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
            return PUBLIC_PREFIX + fileName;
        } catch (CustomerApiException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new CustomerApiException(HttpStatus.INTERNAL_SERVER_ERROR, "unable to store customer logo");
        }
    }

    @Override
    public void delete(String logoPath) {
        if (logoPath == null || !logoPath.startsWith(PUBLIC_PREFIX)) return;
        Path target = storageDirectory.resolve(logoPath.substring(PUBLIC_PREFIX.length())).normalize();
        if (!target.getParent().equals(storageDirectory)) return;
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // File cleanup failure must not roll back saved customer data.
        }
    }

    private String detectType(byte[] bytes) {
        if (bytes.length >= 3 && unsigned(bytes[0]) == 0xFF && unsigned(bytes[1]) == 0xD8 && unsigned(bytes[2]) == 0xFF)
            return "image/jpeg";
        if (bytes.length >= 8 && unsigned(bytes[0]) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E
                && bytes[3] == 0x47 && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A)
            return "image/png";
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P')
            return "image/webp";
        return null;
    }

    private int unsigned(byte value) { return value & 0xFF; }

    private CustomerApiException invalidFile(String message) {
        return new CustomerApiException(HttpStatus.BAD_REQUEST, message);
    }
}
