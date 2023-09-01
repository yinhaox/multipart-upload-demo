package com.github.yinhaox.upload.model.req;

import lombok.Data;

import java.util.List;

@Data
public class GenUploadUrlsReq {
    private String objectKeyInfo;

    private List<PartInfo> partInfos;

    @Data
    public static class PartInfo {
        private int partNumber;

        private long partSize;
    }
}
