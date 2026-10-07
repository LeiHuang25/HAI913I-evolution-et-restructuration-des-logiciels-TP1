package hai913i.tp1.model;

import java.util.List;

public record MethodFact(String name,int nbParametre,boolean constructor,List<CallFact> calls) {
	public MethodFact {
		calls = List.copyOf(calls);
	}
}
