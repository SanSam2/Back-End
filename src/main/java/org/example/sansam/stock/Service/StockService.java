package org.example.sansam.stock.Service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.stock.domain.Stock;
import org.example.sansam.stock.repository.StockRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;


    @Transactional
    public int checkItemStock(Long detailId){
        Stock stock = stockRepository.findByProductDetailsId(detailId)
                .orElseThrow(()->new CustomException(ErrorCode.ZERO_STOCK));
        return stock.getStockQuantity();
    }

    @Transactional
    public void decreaseStock(Long productDetailId, int quanitty){
        Stock stock = stockRepository.findByProductDetailsId(productDetailId)
                .orElseThrow(()->new CustomException(ErrorCode.ZERO_STOCK));
        stock.decrease(quanitty);
    }


}
