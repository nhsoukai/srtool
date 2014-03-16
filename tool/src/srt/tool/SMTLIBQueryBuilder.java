package srt.tool;

import srt.ast.AssertStmt;
import srt.ast.*;


public class SMTLIBQueryBuilder {

	private ExprToSmtlibVisitor exprConverter;
    private CollectConstraintsVisitor constraints;
	private String queryString = "";



	public SMTLIBQueryBuilder(CollectConstraintsVisitor ccv) {
		this.constraints = ccv;
		this.exprConverter = new ExprToSmtlibVisitor();

	}



    public void buildQuery() {

        StringBuilder query = new StringBuilder();
        // Define two functions to convert from boolean to bitvector and vice-versa.
        query.append("(set-logic QF_BV)\n"
                + "(define-fun tobv32 ((p Bool)) (_ BitVec 32) (ite p (_ bv1 32) (_ bv0 32)))\n" +
                "(define-fun toBool ((x (_ BitVec 32))) (Bool) (ite (bvsgt x (_ bv0 32)) true false ))\n");




        //  Declare variables, add constraints, add properties to check

        for(String str:constraints.variableNames){

            query.append("(declare-const ").append(str).append(" (_ BitVec 32))\n");
        }

        if(!constraints.variableNames.contains("$P0"))
            query.append("(declare-const $P0 (_ BitVec 32))\n");
        if(!constraints.variableNames.contains("$G0"))
            query.append("(declare-const $G0 (_ BitVec 32))\n");

        query.append("(assert(= $P0 (_ bv1 32)))\n");





        for(Stmt st: constraints.transitionNodes){

                query.append(exprConverter.visit(st));
        }


        query.append("\n");


        if (constraints.propertyNodes.size()>0){

            query.append("(assert(not ");

            if (constraints.propertyNodes.size()>=2)
                query.append("(and \n");

            for( AssertStmt assertion: constraints.propertyNodes){

                query.append(exprConverter.visit(assertion));
                query.append("\n");

            }
            if (constraints.propertyNodes.size()>=2)
                query.append(")");
            query.append("))");

        }
        query.append("\n(check-sat)\n");

        query.append("(get-value (");

        for(String str: constraints.variableNames){
            if(str.contains("inv"))
                query.append(str);
        }
        query.append("))");

        queryString = query.toString();
    }

    public String getQuery() {
        return queryString;
    }

}
