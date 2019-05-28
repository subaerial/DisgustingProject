package com.mhc.yunxian.bean.index;

import com.mhc.yunxian.bean.BaseRequest;
import lombok.Data;

@Data
public class GetIndexDragonListRequest extends BaseRequest {

    private String sessionId;

    private Integer page = 1;

    private Integer size = 5;

    private Integer dragonSize = 5;

    private Integer state = 0;

}
