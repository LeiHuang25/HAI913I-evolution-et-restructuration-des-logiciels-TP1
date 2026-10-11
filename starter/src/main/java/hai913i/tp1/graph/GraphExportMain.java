package hai913i.tp1.graph;

import java.nio.file.Path;
import java.util.List;

import hai913i.tp1.extract.StructureVisitor;
import hai913i.tp1.model.ProjectFact;
import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;

public final class GraphExportMain {

    private GraphExportMain() {
    }

    public static void main(String[] args)
            throws Exception {

    	if (args.length < 2 || args.length > 3) {
    	    System.err.println(
    	            "Usage : GraphExportMain "
    	            + "DOSSIER_DU_PROJET "
    	            + "FICHIER_DOT "
    	            + "[PREFIXE_PAQUETAGE]"
    	    );
    	    return;
    	}

        Path projectPath =
                Path.of(args[0]);

        Path outputFile =
                Path.of(args[1]);

        ProjectSources sources =
                ProjectSources.of(projectPath);

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

        ProjectFact facts =
                new ProjectFact(
                        visitor.getTypes(),
                        List.of()
                );

        CallGraph graph =
                CallgraphBuilder.build(facts);
        
        String packagePrefix =
                args.length == 3
                ? args[2]
                : null;

        DotExporter.write(
                graph,
                outputFile,
                packagePrefix
        );
        
        if (packagePrefix != null) {
            System.out.println(
                    "Filtre de paquetage : "
                    + packagePrefix
            );
        }

        System.out.println(
                "Graphe DOT genere : "
                + outputFile
                    .toAbsolutePath()
                    .normalize()
        );

        System.out.println(
                "Noeuds : "
                + graph.nodeCount()
        );

        System.out.println(
                "Arcs   : "
                + graph.edgeCount()
        );

        System.out.println(
                "Sites internes : "
                + graph.internalCallSites()
        );
    }
}