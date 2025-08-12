package org.example.sansam.s3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.sansam.s3.dto.PresignedRequest;
import org.example.sansam.s3.dto.PresignedResponse;
import org.example.sansam.s3.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/s3")
public class S3Controller {
	private final S3Service s3Service;

	@Operation(summary = "predigned-url 발급", description = "predigned-url 발급합니다.",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
					required = true,
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = PresignedRequest.class),
							examples = @ExampleObject(value = """
                                    {
									            "fileName": "heart.jpg",
									            "type": "jpg",
									            "size": 5.5
									}
                            """)
					)
			)
	)
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "발급 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = PresignedResponse.class),
							examples = @ExampleObject(value = """
									{
									    "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/jpg/3a31e069-76c5-4394-926d-94aa1c6bbcef_img1?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250812T015608Z&X-Amz-SignedHeaders=host&X-Amz-Expires=299&X-Amz-Credential=AKIATQRBIEG7VRTKSGB5%2F20250812%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Signature=a113682f94fff43d217fc9f3d013f9ec1ca1d991b5c4ad9cc3a2ac47f0effa7e",
									    "accessUrl": "https://sansam2-bucket.s3.amazonaws.com/jpg/3a31e069-76c5-4394-926d-94aa1c6bbcef_img1",
									    "fileName": "jpg/3a31e069-76c5-4394-926d-94aa1c6bbcef_img1",
									    "size": 5.5
									}
                                """))),
			@ApiResponse(responseCode = "400", description = "유효성 오류")
	})
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

//	@PostMapping("/upload")
//	public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file) {
//		try {
//			String imageUrl = s3Service.uploadFile(file);
//			return ResponseEntity.ok(imageUrl);
//		} catch (Exception e) {
//			return ResponseEntity.status(400).body(e.getMessage());
//		}
//	}

}
