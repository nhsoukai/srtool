package srt.ast;

import java.util.List;

public class Program extends Node {
	
  private String functionName;
  
	public Program(String functionName, DeclList declList, BlockStmt block) {
		this(functionName, declList, block,null);
	}
	
	public Program(String functionName, DeclList declList, BlockStmt block, NodeInfo nodeInfo) {
		super(nodeInfo);
		this.functionName = functionName;
		children.add(declList);
		children.add(block);
	}

    public void setInvariants(List<Integer> invariants){
        BlockStmt bl= getBlockStmt();
        bl.setInvariants(invariants);
        children.set(1,bl);
    }
    public void setInvariants1(List<Integer> invariants){
        BlockStmt bl= getBlockStmt();
        bl.setInvariants1(invariants);
        children.set(1,bl);
    }
	public DeclList getDeclList() {
		return (DeclList) children.get(0);
	}
	
	public BlockStmt getBlockStmt() {
		return (BlockStmt) children.get(1);
	}

  public String getFunctionName() {
    return functionName;
  }
	
}
