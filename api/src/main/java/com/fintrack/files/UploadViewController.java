package com.fintrack.files;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class UploadViewController {

    // Support both layouts:
    // - running from project root → files in "api/uploads"
    // - running from api/ directory → files in "uploads"
    private static final Path UPLOADS_PRIMARY = Paths.get("uploads").toAbsolutePath().normalize();
    private static final Path UPLOADS_FALLBACK = Paths.get("api/uploads").toAbsolutePath().normalize();

    private Path resolveInEitherRoot(String fileName) {
        Path p1 = UPLOADS_PRIMARY.resolve(fileName).normalize();
        if (Files.exists(p1))
            return p1;
        Path p2 = UPLOADS_FALLBACK.resolve(fileName).normalize();
        if (Files.exists(p2))
            return p2;
        return p1; // default to primary (will 404)
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<?> getUploaded(@PathVariable("filename") String filename) {
        try {
            Path file = resolveInEitherRoot(filename);

            // Prevent path traversal
            if (!(file.startsWith(UPLOADS_PRIMARY) || file.startsWith(UPLOADS_FALLBACK))) {
                return ResponseEntity.status(403).body("Invalid path");
            }

            if (!Files.exists(file) || !Files.isReadable(file)) {
                return ResponseEntity.status(404).body("File not found");
            }

            Resource resource = new UrlResource(file.toUri());
            String contentType = Files.probeContentType(file);
            if (contentType == null)
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + file.getFileName().toString() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN_VALUE)
                    .body("Bad file URL");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .header(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN_VALUE)
                    .body("Failed to read file");
        }
    }
}
