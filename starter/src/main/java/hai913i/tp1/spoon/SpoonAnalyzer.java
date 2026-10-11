package hai913i.tp1.spoon;

import java.util.List;
import java.util.Objects;

import hai913i.tp1.model.ProjectFact;
import hai913i.tp1.model.SourceFileFact;
import hai913i.tp1.model.TypeFact;
import hai913i.tp1.parse.ProjectSources;
import spoon.reflect.CtModel;

public final class SpoonAnalyzer {

    private SpoonAnalyzer() {
    }

    public static ProjectFact analyze(
            ProjectSources sources,
            List<SourceFileFact> sourceFiles,
            boolean noClasspath) {

        Objects.requireNonNull(
                sources,
                "Les sources ne doivent pas etre nulles"
        );

        Objects.requireNonNull(
                sourceFiles,
                "Les fichiers sources ne doivent pas etre nuls"
        );

        CtModel model =
                SpoonParser.parse(
                        sources,
                        noClasspath
                );

        SpoonStructureExtractor extractor =
                new SpoonStructureExtractor();

        List<TypeFact> types =
                extractor.extractTypeFacts(model);

        return new ProjectFact(
                types,
                sourceFiles
        );
    }
}