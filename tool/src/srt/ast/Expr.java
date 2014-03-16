package srt.ast;

public abstract class Expr extends Node {
    boolean rhs=false;
    boolean lhs= false;
    boolean ass= false;
    boolean assu=false;
	public Expr(NodeInfo nodeInfo) {
		super(nodeInfo);
	}


    public void setIsrhs(){
        rhs=true;
        lhs= false;
    }

    public void setIsLhs(){
        rhs=false;
        lhs= true;
    }

    public boolean isAssert(){
        return ass;
    }

    public boolean isAssume(){
        return assu;
    }

    public boolean isRhs(){
        return rhs;
    }

    public boolean isLhs(){
        return lhs;
    }
    public void setIsAss(){
        ass=true;
    }

    public void setIsAssume(){
        assu=true;
    }



}
