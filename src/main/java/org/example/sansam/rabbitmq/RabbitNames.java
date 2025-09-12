package org.example.sansam.rabbitmq;

public final class RabbitNames {
    private RabbitNames(){}
    public static final String EXCHANGE = "stock.events";
    public static final String DLX      = "stock.dlx";

    //decreaseStock에 쓰는 용도
    public static final String Q_REQ    = "stock.decrease.requested.q";
    public static final String Q_REQ_DLQ= "stock.decrease.requested.dlq";
    public static final String RK_REQ   = "stock.decrease.requested";
    public static final String RK_REQ_DEAD = "stock.decrease.requested.dead";


    //increaseStock에 쓰는 용도
    public static final String RK_INC_REQ     = "stock.increase.requested";
    public static final String RK_INC_REQ_DEAD= "stock.increase.requested.dead";
    public static final String Q_INC_REQ      = "stock.increase.requested.q";
    public static final String Q_INC_REQ_DLQ  = "stock.increase.requested.dlq";
}
