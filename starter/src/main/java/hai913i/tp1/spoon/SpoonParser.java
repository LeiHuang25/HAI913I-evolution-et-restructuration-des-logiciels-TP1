package hai913i.tp1.spoon;

import java.nio.file.Path;
import java.util.Objects;

import hai913i.tp1.parse.ProjectSources;
import spoon.Launcher;
import spoon.OutputType;
import spoon.reflect.CtModel;

public final class SpoonParser {

    private SpoonParser() {
    }

    public static CtModel parse(
            ProjectSources sources,
            boolean noClasspath) {

        Objects.requireNonNull(
                sources,
                "Les sources ne doivent pas etre nulles"
        );

        Launcher launcher = new Launcher();

        launcher.addInputResource(
                sources.sourceRoot().toString()
        );

        launcher.getEnvironment()
                .setNoClasspath(noClasspath);

        launcher.getEnvironment()
                .setComplianceLevel(17);

        launcher.getEnvironment()
                .setCommentEnabled(false);

        launcher.getEnvironment()
                .setShouldCompile(false);

        Path outputDirectory = Path.of(
                System.getProperty("java.io.tmpdir"),
                "hai913i-spooned"
        );

        launcher.setSourceOutputDirectory(
                outputDirectory.toString()
        );

        launcher.getEnvironment()
                .setOutputType(OutputType.NO_OUTPUT);

        return launcher.buildModel();
    }
}