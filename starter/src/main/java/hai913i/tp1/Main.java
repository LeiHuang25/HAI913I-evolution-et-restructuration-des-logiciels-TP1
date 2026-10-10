package hai913i.tp1;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.compiler.IProblem;

import hai913i.tp1.extract.StructureVisitor;
import hai913i.tp1.metrics.MetricsCalculator;
import hai913i.tp1.model.CallFact;
import hai913i.tp1.model.FieldFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.TypeFact;
import hai913i.tp1.model.TypeKind;
import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;
import hai913i.tp1.visitor.Visiteur;
import hai913i.tp1.model.ProjectFact;
import hai913i.tp1.model.SourceFileFact;
/**
 * Point d'entrée en ligne de commande de l'analyseur (version de départ).
 *
 * Le squelette ne vérifie que l'environnement (point de contrôle A0) : il analyse le projet et affiche
 * le nombre d'unités de compilation et d'erreurs. Tout le reste est à concevoir : extraction de la
 * structure, appels, métriques, graphe d'appel, options comme le seuil X.
 *
 * Usage : java -jar target/hai913i-tp1-analyzer.jar DOSSIER_DU_PROJET
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage : java -jar target/hai913i-tp1-analyzer.jar" + "DOSSIER_DU_PROJET X");
            System.exit(2);
        }
        Path project = Path.of(args[0]);
        //B2
        int threshold;
        try {
        	threshold = Integer.parseInt(args[1]);
        }catch (NumberFormatException exception) {
        	System.err.println("Erreur : X doit etre un entier positif ou nul.");
        	System.exit(2);
        	return;
        }
        if(threshold < 0) {
        	System.err.println("Erreur : X doit etre positif ou nul.");
        	System.exit(2);
        	return;
        }
        
        ProjectSources sources;
        try {
            sources = ProjectSources.of(project);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur : " + e.getMessage());
            System.exit(3);
            return;
        }

        List<ParsedFile> files = JdtParser.parse(sources, List.of());
        int errors = 0;
        for (ParsedFile file : files) {
            for (IProblem problem : file.unit().getProblems()) {
                if (problem.isError()) {
                    errors++;
                    System.err.println(file.path().getFileName() + ":" + problem.getSourceLineNumber() + " "
                            + problem.getMessage());
                }
            }
        }
        System.out.println("Racine des sources     : " + sources.sourceRoot());
        System.out.println("Unites de compilation  : " + files.size());
        System.out.println("Erreurs de compilation : " + errors);
        
        //A1 Parcourir un AST
        for (ParsedFile file : files) {
            String fileName = file.path().getFileName().toString();

            if (fileName.equals("Dvd.java") || fileName.equals("Category.java")) {
                System.out.println();
                System.out.println("AST de " + fileName + "\n");

                file.unit().accept(new Visiteur());
            }
        }
        
        System.out.println();
        //A2 structureVisitor
        StructureVisitor structureVisitor = new StructureVisitor();
        
        for (ParsedFile file : files) {
            file.unit().accept(structureVisitor);
        }
        //B2.2
        List<SourceFileFact> sourceFiles = new ArrayList<>();
        
        for (ParsedFile file : files) {
        	String relativePath = sources.sourceRoot()
        			.relativize(file.path())
        			.toString()
        			.replace('\\', '/');
        	String packageName;
        	
        	if (file.unit().getPackage() == null) {
        		packageName = "(défaut)";
        	}else {
        		packageName = file.unit()
        				.getPackage()
        				.getName()
        				.getFullyQualifiedName();
        	}
        	
        	int lineCount = Files.readAllLines(file.path()).size();
        	
        	sourceFiles.add(new SourceFileFact(
        			relativePath,
        			packageName,
        			lineCount));
        }
        
        //B1.2 je utilise mon propre fact model
        ProjectFact facts = new ProjectFact(structureVisitor.getTypes(),sourceFiles);
        List<TypeFact> types = facts.types();
        //B2
        MetricsCalculator metrics = new MetricsCalculator(facts);
        
        long classCount = types.stream()
                .filter(type -> type.kind() == TypeKind.CLASS)
                .count();

        long interfaceCount = types.stream()
                .filter(type -> type.kind() == TypeKind.INTERFACE)
                .count();

        long enumCount = types.stream()
                .filter(type -> type.kind() == TypeKind.ENUM)
                .count();

        long recordCount = types.stream()
                .filter(type -> type.kind() == TypeKind.RECORD)
                .count();
        
        System.out.println("Types extraits :");
        types.stream()
                .sorted((first, second) ->
                        first.qualifiedName()
                                .compareTo(second.qualifiedName()))
                .forEach(type ->
                        System.out.println(
                                type.kind()
                                + " : "
                                + type.qualifiedName()
                        )
                );

        //暂时
        for (TypeFact type : types) {
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
                        + " parameters=" + method.nbParametre()
                        + " constructor=" + method.constructor()
                        + " bodyLines=" + method.bodyLineCount()
                );
                
                for (CallFact call : method.calls()) {
            		System.out.println(
            				"  CALL "
            				+ call.methodName() + "\n"
            				+ "  receiver=" + call.receiverType() + "\n"
            				+ "  target=" + call.targetMethod() + "\n"
            				+ "  line=" + call.line() + "\n"
            				+ "  resolved=" + call.resolved() + "\n"
            				+ "  project=" + call.projectTarget() + "\n"
            				);
            	}
            }
        	
        	
        }
        
        int fieldCount = types.stream()
                .mapToInt(type -> type.fields().size())
                .sum();
        
        int methodCount = facts.methods().size();

        long constructorCount = facts.methods().stream()
                .filter(MethodFact::constructor)
                .count();
        
        long callCount = facts.calls().size();

        long projectCallCount = facts.calls().stream()
        		.filter(CallFact::projectTarget)
        		.count();

        long unresolvedCallCount = facts.calls().stream()
                .filter(call -> !call.resolved())
                .count();

        long externalCallCount =
                callCount 
                - projectCallCount 
                - unresolvedCallCount;
        
        
        System.out.println();
        System.out.println("Types         : " + types.size());
        System.out.println("Classes       : " + classCount);
        System.out.println("Interfaces    : " + interfaceCount);
        System.out.println("Enumerations  : " + enumCount);
        System.out.println("Records       : " + recordCount);
        System.out.println("Fields        : " + fieldCount);
        System.out.println("Methodes      : " + methodCount);
        System.out.println("Constructeurs : " + constructorCount);
        System.out.println();
        System.out.println("Appels totaux           : " + callCount);
        System.out.println("Appels vers le projet   : " + projectCallCount);
        System.out.println("Appels externes         : " + externalCallCount);
        System.out.println("Appels non resolus      : " + unresolvedCallCount);
        
        long resolvedProjectTargets = facts.calls().stream()
                .filter(CallFact::projectTarget)
                .filter(call -> facts.findTarget(call).isPresent())
                .count();

        System.out.println("Cibles projet retrouvees : " + resolvedProjectTargets);
        
        long methodsWithBody = facts.methods().stream()
                .filter(method -> method.bodyLineCount() > 0)
                .count();

        int totalMethodBodyLines = facts.methods().stream()
                .mapToInt(MethodFact::bodyLineCount)
                .sum();

        double averageMethodBodyLines =
                methodsWithBody == 0
                        ? 0.0
                        : (double) totalMethodBodyLines
                            / methodsWithBody;

        System.out.println();
        System.out.println("Methodes avec corps       : " + methodsWithBody);

        System.out.println("Lignes des corps          : " + totalMethodBodyLines);

        System.out.printf("Moyenne lignes / methode  : %.2f%n",averageMethodBodyLines);
        
        System.out.println();
        System.out.println("Fichiers sources :");

        for (SourceFileFact sourceFile : facts.sourceFiles()) {
            System.out.println(
                    "  "
                    + sourceFile.relativePath()
                    + " package=" + sourceFile.packageName()
                    + " lines=" + sourceFile.lineCount()
            );
        }
        System.out.println(
                "Lignes de l'application : "
                + facts.applicationLineCount()
        );

        System.out.println(
                "Paquetages distincts    : "
                + facts.packageCount()
        );
        
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
}
