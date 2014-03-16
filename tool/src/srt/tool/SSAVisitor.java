package srt.tool;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.ast.Node;

import java.util.*;

public class SSAVisitor extends DefaultVisitor {
    public Set<String> variableNames = new HashSet<String>();
    public Map<String,Integer> finalVariables = new HashMap<String,Integer>();
    String lastDecl="";




    public SSAVisitor() {
		super(true);
	}

	@Override
	public Object visit(Decl decl) {
		return visitChildren(decl);
	}

    @Override
    public Object visit(AssignStmt assign) {


        DeclRef lhs= assign.getLhs();
        lastDecl=lhs.getName().split("\\$")[0];
        lhs.setIsLhs();
        AssignStmt assign1= new AssignStmt(lhs,assign.getRhs());

        return visitChildren(assign1);
    }

	@Override
	public Object visit(DeclRef declRef) {


        String name= declRef.getName().split("\\$")[0];
        variableNames.add(declRef.getName());


        //update the last SSA index if new then 1 else increase the last index by 1
        if (finalVariables.containsKey(name) ){

                //Dont update if you visit an assertion or a right hand side
                if( (declRef.isLhs() && !declRef.isRhs() && !declRef.isAssert()) || declRef.getName().contains("$h") ){
                    finalVariables.put(name,finalVariables.get(name)+1);

                 }

        }

        else {

              finalVariables.put(name,1);
        }


       //convert to String
       // predicates and global variables are managed from predication visitor dont change their name
       if(declRef.getName().contains("P") || declRef.getName().contains("G") || declRef.getName().contains("Q"))
       {
            name= declRef.getName();
       }

       //havoc variables
       else if(declRef.getName().contains("$h")||declRef.getName().contains("$n"))
       {
            name= declRef.getName().split("\\$")[0]+"$"+(finalVariables.get(name))+" ";
        }

       //Dont update if you visit an assertion
       else if (declRef.isAssert() )
       {

           name= name+"$"+(finalVariables.get(name))+" ";
       }

       //Remember the last variable you updated
       else if(declRef.isLhs())
       {

             lastDecl=declRef.getName().split("\\$")[0];

             name= name+"$"+(finalVariables.get(name))+" ";

       }

       //Variable first seen
       else if (finalVariables.get(declRef.getName())<1){

              name= name+"$"+finalVariables.get(name)+" ";
       }

       else
       {
            //Case when the variable is just updated from left hand side
            // and the same variable appears in right hand side
           // e.g i=i+1
            if (lastDecl.equals(name))

                name= name+"$"+(finalVariables.get(name)-1)+" ";

            else

                name= name+"$"+finalVariables.get(name)+" ";

        }

        //new SSA variable
        declRef= new DeclRef(name, declRef.getNodeInfo());


        return visitChildren(declRef);
	}



    @Override
    public Object visit(TernaryExpr ter) {

        Expr cond= ter.getCondition();
        Expr trueEx= ter.getTrueExpr();
        Expr falseEx= ter.getFalseExpr();
        cond.setIsrhs();
        trueEx.setIsrhs();
        falseEx.setIsrhs();

        TernaryExpr ter1= new TernaryExpr(cond,trueEx,falseEx,ter.getNodeInfo());
        return visitChildren(ter1);
    }




    @Override
    public Object visit(IfStmt ifStmt) {
        lastDecl="";
        return visitChildren(ifStmt);
    }

    @Override
    public Object visit(IntLiteral intLiteral) {
        return visitChildren(intLiteral);
    }

    @Override
    public Object visitChildren(Node node)
    {

        List<Node> children = node.getChildrenCopy();
        boolean modifiedChildren = false;



        for(int i=0; !stopVisitingChildren && i < children.size(); i++)
        {
            Node child = children.get(i);
            if(node.isAssert())
                child.setIsAss();
            if(child != null)
            {
                Object res = visit(child);

                if(res != child) {
                    children.set(i, (Node) res);
                    modifiedChildren = true;
                }
            }

        }

        if(modifiedChildren) {
            return node.withNewChildren(children);
        }




        return node;

    }


    @Override
    public Object visit(AssertStmt assertStmt) {
        lastDecl="";
        assertStmt.setIsAss();
        return visitChildren(assertStmt);
    }



}
