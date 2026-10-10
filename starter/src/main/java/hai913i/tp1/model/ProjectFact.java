package hai913i.tp1.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ProjectFact {

    private final List<TypeFact> types;
    private final Map<String, MethodFact> methodsById;

    public ProjectFact(List<TypeFact> extractedTypes) {
        this.types = extractedTypes.stream()
                .map(ProjectFact::sortedType)
                .sorted(Comparator.comparing(TypeFact::qualifiedName))
                .toList();

        Map<String, MethodFact> index = new LinkedHashMap<>();

        for (TypeFact type : types) {
            for (MethodFact method : type.methods()) {
                MethodFact previous =
                        index.putIfAbsent(method.id(), method);

                if (previous != null) {
                    throw new IllegalArgumentException(
                            "Identifiant de methode duplique : "
                            + method.id()
                    );
                }
            }
        }

        this.methodsById =
                Collections.unmodifiableMap(index);
    }

    public List<TypeFact> types() {
        return types;
    }

    public List<MethodFact> methods() {
        return types.stream()
                .flatMap(type -> type.methods().stream())
                .toList();
    }

    public List<CallFact> calls() {
        return methods().stream()
                .flatMap(method -> method.calls().stream())
                .toList();
    }

    public Optional<MethodFact> findMethod(String methodId) {
        return Optional.ofNullable(
                methodsById.get(methodId)
        );
    }

    public Optional<MethodFact> findTarget(CallFact call) {
        if (!call.resolved() || !call.projectTarget()) {
            return Optional.empty();
        }

        return findMethod(call.targetMethod());
    }

    private static TypeFact sortedType(TypeFact type) {
        List<String> interfaces = type.interfaces().stream()
                .sorted()
                .toList();

        List<FieldFact> fields = type.fields().stream()
                .sorted(
                        Comparator.comparing(FieldFact::name)
                                .thenComparing(FieldFact::declaredType)
                )
                .toList();

        List<MethodFact> methods = type.methods().stream()
                .map(ProjectFact::sortedMethod)
                .sorted(Comparator.comparing(MethodFact::id))
                .toList();

        return new TypeFact(
                type.qualifiedName(),
                type.kind(),
                type.packageName(),
                List.copyOf(type.superclasses()),
                interfaces,
                fields,
                methods
        );
    }

    private static MethodFact sortedMethod(MethodFact method) {
        List<CallFact> calls = new ArrayList<>(
                method.calls()
        );

        calls.sort(
                Comparator.comparingInt(CallFact::line)
                        .thenComparing(CallFact::methodName)
                        .thenComparing(CallFact::targetMethod)
                        .thenComparing(CallFact::receiverType)
        );

        return new MethodFact(
                method.id(),
                method.name(),
                method.nbParametre(),
                method.constructor(),
                calls
        );
    }
}