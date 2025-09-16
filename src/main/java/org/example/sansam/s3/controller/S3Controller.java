package org.example.sansam.s3.controller;

import lombok.RequiredArgsConstructor;
import org.example.sansam.s3.dto.PresignedRequest;
import org.example.sansam.s3.dto.PresignedResponse;
import org.example.sansam.s3.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/s3")
public class S3Controller {
	private final S3Service s3Service;

	@PostMapping("/presigned-url")
	public ResponseEntity<?> generatePresignedUrl(@RequestBody PresignedRequest request) {
		try {
			PresignedResponse response = s3Service.generatePresignedUploadUrl(
					request.getFileName(),
					request.getType(),
					request.getSize()
			);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(400).body(e.getMessage());
		}
	}

	@PostMapping("/upload")
	public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file) {
		try {
			String imageUrl = s3Service.uploadFile(file);
			return ResponseEntity.ok(imageUrl);
		} catch (Exception e) {
			return ResponseEntity.status(400).body(e.getMessage());
		}
	}

	@GetMapping("/main")
	public ResponseEntity<?> getMainImages() {
		List<String> urls = s3Service.getMainImageUrls();
		return ResponseEntity.ok(urls);
	}

}
