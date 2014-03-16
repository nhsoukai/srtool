package srt.ast;

public class AssertStmt extends Stmt {



    public AssertStmt(Expr condition) {
		this(condition, null);
	}
	
	public AssertStmt(Expr condition, NodeInfo nodeInfo) {
		super(nodeInfo);
        condition.setIsAss();
		children.add(condition);
	}
	
	public Expr getCondition() {
        Expr cond= (Expr) children.get(0);
        cond.setIsAss();
		return cond;
	}


}
