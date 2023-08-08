package com.wol.annotations;

@ConfigurationProperties("resources.external")
public class FDummyClass {
    @Mandatory
    private String url;
    @NestedConfigurationProperty
    @Mandatory
    private ADummyClass credentials;
    @Mandatory
    private int timeout;

    @Mandatory
    @NestedConfigurationProperty
    private XDummyClass protocol;

}
