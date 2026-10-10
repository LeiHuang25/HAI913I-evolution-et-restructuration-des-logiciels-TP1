package hai913i.tp1.graph;

public record CallEdge(
		String callerId,
		String targetId,
		int weight) {
	public CallEdge {
		if (weight <= 0) {
			throw new IllegalArgumentException("Le poids doit etre positif.");
		}
	}
}
