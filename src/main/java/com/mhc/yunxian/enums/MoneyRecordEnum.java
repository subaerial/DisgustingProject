package com.mhc.yunxian.enums;

public enum MoneyRecordEnum implements EnumMsg<Integer>{


    IN_THE_PRESENT(0,"提现中"),
        IMCOME(1,"交易收入"),
            WAIT_FOR_INCOME(2,"待收入"), //暂时废除此状态
                PRESENT_COMPLETION(3,"提现完成"),
                    TRANSACTION_COST(4,"交易费"),
                        SERVICE_CHARGE(5,"提现手续费"),
    REFUNED(6,"退款");



    private Integer code;

    private String msg;

    private MoneyRecordEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public Integer getCode() {
        return code;
    }
}

