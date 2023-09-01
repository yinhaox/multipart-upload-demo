package com.github.yinhaox.upload.model.req;

import lombok.Data;

@Data
public class RegisterReq {
    private String name;

    private String extName;

    private long size;
}
