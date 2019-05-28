package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.Bank;
import lombok.Data;

import java.util.List;

@Data
public class GetAllBankResponse extends BaseResponse{

    List<Bank> bankList;

}
