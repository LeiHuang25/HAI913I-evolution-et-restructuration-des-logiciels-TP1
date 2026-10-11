package hai913i.tp1.output;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import hai913i.tp1.graph.CallEdge;
import hai913i.tp1.graph.CallGraph;
import hai913i.tp1.metrics.MetricsCalculator;
import hai913i.tp1.model.CallFact;
import hai913i.tp1.model.FieldFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFact;
import hai913i.tp1.model.TypeFact;

public final class ConsoleReporter {
	private ConsoleReporter() {
		
	}
	
    public static void printStructure(ProjectFact facts) {
        System.out.println();
        System.out.println("Structure du projet");
        System.out.println("-------------------");

        for (TypeFact type : facts.types()) {
            System.out.println();
            System.out.println(type.qualifiedName());
            System.out.println("  kind        : " + type.kind());
            System.out.println("  package     : " + type.packageName());
            System.out.println("  superclasses: " + type.superclasses());
            System.out.println("  interfaces  : " + type.interfaces());
            System.out.println("  fields      : " + type.fields().size());
            System.out.println("  methods     : " + type.methods().size());

            for (FieldFact field : type.fields()) {
                System.out.println(
                        "  FIELD "
                        + field.visibility() + " "
                        + field.declaredType() + " "
                        + field.name()
                );
            }

            for (MethodFact method : type.methods()) {
                System.out.println(
                        "  METHOD "
                        + method.name()
                        + " id=" + method.id()
                        + " nbParametre=" + method.nbParametre()
                        + " constructor=" + method.constructor()
                        + " bodyLines=" + method.bodyLineCount()
                );
            }
        }
    }
    
    public static void printCalls(ProjectFact facts) {
        System.out.println();
        System.out.println("Appels de methodes");
        System.out.println("------------------");

        for (TypeFact type : facts.types()) {
            for (MethodFact method : type.methods()) {

                if (method.calls().isEmpty()) {
                    continue;
                }

                System.out.println();
                System.out.println(
                        method.id()
                );

                for (CallFact call : method.calls()) {
                    System.out.println(
                            "  CALL "
                            + call.methodName()
                            + " receiver=" + call.receiverType()
                            + " target=" + call.targetMethod()
                            + " line=" + call.line()
                            + " resolved=" + call.resolved()
                            + " project=" + call.projectTarget()
                    );
                }
            }
        }

        long totalCalls = facts.calls().size();

        long projectCalls = facts.calls().stream()
                .filter(CallFact::projectTarget)
                .count();

        long unresolvedCalls = facts.calls().stream()
                .filter(call -> !call.resolved())
                .count();

        long externalCalls =
                totalCalls
                - projectCalls
                - unresolvedCalls;

        System.out.println();
        System.out.println(
                "Appels totaux         : " + totalCalls
        );
        System.out.println(
                "Appels vers le projet : " + projectCalls
        );
        System.out.println(
                "Appels externes       : " + externalCalls
        );
        System.out.println(
                "Appels non resolus    : " + unresolvedCalls
        );
    }

    public static void printMetrics(
            MetricsCalculator metrics,
            int threshold) {
        //B2 metrics
        System.out.println();
        System.out.println("Metriques B2");
        System.out.println("------------");


        System.out.println(
                "Q1  Nombre de classes               : "
                + metrics.classCount()
        );

        System.out.println(
                "Q2  Lignes de l'application         : "
                + metrics.applicationLineCount()
        );

        System.out.println(
                "Q3  Nombre total de methodes        : "
                + metrics.methodCount()
        );

        System.out.println(
                "Q4  Nombre total de paquetages      : "
                + metrics.packageCount()
        );

        System.out.printf(
                Locale.ROOT,
                "Q5  Moyenne methodes / classe       : %.2f%n",
                metrics.averageMethodsPerClass()
        );

        System.out.printf(
                Locale.ROOT,
                "Q6  Moyenne lignes / methode        : %.2f%n",
                metrics.averageBodyLinesPerMethod()
        );

        System.out.printf(
                Locale.ROOT,
                "Q7  Moyenne attributs / classe      : %.2f%n",
                metrics.averageFieldsPerClass()
        );
        
        System.out.println(
                "Q8  10 % des classes avec le plus de methodes :"
        );

        for (TypeFact type : metrics.topTenPercentByMethods()) {
            System.out.println(
                    "    "
                    + type.qualifiedName()
                    + " : "
                    + type.methods().size()
            );
        }
        
        System.out.println(
                "Q9  10 % des classes avec le plus d'attributs :"
        );

        for (TypeFact type : metrics.topTenPercentByFields()) {
            System.out.println(
                    "    "
                    + type.qualifiedName()
                    + " : "
                    + type.fields().size()
            );
        }
        
        System.out.println(
                "Q10 Intersection de Q8 et Q9 :"
        );

        for (TypeFact type :
                metrics.topTenPercentIntersection()) {

            System.out.println(
                    "    " + type.qualifiedName()
            );
        }
        
        System.out.println(
                "Q11 Classes avec strictement plus de "
                + threshold
                + " methodes :"
        );

        for (TypeFact type :
                metrics.classesWithMoreThanMethods(threshold)) {

            System.out.println(
                    "    "
                    + type.qualifiedName()
                    + " : "
                    + type.methods().size()
            );
        }
        
        System.out.println(
                "Q12 Methodes avec le plus de lignes, par classe :"
        );

        for (Map.Entry<TypeFact, List<MethodFact>> entry :
                metrics
                        .topTenPercentMethodsByLinesForEachClass()
                        .entrySet()) {

            TypeFact type = entry.getKey();
            List<MethodFact> selectedMethods =
                    entry.getValue();

            System.out.println(
                    "    " + type.qualifiedName()
            );

            if (selectedMethods.isEmpty()) {
                System.out.println(
                        "        aucune methode avec corps"
                );
            } else {
                for (MethodFact method : selectedMethods) {
                    System.out.println(
                            "        "
                            + method.id()
                            + " : "
                            + method.bodyLineCount()
                            + " lignes"
                    );
                }
            }
        }
        
        System.out.println(
                "Q13 Nombre maximal de parametres : "
                + metrics.maximumParameterCount()
        );

        for (MethodFact method :
                metrics.methodsWithMaximumParameters()) {

            System.out.println(
                    "    " + method.id()
            );
        }
    }
    
    //B3 affichage
    private static void printCallees(CallGraph graph, String methodId) {
    	System.out.println();
    	System.out.println("Methodes appelees par : " + methodId);
    	
    	List<CallEdge> edges = graph.calleesOf(methodId);
    	
    	if (edges.isEmpty()) {
    		System.out.println("aucune");
    		return;
    	}
    	
    	for (CallEdge edge : edges) {
    		System.out.println(
    				" "
    				+ edge.targetId()
    				+ "[poids="
    				+ edge.weight()
    				+ "]");
    	}
    }
    
    // recherche callers
    private static void printCallers(CallGraph graph, String methodId) {
    	System.out.println();
        System.out.println("Methodes appelantes de : " + methodId);
        
        List<CallEdge> edges = graph.callersOf(methodId);
        
        if (edges.isEmpty()) {
    		System.out.println("aucune");
    		return;
    	}
        
        for (CallEdge edge : edges) {
            System.out.println(
                    "    "
                    + edge.callerId()
                    + " [poids="
                    + edge.weight()
                    + "]"
            );
        }
    }
    
    public static void printGraph(
            CallGraph callGraph) {

        System.out.println();
        System.out.println("Graphe d'appel B3");
        System.out.println("-----------------");

        System.out.println(
                "Noeuds             : "
                + callGraph.nodeCount()
        );

        System.out.println(
                "Arcs               : "
                + callGraph.edgeCount()
        );

        System.out.println(
                "Sites internes     : "
                + callGraph.internalCallSites()
        );

        System.out.println(
                "Appels externes    : "
                + callGraph.externalCallSites()
        );

        System.out.println(
                "Appels non resolus : "
                + callGraph.unresolvedCallSites()
        );

        System.out.println();
        System.out.println("Arcs :");

        for (CallEdge edge : callGraph.edges()) {
            System.out.println(
                    "  "
                    + edge.callerId()
                    + " -> "
                    + edge.targetId()
                    + " [poids="
                    + edge.weight()
                    + "]"
            );
        }

        String loanableCheckOut =
                "library.model.Loanable"
                + "#checkOut(library.model.Member)";

        if (callGraph.containsNode(loanableCheckOut)) {
            printCallers(
                    callGraph,
                    loanableCheckOut
            );
        }

        String itemCheckOut =
                "library.model.Item"
                + "#checkOut(library.model.Member)";

        if (callGraph.containsNode(itemCheckOut)) {
            printCallers(
                    callGraph,
                    itemCheckOut
            );
        }

        String borrow =
                "library.service.LoanService"
                + "#borrow("
                + "library.model.Member,"
                + "java.lang.String,"
                + "int)";

        if (callGraph.containsNode(borrow)) {
            printCallees(
                    callGraph,
                    borrow
            );
        }
    }



}
