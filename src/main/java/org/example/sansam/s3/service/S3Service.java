package org.example.sansam.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.review.domain.Review;
import org.example.sansam.review.repository.ReviewJpaRepository;
import org.example.sansam.s3.domain.FileDetail;
import org.example.sansam.s3.dto.PresignedResponse;
import org.example.sansam.s3.repository.FileDetailJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service  // @Component 제거, @Service만 사용
public class S3Service {
	private final AmazonS3Client amazonS3;
	private final ReviewJpaRepository reviewJpaRepository;
	private final FileDetailJpaRepository fileDetailJpaRepository;
	private final FileService fileService;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;
	@Value("${cloud.aws.s3.custom.dir}")
	private String dir;
	@Value("${cloud.aws.s3.custom.defaultUrl}")
	private String defaultUrl;

	public PresignedResponse generatePresignedUploadUrl(String originalFileName, String type, Float sizeMb) {
		String uuidFileName = UUID.randomUUID() + "_" + originalFileName;
		String key = type + "/" + uuidFileName;

		Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 5); // 5분

		GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
				.withMethod(HttpMethod.PUT)
				.withExpiration(expiration);

		URL presignedUrl = amazonS3.generatePresignedUrl(request);

		String accessUrl = "https://" + bucketName + ".s3.amazonaws.com/" + key;

		return PresignedResponse.builder()
				.url(presignedUrl.toString())
				.accessUrl(accessUrl)
				.fileName(key)
				.size(sizeMb)
				.build();
	}

	public String uploadFile(MultipartFile file) throws IOException {
		String bucketDir = bucketName + dir;
		String dirUrl = defaultUrl + dir + "/";
		if (file == null) {
			return null;
		}
		String fileName = generateFileName(file);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());

		amazonS3.putObject(bucketDir, fileName, file.getInputStream(), getObjectMetadata(file));
		return dirUrl + fileName;
	}

//	@Transactional
//	public void updateImageUrl(String imageUrl, Long userId, Long productId, Float fileSize) {
//		Review review = reviewJpaRepository.findByProductIdAndUserId(productId, userId);
//		if (review == null) {
//			throw new RuntimeException("리뷰를 찾을 수 없습니다.");
//		}
//
//		// 기존 파일이 있다면 S3에서 삭제하고 DB에서도 제거
//		if (review.getFile() != null) {
//			String existingUrl = review.getFile().getFileUrl();
//			if (existingUrl != null) {
//				deleteImage(existingUrl);
//			}
//
//			// 기존 FileManagement 삭제 (CASCADE로 FileDetail도 함께 삭제됨)
//			Long fileManagementId = review.getFile().getId();
//			//fileService.(fileManagementId);
//		}
//
//		// 새로운 파일 정보 생성 및 저장
//		var newFileManagement = fileService.AddFile(imageUrl, fileSize);
//
//		// 리뷰에 새로운 파일 연결
//		review.setFile(newFileManagement);
//		reviewJpaRepository.save(review);
//	}

	private ObjectMetadata getObjectMetadata(MultipartFile file) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(file.getContentType());
		objectMetadata.setContentLength(file.getSize());
		return objectMetadata;
	}

	private String generateFileName(MultipartFile file) {
		return UUID.randomUUID().toString() + "." + file.getOriginalFilename();
	}

	public void deleteImage(String imgUrl) {
		try {
			String splitStr = ".com/";
			String fileName = imgUrl.substring(imgUrl.lastIndexOf(splitStr) + splitStr.length());

			amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
			log.info("S3에서 이미지 삭제 완료: {}", fileName);
		} catch (Exception e) {
			log.error("이미지 삭제 실패: {}", e.getMessage());
			throw new RuntimeException("이미지 삭제 실패: " + e.getMessage());
		}
	}
}