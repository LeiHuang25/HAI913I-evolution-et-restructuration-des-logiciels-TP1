package hai913i.tp1.spoon;

import java.nio.file.Path;
import java.util.List;

import hai913i.tp1.extract.StructureVisitor;
import hai913i.tp1.graph.CallGraph;
import hai913i.tp1.graph.CallgraphBuilder;
import hai913i.tp1.model.ProjectFact;
import hai913i.tp1.model.TypeFact;
import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;

public final class SpoonComparisonMain {

    private SpoonComparisonMain() {
    }
    
    private static void printUsage() {
        System.err.println(
                "Usage : SpoonComparisonMain "
                + "DOSSIER_DU_PROJET "
                + "[--noclasspath]"
        );
    }

    public static void main(String[] args)
            throws Exception {

    	if (args.length < 1 || args.length > 2) {
    	    printUsage();
    	    return;
    	}

    	boolean noClasspath = false;

    	if (args.length == 2) {
    	    if (!"--noclasspath".equals(args[1])) {
    	        System.err.println(
    	                "Option inconnue : " + args[1]
    	        );
    	        printUsage();
    	        return;
    	    }

    	    noClasspath = true;
    	}

        ProjectSources sources =
                ProjectSources.of(
                        Path.of(args[0])
                );

        List<TypeFact> jdtTypes =
                extractJdtTypes(sources);

        CtModel spoonModel =
                SpoonParser.parse(
                        sources,
                        noClasspath
                );

        SpoonStructureExtractor spoonExtractor =
                new SpoonStructureExtractor();

        List<CtType<?>> spoonCtTypes =
                spoonExtractor.extractTypes(
                        spoonModel
                );

        List<TypeFact> spoonTypes =
                spoonExtractor.extractTypeFacts(
                        spoonModel
                );

        int spoonRawFields = spoonCtTypes.stream()
                .mapToInt(type ->
                        spoonExtractor
                                .extractFields(
                                        type,
                                        true
                                )
                                .size()
                )
                .sum();

        int spoonRawMethods = spoonCtTypes.stream()
                .mapToInt(type ->
                        spoonExtractor
                                .extractMethods(type)
                                .size()
                        + spoonExtractor
                                .extractConstructors(
                                        type,
                                        true
                                )
                                .size()
                )
                .sum();

        SpoonCallExtractor callExtractor =
                new SpoonCallExtractor();

        int spoonRawCalls =
                callExtractor.extractInvocations(
                        spoonModel,
                        true
                ).size();

        ProjectFact jdtFacts =
                new ProjectFact(
                        jdtTypes,
                        List.of()
                );

        ProjectFact spoonFacts =
                new ProjectFact(
                        spoonTypes,
                        List.of()
                );
        
        System.out.println(
                "Mode Spoon noClasspath : "
                + noClasspath
        );

        printComparison(
                jdtTypes,
                spoonTypes,
                spoonRawFields,
                spoonRawMethods,
                spoonRawCalls,
                CallgraphBuilder.build(jdtFacts),
                CallgraphBuilder.build(spoonFacts),
                spoonModel.getAllTypes().size()
        );
        
        System.out.println();
        System.out.println(
                "Appels Spoon non resolus :"
        );

        int unresolvedCount = 0;

        for (TypeFact type : spoonTypes) {
            for (var method : type.methods()) {
                for (var call : method.calls()) {
                    if (!call.resolved()) {
                        unresolvedCount++;

                        String receiver =
                                call.receiverType() == null
                                ? "non resolu"
                                : call.receiverType();

                        System.out.println(
                                "  "
                                + method.id()
                                + " -> "
                                + call.methodName()
                                + " receiver="
                                + receiver
                                + " line="
                                + call.line()
                        );
                    }
                }
            }
        }

        if (unresolvedCount == 0) {
            System.out.println("  aucun");
        }
    }

    private static List<TypeFact> extractJdtTypes(
            ProjectSources sources)
            throws Exception {

        List<ParsedFile> files =
                JdtParser.parse(
                        sources,
                        List.of()
                );

        StructureVisitor visitor =
                new StructureVisitor();

        for (ParsedFile file : files) {
            file.unit().accept(visitor);
        }

        return visitor.getTypes();
    }

    private static void printComparison(
            List<TypeFact> jdtTypes,
            List<TypeFact> spoonTypes,
            int spoonRawFields,
            int spoonRawMethods,
            int spoonRawCalls,
            CallGraph jdtGraph,
            CallGraph spoonGraph,
            int spoonTopLevelTypes) {

        int jdtFieldCount =
                fieldCount(jdtTypes);

        int jdtMethodCount =
                methodCount(jdtTypes);

        int jdtCallCount =
                callCount(jdtTypes);

        int spoonFieldCount =
                fieldCount(spoonTypes);

        int spoonMethodCount =
                methodCount(spoonTypes);

        int spoonCallCount =
                callCount(spoonTypes);

        System.out.println();
        System.out.println(
                "Comparaison JDT / Spoon"
        );
        System.out.println(
                "-----------------------"
        );

        System.out.printf(
                "%-22s %8s %12s %16s%n",
                "Mesure",
                "JDT",
                "Spoon brut",
                "Spoon normalise"
        );

        System.out.printf(
                "%-22s %8d %12d %16d%n",
                "Types",
                jdtTypes.size(),
                spoonTypes.size(),
                spoonTypes.size()
        );

        System.out.printf(
                "%-22s %8d %12d %16d%n",
                "Attributs",
                jdtFieldCount,
                spoonRawFields,
                spoonFieldCount
        );

        System.out.printf(
                "%-22s %8d %12d %16d%n",
                "Methodes",
                jdtMethodCount,
                spoonRawMethods,
                spoonMethodCount
        );

        System.out.printf(
                "%-22s %8d %12d %16d%n",
                "Appels",
                jdtCallCount,
                spoonRawCalls,
                spoonCallCount
        );

        System.out.println();
        System.out.println(
                "Types Spoon premier niveau : "
                + spoonTopLevelTypes
        );

        System.out.println();
        System.out.println(
                "Graphe JDT   : "
                + jdtGraph.nodeCount()
                + " noeuds, "
                + jdtGraph.edgeCount()
                + " arcs, "
                + jdtGraph.internalCallSites()
                + " sites internes"
        );

        System.out.println(
                "Graphe Spoon : "
                + spoonGraph.nodeCount()
                + " noeuds, "
                + spoonGraph.edgeCount()
                + " arcs, "
                + spoonGraph.internalCallSites()
                + " sites internes"
        );
    }

    private static int fieldCount(
            List<TypeFact> types) {

        return types.stream()
                .mapToInt(type ->
                        type.fields().size()
                )
                .sum();
    }

    private static int methodCount(
            List<TypeFact> types) {

        return types.stream()
                .mapToInt(type ->
                        type.methods().size()
                )
                .sum();
    }

    private static int callCount(
            List<TypeFact> types) {

        return types.stream()
                .flatMap(type ->
                        type.methods().stream()
                )
                .mapToInt(method ->
                        method.calls().size()
                )
                .sum();
    }
}