package hai913i.tp1.cli;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public record CommandLineOptions(
		Path projectPath,
		OutputMode outputMode,
		int threshold) {
	public static final String USAGE = 
			"Usage : java -jar "
            + "target/hai913i-tp1-analyzer.jar "
            + "DOSSIER_DU_PROJET "
            + "MODE "
            + "X%n"
            + "  MODE : structure | calls | metrics | graph | all%n"
            + "  X    : entier positif ou nul%n"
            + "Exemple : java -jar "
            + "target/hai913i-tp1-analyzer.jar "
            + "\"C:\\\\projets\\\\validation\" all 5";
	
	public static CommandLineOptions parse(String[] args) {
		if (args == null || args.length != 3) {
			throw new IllegalArgumentException("3 Arguments sont obligatoires.");
		}
		
		Path projectPath;
		
		try {
			projectPath = Path.of(args[0]);
		} catch (InvalidPathException exception) {
			throw new IllegalArgumentException("Chemin de projet invalid :" + args[0]);
		}
		
		OutputMode outputMode = OutputMode.parse(args[1]);
		
		int threshold;
		
		try {
			threshold = Integer.parseInt(args[2]);
		}catch(NumberFormatException exception) {
			throw new IllegalArgumentException("Le seuil X doit etre positif ou nul :" + args[2]);
		}
		
		if (threshold < 0 ) {
			throw new IllegalArgumentException("Le seuil X doit etre positif ou nul :" + threshold);
		}
		
		return new CommandLineOptions(
				projectPath,
				outputMode,
				threshold);
	}
}
