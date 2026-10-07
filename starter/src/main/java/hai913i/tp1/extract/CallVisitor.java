package hai913i.tp1.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import hai913i.tp1.model.CallFact;

public final class CallVisitor extends ASTVisitor{
	private static final String UNRESOLVED = "non resolu";
	
	private final CompilationUnit compilationUnit;
	private final List<CallFact> calls = new ArrayList<>();
	
	public CallVisitor(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding binding = node.resolveMethodBinding();
		
		String receiverType = findReceiverType(node, binding);
		String targetMethod = findTargetMethod(binding);
		int line = compilationUnit.getLineNumber(node.getStartPosition());
		boolean resolved = (binding != null);
		boolean projectTarget = isProjectTarget(binding);
		
		calls.add(new CallFact(
				node.getName().getIdentifier(),
				receiverType,
				targetMethod,
				line,
				resolved,
				projectTarget));
		
		return true;
	}
	
	@Override
	public boolean visit(SuperMethodInvocation node) {
	    IMethodBinding binding = node.resolveMethodBinding();

	    String receiverType = findSuperReceiverType(node);
	    String targetMethod = findTargetMethod(binding);
	    int line =
	            compilationUnit.getLineNumber(node.getStartPosition());

	    boolean resolved = binding != null;
	    boolean projectTarget = isProjectTarget(binding);

	    calls.add(new CallFact(
	            node.getName().getIdentifier(),
	            receiverType,
	            targetMethod,
	            line,
	            resolved,
	            projectTarget
	    ));

	    return true;
	}
	
	private String findSuperReceiverType(SuperMethodInvocation node) {
		ASTNode current = node.getParent();
		
		while (current != null) {
			if (current instanceof AnonymousClassDeclaration anonymousClass) {
                ITypeBinding typeBinding = anonymousClass.resolveBinding();
                
                if (typeBinding != null) {
                	return typeName(typeBinding.getSuperclass());
                }
                return UNRESOLVED;
            }

            if (current instanceof AbstractTypeDeclaration typeDeclaration) {
            	ITypeBinding typeBinding = typeDeclaration.resolveBinding();
            	
            	if (typeBinding != null) {
                	return typeName(typeBinding.getSuperclass());
                }
            	return UNRESOLVED;
            }
            current = current.getParent();
		}
		return UNRESOLVED;
	}

	private boolean isProjectTarget(IMethodBinding binding) {
		if (binding == null) {
			return false;
		}
		
		IMethodBinding declaration = binding.getMethodDeclaration();
		ITypeBinding declaringClass = declaration.getDeclaringClass();
		
		return (declaringClass != null) && (declaringClass.getTypeDeclaration().isFromSource());
	}

	private String findTargetMethod(IMethodBinding binding) {
		if (binding == null) {
			return UNRESOLVED;
		}
		IMethodBinding declaration = binding.getMethodDeclaration();
		String declaringType = typeName(declaration.getDeclaringClass());
		String parameters = Arrays.stream(declaration.getParameterTypes()).map(this::typeName).collect(Collectors.joining(","));
		
		return declaringType
				+ "#"
				+ declaration.getName()
				+ "("
				+ parameters
				+ ")";
	}

	public List<CallFact> getCalls() {
		return List.copyOf(calls);
	}
	
	private String findReceiverType(MethodInvocation node, IMethodBinding binding) {
		// receiver visible
		if (node.getExpression() != null) {
			ITypeBinding receiverBinding = node.getExpression().resolveTypeBinding();
			return typeName(receiverBinding);
		}
		
		// receiver invisible
		if (binding != null && Modifier.isStatic(binding.getModifiers())) {
			return typeName(binding.getDeclaringClass());
		}
		
		return findEnclosingType(node);
	}

	private String findEnclosingType(MethodInvocation node) {
		ASTNode current = node.getParent();
		
		while (current != null) {
			if (current instanceof AnonymousClassDeclaration anonymousClass) {
                return typeName(anonymousClass.resolveBinding());
            }

            if (current instanceof AbstractTypeDeclaration typeDeclaration) {
                return typeName(typeDeclaration.resolveBinding());
            }
            current = current.getParent();
		}
		return UNRESOLVED;
	}

	private String typeName(ITypeBinding binding) {
		if (binding == null) {
			return UNRESOLVED;
		}
		
		ITypeBinding erasedType = binding.getErasure();
		String qualifiedName = erasedType.getQualifiedName();
		
		if (qualifiedName == null || qualifiedName.isBlank()) {
			return erasedType.getName();
		}
		return qualifiedName;
	}
	
	
}
