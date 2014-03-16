package srt.tool;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;

import java.util.ArrayList;
import java.util.List;

public class LoopUnwinderVisitor extends DefaultVisitor {

    private boolean unsound;
    private int defaultUnwindBound;

    public LoopUnwinderVisitor(boolean unsound,
                               int defaultUnwindBound) {
        super(true);
        this.unsound = unsound;
        this.defaultUnwindBound = defaultUnwindBound;
    }

    @Override
    public Object visit(WhileStmt whileStmt) {

        int bound = defaultUnwindBound;
        if(whileStmt.getBound()!=null)
            bound=whileStmt.getBound().getValue();




        int count;
        Stmt body= whileStmt.getBody();
        AssumeStmt assumeFalse= new AssumeStmt(new IntLiteral(0));
        IfStmt IfAssume;
        IfAssume= new IfStmt(whileStmt.getCondition(),assumeFalse, new EmptyStmt());

        // ADD INVARIANT CHECKING IF INV NOT FOR CAND
        Expr invariant= new IntLiteral(1);
        Invariant finalInvariant= new Invariant(false,invariant);
        AssertStmt assertInv= new AssertStmt(finalInvariant.getExpr());

        if(whileStmt.getInvariantList()!=null) {
            List<Invariant> invariants= whileStmt.getInvariantList().getInvariants();



            for(Invariant inv: invariants){
                // Consider only invariant that are not candidate
                if(!inv.isCandidate()){
                    invariant= new BinaryExpr(BinaryExpr.BAND, invariant, inv.getExpr() );
                }


            }
            finalInvariant= new Invariant(false,invariant);
            assertInv= new AssertStmt(finalInvariant.getExpr());

        }

        BlockStmt blockAssert;
        if(!unsound){
            AssertStmt assertFalse= new AssertStmt(new IntLiteral(0));
            blockAssert = new BlockStmt(new Stmt[] { assertFalse, assumeFalse});
            IfAssume= new IfStmt(whileStmt.getCondition(),blockAssert, new EmptyStmt());
        }


        BlockStmt block = new BlockStmt(new Stmt[] { body, IfAssume});



        IfStmt ifStmt= new IfStmt(whileStmt.getCondition(),block, new EmptyStmt());

        List<Stmt> stm1  = new ArrayList<Stmt>();
        stm1.add(assertInv);

        // if bound is 0 then still checks for invariants once
        if(bound==0){

            return visit(new BlockStmt(new Stmt[]{assertInv, new EmptyStmt()}));
        }
        count=1;
        // return ifStmt;
        while(count<bound){
            body= whileStmt.getBody();
            BlockStmt blocki = new BlockStmt(new Stmt[] {body, assertInv, ifStmt});
            ifStmt= new IfStmt(whileStmt.getCondition(),blocki, new EmptyStmt());

            count++;

        }
        stm1.add(ifStmt);
        BlockStmt finBlock = new BlockStmt(stm1);
        return visit(finBlock);

    }

    @Override
    public Object visit(AssumeStmt ass) {

        return super.visit(ass);
    }
}
