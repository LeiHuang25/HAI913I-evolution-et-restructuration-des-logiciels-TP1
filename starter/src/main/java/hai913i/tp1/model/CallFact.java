package hai913i.tp1.model;

public record CallFact(
        String methodName,
        String receiverType,
        String targetMethod,
        int line,
        boolean resolved,
        boolean projectTarget
) {
}