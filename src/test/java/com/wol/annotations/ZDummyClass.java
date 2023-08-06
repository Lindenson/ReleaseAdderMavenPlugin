package com.wol.annotations;


@ConfigurationProperties("service")
public class ZDummyClass {

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
