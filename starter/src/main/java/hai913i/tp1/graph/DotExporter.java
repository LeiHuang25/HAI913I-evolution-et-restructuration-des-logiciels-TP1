package hai913i.tp1.graph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class DotExporter {

    private DotExporter() {
    }

    public static void write(
            CallGraph graph,
            Path outputFile,
            String packagePrefix)
            throws IOException {

        Objects.requireNonNull(
                graph,
                "Le graphe ne doit pas etre null"
        );

        Objects.requireNonNull(
                outputFile,
                "Le fichier de sortie ne doit pas etre null"
        );

        Path parent = outputFile.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(
                outputFile,
                toDot(graph, packagePrefix),
                StandardCharsets.UTF_8
        );
    }

    public static String toDot(
            CallGraph graph,
            String packagePrefix) {

        StringBuilder dot =
                new StringBuilder();

        dot.append("digraph CallGraph {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  graph [overlap=false, splines=true];\n");

        if (packagePrefix != null
                && !packagePrefix.isBlank()) {

            dot.append("  label=\"Paquetage : ")
               .append(escape(packagePrefix))
               .append("\";\n");

            dot.append("  labelloc=\"t\";\n");
            dot.append("  fontsize=18;\n");
        }

        dot.append(
                "  node [shape=box, style=\"rounded,filled\", "
                + "fillcolor=\"#EAF2F8\", "
                + "color=\"#2471A3\", "
                + "fontname=\"Arial\", fontsize=10];\n"
        );

        dot.append(
                "  edge [color=\"#566573\", "
                + "fontname=\"Arial\", fontsize=9];\n"
        );

        dot.append("\n");

        Set<String> includedNodes =
                new LinkedHashSet<>();

        for (String methodId : graph.nodeIds()) {
            if (belongsToPackage(
                    methodId,
                    packagePrefix)) {

                includedNodes.add(methodId);

                dot.append("  \"")
                   .append(escape(methodId))
                   .append("\" [label=\"")
                   .append(escape(methodLabel(methodId)))
                   .append("\"];\n");
            }
        }

        dot.append("\n");

        List<CallEdge> edges = graph.edges()
                .stream()
                .filter(edge ->
                        includedNodes.contains(
                                edge.callerId()
                        )
                        && includedNodes.contains(
                                edge.targetId()
                        )
                )
                .sorted(
                    Comparator
                        .comparing(CallEdge::callerId)
                        .thenComparing(CallEdge::targetId)
                )
                .toList();

        for (CallEdge edge : edges) {
            dot.append("  \"")
               .append(escape(edge.callerId()))
               .append("\" -> \"")
               .append(escape(edge.targetId()))
               .append("\" [label=\"")
               .append(edge.weight())
               .append("\"];\n");
        }

        dot.append("}\n");

        return dot.toString();
    }
    
    private static boolean belongsToPackage(
            String methodId,
            String packagePrefix) {

        if (packagePrefix == null
                || packagePrefix.isBlank()) {
            return true;
        }

        return methodId.startsWith(
                packagePrefix + "."
        );
    }

    private static String methodLabel(
            String methodId) {

        int separator =
                methodId.indexOf('#');

        if (separator < 0) {
            return methodId;
        }

        String typeName =
                methodId.substring(0, separator);

        String methodName =
                methodId.substring(separator + 1);

        return typeName
                + "\\n"
                + methodName;
    }

    private static String escape(
            String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}