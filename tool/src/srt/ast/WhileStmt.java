package srt.ast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhileStmt extends Stmt {

    public WhileStmt(Expr condition, IntLiteral bound, InvariantList invariants, Stmt body) {
        this(condition, bound, invariants, body, null);
    }

    public WhileStmt(Expr condition, IntLiteral bound, InvariantList invariants, Stmt body, NodeInfo nodeInfo) {
        super(nodeInfo);
        children.add(condition);
        children.add(bound);
        children.add(invariants);
        children.add(body);
        children.add(invariants);
    }

    public Expr getCondition() {
        return (Expr) children.get(0);
    }


    public void setInvariants(List<Integer> invs){
        List<Invariant> invars = new ArrayList<Invariant>();
        List<Invariant> oldinvars= ((InvariantList) children.get(2)).getInvariants();
        for(Invariant inv: oldinvars){
            if(invs.contains(oldinvars.indexOf(inv))){
                invars.add(inv);
            }
        }
        children.set(2,new InvariantList(invars)); //original
        children.set(4,new InvariantList(invars)); //updated

    }
    public void setInvariants1(List<Integer> invs){
        List<Invariant> invars = new ArrayList<Invariant>();
        List<Invariant> oldinvars= ((InvariantList) children.get(2)).getInvariants();
        List<Integer> invsinner= new ArrayList<Integer>(); //invariants indexes for innerloop

        for(Invariant inv: oldinvars){

            if(invs.contains(oldinvars.indexOf(inv))){
                    invars.add(inv);

            }
        }

        for(Integer i:invs){
            if(i>=invars.size())
                invsinner.add(i-invars.size());

        }

        children.set(4,new InvariantList(invars));// the updates invariants

        ArrayList<Node> chcopy= new ArrayList<Node>();

        for(Node stm:getBody().getChildrenCopy()){
            if(stm instanceof StmtList){
                StmtList stlist= (StmtList)stm;
                List<Stmt> stts= stlist.getStatements();
                for(Stmt st: stts){
                    if (st instanceof WhileStmt){

                        ((WhileStmt)st).setInvariants1(invsinner);
                    }

                }
                stlist=new StmtList(stts);
                chcopy.add(stlist);
            }
            else
                chcopy.add(stm);
        }

        Stmt body=getBody();
        body=(Stmt)body.withNewChildren(chcopy);


        children.set(3,body);
    }


    /* sets the permanent invariant and a copy for it that will be updated with houdini

     */
    public void setOrigInvariants(List<Invariant> invs){
        List<Invariant> invars = new ArrayList<Invariant>();

        Set<String> values= new HashSet<String>();
        BinaryExpr Cond= (BinaryExpr)getCondition();

        /* relevant invariants are likely to be relative to
         the loop condition so we store the loop variables
         */
        if(Cond.getLhs() instanceof DeclRef){
        String lhsC= ((DeclRef)(Cond.getLhs())).getName();
        values.add(lhsC);
        }
        String rhsC="";
        if((Cond.getRhs()) instanceof DeclRef )
            rhsC= ((DeclRef)Cond.getRhs()).getName();
        if((Cond.getRhs()) instanceof IntLiteral )
            rhsC= ((IntLiteral)Cond.getRhs()).getValue()+"";
        if(!rhsC.equals(""))
            values.add(rhsC);


        /* relevant invariants would be relative to the variables
         assigned inside the loop so we store them
         */
        for(Node stm:getBody().getChildrenCopy()){
            if(stm instanceof StmtList){
                StmtList stlist= (StmtList)stm;
                List<Stmt> stts= stlist.getStatements();
                for(Stmt st: stts){
                    if (st instanceof WhileStmt){
                        continue;
                    }
                    if( st instanceof AssignStmt){
                        AssignStmt ass= (AssignStmt)st;
                        if(ass.getRhs() instanceof BinaryExpr){
                            values.add(ass.getLhs().getName());
                        }
                    }

                }

            }

        }

        // look for relevant invariants
        for(Invariant inv: invs){

            String lhs="";
            if((((BinaryExpr)inv.getExpr()).getLhs()) instanceof BinaryExpr )
                lhs= ((DeclRef)((BinaryExpr)(((BinaryExpr)inv.getExpr()).getLhs())).getLhs()).getName();
            if((((BinaryExpr)inv.getExpr()).getLhs()) instanceof DeclRef )
                lhs= ((DeclRef)(((BinaryExpr)inv.getExpr()).getLhs())).getName();
            if((((BinaryExpr)inv.getExpr()).getLhs()) instanceof IntLiteral )
                lhs= ((IntLiteral)(((BinaryExpr)inv.getExpr()).getLhs())).getValue()+"";


                String rhs="";
            if((((BinaryExpr)inv.getExpr()).getRhs()) instanceof BinaryExpr )
                lhs= ((DeclRef)((BinaryExpr)(((BinaryExpr)inv.getExpr()).getRhs())).getLhs()).getName();
            if((((BinaryExpr)inv.getExpr()).getRhs()) instanceof DeclRef )
                rhs= ((DeclRef)(((BinaryExpr)inv.getExpr()).getRhs())).getName();
            if((((BinaryExpr)inv.getExpr()).getRhs()) instanceof IntLiteral )
                rhs= ((IntLiteral)(((BinaryExpr)inv.getExpr()).getRhs())).getValue()+"";

            // see if invariants are relative to values we stored
            if(values.contains(lhs) && values.contains(rhs)){

                invars.add(inv);

            }

        }

        //we remove the outer loop invariants
        invs.removeAll(invars);

        //add existing invariants if they exist
        if(!getInvariantList().getInvariants().isEmpty())
            invars.addAll(getInvariantList().getInvariants());

        children.set(4,new InvariantList(invars));  //copy
        children.set(2,new InvariantList(invars));  //original


        ArrayList<Node> chcopy= new ArrayList<Node>();

        //copy invariants to the inner loop
        for(Node stm:getBody().getChildrenCopy()){
            if(stm instanceof StmtList){
                StmtList stlist= (StmtList)stm;
                List<Stmt> stts= stlist.getStatements();
                for(Stmt st: stts){
                    if (st instanceof WhileStmt ){

                        ((WhileStmt)st).setOrigInvariants(invs);
                    }

                }
                stlist=new StmtList(stts);
                chcopy.add(stlist);
            }
            else
                chcopy.add(stm);
        }

        Stmt body=getBody();
        body=(Stmt)body.withNewChildren(chcopy);


        children.set(3,body);



    }
    /**
     * Get the unwind bound for this loop.
     * This may return null if no unwind bound was given.
     *
     * @return The unwind bound IntLiteral or null
     * if no unwind bound was given.
     */
    public IntLiteral getBound() {
        return (IntLiteral) children.get(1);
    }

    public InvariantList getInvariantList() {
        return (InvariantList) children.get(4);
    }




    public Stmt getBody() {
        return (Stmt) children.get(3);
    }
}