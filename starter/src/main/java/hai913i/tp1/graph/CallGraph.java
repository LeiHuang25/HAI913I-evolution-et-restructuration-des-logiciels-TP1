package hai913i.tp1.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import hai913i.tp1.model.MethodFact;

public final class CallGraph {
	private final Map<String, MethodFact> nodesById;
	private final List<CallEdge> edges;
	
	private final Map<String, List<CallEdge>> outgoingEdges;
	private final Map<String, List<CallEdge>> incomingEdges;
	
	private final int internalCallSites;
	private final int externalCallSites;
	private final int unresolvedCallSites;
	
	//B3.2 recherche binaire
	private static Map<String, List<CallEdge>> createEdgeIndex(
			Map<String, MethodFact> nodes,
			List<CallEdge> edges,
			Function<CallEdge, String> keyFunction){
		Map<String, List<CallEdge>> mutableIndex = new LinkedHashMap<>();
		
		for (String methodId : nodes.keySet()) {
			mutableIndex.put(methodId, new ArrayList<>());
		}
		
		for (CallEdge edge : edges) {
			String key = keyFunction.apply(edge);
			
			List<CallEdge> methodEdges = mutableIndex.get(key);
			
			if (methodEdges == null) {
				throw new IllegalStateException("Arc associe a un node absent :" + key); 
			}
			
			methodEdges.add(edge);
		}
		
		Map<String, List<CallEdge>> immutableIndex = new LinkedHashMap<>();
		
		for (Map.Entry<String, List<CallEdge>> entry : mutableIndex.entrySet()) {
			immutableIndex.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
		
		return Collections.unmodifiableMap(immutableIndex);
	}
	
	public List<CallEdge> calleesOf(String methodId){
		requireKnownMethod(methodId);
		return outgoingEdges.get(methodId);
	}
	public List<CallEdge> callersOf(String methodId) {
	    requireKnownMethod(methodId);
	    return incomingEdges.get(methodId);
	}
	private void requireKnownMethod(String methodId) {
	    if (!nodesById.containsKey(methodId)) {
	        throw new IllegalArgumentException("Methode inconnue dans le graphe : " + methodId);
	    }
	}
	
	CallGraph(
			Map<String, MethodFact> nodesById,
			List<CallEdge> edges,
			int internalCallSites,
			int externalCallSites,
			int unresolvedCallSites){
		this.nodesById =Collections.unmodifiableMap(new LinkedHashMap<>(nodesById));
		this.edges = List.copyOf(edges);
		this.outgoingEdges = createEdgeIndex(
		        this.nodesById,
		        this.edges,
		        CallEdge::callerId
		);

		this.incomingEdges = createEdgeIndex(
		        this.nodesById,
		        this.edges,
		        CallEdge::targetId
		);
		this.internalCallSites = internalCallSites;
		this.externalCallSites = externalCallSites;
		this.unresolvedCallSites = unresolvedCallSites;
	}
	public int nodeCount() {
        return nodesById.size();
    }

    public int edgeCount() {
        return edges.size();
    }

    public int internalCallSites() {
        return internalCallSites;
    }

    public int externalCallSites() {
        return externalCallSites;
    }

    public int unresolvedCallSites() {
        return unresolvedCallSites;
    }

    public List<CallEdge> edges() {
        return edges;
    }

    public Optional<MethodFact> findNode(String methodId) {
        return Optional.ofNullable(
                nodesById.get(methodId)
        );
    }
}
