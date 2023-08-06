package com.wol.annotations;


import com.google.auto.service.AutoService;
import com.wol.reporter.AdocMultiLevelMap;
import org.jetbrains.annotations.NotNull;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static com.wol.annotations.TypeVisitor.PREFIXES;


@AutoService(Processor.class)
@SupportedAnnotationTypes("com.wol.annotations.Mandatory")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MandatoryPropertyAnnotationProcessor extends AbstractProcessor {


    public static final String CONFIGURATION_PROPERTIES = "ConfigurationProperties";
    public static final String NESTED_CONFIGURATION = "NestedConfiguration";
    public static final String PROPERTIES_MANDATORY_ADOC = "properties-mandatory.adoc";



    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            List<String> mandatory = new ArrayList();
            for (TypeElement te : annotations)
                for (Element e : roundEnv.getElementsAnnotatedWith(te)) {
                    e.getEnclosingElement().getAnnotationMirrors().stream()
                            .filter(it -> it.getAnnotationType().asElement()
                            .getSimpleName().toString().contains(CONFIGURATION_PROPERTIES))
                            .findFirst().ifPresent(annotation -> findMandatoryProperties(mandatory, e, annotation));
            }
            if (!mandatory.isEmpty()) makeAdoc(mandatory);
        } catch (Exception e) {
          //nothing to do
        }
        return false;
    }

    private void findMandatoryProperties(List<String> mandatory, Element element, AnnotationMirror annotation) {
        TypeMirror propertyType = element.getEnclosingElement().asType();
        addToPrefixMap(propertyType, annotation);
        List<String> namesOfNested = new ArrayList<>();
        findNestedProperties(element, namesOfNested);
        namesOfNested.stream().forEach(mandatory::add);
        if (namesOfNested.isEmpty())
            mandatory.add(String.format("%s%s",
                    getPrefix(annotation),
                    expandTheName(element.getSimpleName().toString()))
            );
    }


    private void addToPrefixMap(TypeMirror child, AnnotationMirror  m, TypeMirror father, String variable){
        String prefix = getPrefix(m);
        String fatherPrefix = PREFIXES.get(father) + expandTheName(variable) + '.';
        PREFIXES.put(child, fatherPrefix + prefix);
    }



    private void addToPrefixMap(TypeMirror child, AnnotationMirror m){
        String prefix = getPrefix(m);
        PREFIXES.put(child, prefix);
    }



    private void findNestedProperties(Element e, List<String> nested)  {
        try {
            e.getAnnotationMirrors().stream()
                    .filter(it -> it.getAnnotationType().asElement()
                    .getSimpleName().toString().contains(NESTED_CONFIGURATION)).findFirst()
                    .ifPresent(ix -> {
                        Element enclosingElement = e.getEnclosingElement();
                        if (enclosingElement != null) addToPrefixMap(e.asType(), ix, enclosingElement.asType(), e.toString());
                        e.asType().accept(new TypeVisitor(), (Object) nested);
                    });
        }
        catch (Exception ex) {
                // Nothing to do
        }}

    @NotNull
    private String getPrefix(AnnotationMirror z) {
        String prefix = z.getElementValues().values().stream()
                        .map(Object::toString)
                        .map(it -> it.replace("\"", ""))
                        .map(it -> it + '.')
                        .findFirst()
                        .orElse("");
        return prefix;
    }

    private void makeAdoc(List<String> mandatory) throws IOException {
        AdocMultiLevelMap grouper = new AdocMultiLevelMap();
        AtomicReference<Object> forReport = new AtomicReference<>(null);
        mandatory.stream().forEach( it -> forReport.set(grouper.makeMultiLevelMap(it, forReport.get())));
        StringBuilder report = grouper.prettyPrint((Map<String, List<Object>>) forReport.get());
        Path path = Paths.get(PROPERTIES_MANDATORY_ADOC);
        Files.writeString(path, "=== [navy]#Mandatory Properties:#\n");
        Files.writeString(path, report.toString(), StandardOpenOption.APPEND);
    }

    static String expandTheName(String property) {
        return property.replaceAll("([A-Z])", "-$1").toLowerCase();
    }
}


