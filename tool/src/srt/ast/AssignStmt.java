package srt.ast;


public class AssignStmt extends Stmt {
	
	public AssignStmt(DeclRef lhs, Expr rhs) {
		this(lhs, rhs, null);
	}
	
	public AssignStmt(DeclRef lhs, Expr rhs, NodeInfo nodeInfo)
	{
		super(nodeInfo);
		children.add(lhs);
		children.add(rhs);
        lhs.setIsLhs();
        rhs.setIsrhs();
	}

	public DeclRef getLhs() {
        DeclRef child= (DeclRef) children.get(0);
        child.setIsLhs();
		return child;
	}
	
	public Expr getRhs() {
        Expr child= (Expr) children.get(1);
        child.setIsrhs();
		return child;
	}
	
}
