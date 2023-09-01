package com.github.yinhaox.upload.model.rsp;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GenUploadUrlsRsp {
    private List<PartInfo> partInfos;

    @Data
    @Builder
    public static class PartInfo {
        private String url;

        @Builder.Default
        private String method = "PUT";
    }
}
