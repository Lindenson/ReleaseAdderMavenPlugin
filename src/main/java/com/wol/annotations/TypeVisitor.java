package com.wol.annotations;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.*;
import javax.lang.model.util.AbstractTypeVisitor8;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.wol.annotations.MandatoryPropertyAnnotationProcessor.expandTheName;

public class TypeVisitor extends AbstractTypeVisitor8<Object, List<String>> {

    public static final String MANDATORY = "Mandatory";
    public static final Map<TypeMirror, String> PREFIXES = new ConcurrentHashMap<>();

    @Override
    public Object visitIntersection(IntersectionType t, List<String> strings) {
        return null;
    }

    @Override
    public Object visitPrimitive(PrimitiveType t, List<String> strings) {
        return null;
    }

    @Override
    public Object visitNull(NullType t, List<String> strings) {
        return null;
    }

    @Override
    public Object visitArray(ArrayType t, List<String> strings) {
        return null;
    }

    @Override
    public Object visitDeclared(DeclaredType t, List<String> strings) {
        List<String> elements = t.asElement()
                .getEnclosedElements().stream().filter(tx -> tx.getKind().equals(ElementKind.FIELD))
                .filter(field -> field.getAnnotationMirrors().stream().map(annotation ->
                        annotation.getAnnotationType().asElement()
                                .getSimpleName().toString()).anyMatch(name -> name.contains(MANDATORY)))
                .map(itx -> PREFIXES.get(itx.getEnclosingElement().asType()) + expandTheName(itx.toString()))
                .toList();
        strings.addAll(elements);
        return null;
    }

    @Override
    public Object visitError(ErrorType t, List<String> strings) {
        return null;
    }

    @Override
    public Object visitTypeVariable(TypeVariable t, List<String> strings) {
        return null;
    }

    @Override
    public Object visitWildcard(WildcardType t, List<String> strings) {
        return null;
    }

    @Override
    public Object visitExecutable(ExecutableType t, List<String> strings) {
        return null;
    }

    @Override
    public Object visitNoType(NoType t, List<String> strings) {
        return null;
    }

    @Override
    public Object visitUnion(UnionType t, List<String> strings) {
        return null;
    }
}