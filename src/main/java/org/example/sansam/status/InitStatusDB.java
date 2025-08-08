package org.example.sansam.status;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InitStatusDB {

    private final InitStatusService initStatus;

    @PostConstruct
    public void init(){
        initStatus.dbInit();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitStatusService{
        private final EntityManager em;

        public void dbInit(){
            persistIfNotExists("NEW");
            persistIfNotExists("AVAILABLE");
            persistIfNotExists("SOLDOUT");
            persistIfNotExists("SCHEDULED");
            persistIfNotExists("COMPLETED");
            persistIfNotExists("ORDER_WAITING");
            persistIfNotExists("ORDER_PAID");
            persistIfNotExists("ORDER_CANCEL_REQUESTED");
            persistIfNotExists("ORDER_ALL_CANCELED");
            persistIfNotExists("ORDER_PARTIAL_CANCELED");
            persistIfNotExists("ORDER_PRODUCT_WAITING");
            persistIfNotExists("ORDER_PRODUCT_PAID");
            persistIfNotExists("ORDER_PRODUCT_CANCELED");
            persistIfNotExists("CANCEL_COMPLETED");
            persistIfNotExists("SOME_PRODUCT_CANCELED");
        }

        private void persistIfNotExists(String statusName) {
            Long count = em.createQuery(
                            "SELECT COUNT(s) FROM Status s WHERE s.statusName = :name", Long.class)
                    .setParameter("name", statusName)
                    .getSingleResult();

            if (count == 0) {
                em.persist(new Status(statusName));
            }
        }
    }
}
