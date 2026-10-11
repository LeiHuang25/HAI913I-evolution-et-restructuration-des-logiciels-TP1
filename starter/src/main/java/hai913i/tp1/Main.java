package hai913i.tp1;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;

import hai913i.tp1.cli.CommandLineOptions;
import hai913i.tp1.cli.OutputMode;
import hai913i.tp1.extract.StructureVisitor;
import hai913i.tp1.graph.CallgraphBuilder;
import hai913i.tp1.metrics.MetricsCalculator;
import hai913i.tp1.model.ProjectFact;
import hai913i.tp1.model.SourceFileFact;
import hai913i.tp1.output.ConsoleReporter;
import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;
/**
 * Point d'entrée en ligne de commande de l'analyseur statique.
 *
 * L'outil extrait la structure du projet, les appels de méthodes,
 * les métriques et le graphe d'appel.
 *
 * Usage : 
 * java -jar target/hai913i-tp1-analyzer.jar
 * DOSSIER_DU_PROJET MODE X
 */
public final class Main {

    private Main() {
    }
    //B4.3
    public static void main(String[] args) {
    	int exitCode;
    	
    	try {
    		exitCode = run(args);
    	} catch (Exception e) {
    		String message = e.getMessage();
    		
    		if (message == null || message.isBlank()) {
    			message = e.getClass().getSimpleName();
    		}
    		System.err.println("Erreur inattendue : " + message);
    		exitCode = 10;
    	}
    	if (exitCode != 0) {
    		System.exit(exitCode);
    	}
    }
    static int run(String[] args) throws Exception {	
    	//B4
    	CommandLineOptions options;
    	try {
    		options = CommandLineOptions.parse(args);
    	} catch (IllegalArgumentException exception) {
    		System.err.println("Erreur :" + exception.getMessage());
    		System.err.print(CommandLineOptions.USAGE);
    		return 2;
    	}
    	Path project = options.projectPath();
    	int threshold = options.threshold();
    	OutputMode outputMode = options.outputMode();
    	
        ProjectSources sources;
        try {
            sources = ProjectSources.of(project);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur : " + e.getMessage());
            return 3;
        }

        List<ParsedFile> files = JdtParser.parse(sources, List.of());
        if (files.isEmpty()) {
        	System.err.println("Erreur : aucun fichier Java trouve dans le projet :" + project.toAbsolutePath());
        	return 4;
        }
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
        if (outputMode.includes(OutputMode.STRUCTURE)) {
            ConsoleReporter.printStructure(facts);
        }

        if (outputMode.includes(OutputMode.CALLS)) {
        	ConsoleReporter.printCalls(facts);
        }

        if (outputMode.includes(OutputMode.METRICS)) {
            ConsoleReporter.printMetrics(
                    new MetricsCalculator(facts),
                    threshold
            );
        }

        if (outputMode.includes(OutputMode.GRAPH)) {
            ConsoleReporter.printGraph(
                    CallgraphBuilder.build(facts)
            );
        }
        
        return 0;

    }

}
