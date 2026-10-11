package hai913i.tp1.cli;

import java.util.Locale;

public enum OutputMode {
	STRUCTURE,
	CALLS,
	METRICS,
	GRAPH,
	ALL;
	
	public static OutputMode parse(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Le mode de sortie est absent.");
			
		}
		
		return switch (value.toLowerCase(Locale.ROOT)){
			case "structure" -> STRUCTURE;
			case "calls", "appels" -> CALLS;
			case "metrics", "metriques" -> METRICS;
			case "graph", "graphe" -> GRAPH;
			case "all", "tout" -> ALL;
			
			default -> throw new IllegalArgumentException(
					"Mode de sortie inconnu :"
					+ value
					+ ". Modes acceptes :"
					+ "structure, calls, metrics, graph, all.");
		};
	}
	
	public boolean includes(OutputMode requestedMode) {
		return this == ALL || this == requestedMode;
	}
}
