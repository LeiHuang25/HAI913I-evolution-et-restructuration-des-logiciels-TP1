package hai913i.tp1.model;

import java.util.List;

public record TypeFact(
		String quilifiedName,
		TypeKind kind,
		String packageName,
		List<String> superclasses,
        List<String> interfaces,
        List<FieldFact> fields,
        List<MethodFact> methods
) {
	public TypeFact {
		superclasses = List.copyOf(superclasses);
        interfaces = List.copyOf(interfaces);
        fields = List.copyOf(fields);
        methods = List.copyOf(methods);
	}
}
