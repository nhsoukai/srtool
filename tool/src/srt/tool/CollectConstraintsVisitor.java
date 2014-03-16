package srt.tool;

import java.util.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;

public class CollectConstraintsVisitor extends DefaultVisitor {
	
	public Set<String> variableNames = new HashSet<String>();
    public Set<String> freshVariables = new HashSet<String>();
	public List<Stmt> transitionNodes = new ArrayList<Stmt>();
	public HashSet<AssertStmt> propertyNodes = new HashSet<AssertStmt>();
	
	
	public CollectConstraintsVisitor() {
		super(false);
	}
	
	@Override
	public Object visit(DeclRef declRef) {
        if(declRef.isFresh())
            freshVariables.add(declRef.getName());

        else
        {
            String name= declRef.getName().split("$")[0];
		    variableNames.add(name);
        }
		return super.visit(declRef);
	}

	@Override
	public Object visit(AssertStmt assertStmt) {
		propertyNodes.add(assertStmt);
		return super.visit(assertStmt);
	}


	@Override
	public Object visit(AssignStmt assignment) {
		transitionNodes.add(assignment);
		return super.visit(assignment);
	}

    @Override
    public Object visit(WhileStmt whileStmt) {
        transitionNodes.add(whileStmt);
        return super.visit(whileStmt);
    }

    @Override
    public Object visit(IfStmt ifStmt) {
        transitionNodes.add(ifStmt);
        return super.visit(ifStmt);
    }
	
}




