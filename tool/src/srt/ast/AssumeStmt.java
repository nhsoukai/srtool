package srt.ast;

public class AssumeStmt extends Stmt {

    private DeclRef predicate;
	
	public AssumeStmt(Expr condition) {
		this(condition, null);
	}
	
	public AssumeStmt(Expr condition, NodeInfo nodeInfo) {
		super(nodeInfo);
        condition.setIsAssume();
		children.add(condition);
	}
	
	public Expr getCondition() {
        Expr condition= (Expr) children.get(0);
        condition.setIsAssume();
		return condition;
	}


}
