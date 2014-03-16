package srt.tool;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ah5713
 * Date: 12/03/14
 * Time: 21:46
 * To change this template use File | Settings | File Templates.
 * if we have no assertions and no invariants: program CORRECT
 * if we have no while: mode BMC
 * if we have no candidate invariants: mode VERIFIER
 * if we have candidate invariants: mdoe HOUDINI
 * if we have no invariants: mode is VERIFIER or INVGEN
 */
public class CompetitionVisitor extends DefaultVisitor{

    private List<Stmt> stmtList;
    private boolean assertStmt;
    private boolean whileStmt;
    private boolean hasInv;
    private boolean candInv;
    private IntLiteral bound = null;

    private ArrayList<String> modes = new ArrayList<String>();



    public CompetitionVisitor(Program program){
        super(true);
        stmtList = program.getBlockStmt().getStmtList().getStatements();
    }

    public void checkForStmtTypes(){
        for(Stmt st: stmtList){

            if(st instanceof AssertStmt){
                assertStmt = true;
                //apply BMC with unwinding assertions
                //report incorrect b/c it couldnt verify loop unwinding assertion
            }
            if(st instanceof WhileStmt){
               whileStmt = true;
                InvariantList invList = ((WhileStmt)st).getInvariantList();


                if(!invList.getInvariants().isEmpty()) {
                    hasInv = true;
                    List <Invariant> invariants = invList.getInvariants();
                    for(Invariant inv: invariants){  //look for candidate invariants
                        if(inv.isCandidate()){

                            candInv = true;
                        }

                    }
                }

                bound = ((WhileStmt) st).getBound();
                Stmt body = ((WhileStmt)st).getBody();
                for(Node bod:body.getChildrenCopy()){
                    if(bod instanceof StmtList){
                        StmtList stlist= (StmtList)bod;
                        List<Stmt> stmts= stlist.getStatements();
                        for(Stmt stmt: stmts){
                            if (stmt instanceof WhileStmt){
                                InvariantList innerWhileInv = ((WhileStmt) stmt).getInvariantList();
                                if(!innerWhileInv.getInvariants().isEmpty()){
                                    hasInv = true;
                                    List <Invariant> innerInvariants = innerWhileInv.getInvariants();
                                    for(Invariant innerInv: innerInvariants){
                                        if(innerInv.isCandidate()){
                                            candInv = true;
                                        }

                                    } //end invariants
                                }
                            }
                        }//end stmts
                    }
                }//end body

            }//endWhileStmt

        }//end All Stmts
    }//end


    public ArrayList<String> getMultipleModes(){
        checkForStmtTypes();


        if(!assertStmt && !whileStmt){

            modes.add("CORRECT");
        }
        if(!assertStmt && whileStmt){
            modes.add(CLArgs.BMC);
        }
        else if(assertStmt && !whileStmt){
            modes.add(CLArgs.VERIFIER);
        }

        else if(assertStmt &&  hasInv && !candInv){
            modes.add(CLArgs.BMC);
            modes.add(CLArgs.VERIFIER);
        }
        else if(assertStmt && bound==null){  // invgen and houdini cases
            modes.add(CLArgs.INVGEN);
            //BMC or invGen compete or VERIFIER
        }
        else if(assertStmt &&  !hasInv ){
            modes.add(CLArgs.BMC);
            modes.add(CLArgs.VERIFIER);
        }

        return modes;


    }


}
