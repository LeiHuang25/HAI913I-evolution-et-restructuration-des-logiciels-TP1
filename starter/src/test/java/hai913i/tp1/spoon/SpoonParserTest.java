package hai913i.tp1.spoon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import hai913i.tp1.graph.CallGraph;
import hai913i.tp1.graph.CallgraphBuilder;
import hai913i.tp1.model.FieldFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFact;
import hai913i.tp1.model.TypeFact;
import hai913i.tp1.model.TypeKind;
import hai913i.tp1.parse.ProjectSources;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtType;


public class SpoonParserTest {

    @Test
    void shouldBuildValidationModel() throws Exception{
        Path projectPath = Path.of(
                "..",
                "resources",
                "validation"
        );

        ProjectSources sources =
                ProjectSources.of(projectPath);

        CtModel model =
                SpoonParser.parse(sources, false);

        int topLevelTypeCount =
                model.getAllTypes().size();

        SpoonStructureExtractor extractor =
                new SpoonStructureExtractor();

        List<CtType<?>> allTypes =
                extractor.extractTypes(model);

        int allTypeCount = allTypes.size();

        System.out.println("Types extraits avec Spoon :");

        for (CtType<?> type : allTypes) {
            System.out.println(
                    extractor.qualifiedName(type)
            );
        }

        System.out.println(
                "Types de premier niveau : "
                + topLevelTypeCount
        );

        System.out.println(
                "Tous les types Spoon     : "
                + allTypeCount
        );

        assertEquals(14, topLevelTypeCount);
        assertEquals(15, allTypeCount);
        
        boolean containsLoan =
                allTypes.stream()
                        .map(extractor::qualifiedName)
                        .anyMatch(name ->
                                name.equals(
                                    "library.service.LoanService.Loan"
                                )
                        );

        assertEquals(true, containsLoan);
        
        List<TypeFact> typeFacts =
                extractor.extractTypeFacts(model);

        long classCount = typeFacts.stream()
                .filter(type -> type.kind() == TypeKind.CLASS)
                .count();

        long interfaceCount = typeFacts.stream()
                .filter(type -> type.kind() == TypeKind.INTERFACE)
                .count();

        long enumCount = typeFacts.stream()
                .filter(type -> type.kind() == TypeKind.ENUM)
                .count();

        long recordCount = typeFacts.stream()
                .filter(type -> type.kind() == TypeKind.RECORD)
                .count();

        System.out.println(
                "Classes      : " + classCount
        );

        System.out.println(
                "Interfaces   : " + interfaceCount
        );

        System.out.println(
                "Enumerations : " + enumCount
        );

        System.out.println(
                "Records      : " + recordCount
        );

        assertEquals(15, typeFacts.size());
        assertEquals(12, classCount);
        assertEquals(2, interfaceCount);
        assertEquals(1, enumCount);
        assertEquals(0, recordCount);
        
        TypeFact book = typeFacts.stream()
                .filter(type -> type.qualifiedName()
                        .equals("library.model.Book"))
                .findFirst()
                .orElseThrow();

        TypeFact item = typeFacts.stream()
                .filter(type -> type.qualifiedName()
                        .equals("library.model.Item"))
                .findFirst()
                .orElseThrow();

        TypeFact loanException = typeFacts.stream()
                .filter(type -> type.qualifiedName()
                        .equals("library.service.LoanException"))
                .findFirst()
                .orElseThrow();

        assertEquals(
                List.of("library.model.Item"),
                book.superclasses()
        );

        assertEquals(
                List.of("library.model.Loanable"),
                item.interfaces()
        );

        assertEquals(
                List.of(
                        "java.lang.Exception",
                        "java.lang.Throwable"
                ),
                loanException.superclasses()
        );
        
        int rawFieldCount = allTypes.stream()
                .mapToInt(type ->
                        extractor.extractFields(
                                type,
                                true
                        ).size()
                )
                .sum();

        int normalizedFieldCount = allTypes.stream()
                .mapToInt(type ->
                        extractor.extractFields(
                                type,
                                false
                        ).size()
                )
                .sum();

        System.out.println(
                "Attributs Spoon bruts : "
                + rawFieldCount
        );

        System.out.println(
                "Attributs convention TP : "
                + normalizedFieldCount
        );

        assertEquals(24, rawFieldCount);
        assertEquals(21, normalizedFieldCount);
        
        int methodCount = allTypes.stream()
                .mapToInt(type ->
                        extractor.extractMethods(type).size()
                )
                .sum();

        int rawConstructorCount = allTypes.stream()
                .mapToInt(type ->
                        extractor.extractConstructors(
                                type,
                                true
                        ).size()
                )
                .sum();

        int explicitConstructorCount = allTypes.stream()
                .mapToInt(type ->
                        extractor.extractConstructors(
                                type,
                                false
                        ).size()
                )
                .sum();

        int rawExecutableCount =
                methodCount + rawConstructorCount;

        int normalizedExecutableCount =
                methodCount + explicitConstructorCount;

        System.out.println(
                "Methodes Spoon             : "
                + methodCount
        );

        System.out.println(
                "Constructeurs Spoon bruts  : "
                + rawConstructorCount
        );

        System.out.println(
                "Constructeurs explicites   : "
                + explicitConstructorCount
        );

        System.out.println(
                "Executables Spoon bruts    : "
                + rawExecutableCount
        );

        System.out.println(
                "Executables convention TP  : "
                + normalizedExecutableCount
        );

        assertEquals(50, methodCount);
        assertEquals(13, rawConstructorCount);
        assertEquals(9, explicitConstructorCount);
        assertEquals(63, rawExecutableCount);
        assertEquals(59, normalizedExecutableCount);
        
        SpoonCallExtractor callExtractor =
                new SpoonCallExtractor();

        List<CtInvocation<?>> rawInvocations =
                callExtractor.extractInvocations(
                        model,
                        true
                );

        List<CtInvocation<?>> normalizedInvocations =
                callExtractor.extractInvocations(
                        model,
                        false
                );

        long constructorInvocationCount =
                rawInvocations.stream()
                        .filter(
                            callExtractor::isConstructorInvocation
                        )
                        .count();

        System.out.println(
                "Invocations Spoon brutes       : "
                + rawInvocations.size()
        );

        System.out.println(
                "Invocations de constructeur    : "
                + constructorInvocationCount
        );

        System.out.println(
                "Invocations convention du TP   : "
                + normalizedInvocations.size()
        );

        assertEquals(107, rawInvocations.size());
        assertEquals(13, constructorInvocationCount);
        assertEquals(94, normalizedInvocations.size());
        
        int fieldFactCount = typeFacts.stream()
                .mapToInt(type ->
                        type.fields().size()
                )
                .sum();

        System.out.println(
                "FieldFact produits : "
                + fieldFactCount
        );

        assertEquals(21, fieldFactCount);
        
        assertTrue(
                book.fields().contains(
                        new FieldFact(
                                "author",
                                "String",
                                "private"
                        )
                )
        );

        assertTrue(
                book.fields().contains(
                        new FieldFact(
                                "pages",
                                "int",
                                "private"
                        )
                )
        );
        
        int methodFactCount = typeFacts.stream()
                .mapToInt(type ->
                        type.methods().size()
                )
                .sum();

        System.out.println(
                "MethodFact produits : "
                + methodFactCount
        );

        assertEquals(59, methodFactCount);
        
        MethodFact bookConstructor = book.methods()
                .stream()
                .filter(MethodFact::constructor)
                .findFirst()
                .orElseThrow();

        assertEquals(
                "library.model.Book#<init>"
                + "(java.lang.String,"
                + "java.lang.String,"
                + "java.lang.String,int)",
                bookConstructor.id()
        );

        assertEquals("Book", bookConstructor.name());
        assertEquals(4, bookConstructor.nbParametre());
        assertEquals(true, bookConstructor.constructor());
        assertEquals(5, bookConstructor.bodyLineCount());
        
        long callFactCount = typeFacts.stream()
                .flatMap(type -> type.methods().stream())
                .flatMap(method -> method.calls().stream())
                .count();

        long projectCallCount = typeFacts.stream()
                .flatMap(type -> type.methods().stream())
                .flatMap(method -> method.calls().stream())
                .filter(call -> call.projectTarget())
                .count();

        long unresolvedCallCount = typeFacts.stream()
                .flatMap(type -> type.methods().stream())
                .flatMap(method -> method.calls().stream())
                .filter(call -> !call.resolved())
                .count();

        long externalCallCount =
                callFactCount
                - projectCallCount
                - unresolvedCallCount;

        System.out.println(
                "CallFact produits      : "
                + callFactCount
        );

        System.out.println(
                "Appels projet          : "
                + projectCallCount
        );

        System.out.println(
                "Appels externes        : "
                + externalCallCount
        );

        System.out.println(
                "Appels non resolus     : "
                + unresolvedCallCount
        );

        assertEquals(94, callFactCount);
        assertEquals(46, projectCallCount);
        assertEquals(48, externalCallCount);
        assertEquals(0, unresolvedCallCount);
        
        ProjectFact spoonFacts =
                new ProjectFact(
                        typeFacts,
                        List.of()
                );

        CallGraph spoonGraph =
                CallgraphBuilder.build(spoonFacts);

        System.out.println(
                "Noeuds du graphe Spoon : "
                + spoonGraph.nodeCount()
        );

        System.out.println(
                "Arcs du graphe Spoon   : "
                + spoonGraph.edgeCount()
        );

        System.out.println(
                "Sites internes Spoon   : "
                + spoonGraph.internalCallSites()
        );

        System.out.println(
                "Appels externes Spoon  : "
                + spoonGraph.externalCallSites()
        );

        System.out.println(
                "Non resolus Spoon      : "
                + spoonGraph.unresolvedCallSites()
        );

        assertEquals(59, spoonGraph.nodeCount());
        assertEquals(44, spoonGraph.edgeCount());
        assertEquals(46, spoonGraph.internalCallSites());
        assertEquals(48, spoonGraph.externalCallSites());
        assertEquals(0, spoonGraph.unresolvedCallSites());
    }
}