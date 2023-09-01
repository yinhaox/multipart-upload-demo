package com.github.yinhaox.upload;

import com.github.yinhaox.upload.model.req.GenUploadUrlsReq;
import com.github.yinhaox.upload.model.req.RegisterReq;
import com.github.yinhaox.upload.model.rsp.GenUploadUrlsRsp;
import com.github.yinhaox.upload.model.rsp.RegisterRsp;
import com.github.yinhaox.upload.utils.TempFileUtils;
import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainVerticle extends AbstractVerticle {
    private int uploadServerPort;

    @Override
    public void start(Promise<Void> startPromise) {

        Router uploadRouter = Router.router(vertx);

        uploadRouter.post("/file/v1/register")
                .handler(BodyHandler.create())
                .handler(ctx -> {
            RegisterReq request = ctx.body().asPojo(RegisterReq.class);

            File tempFile = TempFileUtils.createTempFile(request.getName(), request.getExtName());
            try (RandomAccessFile file = new RandomAccessFile(tempFile, "rw")) {
                file.setLength(request.getSize());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String objectKeyInfo = BaseEncoding.base64().encode(tempFile.getPath().getBytes(StandardCharsets.UTF_8));
            ctx.json(RegisterRsp.builder().objectKeyInfo(objectKeyInfo).build());
        });

        uploadRouter.post("/file/v1/gen-upload-urls")
                .handler(BodyHandler.create())
                .handler(ctx -> {
            GenUploadUrlsReq request = ctx.body().asPojo(GenUploadUrlsReq.class);

            List<GenUploadUrlsReq.PartInfo> reqPartInfos = request.getPartInfos().stream()
                    .sorted(Comparator.comparingInt(GenUploadUrlsReq.PartInfo::getPartNumber)).toList();

            long skipBytes = 0;
            List<GenUploadUrlsRsp.PartInfo> rspPartInfos = new ArrayList<>();
            for (GenUploadUrlsReq.PartInfo reqPartInfo : reqPartInfos) {
                GenUploadUrlsRsp.PartInfo  rspPartInfo = GenUploadUrlsRsp.PartInfo.builder()
                        .url("http://localhost:" + uploadServerPort
                                + "/file/v1/upload?objectKeyInfo=" + request.getObjectKeyInfo()
                                + "&skipBytes=" + skipBytes).build();
                rspPartInfos.add(rspPartInfo);
                skipBytes += reqPartInfo.getPartSize();
            }

            ctx.json(GenUploadUrlsRsp.builder().partInfos(rspPartInfos).build());
        });

        uploadRouter.put("/file/v1/upload").handler(ctx -> {
            String objectKeyInfo = ctx.request().getParam("objectKeyInfo");
            long skipBytes = Long.parseLong(ctx.request().getParam("skipBytes"));

            ctx.request().body(body -> {
                Buffer buf = body.result();
                String path = new String(BaseEncoding.base64().decode(objectKeyInfo), StandardCharsets.UTF_8);
                try (RandomAccessFile file = new RandomAccessFile(path, "rw")) {
                    file.seek(skipBytes);
                    for (int i = 0; i < buf.length(); i++) {
                        file.write(buf.getByte(i));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            ctx.response().end();
        });


        vertx.createHttpServer()
                .requestHandler(uploadRouter)
                .listen(8080)
                .onSuccess(server -> {
                    uploadServerPort = server.actualPort();
                    System.out.println("upload server is running on port: " + uploadServerPort);
                });
    }
}
