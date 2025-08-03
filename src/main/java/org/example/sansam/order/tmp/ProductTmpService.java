package org.example.sansam.order.tmp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductTmpService {

    private final ProductRepository productRepository;

    public Optional<Products> findById(Long id) {
        return productRepository.findById(id);
    }

}
