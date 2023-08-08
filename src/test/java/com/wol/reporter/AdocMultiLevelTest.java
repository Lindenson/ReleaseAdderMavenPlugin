package com.wol.reporter;


import com.wol.reporter.strategies.AdocMultiLevelMap;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class AdocMultiLevelTest {

    @Test
    void makeComplexMapGroups() {
        AdocMultiLevelMap adocMultiLevelMap = new AdocMultiLevelMap();
        Set<String> strings = Set.of(   "source.set1.subset1",
                                        "source.set2.subset1.susubset33",
                                        "source.set2",
                                        "source",
                                        "source.set2.subset1",
                                        "source.set2.subset2",
                                        "source.set2.subset3",
                                        "source.set3.subset4",
                                        "source.set1",
                                        "source.set2.subset1.susubset22");
        StringBuilder stringBuilder = adocMultiLevelMap.prettyPrint(strings);
        assertEquals(
                "* source\n" +
                        "* source\n" +
                        "** set1\n" +
                        "** set2\n" +
                        "** set1\n" +
                        "*** subset1\n" +
                        "** set2\n" +
                        "*** subset1\n" +
                        "*** subset2\n" +
                        "*** subset3\n" +
                        "*** subset1\n" +
                        "**** susubset22\n" +
                        "**** susubset33\n" +
                        "** set3\n" +
                        "*** subset4\n",
                stringBuilder.toString());
    }

    @Test
    void makeEmptyMapGroups() {
        AtomicReference<Object> result = new AtomicReference<>(null);
        AdocMultiLevelMap adocMultiLevelMap = new AdocMultiLevelMap();
        Set<String> strings = Set.of( );

        StringBuilder stringBuilder = adocMultiLevelMap.prettyPrint(strings);
        assertEquals(
                "", stringBuilder.toString());

    }


    @Test
    void makeOneLeafMapGroups() {
        AtomicReference<Object> result = new AtomicReference<>(null);
        AdocMultiLevelMap adocMultiLevelMap = new AdocMultiLevelMap();
        Set<String> strings = Set.of( "source");

        StringBuilder stringBuilder = adocMultiLevelMap.prettyPrint(strings);
        assertEquals("* source\n", stringBuilder.toString());

    }

    @Test
    void makeTwoLeafMapGroups() {
        AtomicReference<Object> result = new AtomicReference<>(null);
        AdocMultiLevelMap adocMultiLevelMap = new AdocMultiLevelMap();
        Set<String> strings = Set.of( "source1", "source2");

        StringBuilder stringBuilder = adocMultiLevelMap.prettyPrint(strings);
        assertEquals("* source1\n* source2\n", stringBuilder.toString());

    }
}
