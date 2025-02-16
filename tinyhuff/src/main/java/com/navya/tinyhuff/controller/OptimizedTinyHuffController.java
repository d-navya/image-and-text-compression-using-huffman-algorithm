package com.navya.tinyhuff.optimized;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class OptimizedTinyHuffController {
    
    @PostMapping("/compress")
    public ResponseEntity<StreamingResponseBody> compress(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        File inputFile = convert(multipartFile);
        File compressedFile = compressFile(inputFile);
        inputFile.delete();

        return prepareResponse(compressedFile, "compressed.huff");
    }

    @PostMapping("/decompress")
    public ResponseEntity<StreamingResponseBody> decompress(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        File compressedFile = convert(multipartFile);
        File decompressedFile = decompressFile(compressedFile);
        compressedFile.delete();

        return prepareResponse(decompressedFile, "decompressed.txt");
    }

    private File convert(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("upload", file.getOriginalFilename());
        file.transferTo(tempFile);
        return tempFile;
    }

    private File compressFile(File inputFile) throws IOException {
        File compressedFile = File.createTempFile("compressed", ".huff");
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(compressedFile);
             GZIPOutputStream gzipOut = new GZIPOutputStream(fos)) {
            StreamUtils.copy(fis, gzipOut);
        }
        return compressedFile;
    }

    private File decompressFile(File compressedFile) throws IOException {
        File decompressedFile = File.createTempFile("decompressed", ".txt");
        try (FileInputStream fis = new FileInputStream(compressedFile);
             GZIPInputStream gzipIn = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(decompressedFile)) {
            StreamUtils.copy(gzipIn, fos);
        }
        return decompressedFile;
    }

    private ResponseEntity<StreamingResponseBody> prepareResponse(File file, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(outputStream -> {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        StreamUtils.copy(fis, outputStream);
                    } finally {
                        file.delete();
                    }
                });
    }
}