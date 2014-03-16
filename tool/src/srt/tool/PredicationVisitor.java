package srt.tool;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;


public class PredicationVisitor extends DefaultVisitor {
    private int lastIndex=0;
    private DeclRef assumeFlag = new DeclRef("$G0");
    private DeclRef assumePrevFlag=assumeFlag;
    private DeclRef currentP=new DeclRef("$P0");
    private int globalIndex = 0;
    private DeclRef Q = new DeclRef("$Q0");



	public PredicationVisitor() {
		super(true);


    }

	
	@Override
	public Object visit(IfStmt ifStmt) {

        //store the last predicate index before entering the if statement
        //so that when exiting the if statement the predicate is updated
        // to the last predicate before entering
        int upperIndex = lastIndex;


        DeclRef freshP=new DeclRef("$P"+(++lastIndex));
        freshP.setIsFresh();
        DeclRef upperFreshP=new DeclRef("$P"+(upperIndex));
        upperFreshP.setIsFresh();
        Expr cond=ifStmt.getCondition();
        cond.setIsAss();

        AssignStmt cond1= new AssignStmt(freshP,new BinaryExpr(BinaryExpr.BAND,cond,upperFreshP));

        currentP=freshP;

        Stmt thenStmt = (Stmt) visit(ifStmt.getThenStmt());
        DeclRef freshNotP=new DeclRef("$P"+(++lastIndex));
        freshNotP.setIsFresh();

        currentP=freshNotP;
        Stmt elseStmt = (Stmt) visit(ifStmt.getElseStmt());


        Stmt elseCond1= new AssignStmt(freshNotP,new BinaryExpr(BinaryExpr.BAND,new UnaryExpr(UnaryExpr.BNOT,freshP),upperFreshP));


        currentP=upperFreshP;


        return new BlockStmt(new Stmt[] { cond1, thenStmt, elseCond1, elseStmt },
                ifStmt.getNodeInfo());

	}


	@Override
	public Object visit(AssignStmt assignment) {

        DeclRef lhs=assignment.getLhs();
        lhs.setIsLhs();
        BinaryExpr globalAndPred = new BinaryExpr(BinaryExpr.BAND, currentP, assumeFlag);
        assignment= new AssignStmt(lhs,new TernaryExpr(globalAndPred,assignment.getRhs(),assignment.getLhs()));

        return super.visit(assignment);
	}

    @Override
    public Object visit(AssumeStmt assumeStmt) {


        BinaryExpr rhs;

        BinaryExpr globalAndPred = new BinaryExpr(BinaryExpr.BAND, currentP, assumeFlag);
        rhs = new BinaryExpr(99, globalAndPred, assumeStmt.getCondition());

        AssignStmt assignNewStmt = new AssignStmt(Q, rhs);

        BinaryExpr QandG = new BinaryExpr(BinaryExpr.BAND, Q, assumePrevFlag);

        ++globalIndex;

        assumeFlag=new DeclRef("$G"+globalIndex);
        assumePrevFlag =  assumeFlag;

        AssignStmt assignGlob = new AssignStmt(assumeFlag, QandG);
        Q=new DeclRef("$Q"+globalIndex);

        return new BlockStmt(new Stmt[] { assignNewStmt,assignGlob},
                assumeStmt.getNodeInfo());

    }

	@Override
	public Object visit(HavocStmt havocStmt) {

        DeclRef havocH = new DeclRef(havocStmt.getVariable().getName()+"$h");
        DeclRef lhs= new DeclRef(havocStmt.getVariable().getName()+"$n");
        DeclRef rhs= havocStmt.getVariable();
        AssignStmt havocAssign;
        TernaryExpr havocTern = new TernaryExpr(new BinaryExpr(BinaryExpr.BAND, currentP, assumePrevFlag),lhs,rhs,havocStmt.getNodeInfo());

        havocAssign = new AssignStmt(havocH, havocTern);


        return super.visit(havocAssign);


	}

    @Override
    public Object visit(AssertStmt assertst) {

         AssertStmt asserts= new AssertStmt(new BinaryExpr(BinaryExpr.IMPLY,new BinaryExpr(BinaryExpr.BAND, currentP, assumePrevFlag),assertst.getCondition(),assertst.getNodeInfo()));

         return super.visit(asserts);


    }

    @Override
    public Object visit(WhileStmt whileStmt) {


        return super.visit(whileStmt);


    }



}