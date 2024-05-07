package com.lelexuetang.base.exception;

public class LeLeXueTangException extends RuntimeException{
    private String errMessage;
    public LeLeXueTangException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }
    public String getErrMessage() {
        return errMessage;
    }
    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    /**
     * 自定义抛出异常
     * @param errMessage
     */
    public static void cast(String errMessage) {
        throw new LeLeXueTangException(errMessage);
    }

    public static void cast(CommonError commonError) {
        throw new LeLeXueTangException(commonError.getErrMessage());
    }
}
