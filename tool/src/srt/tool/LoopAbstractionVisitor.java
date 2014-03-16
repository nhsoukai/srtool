package srt.tool;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoopAbstractionVisitor extends DefaultVisitor {

	public LoopAbstractionVisitor() {
		super(true);
	}

    @Override
    public Object visit(WhileStmt whileStmt) {
        Set<String> modset= new HashSet<String>();

        Expr invariant= new IntLiteral(1);
        Invariant finalInvariant= new Invariant(false,invariant);
        AssertStmt assertInv= new AssertStmt(finalInvariant.getExpr());


        List<Stmt> stmts= new ArrayList<Stmt>();


        if(whileStmt.getInvariantList()!=null) {
            List<Invariant> invariants= whileStmt.getInvariantList().getInvariants();



            for(Invariant inv: invariants){


                invariant= new BinaryExpr(BinaryExpr.BAND, invariant, inv.getExpr() );

            }
            finalInvariant= new Invariant(false,invariant);
            assertInv= new AssertStmt(finalInvariant.getExpr());
            stmts.add(assertInv);




        }


        for(Node node: whileStmt.getBody().getChildrenCopy()) {
            if(node instanceof StmtList){
                StmtList stlist= (StmtList)node;
                for(Stmt st: stlist.getStatements()){
                    if (st instanceof AssignStmt) {

                        AssignStmt ass = (AssignStmt) st;
                        DeclRef lhs = ass.getLhs();
                        if(!modset.contains(lhs.getName().split("\\$")[0])) {
                            HavocStmt havocSt= new HavocStmt(lhs);

                            stmts.add((Stmt)super.visit(havocSt));
                            modset.add(lhs.getName().split("\\$")[0]);
                        }

                    }
                }
            }


        }


        AssumeStmt assumeInv= new AssumeStmt(finalInvariant.getExpr());
        //stmts.add(assumeInv);
        if(whileStmt.getInvariantList()!=null && !whileStmt.getInvariantList().getChildrenCopy().isEmpty()) {

            stmts.add(assumeInv);
        }

        AssumeStmt assumeFalse= new AssumeStmt(new IntLiteral(0));
        BlockStmt blockAssert = new BlockStmt(new Stmt[] { whileStmt.getBody(),assertInv, assumeFalse});
        BlockStmt blockAssert1 = new BlockStmt(new Stmt[] { whileStmt.getBody(), assumeFalse});

        IfStmt IfAssume= new IfStmt(whileStmt.getCondition(),blockAssert, new EmptyStmt());
        if(whileStmt.getInvariantList()==null || whileStmt.getInvariantList().getChildrenCopy().isEmpty()) {
            IfAssume =new IfStmt(whileStmt.getCondition(),blockAssert1, new EmptyStmt());
        }


        stmts.add(IfAssume);

        //Stmt stmt= new Stmt();
        //node.withNewChildren(stmts);
        //StmtList smtlist= new StmtList(stmts, whileStmt.getNodeInfo());
        BlockStmt block = new BlockStmt(stmts);
        return super.visit(block);



    }



}