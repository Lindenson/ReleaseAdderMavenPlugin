package com.wol.annotations;

import org.jetbrains.annotations.NotNull;

@NotNull("root.example1")
public class DummyClass {
    @Mandatory
    private String forTest1;
    private String forTest2;
}
