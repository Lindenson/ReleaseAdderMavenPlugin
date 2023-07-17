package com.wol.file.dto;

import java.nio.file.Path;
import java.util.List;

public interface MetadataInfo {
    List<Path> files();
    boolean valid();
}
