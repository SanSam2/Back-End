package org.example.sansam.order.tmp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductTmpService {

    private final TmpProductRepository productRepository;

    public Optional<TmpProducts> findById(Long id) {
        return productRepository.findById(id);
    }

}
