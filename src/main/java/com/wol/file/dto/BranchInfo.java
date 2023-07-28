package com.wol.file.dto;

import org.eclipse.jgit.lib.Ref;

public record  BranchInfo(String lastRelease, String beforeLast,Ref beforeRef){}