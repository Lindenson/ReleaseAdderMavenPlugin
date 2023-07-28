package com.wol.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {

    private FileUtils(){}
    public static void deleteFolder(File folder) throws IOException {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    Files.deleteIfExists(f.toPath());
                }
            }
        }
        Files.deleteIfExists(folder.toPath());
    }
}
