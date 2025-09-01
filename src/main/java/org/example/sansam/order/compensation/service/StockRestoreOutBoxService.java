package org.example.sansam.order.compensation.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.order.compensation.domain.StockRestoreOutBox;
import org.example.sansam.order.compensation.repository.StockRestoreOutBoxRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockRestoreOutBoxService {
    private final StockRestoreOutBoxRepository repo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueue(Long orderId, Long detailId, int qty, String idemKey) {
        try {
            repo.save(StockRestoreOutBox.create(orderId, detailId, qty, idemKey));
        } catch (DataIntegrityViolationException e) {
            // idempotencyKey 중복 이미 큐에 있으니 OK로 취급
        }
    }
}