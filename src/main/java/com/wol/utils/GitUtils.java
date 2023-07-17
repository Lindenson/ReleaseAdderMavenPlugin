package com.wol.utils;

import com.wol.file.dto.BranchInfo;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.lib.Ref;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class GitUtils {

    public static final String REFS_HEADS = "refs/heads/";
    public static final String DOT = ".";


    public static String refNameToBranchName(String refName) {
        if (refName == null) return null;
        return refName.replace(REFS_HEADS, "");
    }

    public static BranchInfo currentAndPreviousReleaseInfo(@NotNull TreeMap<Integer, Ref> branchNames)  {
        Optional<Map.Entry<Integer, Ref>> le = Optional.ofNullable(branchNames.lastEntry());
        String last = le.map(it -> it.getValue().getName()).map(GitUtils::refNameToBranchName).orElse(null);
        Optional<SortedMap<Integer, Ref>> ble = le.map(it -> it.getKey()).map(it -> branchNames.subMap(0, it)).filter(it -> !it.isEmpty());
        Ref ref = ble.map(it -> it.get(it.lastKey())).orElse(null);
        String previous = Optional.ofNullable(ref).map(it -> it.getName()).map(GitUtils::refNameToBranchName).orElse(null);
        return new BranchInfo(last, previous, ref);
    }

    public static int releaseNameToNumber(@NotNull Pattern pattern, @NotNull String releaseName, Log logger) {
        try {
            Matcher matcher = pattern.matcher(releaseName);
            if (matcher.matches()) {
                String s_branch = matcher.group("number");
                String n_branch = s_branch.replaceAll("\\D", "");
                return Integer.parseInt(n_branch);
            }
        }
        catch (Exception e) {
            logger.error("regex did not found a correct release number");
        }
        // not following the pattern name - not a release
        return -1;
    }

    public static TreeMap<Integer, Ref> dropBranchesLaterThenCurrentBranches(@NotNull TreeMap<Integer, Ref> source, int current) {
        if (current < 0) return source;
        String stringTo = String.valueOf(current);
        int lengthTo = stringTo.length();
        Map.Entry<Integer, Ref> integerRefEntry = source.firstEntry();
        Integer maxLength = Optional.ofNullable(integerRefEntry).map(it -> String.valueOf(it.getKey())).map(String::length).orElse(null);
        Optional<Integer> expandedTo = Optional.ofNullable(maxLength).map(it -> stringTo + "0".repeat(it - lengthTo)).map(Integer::valueOf);
        if (expandedTo.isPresent()) {
            TreeMap<Integer, Ref> droppedMap = new TreeMap<>(source.subMap(0, true, expandedTo.get(), true));
            return droppedMap;
        }
        return source;
    }


    public static TreeMap<Integer, Ref> expandBranchNamesAndReorderBranches(@NotNull TreeMap<Integer, Ref> branches, Log logger)  {
        List<String> keys = branches.keySet().stream().map(String::valueOf).collect(Collectors.toList());
        Optional<Integer> maxLength = keys.stream().map(String::length).max(Integer::compareTo);
        return maxLength.map(it ->
                branches.entrySet().stream().map(itt ->
                {
                    String s1 = String.valueOf(itt.getKey());
                    int l1 = it - s1.length();
                    StringBuilder sb = new StringBuilder();
                    sb.append(s1);
                    sb.append("0".repeat(l1));
                    AbstractMap.SimpleEntry<Integer, Ref> res = new AbstractMap.SimpleEntry<>(Integer.valueOf(sb.toString()), itt.getValue());
                    return res;
                }).collect(Collectors.toMap(it1 -> it1.getKey(), it2 -> it2.getValue(),
                        (is1, is2) -> {
                            logger.error("release names collides! correct names or regex");
                            return is1;
                        },
                TreeMap::new))).orElse(branches);
    }

}
