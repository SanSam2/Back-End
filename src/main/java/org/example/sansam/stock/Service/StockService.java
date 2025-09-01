package org.example.sansam.stock.Service;



import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.stock.domain.Stock;
import org.example.sansam.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;


    @Transactional
    public void decreaseStock(Long productDetailId, int quantity) {
        int updated = stockRepository.decreaseIfEnough(productDetailId, quantity);
        if (updated == 0) {
            throw new CustomException(ErrorCode.NOT_ENOUGH_STOCK);
        }
    }

    @Transactional
    public void increaseStock(Long productDetailId, int quantity) {
        stockRepository.increase(productDetailId, quantity);
    }

    @Transactional(readOnly = true)
    public int checkItemStock(Long detailId){
        return stockRepository.findByProductDetailsId(detailId)
                .map(Stock::getStockQuantity)
                .orElse(0);
    }

}
