package com.navya.tinyhuff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.navya.tinyhuff.optimized.OptimizedTinyHuffController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TinyhuffApplicationTests {

	@Autowired
	private OptimizedTinyHuffController controller;

	@Test
	void contextLoads() {
		// Verify that the application context loads and the controller is not null
		assertThat(controller).isNotNull();
	}

	@Test
	void testCompressEndpoint() throws Exception {
		// Mock a file upload
		MultipartFile mockFile = new MockMultipartFile(
				"file",
				"test.txt",
				"text/plain",
				"This is a test file".getBytes());

		// Call the compress method and verify the response
		var response = controller.compress(mockFile);
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(200);
	}

	@Test
	void testDecompressEndpoint() throws Exception {
		// Mock a file upload for decompression
		MultipartFile mockFile = new MockMultipartFile(
				"file",
				"test.huff",
				"application/octet-stream",
				"Compressed content".getBytes());

		// Call the decompress method and verify the response
		var response = controller.decompress(mockFile);
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(200);
	}
}