package org.example.sansam.payment.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
public class PaymentsType {

    @Id
    @Column(name="payments_type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="type_name")
    private PaymentMethodType typeName;

    public PaymentsType() {
        //JPA 기본 생성용 생성자
    }

    public PaymentsType(PaymentMethodType typeName) {
        this.typeName = java.util.Objects.requireNonNull(typeName);
    }


}
