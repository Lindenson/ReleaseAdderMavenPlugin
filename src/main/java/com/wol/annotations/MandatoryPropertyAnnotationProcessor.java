package com.wol.annotations;


import com.google.auto.service.AutoService;
import com.wol.reporter.AdocSimpleList;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@AutoService(Processor.class)
@SupportedAnnotationTypes("com.wol.annotations.Mandatory")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MandatoryPropertyAnnotationProcessor extends AbstractProcessor {

    public static final String NAME_OF_SPRING_ANNOTATION = "Non";
    public static final String PROPERTIES_MANDATORY_ADOC = "properties-mandatory.adoc";


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (TypeElement annotation : annotations) {
                Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

                // prefix
                Optional<? extends AnnotationMirror> first = annotatedElements.stream()
                        .map(Element::getEnclosingElement)
                        .findFirst()
                        .stream()
                        .flatMap(it -> it.getAnnotationMirrors().stream())
                        .filter(it -> it.getAnnotationType().asElement().getSimpleName().toString().contains(NAME_OF_SPRING_ANNOTATION))
                        .findFirst();

                if (!first.isPresent()) return false;

                String prefix = first.get().getElementValues().values().stream()
                        .map(Object::toString)
                        .map(it -> it.replace("\"", ""))
                        .findFirst()
                        .orElse("");

                // mandatory parameters
                List<String> mandatory = annotatedElements.stream()
                        .map(Element::getSimpleName)
                        .map(Object::toString)
                        .map(this::expandTheName)
                        .map(it -> String.format("%s.%s", prefix, it))
                        .toList();

                makeAdoc(mandatory);
            }

        } catch (IOException e) {
            // Nothing to do
        }
        return false;
    }

    private void makeAdoc(List<String> mandatory) throws IOException {
        String result = AdocSimpleList.prettyPrint(mandatory);
        Path path = Paths.get(PROPERTIES_MANDATORY_ADOC);
        Files.writeString(path, "=== [navy]#Mandatory Properties:#\n");
        Files.writeString(path, result, StandardOpenOption.APPEND);
    }

    private String expandTheName(String property) {
        return property.replaceAll("([A-Z])", "-$1").toLowerCase();
    }
}