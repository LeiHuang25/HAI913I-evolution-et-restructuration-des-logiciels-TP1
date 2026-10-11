package hai913i.tp1.spoon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import hai913i.tp1.model.CallFact;
import hai913i.tp1.model.FieldFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.TypeFact;
import hai913i.tp1.model.TypeKind;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public final class SpoonStructureExtractor {

	public List<CtType<?>> extractTypes(CtModel model) {
	    Objects.requireNonNull(
	            model,
	            "Le modele Spoon ne doit pas etre null"
	    );

	    List<CtType<?>> types = new ArrayList<>();

	    for (Object element : model.getElements(
	            new TypeFilter<>(CtType.class))) {

	        types.add((CtType<?>) element);
	    }

	    types.sort(
	            Comparator.comparing(
	                    this::qualifiedName
	            )
	    );

	    return List.copyOf(types);
	}
	
	public List<CtField<?>> extractFields(
	        CtType<?> type,
	        boolean includeEnumConstants) {

	    List<CtField<?>> fields =
	            new ArrayList<>();

	    for (CtField<?> field : type.getFields()) {
	        if (!includeEnumConstants
	                && field instanceof CtEnumValue<?>) {
	            continue;
	        }

	        fields.add(field);
	    }

	    fields.sort(
	            Comparator.comparing(
	                    CtField::getSimpleName
	            )
	    );

	    return List.copyOf(fields);
	}
	
	public List<CtMethod<?>> extractMethods(
	        CtType<?> type) {

	    List<CtMethod<?>> methods =
	            new ArrayList<>(type.getMethods());

	    methods.sort(
	            Comparator.comparing(
	                    CtMethod::getSimpleName
	            )
	    );

	    return List.copyOf(methods);
	}
	
	public List<CtConstructor<?>> extractConstructors(
	        CtType<?> type,
	        boolean includeImplicit) {

	    if (!(type instanceof CtClass<?> ctClass)) {
	        return List.of();
	    }

	    List<CtConstructor<?>> constructors =
	            new ArrayList<>();

	    for (CtConstructor<?> constructor
	            : ctClass.getConstructors()) {

	        if (!includeImplicit
	                && constructor.isImplicit()) {
	            continue;
	        }

	        constructors.add(constructor);
	    }

	    constructors.sort(
	            Comparator.comparing(
	                    CtConstructor::getSimpleName
	            )
	    );

	    return List.copyOf(constructors);
	}

    public String qualifiedName(CtType<?> type) {
        return type.getQualifiedName()
                .replace('$', '.');
    }
    
    public List<TypeFact> extractTypeFacts(
            CtModel model) {

        List<TypeFact> typeFacts =
                new ArrayList<>();

        Set<String> projectMethodIds =
                collectProjectMethodIds(model);

        SpoonCallExtractor callExtractor =
                new SpoonCallExtractor();

        for (CtType<?> type : extractTypes(model)) {
            typeFacts.add(
                    createTypeFact(
                            type,
                            callExtractor,
                            projectMethodIds
                    )
            );
        }

        return List.copyOf(typeFacts);
    }

    private TypeFact createTypeFact(
            CtType<?> type,
            SpoonCallExtractor callExtractor,
            Set<String> projectMethodIds) {
        String qualifiedName = qualifiedName(type);
        String packageName = packageName(type);
        TypeKind kind = typeKind(type);
        
        List<String> superclasses =
                extractSuperclasses(type);

        List<String> interfaces =
                extractInterfaces(type);
        
        List<FieldFact> fields =
                extractFieldFacts(type);
        
        List<MethodFact> methods =
        		extractMethodFacts(
                        type,
                        callExtractor,
                        projectMethodIds
                );
        
        return new TypeFact(
                qualifiedName,
                kind,
                packageName,
                superclasses,
                interfaces,
                fields,
                methods
        );
    }
    
    private List<String> extractSuperclasses(
            CtType<?> type) {

        List<String> superclasses = new ArrayList<>();

        if (!(type instanceof CtClass<?> ctClass)) {
            return List.of();
        }

        CtTypeReference<?> current =
                ctClass.getSuperclass();

        Set<String> visited =
                new LinkedHashSet<>();

        while (current != null) {
            String qualifiedName =
                    current.getQualifiedName()
                           .replace('$', '.');

            if ("java.lang.Object".equals(qualifiedName)) {
                break;
            }

            if (!visited.add(qualifiedName)) {
                break;
            }

            superclasses.add(
                    referenceName(current)
            );

            current = current.getSuperclass();
        }

        return List.copyOf(superclasses);
    }
    
    private List<String> extractInterfaces(
            CtType<?> type) {

        List<String> interfaces =
                new ArrayList<>();

        for (CtTypeReference<?> interfaceReference
                : type.getSuperInterfaces()) {

            interfaces.add(
                    referenceName(interfaceReference)
            );
        }

        interfaces.sort(String::compareTo);

        return List.copyOf(interfaces);
    }
    
    private String referenceName(
            CtTypeReference<?> reference) {

        return reference.toString()
                .replace('$', '.');
    }

    private String packageName(CtType<?> type) {
        CtPackage ctPackage = type.getPackage();

        if (ctPackage == null
                || ctPackage.getQualifiedName() == null
                || ctPackage.getQualifiedName().isBlank()) {

            return "(défaut)";
        }

        return ctPackage.getQualifiedName();
    }

    private TypeKind typeKind(CtType<?> type) {
        if (type instanceof CtInterface<?>) {
            return TypeKind.INTERFACE;
        }

        if (type instanceof CtEnum<?>) {
            return TypeKind.ENUM;
        }

        if (type instanceof CtRecord) {
            return TypeKind.RECORD;
        }

        return TypeKind.CLASS;
    }
    
    private List<FieldFact> extractFieldFacts(
            CtType<?> type) {

        List<FieldFact> fieldFacts =
                new ArrayList<>();

        for (CtField<?> field
                : extractFields(type, false)) {

            fieldFacts.add(
                    new FieldFact(
                            field.getSimpleName(),
                            declaredType(field.getType()),
                            visibility(field)
                    )
            );
        }

        return List.copyOf(fieldFacts);
    }
    
    private String declaredType(
            CtTypeReference<?> typeReference) {

        if (typeReference == null) {
            return "non resolu";
        }

        String simpleName =
                typeReference.getSimpleName()
                             .replace('$', '.');

        if (typeReference
                .getActualTypeArguments()
                .isEmpty()) {

            return simpleName;
        }

        String arguments = typeReference
                .getActualTypeArguments()
                .stream()
                .map(this::declaredType)
                .collect(Collectors.joining(", "));

        return simpleName
                + "<"
                + arguments
                + ">";
    }
    
    private String visibility(
            CtField<?> field) {

        if (field.hasModifier(ModifierKind.PUBLIC)) {
            return "public";
        }

        if (field.hasModifier(ModifierKind.PROTECTED)) {
            return "protected";
        }

        if (field.hasModifier(ModifierKind.PRIVATE)) {
            return "private";
        }

        return "package";
    }
    
    private List<MethodFact> extractMethodFacts(
            CtType<?> type,
            SpoonCallExtractor callExtractor,
            Set<String> projectMethodIds) {

        List<MethodFact> methodFacts =
                new ArrayList<>();

        for (CtMethod<?> method
                : extractMethods(type)) {

            methodFacts.add(
                    createMethodFact(
                            method,
                            method.getSimpleName(),
                            false,
                            callExtractor,
                            projectMethodIds
                    )
            );
        }

        for (CtConstructor<?> constructor
                : extractConstructors(type, false)) {

            methodFacts.add(
                    createMethodFact(
                            constructor,
                            type.getSimpleName(),
                            true,
                            callExtractor,
                            projectMethodIds
                    )
            );
        }

        methodFacts.sort(
                Comparator.comparing(
                        MethodFact::id
                )
        );

        return List.copyOf(methodFacts);
    }
    
    private MethodFact createMethodFact(
            CtExecutable<?> executable,
            String displayedName,
            boolean constructor,
            SpoonCallExtractor callExtractor,
            Set<String> projectMethodIds) {

        String id =
                SpoonIdentifiers
                        .executableId(executable);

        int nbParametre =
                executable.getParameters().size();

        int bodyLineCount =
                bodyLineCount(executable);

        List<CallFact> calls =
                callExtractor.extractCallFacts(
                        executable,
                        projectMethodIds
                );

        return new MethodFact(
                id,
                displayedName,
                nbParametre,
                constructor,
                bodyLineCount,
                calls
        );
    }
    
    
    private int bodyLineCount(
            CtExecutable<?> executable) {

        if (executable.getBody() == null
                || executable.getBody()
                             .getPosition() == null
                || !executable.getBody()
                              .getPosition()
                              .isValidPosition()) {

            return 0;
        }

        int startLine = executable
                .getBody()
                .getPosition()
                .getLine();

        int endLine = executable
                .getBody()
                .getPosition()
                .getEndLine();

        return endLine - startLine + 1;
    }
    
    private Set<String> collectProjectMethodIds(
            CtModel model) {

        Set<String> methodIds =
                new LinkedHashSet<>();

        for (CtType<?> type : extractTypes(model)) {

            for (CtMethod<?> method
                    : extractMethods(type)) {

                String id =
                        SpoonIdentifiers
                                .executableId(method);

                if (id != null) {
                    methodIds.add(id);
                }
            }

            for (CtConstructor<?> constructor
                    : extractConstructors(type, false)) {

                String id =
                        SpoonIdentifiers
                                .executableId(constructor);

                if (id != null) {
                    methodIds.add(id);
                }
            }
        }

        return Set.copyOf(methodIds);
    }
}