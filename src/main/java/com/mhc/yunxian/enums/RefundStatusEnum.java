package com.mhc.yunxian.enums;

public enum RefundStatusEnum implements EnumMsg<Integer>{

        DEFAULT(0,"退款未发起"),
        REFUNDING(1,"退款已发起"),
        REFUSED(2,"卖家拒绝"),
        CANCELLED(3,"买家撤销"),
        REFUNDED(4,"退款成功");

        private Integer code;

        private String msg;

        RefundStatusEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        @Override
        public Integer getCode() {
        return code;
    }

        @Override
        public String getMsg() {
            return msg;
        }

}