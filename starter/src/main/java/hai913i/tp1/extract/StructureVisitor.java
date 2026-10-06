package hai913i.tp1.extract;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import hai913i.tp1.model.FieldFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.TypeFact;
import hai913i.tp1.model.TypeKind;

public final class StructureVisitor extends ASTVisitor {
	private final List<TypeFact> types = new ArrayList<>();
	
	// Extrait les informations structurelles des types Java.
	@Override
	public boolean visit(TypeDeclaration node) {
		if (isLocalType(node)) {
            return false;
        }
		
		TypeKind kind = node.isInterface()
				? TypeKind.INTERFACE
				: TypeKind.CLASS;
		
		types.add(createTypeFact(node,kind));
		
		return true;
	}
	
	@Override
    public boolean visit(EnumDeclaration node) {
        if (isLocalType(node)) {
            return false;
        }

        types.add(createTypeFact(node, TypeKind.ENUM));

        return true;
    }

    @Override
    public boolean visit(RecordDeclaration node) {
        if (isLocalType(node)) {
            return false;
        }

        types.add(createTypeFact(node, TypeKind.RECORD));

        return true;
    }
    
    private boolean isLocalType(AbstractTypeDeclaration node) {
    	return node.getParent() instanceof TypeDeclarationStatement;
    }
    
    private String findPackageName(ASTNode node) {
    	CompilationUnit unit = (CompilationUnit) node.getRoot();
    	PackageDeclaration packageDeclaration = unit.getPackage();
    	if (packageDeclaration == null) {
            return "(défaut)";
        }
    	
    	return packageDeclaration.getName().getFullyQualifiedName();
    }
    
    private String buildFallbackQualifiedName(AbstractTypeDeclaration node,String packageName) {
    	Deque<String> names = new ArrayDeque<>();
        names.addFirst(node.getName().getIdentifier());

        ASTNode parent = node.getParent();
        while (parent != null) {
        	if (parent instanceof AbstractTypeDeclaration parentType) {
                names.addFirst(parentType.getName().getIdentifier());
            }
        	parent = parent.getParent();
        }
        String typeName = String.join(".",names);
        if (packageName.equals("(défaut)")) {
            return typeName;
        }
        
        return packageName + "." + typeName;
    }
    
    private String findQualifiedName(AbstractTypeDeclaration node,String packageName) {
    	ITypeBinding binding = node.resolveBinding();

        if (binding != null
                && !binding.getQualifiedName().isBlank()) {
            return binding.getQualifiedName();
        }

        return buildFallbackQualifiedName(node, packageName);
    }
    
    private TypeFact createTypeFact (AbstractTypeDeclaration node, TypeKind kind) {
    	String packageName = findPackageName(node);
    	String qualifiedName = findQualifiedName(node,packageName);
    	
    	List<String> superclasses =extractSuperclasses(node);
        List<String> interfaces =extractInterfaces(node);
    	List<FieldFact> fields = extractFields(node);
    	List<MethodFact> methods = extractMethods(node);
    	
    	return new TypeFact(
    			qualifiedName,
    			kind,
    			packageName,
    			superclasses, //superclass
    			interfaces, //interfaces
    			fields,
    			methods //methodes
    			);
    }
    
    public List<TypeFact> getTypes() {
    	return List.copyOf(types);
    }
    
    // visibility
    private String visibilityOf(int modifiers) {
        if (Modifier.isPublic(modifiers)) {
            return "public";
        }

        if (Modifier.isProtected(modifiers)) {
            return "protected";
        }

        if (Modifier.isPrivate(modifiers)) {
            return "private";
        }

        return "package";
    }
    
    //extraction superclass
    private List<String> extractSuperclasses(AbstractTypeDeclaration declaration) {

        List<String> superclasses = new ArrayList<>();

        ITypeBinding binding = declaration.resolveBinding();

        if (binding == null) {
            return superclasses;
        }

        ITypeBinding superclass = binding.getSuperclass();

        while (superclass != null
                && !superclass.getQualifiedName().equals("java.lang.Object")) {

            superclasses.add(superclass.getQualifiedName());
            superclass = superclass.getSuperclass();
        }

        return superclasses;
    }
    
    //extraction interfaces
    private List<String> extractInterfaces(AbstractTypeDeclaration declaration) {

        List<String> interfaces = new ArrayList<>();

        ITypeBinding binding = declaration.resolveBinding();

        if (binding == null) {
            return interfaces;
        }

        for (ITypeBinding interfaceBinding : binding.getInterfaces()) {
            interfaces.add(interfaceBinding.getQualifiedName());
        }

        return interfaces;
    }
    
    //extraction fields
    private List<FieldFact> extractFields(AbstractTypeDeclaration declaration) {
    	List<FieldFact> fields = new ArrayList<>();
    	
    	for (Object element : declaration.bodyDeclarations()) {
            BodyDeclaration bodyDeclaration = (BodyDeclaration) element;

            if (bodyDeclaration instanceof FieldDeclaration fieldDeclaration) {
                String baseType = fieldDeclaration.getType().toString();
                String visibility = visibilityOf(fieldDeclaration.getModifiers());

                for (Object fragmentObject : fieldDeclaration.fragments()) {
                    VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragmentObject;

                    String declaredType = baseType + "[]".repeat(fragment.extraDimensions().size());

                    fields.add(new FieldFact(
                            fragment.getName().getIdentifier(),
                            declaredType,
                            visibility
                    ));
                }
            }
    	}
    	return fields;
    }
    
    //extraction methods
    private List<MethodFact> extractMethods(AbstractTypeDeclaration declaration) {

        List<MethodFact> methods = new ArrayList<>();

        for (Object element : declaration.bodyDeclarations()) {
            BodyDeclaration bodyDeclaration =
                    (BodyDeclaration) element;

            if (bodyDeclaration instanceof MethodDeclaration methodDeclaration) {
                String name = methodDeclaration.getName().getIdentifier();

                int parameterCount = methodDeclaration.parameters().size();

                boolean constructor = methodDeclaration.isConstructor();

                methods.add(new MethodFact(
                        name,
                        parameterCount,
                        constructor
                ));
            }
        }

        return methods;
    }
}
