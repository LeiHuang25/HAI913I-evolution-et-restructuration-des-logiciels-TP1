package hai913i.tp1.spoon;

import java.util.stream.Collectors;

import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

public final class SpoonIdentifiers {

    private SpoonIdentifiers() {
    }

    public static String executableId(
            CtExecutable<?> executable) {

    	if (executable == null) {
    	    return null;
    	}

    	CtType<?> declaringType =
    	        executable.getParent(CtType.class);

    	if (declaringType == null) {
    	    return null;
    	}

        String qualifiedTypeName =
                declaringType.getQualifiedName()
                             .replace('$', '.');

        boolean constructor =
                executable instanceof CtConstructor<?>;

        String methodName = constructor
                ? "<init>"
                : executable.getSimpleName();

        String parameters = executable
                .getParameters()
                .stream()
                .map(parameter ->
                        parameterType(
                                parameter.getType()
                        )
                )
                .collect(Collectors.joining(","));

        return qualifiedTypeName
                + "#"
                + methodName
                + "("
                + parameters
                + ")";
    }

    public static String executableReferenceId(
            CtExecutableReference<?> reference) {

        if (reference == null
                || reference.getDeclaringType() == null) {
            return null;
        }

        String qualifiedTypeName =
                parameterType(
                        reference.getDeclaringType()
                );

        String methodName = reference.isConstructor()
                ? "<init>"
                : reference.getSimpleName();

        String parameters = reference
                .getParameters()
                .stream()
                .map(SpoonIdentifiers::parameterType)
                .collect(Collectors.joining(","));

        return qualifiedTypeName
                + "#"
                + methodName
                + "("
                + parameters
                + ")";
    }

    public static String parameterType(
            CtTypeReference<?> typeReference) {

        if (typeReference == null) {
            return "non resolu";
        }

        String qualifiedName =
                typeReference.getQualifiedName();

        if (qualifiedName == null
                || qualifiedName.isBlank()) {

            qualifiedName =
                    typeReference.getSimpleName();
        }

        return qualifiedName.replace('$', '.');
    }
}