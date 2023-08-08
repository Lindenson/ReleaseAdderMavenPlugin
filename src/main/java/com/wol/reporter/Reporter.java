package com.wol.reporter;

import java.util.Map;
import java.util.Set;

public interface Reporter {
    void generate(Set<String> added,
                  Set<String> removed,
                  Map<String, String> defaultsChanged,
                  String currentBranch,
                  String beforeBranch);
}
