package srt.tool;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: nhsoukaina
 * Date: 09/03/14
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */
public class HoudiniVisitor extends DefaultVisitor {


    private int i=0;

    public HoudiniVisitor() {
        super(true);
    }




   @Override
   public Object visit(WhileStmt whileStmt) {
       Set<String> modset= new HashSet<String>();

       Expr invariant= new IntLiteral(1);
       Invariant finalInvariant= new Invariant(false,invariant);
       AssertStmt assertInv= new AssertStmt(finalInvariant.getExpr());

       List<Stmt> stmts= new ArrayList<Stmt>();
       InvariantList invariantList=whileStmt.getInvariantList();

       if(invariantList == null){
           invariantList = whileStmt.getInvariantList();
           System.out.println("INV IS NULL ");
           if(!whileStmt.getInvariantList().getInvariants().isEmpty()){
            System.out.println("SIZE : " + whileStmt.getInvariantList().getInvariants().size());
           }
       }

       if(invariantList!=null) {

           List<Invariant> invariants= invariantList.getInvariants();



           for(Invariant inv: invariants){


               invariant= new BinaryExpr(BinaryExpr.BAND, invariant, inv.getExpr() );
               inv.setName("inv"+i++);
               stmts.add((Stmt)visit(inv));

           }
           finalInvariant= new Invariant(false,invariant);
           assertInv= new AssertStmt(finalInvariant.getExpr());
           stmts.add((Stmt)visit(assertInv));




       }
       /*if(invGenerated){
           if(invariantList==null) {
               stmts=new ArrayList<Stmt>();
               //  stmts.add(new HavocStmt(new DeclRef("i")));
           }
       } else{*/
           if(whileStmt.getInvariantList()==null||whileStmt.getInvariantList().getChildrenCopy().isEmpty()) {
               stmts=new ArrayList<Stmt>();
               //  stmts.add(new HavocStmt(new DeclRef("i")));
           }
       //}



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
       /*if(invGenerated){
           if(invariantList!=null) {

               stmts.add((Stmt)visit(assumeInv));
           }
       } else{ */
           if(whileStmt.getInvariantList()!=null && !whileStmt.getInvariantList().getChildrenCopy().isEmpty()) {

               stmts.add((Stmt)visit(assumeInv));
           }
      // }



       AssumeStmt assumeFalse= new AssumeStmt(new IntLiteral(0));
       BlockStmt blockAssert = new BlockStmt(new Stmt[] { (Stmt)visit(whileStmt.getBody()),(Stmt)visit(assertInv), (Stmt)visit(assumeFalse)});
       BlockStmt blockAssert1 = new BlockStmt(new Stmt[] { (Stmt)visit(whileStmt.getBody()), (Stmt)visit(assumeFalse)});

       IfStmt IfAssume= new IfStmt(whileStmt.getCondition(),blockAssert, new EmptyStmt());
       if(whileStmt.getInvariantList()==null||whileStmt.getInvariantList().getChildrenCopy().isEmpty()) {
           IfAssume =new IfStmt(whileStmt.getCondition(),blockAssert1,new EmptyStmt());

       }
       stmts.add((Stmt)visit(IfAssume));

       BlockStmt block = new BlockStmt(stmts);
       return super.visit(block);



   }

    @Override
    public Object visit(Invariant invariant) {

        return new AssignStmt(new DeclRef(invariant.getName()),(invariant.getExpr()));


    }
}
