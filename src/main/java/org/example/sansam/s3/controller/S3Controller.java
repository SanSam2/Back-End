package org.example.sansam.s3.controller;

import lombok.RequiredArgsConstructor;
import org.example.sansam.s3.dto.PresignedRequest;
import org.example.sansam.s3.dto.PresignedResponse;
import org.example.sansam.s3.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/s3")
public class S3Controller {
	private final S3Service s3Service;

	@PostMapping("/presigned-url")
	public ResponseEntity<?> generatePresignedUrl(@RequestBody PresignedRequest request) {
		PresignedResponse response = s3Service.generatePresignedUploadUrl(
				request.getFileName(),
				request.getType(),
				request.getSize()
		);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/upload")
	public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file) throws IOException {
		String imageUrl = s3Service.uploadFile(file);
		return ResponseEntity.ok(imageUrl);
	}

}
