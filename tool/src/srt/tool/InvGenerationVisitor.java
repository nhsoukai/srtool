package srt.tool;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: nhsoukaina
 * Date: 09/03/14
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */
public class InvGenerationVisitor extends DefaultVisitor {

    public InvariantList invariantList;
    Set<Integer> modsetInt = new HashSet<Integer>();
    Set<String> modset= new HashSet<String>();
    // {"<", "<=", ">", ">=", "="}
    HashSet<Integer> comparators= new HashSet<Integer>();
    HashMap<String,HashSet<BinaryExpr>> varValue= new HashMap<String,HashSet<BinaryExpr>>();

    public InvGenerationVisitor() {
        super(true);
    }

    @Override
    public Object visit(WhileStmt whileStmt) {



        List<Invariant> generatedInv= new ArrayList<Invariant>();

        comparators.add(BinaryExpr.EQUAL);

        // If rhs of while condition is number then add it to list of modsetInt
        if(whileStmt.getCondition() instanceof BinaryExpr){
            BinaryExpr cond= ((BinaryExpr) whileStmt.getCondition());

            DeclRef lhs= (DeclRef)cond.getLhs();
            HashSet<BinaryExpr> ExSet= new HashSet<BinaryExpr>();

             if (cond.getOperator()==BinaryExpr.GT ||cond.getOperator()==BinaryExpr.GEQ){

                generatedInv.add(new Invariant (true, new BinaryExpr(BinaryExpr.GT,  cond.getLhs(), cond.getRhs())));
                ExSet.add(new BinaryExpr(BinaryExpr.GT, cond.getLhs(), cond.getRhs()));
                generatedInv.add(new Invariant (true, new BinaryExpr(BinaryExpr.GEQ, cond.getLhs(), cond.getRhs())));
                 ExSet.add(new BinaryExpr(BinaryExpr.GEQ, cond.getLhs(), cond.getRhs()));

            }
            if (cond.getOperator()==BinaryExpr.LT ||cond.getOperator()==BinaryExpr.LEQ){

                generatedInv.add(new Invariant (true, new BinaryExpr(BinaryExpr.LT, cond.getLhs(), cond.getRhs())));
                ExSet.add(new BinaryExpr(BinaryExpr.GT, cond.getLhs(), cond.getRhs()));
                generatedInv.add(new Invariant (true, new BinaryExpr(BinaryExpr.LEQ, cond.getLhs(), cond.getRhs())));
                ExSet.add(new BinaryExpr(BinaryExpr.GEQ, cond.getLhs(), cond.getRhs()));
            }

            if (cond.getOperator()==BinaryExpr.EQUAL ){

                generatedInv.add(new Invariant (true, new BinaryExpr(BinaryExpr.EQUAL, cond.getLhs(), cond.getRhs())));
                ExSet.add(new BinaryExpr(BinaryExpr.GT, cond.getLhs(), cond.getRhs()));
                generatedInv.add(new Invariant (true, new BinaryExpr(BinaryExpr.LEQ, cond.getLhs(), cond.getRhs())));
                ExSet.add(new BinaryExpr(BinaryExpr.GEQ, cond.getLhs(), cond.getRhs()));
            }

            varValue.put(lhs.getName(),ExSet);


        }


        for(Node node: whileStmt.getBody().getChildrenCopy()) {
            if(node instanceof StmtList){
                StmtList stlist= (StmtList)node;
                for(Stmt st: stlist.getStatements()){
                    if (st instanceof AssignStmt) {

                        AssignStmt ass = (AssignStmt) st;
                        DeclRef lhs = ass.getLhs();
                        if(!(ass.getRhs() instanceof IntLiteral) && varValue.containsKey(lhs.getName())){
                            for(BinaryExpr ex: varValue.get(lhs.getName())){

                            int comp=ex.getOperator();

                            generatedInv.add(new Invariant (true, new BinaryExpr(comp,  ass.getRhs(), ex.getRhs())));
                            }


                        }

                        // lhs always variable so add it to the list modset
                        if(!modset.contains(lhs.getName().split("\\$")[0])) {
                            modset.add(lhs.getName().split("\\$")[0]);
                            System.out.println("MODSET " + lhs.getName().split("\\$")[0]);
                        }


                        Expr rhs = ass.getRhs();

                         if(rhs instanceof BinaryExpr){

                            visit(rhs);



                        }
                        if (st instanceof AssertStmt){
                            AssertStmt asser = (AssertStmt) st;
                            BinaryExpr cond1= ((BinaryExpr) asser.getCondition());
                            generatedInv.add(new Invariant (true, new BinaryExpr(cond1.getOperator(), cond1.getLhs(), cond1.getRhs())));

                        }

                    }


                }
            }


        }




        // generate invariant between variables and numbers
        if(!modsetInt.isEmpty()){
            for(int comp: comparators){
                for(String mods: modset){
                    for(int invNb: modsetInt){

                        Invariant invariant = new Invariant(true, new BinaryExpr(comp, new DeclRef(mods), new IntLiteral(invNb)));
                        generatedInv.add(invariant);
                        System.out.println("INVARIANTS " + invariant);
                    }
                }
            }
        }

        // generate invariant between variables and variables
        ArrayList<String> modsetArr= new ArrayList<String>(modset);
        // generate invariant between variables and variables
        for(int i=0;i<modsetArr.size()-1;i++) {
            for(int j=i+1;j<modsetArr.size();j++){
                Invariant invariant = new Invariant(true, new BinaryExpr(22, new DeclRef(modsetArr.get(i)), new DeclRef(modsetArr.get(j))));
                generatedInv.add(invariant);
                System.out.println("INVARIANTS " + invariant);


            }
        }


        whileStmt.setOrigInvariants(generatedInv);

        return whileStmt;





    }


    @Override
    public Object visit(IntLiteral intlit){
        return intlit;
    }

    @Override
    public Object visit(BinaryExpr binaExpr){

        Expr lhs;
        lhs = binaExpr.getLhs();
        Expr rhs;
        rhs = binaExpr.getRhs();

        if(lhs instanceof IntLiteral){
            int temp = ((IntLiteral)lhs).getValue();
            if(!modsetInt.contains(temp)){
                modsetInt.add(temp);
            }
        } else if(lhs instanceof DeclRef){
            String temp = ((DeclRef)lhs).getName();
            if(!modset.contains(temp)){
                modset.add(temp);
            }
        }

        // if the rhs is also a binary expression then visit again
        if(rhs instanceof BinaryExpr){

            return visit(binaExpr);

        } else if(rhs instanceof DeclRef){
            String temp = ((DeclRef)rhs).getName();
            if(!modset.contains(temp)){
                modset.add(temp);
            }
        }

        return super.visit(binaExpr);
    }

    @Override
    public Object visit(DeclRef declref){
        return declref;
    }

    @Override
    public Object visit(AssignStmt assign){
        return assign;
    }



}
