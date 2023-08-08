package com.wol.reporter.strategies;

public interface ReportStyles<T> {
    StringBuilder prettyPrint(T source);
    enum Style {LIST, TREE}

    static Style styleFrom(String styleString) {
        try {return Style.valueOf(styleString.toUpperCase());}
        catch (Exception e) {return Style.LIST;}
    }
}
