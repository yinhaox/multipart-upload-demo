package com.github.yinhaox.upload.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class TempFileUtils {
    private File tmpdir = null;

    @SneakyThrows
    public File createTempFile(String prefix, String suffix) {
        createTmpdir();
        return File.createTempFile(prefix, suffix, tmpdir);
    }

    @SneakyThrows
    private void createTmpdir() {
        if (tmpdir != null) {
            return;
        }
        tmpdir = Files.createTempDirectory(Path.of(System.getProperty("java.io.tmpdir")), "upload").toFile();
    }
}
