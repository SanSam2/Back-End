package org.example.sansam.kafka.topic;

public final class KafkaTopics {
    private KafkaTopics() {

    }

    public static final String STOCK_DECREASE_REQUEST="stock.decrease.requested";
    public static final String STOCK_DECREASE_DLT="stock.decrease.requested.dlt";

    public static final String STOCK_INCREASE_REQUEST="stock.increase.requested";
    public static final String STOCK_INCREASE_DLT="stock.increase.requested.dlt";

    public static final String STOCK_DECREASE_RESULT = "stock.decrease.result";
    public static final String STOCK_INCREASE_RESULT = "stock.increase.result";
}
