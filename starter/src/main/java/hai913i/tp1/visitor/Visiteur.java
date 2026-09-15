package hai913i.tp1.visitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public final class Visiteur extends ASTVisitor {
	private int depth = 0;
	
	@Override
    public boolean preVisit2(ASTNode node) {
        String indentation = "  ".repeat(depth);

        System.out.println( indentation + node.getClass().getSimpleName() );

        depth++;

        return true;
    }

    @Override
    public void postVisit(ASTNode node) {
        depth--;
    }
}
