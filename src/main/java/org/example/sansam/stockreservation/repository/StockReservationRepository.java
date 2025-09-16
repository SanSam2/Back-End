package org.example.sansam.stockreservation.repository;

import org.example.sansam.stockreservation.domain.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation,String> {

}