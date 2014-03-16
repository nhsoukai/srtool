package srt.ast;

public class Invariant extends Node {

	private boolean candidate;
    String name="";
	
	public Invariant(boolean candidate, Expr expr)
	{
		this(candidate, expr, null);
	}
	
	public Invariant(boolean candidate, Expr expr, NodeInfo nodeInfo) {
		super(nodeInfo);
		this.candidate = candidate;
		children.add(expr);
	}
	
	public boolean isCandidate()
	{
		return candidate;
	}
	
	public Expr getExpr()
	{
		return (Expr) children.get(0); 
	}

    public void setName(String name){
        this.name=name;

    }

    public String getName(){
        return this.name;
    }
	

}
