package hai913i.tp1.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hai913i.tp1.model.CallFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFact;

public final class CallgraphBuilder {
	private CallgraphBuilder() {}
	public static CallGraph build(ProjectFact facts) {
		Map<String, MethodFact> nodes = createNodes(facts);
		Map<EdgeKey, Integer> edgeWeights = new HashMap<>();
		
		int internalCallSites = 0;
		int externalCallSites = 0;
		int unresolvedCallSites = 0;
		
		for (MethodFact caller : facts.methods()) {
			for (CallFact call : caller.calls()) {
				if (!call.resolved()) {
					unresolvedCallSites++;
					continue;
				}
				if (!call.projectTarget()) {
					externalCallSites++;
					continue;
				}
				
				MethodFact target = facts.findTarget(call)
										 .orElseThrow(() ->
												 new IllegalStateException("Cible projet absente du modele :" + call.targetMethod()));
				internalCallSites++;
				
				EdgeKey key = new EdgeKey(
						caller.id(),
						target.id());
				
				edgeWeights.merge(
						key,
						1,
						Integer::sum);
			}
		}
		List<CallEdge> edges = createEdges(edgeWeights);
		
		return new CallGraph(
				nodes,
				edges,
				internalCallSites,
				externalCallSites,
				unresolvedCallSites);
	}
	
	private static Map<String, MethodFact> createNodes(ProjectFact facts){
		Map<String, MethodFact> nodes = new LinkedHashMap<>();
		
		for (MethodFact method : facts.methods()) {
			nodes.put(method.id(), method);
		}
		
		return nodes;
	}
	
	private static List<CallEdge> createEdges(Map<EdgeKey, Integer> edgeWeights) {
		List<CallEdge> edges = new ArrayList<>();
		
		 for (Map.Entry<EdgeKey, Integer> entry : edgeWeights.entrySet()) {
			 EdgeKey key = entry.getKey();
			 
			 edges.add(new CallEdge(
					 key.callerId(),
					 key.targetId(),
					 entry.getValue()));
		 }
		 
		 edges.sort(Comparator.comparing(CallEdge::callerId)
				 			  .thenComparing(CallEdge::targetId));
		 
		 return edges;
	}
	
	private record EdgeKey(
			String callerId,
			String targetId) {}
}
