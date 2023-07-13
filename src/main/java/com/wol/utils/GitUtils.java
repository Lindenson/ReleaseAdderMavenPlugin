package com.wol.utils;

import com.wol.file.dto.BranchInfo;
import org.eclipse.jgit.lib.Ref;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class GitUtils {

    public static final String REFS_HEADS = "refs/heads/";
    public static final String DOT = ".";


    public static String refNameToBranchName(String refName) {
        return refName.replace(REFS_HEADS, "");
    }

    public static BranchInfo currentAndPreviousReleaseInfo(TreeMap<Integer, Ref> branchNames)  {
        Optional<Map.Entry<Integer, Ref>> le = Optional.ofNullable(branchNames.lastEntry());
        String last = le.map(it -> it.getValue().getName()).map(GitUtils::refNameToBranchName).orElse(null);
        Optional<SortedMap<Integer, Ref>> ble = le.map(it -> it.getKey()).map(it -> branchNames.subMap(0, it)).filter(it -> !it.isEmpty());
        Ref ref = ble.map(it -> it.get(it.lastKey())).orElse(null);
        String previous = Optional.ofNullable(ref).map(it -> it.getName()).map(GitUtils::refNameToBranchName).orElse(null);
        return new BranchInfo(last, previous, ref);
    }

    public static int releaseNameToNumber(Pattern pattern, String releaseName, String releaseSuffix) {
        Matcher matcher = pattern.matcher(releaseName);
        if (matcher.matches()) {
            String n_branch = releaseName.replace(releaseSuffix, "").replace(DOT, "");
            return Integer.parseInt(n_branch);
        }
        return -1;
    }

    public static TreeMap<Integer, Ref> dropBranchesLaterThenCurrentBranches(TreeMap<Integer, Ref> source, int current) {
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


    public static TreeMap<Integer, Ref> expandBranchNamesAndReorderBranches(TreeMap<Integer, Ref> branches)  {
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
                }).collect(Collectors.toMap(it1 -> it1.getKey(), it2 -> it2.getValue(), (is1, is2) -> is1,
                        TreeMap::new))).orElse(branches);
    }

}
