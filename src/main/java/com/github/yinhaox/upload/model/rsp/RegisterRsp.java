package com.github.yinhaox.upload.model.rsp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRsp {
    private String objectKeyInfo;
}
