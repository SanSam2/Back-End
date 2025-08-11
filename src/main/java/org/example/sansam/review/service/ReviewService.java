package org.example.sansam.review.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.repository.OrderProductRepository;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.review.domain.Review;
import org.example.sansam.review.dto.*;
import org.example.sansam.review.repository.ReviewJpaRepository;
import org.example.sansam.s3.domain.FileDetail;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.s3.repository.FileDetailJpaRepository;
import org.example.sansam.s3.repository.FileJpaRepository;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.s3.service.S3Service;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
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
    private final FileDetailJpaRepository fileDetailJpaRepository;
    private final OrderProductRepository orderProductRepository;

    private final StatusRepository statusRepository;

    public void createReview(AddReviewRequest request) {
        Review review = new Review();
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Product product = productJpaRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        FileManagement file = null;
        if (request.getUrl() != null) {
            file = fileService.AddFile(request.getUrl(), request.getSize());
        }
        review = Review.builder()
                .user(user)
                .product(product)
                .file(file)
                .message(request.getMessage())
                .starRating(request.getRating())
                .build();
        OrderProduct orderProduct = orderProductRepository.findByOrder_OrderNumberAndProduct_Id(request.getOrderNumber(), request.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException("주문된 상품을 찾을 수 없습니다. "));
        Status reviewCompleted = statusRepository.findByStatusName(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_COMPLETED);
        orderProduct.updateOrderProductStatus(reviewCompleted);
        reviewJpaRepository.save(review);
    }

    public UpdateReviewResponse updateReview(UpdateReviewRequest request) {
        Review review = reviewJpaRepository.findByProductIdAndUserId(request.getProductId(), request.getUserId());
        if (review == null) {
            throw new EntityNotFoundException("리뷰가 존재하지 않습니다.");
        }
        String url = null;

        if (request.getUrl() != null) {
            Long detailId = fileService.getFileDetail(review.getFile());
            FileDetail detail = fileDetailJpaRepository.findById(detailId)
                    .orElseThrow(() -> new EntityNotFoundException("파일정보가 존재하지 않습니다."));
            detail.setUrl(request.getUrl());
            detail.setSize(request.getSize());
            fileDetailJpaRepository.save(detail);
            url = request.getUrl();
        }

        review.setMessage(request.getMessage());
        review.setStarRating(request.getRating());
        reviewJpaRepository.save(review);
        UpdateReviewResponse updateReviewResponse = UpdateReviewResponse.from(review);
        if (url != null) {
            updateReviewResponse.setUrl(url);
        }
        return updateReviewResponse;
    }

    public void deleteReview(DeleteReviewRequest deleteReviewRequest) {
        Review review = reviewJpaRepository.findByProductIdAndUserId(deleteReviewRequest.getProductId(), deleteReviewRequest.getUserId());
        if (review == null) {
            throw new EntityNotFoundException("리뷰가 존재하지 않습니다.");
        }
        reviewJpaRepository.delete(review);

        if (review.getFile() != null) {
            String url = review.getFile().getFileUrl();
            if (url != null) {
                s3Service.deleteImage(url);
                fileDetailJpaRepository.delete(review.getFile().getFileDetail());
                fileJpaRepository.delete(review.getFile());
            }
        }

        reviewJpaRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<SearchReviewListResponse> searchReviews(Long productId) {
        List<Review> reviews = reviewJpaRepository.findAllByProductId(productId);
        return reviews.stream()
                .map(SearchReviewListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SearchReviewListResponse> searchMyReviews(Long userId) {
        List<Review> reviews = reviewJpaRepository.findAllByUserId(userId);
        return reviews.stream()
                .map(SearchReviewListResponse::from)
                .toList();
    }
}