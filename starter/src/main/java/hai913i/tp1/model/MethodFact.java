package hai913i.tp1.model;

import java.util.List;

public record MethodFact(
		String id,//B1.1
		String name,
		int nbParametre,
		boolean constructor,
		int bodyLineCount, //B2.1
		List<CallFact> calls) {
	public MethodFact {
		calls = List.copyOf(calls);
	}
}
