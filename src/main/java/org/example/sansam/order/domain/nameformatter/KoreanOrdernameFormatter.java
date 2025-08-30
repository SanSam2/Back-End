package org.example.sansam.order.domain.nameformatter;

public final class KoreanOrdernameFormatter implements OrderNameFormatter{

    public static final KoreanOrdernameFormatter INSTANCE = new KoreanOrdernameFormatter();
    private KoreanOrdernameFormatter(){

    }

    @Override
    public String format(String first, int others){
        return others<=0 ? first : first+" 외 "+others+"건";
    }
}
