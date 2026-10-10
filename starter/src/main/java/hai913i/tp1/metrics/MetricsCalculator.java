package hai913i.tp1.metrics;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToIntFunction;

import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFact;
import hai913i.tp1.model.TypeFact;

public final class MetricsCalculator {
	private final ProjectFact facts;
	
	public MetricsCalculator(ProjectFact facts) {
		this.facts = Objects.requireNonNull(facts);
	}
	
	//Q1 nbclasses
	public int classCount() {
		return facts.types().size();
	}
	
	 //Q2 nblignes
    public int applicationLineCount() {
        return facts.applicationLineCount();
    }

    //Q3 nbmethodes
    public int methodCount() {
        return facts.methods().size();
    }

    //Q4 nbpaquetes
    public long packageCount() {
        return facts.packageCount();
    }

    // Q5 nbMoyenmethodesClass
    public double averageMethodsPerClass() {
        int classes = classCount();

        if (classes == 0) {
            return 0.0;
        }

        return (double) methodCount() / classes;
    }

    //Q6 nbMoyenlignesMethodes
    public double averageBodyLinesPerMethod() {
        long methodsWithBody = facts.methods().stream()
                .filter(method -> method.bodyLineCount() > 0)
                .count();

        if (methodsWithBody == 0) {
            return 0.0;
        }

        int totalBodyLines = facts.methods().stream()
                .mapToInt(MethodFact::bodyLineCount)
                .sum();

        return (double) totalBodyLines
                / methodsWithBody;
    }

    //Q7 nbMoyenAttributsClass
    public double averageFieldsPerClass() {
        int classes = classCount();

        if (classes == 0) {
            return 0.0;
        }

        int fieldCount = facts.types().stream()
                .mapToInt(type -> type.fields().size())
                .sum();

        return (double) fieldCount / classes;
    }

    //validation Q6Q7
    public long methodsWithBodyCount() {
        return facts.methods().stream()
                .filter(method -> method.bodyLineCount() > 0)
                .count();
    }

    public int totalMethodBodyLines() {
        return facts.methods().stream()
                .mapToInt(MethodFact::bodyLineCount)
                .sum();
    }

    public int fieldCount() {
        return facts.types().stream()
                .mapToInt(type -> type.fields().size())
                .sum();
    }
    
    
	private List<TypeFact> topTenPercent(ToIntFunction<TypeFact> valueFunction){
		List<TypeFact> candidates = facts.types().stream()
				.sorted(
						Comparator
							.<TypeFact>comparingInt(valueFunction)
							.reversed()
							.thenComparing(TypeFact::qualifiedName)
				)
				.toList();
		if (candidates.isEmpty()) {
			return List.of();
		}
		
		int k = (candidates.size() + 9) / 10;
		int threshold = valueFunction.applyAsInt(
				candidates.get(k-1)
				);
		if (threshold == 0) {return List.of();}
		
		return candidates.stream()
				.filter(type -> valueFunction.applyAsInt(type) >= threshold)
				.filter(type -> valueFunction.applyAsInt(type) > 0)
				.toList();
	}
    
    //Q8 10%classPlusdeMethodes
    public List<TypeFact> topTenPercentByMethods() {
        return topTenPercent(
                type -> type.methods().size()
        );
    }
    
    //Q9 10%classPlusdeAttributs
    public List<TypeFact> topTenPercentByFields() {
        return topTenPercent(
                type -> type.fields().size()
        );
    }
    
    //Q10 Q8etQ9
    public List<TypeFact> topTenPercentIntersection() {
        List<TypeFact> byMethods =
                topTenPercentByMethods();

        Set<String> byFieldsNames =
                new HashSet<>();

        for (TypeFact type : topTenPercentByFields()) {
            byFieldsNames.add(type.qualifiedName());
        }

        return byMethods.stream()
                .filter(type ->
                        byFieldsNames.contains(
                                type.qualifiedName()
                        )
                )
                .toList();
    }
    
    //Q11 plusdeXMethodes
	public List<TypeFact> classesWithMoreThanMethods(int threshold){
		if(threshold <0) {
			throw new IllegalArgumentException("X doit etre positif ou nul");
		}
		return facts.types().stream()
				.filter(type -> type.methods().size() > threshold)
				.sorted(Comparator
						.<TypeFact>comparingInt(type -> type.methods().size())
						.reversed()
						.thenComparing(TypeFact::qualifiedName))
				.toList();
				
	}
	
	//Q12 10%MethodesPlusdeLignes
	//un type
	private List<MethodFact> topTenPercentMethodsByLines(TypeFact type) {
	    List<MethodFact> candidates = type.methods().stream()
	            .sorted(Comparator
	                            .comparingInt(MethodFact::bodyLineCount)
	                            .reversed()
	                            .thenComparing(MethodFact::id))
	            .toList();

	    if (candidates.isEmpty()) {
	        return List.of();
	    }

	    int k = (candidates.size() + 9) / 10;

	    int threshold = candidates
	            .get(k - 1)
	            .bodyLineCount();

	    if (threshold == 0) {return List.of();}

	    return candidates.stream()
	            .filter(method -> method.bodyLineCount() >= threshold)
	            .filter(method -> method.bodyLineCount() > 0)
	            .toList();
	}
	
	// tous les types
	public Map<TypeFact, List<MethodFact>> topTenPercentMethodsByLinesForEachClass() {
		Map<TypeFact, List<MethodFact>> result = new LinkedHashMap<>();
		for (TypeFact type : facts.types()) {
		    result.put(type,topTenPercentMethodsByLines(type));
		}
		return Collections.unmodifiableMap(result);
	}
	
	//Q13 nbMaxParam
	public int maximumParameterCount() {
	    return facts.methods().stream()
	            .mapToInt(MethodFact::nbParametre)
	            .max()
	            .orElse(0);
	}
	
	public List<MethodFact> methodsWithMaximumParameters() {
	    if (facts.methods().isEmpty()) {return List.of();}

	    int maximum = maximumParameterCount();

	    return facts.methods().stream()
	            .filter(method -> method.nbParametre() == maximum)
	            .sorted(Comparator.comparing(MethodFact::id))
	            .toList();
	}
	
	
	
	
	
	
	
	
}
