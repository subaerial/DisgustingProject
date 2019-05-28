package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.WxAutoReply;
import lombok.Data;

import java.util.List;

@Data
public class KeywordListResponse extends BaseResponse{

    private List<WxAutoReply> keywordList;

}
