package hai913i.tp1;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;

import hai913i.tp1.extract.StructureVisitor;
import hai913i.tp1.model.TypeFact;
import hai913i.tp1.model.TypeKind;
import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;
import hai913i.tp1.visitor.Visiteur;

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
        if (args.length < 1) {
            System.err.println("Usage : java -jar target/hai913i-tp1-analyzer.jar DOSSIER_DU_PROJET");
            System.exit(2);
        }
        Path project = Path.of(args[0]);
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
        
        //A2 structureVisitor
        StructureVisitor structureVisitor = new StructureVisitor();
        
        for (ParsedFile file : files) {
            file.unit().accept(structureVisitor);
        }

        List<TypeFact> types = structureVisitor.getTypes();
        
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
        
        System.out.println("Structure du projet :");
        System.out.println("Types       : " + types.size());
        System.out.println("Classes     : " + classCount);
        System.out.println("Interfaces  : " + interfaceCount);
        System.out.println("Enumerations: " + enumCount);
        System.out.println("Records     : " + recordCount);
        
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
        
        //A1 Parcourir un AST
        for (ParsedFile file : files) {
            String fileName = file.path().getFileName().toString();

            if (fileName.equals("Dvd.java") || fileName.equals("Category.java")) {
                System.out.println();
                System.out.println("AST de " + fileName + "\n");

                file.unit().accept(new Visiteur());
            }
        }

        // À FAIRE (A1 et suite) : parcourir les AST avec vos visiteurs, construire votre modèle de faits,
        // puis calculer les métriques et le graphe d'appel. Gardez cette classe courte : elle lit les
        // arguments et délègue.
    }
}
