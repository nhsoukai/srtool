package srt.ast;

public class TernaryExpr extends Expr {
	public TernaryExpr(Expr condition, Expr trueExpr, Expr falseExpr) {
		this(condition, trueExpr, falseExpr, null);
	}
	
	public TernaryExpr(Expr condition, Expr trueExpr, Expr falseExpr, NodeInfo nodeInfo) {
		super(nodeInfo);

        condition.setIsrhs();
		children.add(condition);
        trueExpr.setIsrhs();
		children.add(trueExpr);
        falseExpr.setIsrhs();
		children.add(falseExpr);
	}
	
	public Expr getCondition() {
		return (Expr) children.get(0);
	}
	
	public Expr getTrueExpr() {
		return (Expr) children.get(1);
	}
	
	public Expr getFalseExpr() {
		return (Expr) children.get(2);
	}
}
