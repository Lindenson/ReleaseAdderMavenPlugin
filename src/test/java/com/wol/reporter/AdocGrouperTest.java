package com.wol.reporter;


import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class AdocGrouperTest {

    @Test
    void makeComplexMapGroups() {
        AtomicReference<Object> result = new AtomicReference<>(null);
        AdocGrouper adocGrouper = new AdocGrouper();
        List<String> strings = List.of( "source.set1.subset1",
                                        "source.set2.subset1.susubset33",
                                        "source",
                                        "source.set2",
                                        "source.set2.subset1",
                                        "source.set2.subset2",
                                        "source.set2.subset3",
                                        "source.set3.subset4",
                                        "source.set1.subset1",
                                        "source.set1",
                                        "source1.set1.subset1",
                                        "source.set2.subset1.susubset22");
        strings.stream().forEach( it -> result.set(adocGrouper.makeMultiLevelList(it, result.get())));
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) result.get();

        StringBuilder stringBuilder = adocGrouper.prettyPrintForAdoc(resultMap);
        assertEquals(
                "* source\n" +
                "* source\n" +
                "** set1\n" +
                "** set2\n" +
                "** set1\n" +
                "*** subset1\n" +
                "*** subset1\n" +
                "** set2\n" +
                "*** subset1\n" +
                "*** subset2\n" +
                "*** subset3\n" +
                "*** subset1\n" +
                "**** susubset22\n" +
                "**** susubset33\n" +
                "** set3\n" +
                "*** subset4\n" +
                "* source1\n" +
                "** set1\n" +
                "*** subset1\n",
                stringBuilder.toString());
    }

    @Test
    void makeEmptyMapGroups() {
        AtomicReference<Object> result = new AtomicReference<>(null);
        AdocGrouper adocGrouper = new AdocGrouper();
        List<String> strings = List.of( );
        strings.stream().forEach( it -> result.set(adocGrouper.makeMultiLevelList(it, result.get())));
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) result.get();

        StringBuilder stringBuilder = adocGrouper.prettyPrintForAdoc(resultMap);
        assertEquals(
                "", stringBuilder.toString());

    }


    @Test
    void makeOneLeafMapGroups() {
        AtomicReference<Object> result = new AtomicReference<>(null);
        AdocGrouper adocGrouper = new AdocGrouper();
        List<String> strings = List.of( "source");
        strings.stream().forEach( it -> result.set(adocGrouper.makeMultiLevelList(it, result.get())));
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) result.get();

        StringBuilder stringBuilder = adocGrouper.prettyPrintForAdoc(resultMap);
        assertEquals("* source\n", stringBuilder.toString());

    }

    @Test
    void makeTwoLeafMapGroups() {
        AtomicReference<Object> result = new AtomicReference<>(null);
        AdocGrouper adocGrouper = new AdocGrouper();
        List<String> strings = List.of( "source", "source");
        strings.stream().forEach( it -> result.set(adocGrouper.makeMultiLevelList(it, result.get())));
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) result.get();

        StringBuilder stringBuilder = adocGrouper.prettyPrintForAdoc(resultMap);
        assertEquals("* source\n* source\n", stringBuilder.toString());

    }
}
