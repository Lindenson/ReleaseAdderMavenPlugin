package com.wol.file.dto;

import java.nio.file.Path;
import java.util.List;

public record  JarInfo(List<Path> files) implements MetadataInfo {
    public boolean valid(){
        return !files.isEmpty();
    }
}
