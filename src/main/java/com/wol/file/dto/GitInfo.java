package com.wol.file.dto;

import java.nio.file.Path;
import java.util.List;

public record  GitInfo(BranchInfo branch, List<Path> files) implements MetadataInfo {
    public boolean valid(){
        return branch.beforeLast() != null && branch.lastRelease() != null && branch.lastRelease() != null && files.size() > 0;
    }
}