package com.wol.annotations;

public class ADummyClass {

    private String password;

    @Mandatory
    private String name;

    @Mandatory
    private boolean enabled;

    @Mandatory
    private Integer numberOfTries;
}
