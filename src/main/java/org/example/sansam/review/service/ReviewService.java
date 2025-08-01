package org.example.sansam.review.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.review.domain.Review;
import org.example.sansam.review.dto.*;
import org.example.sansam.review.repository.ReviewJpaRepository;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.s3.repository.FileJpaRepository;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.s3.service.S3Service;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class ReviewService {
    private final FileService fileService;
    private final ReviewJpaRepository reviewJpaRepository;
    private final UserRepository userRepository;
    private final ProductJpaRepository productJpaRepository;
    private final FileJpaRepository fileJpaRepository;
    private final S3Service s3Service;

    public void createReview(AddReviewRequest request) {
        Review review = new Review();
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Product product = productJpaRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));


        FileManagement file = fileService.AddFile(request.getUrl(), request.getSize());
        review = Review.builder()
                .user(user)
                .product(product)
                .file(file)
                .message(request.getMessage())
                .starRating(request.getRating())
                .build();

        reviewJpaRepository.save(review);
    }

    public UpdateReviewResponse updateReview(UpdateReviewRequest request) {
        Review review = reviewJpaRepository.findByProductIdAndUserId(request.getProductId(), request.getUserId());
        if (review == null) {
            throw new EntityNotFoundException("리뷰가 존재하지 않습니다.");
        }

        if (review.getFile() != null && review.getFile().getFileDetail() != null) {
            String existUrl = review.getFile().getFileUrl();
            s3Service.deleteImage(existUrl);
        }
        FileManagement file = fileService.AddFile(request.getUrl(), request.getSize());
        review.setMessage(request.getMessage());
        review.setStarRating(request.getRating());
        review.setFile(file);
        reviewJpaRepository.save(review);

        return UpdateReviewResponse.from(review, file);
    }

    public void deleteReview(DeleteReviewRequest deleteReviewRequest) {
        Review review = reviewJpaRepository.findByProductIdAndUserId(deleteReviewRequest.getProductId(), deleteReviewRequest.getUserId());
        reviewJpaRepository.delete(review);

        if (review.getFile() != null) {
            String url = review.getFile().getFileUrl();
            if(url != null) {
                s3Service.deleteImage(url);
            }
        }

        reviewJpaRepository.delete(review);
    }

    public List<SearchReviewListResponse> searchReviews(Long productId) {
        List<Review> reviews = reviewJpaRepository.findAllByProductId(productId);
        return reviews.stream()
                .map(SearchReviewListResponse::from)
                .toList();
    }
}
