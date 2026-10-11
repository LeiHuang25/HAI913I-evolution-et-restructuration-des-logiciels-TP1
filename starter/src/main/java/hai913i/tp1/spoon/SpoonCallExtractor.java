package hai913i.tp1.spoon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import hai913i.tp1.model.CallFact;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public final class SpoonCallExtractor {

    public List<CtInvocation<?>> extractInvocations(
            CtModel model,
            boolean includeConstructorInvocations) {

        Objects.requireNonNull(
                model,
                "Le modele Spoon ne doit pas etre null"
        );

        List<CtInvocation<?>> invocations =
                new ArrayList<>();

        for (Object element : model.getElements(
                new TypeFilter<>(CtInvocation.class))) {

            CtInvocation<?> invocation =
                    (CtInvocation<?>) element;

            if (!includeConstructorInvocations
                    && isConstructorInvocation(invocation)) {
                continue;
            }

            invocations.add(invocation);
        }

        invocations.sort(
                Comparator
                    .comparingInt(this::line)
                    .thenComparing(invocation ->
                            invocation.getExecutable()
                                      .getSimpleName())
        );

        return List.copyOf(invocations);
    }
    
    public List<CallFact> extractCallFacts(
            CtExecutable<?> executable,
            Set<String> projectMethodIds) {

        List<CallFact> calls =
                new ArrayList<>();

        for (Object element : executable.getElements(
                new TypeFilter<>(CtInvocation.class))) {

            CtInvocation<?> invocation =
                    (CtInvocation<?>) element;

            // La convention du TP ne compte pas super(...)
            // des constructeurs.
            if (isConstructorInvocation(invocation)) {
                continue;
            }

            calls.add(
                    createCallFact(
                            invocation,
                            projectMethodIds
                    )
            );
        }

        calls.sort(
                Comparator
                    .comparingInt(CallFact::line)
                    .thenComparing(CallFact::methodName)
        );

        return List.copyOf(calls);
    }
    
    private CallFact createCallFact(
            CtInvocation<?> invocation,
            Set<String> projectMethodIds) {

        CtExecutableReference<?> reference =
                invocation.getExecutable();

        String methodName = reference == null
                ? "non resolu"
                : reference.getSimpleName();

        String receiverType =
                receiverType(invocation);

        String targetMethod =
                resolvedTargetId(reference);

        boolean resolved =
                targetMethod != null;

        boolean projectTarget =
                resolved
                && projectMethodIds.contains(targetMethod);

        return new CallFact(
                methodName,
                receiverType,
                targetMethod,
                line(invocation),
                resolved,
                projectTarget
        );
    }
    
    private String receiverType(
            CtInvocation<?> invocation) {

        CtExpression<?> target =
                invocation.getTarget();

        CtTypeReference<?> receiverType = null;

        if (target != null) {
            receiverType = target.getType();
        }

        if (receiverType == null
                && invocation.getExecutable() != null) {

            receiverType = invocation
                    .getExecutable()
                    .getDeclaringType();
        }

        if (receiverType == null) {
            return null;
        }

        return SpoonIdentifiers
                .parameterType(receiverType);
    }
    
    private String resolvedTargetId(
            CtExecutableReference<?> reference) {

        if (reference == null) {
            return null;
        }

        CtExecutable<?> declaration =
                reference.getExecutableDeclaration();

        if (declaration == null) {
            return null;
        }

        return SpoonIdentifiers
                .executableId(declaration);
    }
    

    public boolean isConstructorInvocation(
            CtInvocation<?> invocation) {

        return invocation.getExecutable() != null
                && invocation.getExecutable()
                             .isConstructor();
    }

    public int line(CtInvocation<?> invocation) {
        if (invocation.getPosition() == null
                || !invocation.getPosition()
                              .isValidPosition()) {

            return -1;
        }

        return invocation.getPosition().getLine();
    }
}