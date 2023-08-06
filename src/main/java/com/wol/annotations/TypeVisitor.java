package com.wol.annotations;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.*;
import javax.lang.model.util.AbstractTypeVisitor8;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.wol.annotations.MandatoryPropertyAnnotationProcessor.expandTheName;

public class TypeVisitor extends AbstractTypeVisitor8 {

    public static final String MANDATORY = "Mandatory";
    public static final Map<TypeMirror, String> PREFIXES = new ConcurrentHashMap<>();


    @Override
    public Object visitPrimitive(PrimitiveType t, Object o) {
        return null;
    }

    @Override
    public Object visitNull(NullType t, Object o) {
        return null;
    }

    @Override
    public Object visitArray(ArrayType t, Object o) {
        return null;
    }

    @Override
    public Object visitDeclared(DeclaredType t, Object o) {
        List<String> elements = t.asElement()
                .getEnclosedElements().stream().filter(tx -> tx.getKind().equals(ElementKind.FIELD))
                .filter(tx -> tx.getAnnotationMirrors().stream().map(iz ->
                                iz.getAnnotationType().asElement()
                                .getSimpleName().toString()).filter(ix -> ix.contains(MANDATORY)
                        ).findFirst().isPresent()
                )
                .map(itx -> PREFIXES.get(itx.getEnclosingElement().asType()) + expandTheName(itx.toString()))
                .toList();
        ((List<String>) o).addAll(elements);
        return null;
    }

    @Override
    public Object visitError(ErrorType t, Object o) {
        return null;
    }

    @Override
    public Object visitTypeVariable(TypeVariable t, Object o) {
        return null;
    }

    @Override
    public Object visitWildcard(WildcardType t, Object o) {
        return null;
    }

    @Override
    public Object visitExecutable(ExecutableType t, Object o) {
        return null;
    }

    @Override
    public Object visitNoType(NoType t, Object o) {
        return null;
    }

    @Override
    public Object visitUnion(UnionType t, Object o) {
        return null;
    }

    @Override
    public Object visitIntersection(IntersectionType t, Object o) {
        return null;
    }
}