package hai913i.tp1.extract;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;

import hai913i.tp1.model.TypeFact;
import hai913i.tp1.model.TypeKind;

public final class StructureVisitor extends ASTVisitor{
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
    
    private String findpackageName(ASTNode node) {
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
    	String packageName = findpackageName(node);
    	String qualifiedName = findQualifiedName(node,packageName);
    	
    	return new TypeFact(
    			qualifiedName,
    			kind,
    			packageName,
    			List.of(), //superclass
    			List.of(), //interfaces
    			List.of(), //attributs
    			List.of() //methodes
    			);
    }
    
    private List<TypeFact> getTypes() {
    	return List.copyOf(types);
    }
}
