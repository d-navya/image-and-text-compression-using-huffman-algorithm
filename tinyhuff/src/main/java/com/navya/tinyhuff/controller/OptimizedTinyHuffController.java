package com.navya.tinyhuff.optimized;

import java.io.*;
import java.util.zip.*;

import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class OptimizedTinyHuffController {

    @PostMapping("/compress")
    public ResponseEntity<StreamingResponseBody> compress(@RequestParam("file") MultipartFile multipartFile)
            throws IOException {
        File inputFile = null;
        File compressedFile = null;
        try {
            inputFile = convert(multipartFile);
            compressedFile = compressFile(inputFile);
            return prepareResponse(compressedFile, "compressed.huff");
        } finally {
            if (inputFile != null && inputFile.exists()) {
                inputFile.delete();
            }
            if (compressedFile != null && compressedFile.exists()) {
                compressedFile.delete();
            }
        }
    }

    @PostMapping("/decompress")
    public ResponseEntity<StreamingResponseBody> decompress(@RequestParam("file") MultipartFile multipartFile)
            throws IOException {
        File compressedFile = null;
        File decompressedFile = null;
        try {
            compressedFile = convert(multipartFile);
            decompressedFile = decompressFile(compressedFile);
            return prepareResponse(decompressedFile, "decompressed.txt");
        } finally {
            if (compressedFile != null && compressedFile.exists()) {
                compressedFile.delete();
            }
            if (decompressedFile != null && decompressedFile.exists()) {
                decompressedFile.delete();
            }
        }
    }

    private File convert(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("upload-", "-" + file.getOriginalFilename());
        file.transferTo(tempFile);
        return tempFile;
    }

    private File compressFile(File inputFile) throws IOException {
        File compressedFile = File.createTempFile("compressed-", ".huff");
        try (FileInputStream fis = new FileInputStream(inputFile);
                FileOutputStream fos = new FileOutputStream(compressedFile);
                GZIPOutputStream gzipOut = new GZIPOutputStream(fos)) {
            StreamUtils.copy(fis, gzipOut);
        }
        return compressedFile;
    }

    private File decompressFile(File compressedFile) throws IOException {
        File decompressedFile = File.createTempFile("decompressed-", ".txt");
        try (FileInputStream fis = new FileInputStream(compressedFile);
                GZIPInputStream gzipIn = new GZIPInputStream(fis);
                FileOutputStream fos = new FileOutputStream(decompressedFile)) {
            StreamUtils.copy(gzipIn, fos);
        }
        return decompressedFile;
    }

    private ResponseEntity<StreamingResponseBody> prepareResponse(File file, String outputFileName) {
        StreamingResponseBody responseBody = outputStream -> {
            try (FileInputStream fis = new FileInputStream(file)) {
                StreamUtils.copy(fis, outputStream);
            } finally {
                file.delete(); // Ensure the file is deleted after streaming
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputFileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }
}