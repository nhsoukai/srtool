package srt.tool;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class ExprToSmtlibVisitor extends DefaultVisitor {
    List<Integer> comparator= Arrays.asList(BinaryExpr.LOR, BinaryExpr.LAND, BinaryExpr.NEQUAL, BinaryExpr.EQUAL, BinaryExpr.BAND,BinaryExpr.IMPLY,BinaryExpr.GEQ,BinaryExpr.GT,BinaryExpr.LEQ,BinaryExpr.LT);


    public ExprToSmtlibVisitor() {
		super(true);
	}

	@Override
	public String visit(BinaryExpr expr) {
		String operator = null;
		switch(expr.getOperator())
		{
			case BinaryExpr.ADD:
				operator = "(bvadd %s %s)";
				break;
			case BinaryExpr.BAND:
                operator = "(bvand %s %s)";
				break;
			case BinaryExpr.BOR:
                operator = "(bvor %s %s)";
				break;
			case BinaryExpr.BXOR:
                operator = "(bvxor %s %s)";
				break;
			case BinaryExpr.DIVIDE:
                operator = "(bvsdiv %s %s)";
				break;
			case BinaryExpr.LSHIFT:
                operator = "(bvshl %s %s)";
				break;
			case BinaryExpr.MOD:
                operator = "(bvsmod %s %s)";
				break;
			case BinaryExpr.MULTIPLY:
                operator = "(bvmul %s %s)";
				break;
			case BinaryExpr.RSHIFT:
                operator = "(bvashr %s %s)";
				break;
			case BinaryExpr.SUBTRACT:
                operator = "(bvsub %s %s)";
				break;
			case BinaryExpr.LAND:
                operator = "(tobv32 (and (toBool %s) (toBool %s)))";
				break;
			case BinaryExpr.LOR:
                operator = "(tobv32 (or (toBool %s) (toBool %s)))";
				break;
			case BinaryExpr.GEQ:
                operator = "(tobv32 (bvuge %s %s))";
				break;
			case BinaryExpr.GT:
                operator = "(tobv32 (bvugt %s %s))";
				break;
			case BinaryExpr.LEQ:
                operator = "(tobv32 (bvule %s %s))";
				break;
			case BinaryExpr.LT:
                operator = "(tobv32 (bvult %s %s))";
				break;
			case BinaryExpr.NEQUAL:
                operator = "(tobv32 (not(= %s %s)))";
                break;
			case BinaryExpr.EQUAL:
                operator = "(tobv32 (= %s %s))";
				break;
            case BinaryExpr.IMPLY:
                operator = "(tobv32 (=> (toBool %s) (toBool %s)))";
                break;

			default:
				throw new IllegalArgumentException("Invalid binary operator");
		}

        if(comparator.contains(operator)){

            return "(tobv32 "+String.format(operator, visit(expr.getLhs()), visit(expr.getRhs()))+")";
        }

		return String.format(operator, visit(expr.getLhs()), visit(expr.getRhs()));
		
	}

	@Override
	public String visit(DeclRef declRef) {
          return declRef.getName();
	}

	@Override
	public String visit(IntLiteral intLiteral) {
		return String.format("(_ bv%d 32)",intLiteral.getValue());
	}

	@Override
	public String visit(TernaryExpr ternaryExpr) {
        Expr trueE= ternaryExpr.getTrueExpr();
        Expr falseE= ternaryExpr.getFalseExpr();
        String trueEx=visit(trueE);
        String falseEx=visit(falseE);/*
        if(trueE instanceof BinaryExpr){
            if(comparator.contains(((BinaryExpr)trueE).getOperator())){
                trueEx="(tobv32 "+trueEx+")";
            }
        }
        if(falseE instanceof BinaryExpr){
            if(comparator.contains(((BinaryExpr)falseE).getOperator())){
                falseEx="(tobv32 "+falseEx+")";
            }
        }   */
		return String.format("(ite(toBool %s) %s %s)",visit(ternaryExpr.getCondition()), trueEx, falseEx);
	}

	@Override
	public String visit(UnaryExpr unaryExpr) {
		String operator = null;
		switch(unaryExpr.getOperator())
		{
		case UnaryExpr.UMINUS:
			operator = "(bvneg %s)";
			break;
		case UnaryExpr.UPLUS:
            operator = "%s";
			break;
		case UnaryExpr.LNOT:
            operator = "(tobv32 (not (toBool %s)))";
			break;
		case UnaryExpr.BNOT:
            operator = "(bvnot %s)";
			break;
		default:
			throw new IllegalArgumentException("Invalid binary operator");
		}
		
		return String.format(operator, visit(unaryExpr.getOperand()));
	}
	
	
	/* Overridden just to make return type String. 
	 * @see srt.ast.visitor.DefaultVisitor#visit(srt.ast.Expr)
	 */
	@Override
	public String visit(Expr expr) {
		return (String) super.visit(expr);
	}



    @Override
    public String visit(IfStmt ifstmt) {
        String ifst=visit(ifstmt);
        return String.format("(assert(= %s))\n",ifst);

    }

        @Override
    public String visit(AssignStmt assign) {

        String rhs=visit(assign.getRhs());
        /*if(assign.getRhs() instanceof BinaryExpr){
          /*  if(comparator.contains(((BinaryExpr)assign.getRhs()).getOperator())){
                rhs="(tobv32 "+rhs+")";
            }
            /*if (visit(assign.getLhs()).contains("P")){
                rhs="(toBool "+rhs+")";

            }
        } */

        return String.format("(assert(= %s %s))\n",visit(assign.getLhs()), rhs);
    }
    @Override
    public String visit(AssertStmt assertst) {
        Expr condition= assertst.getCondition();
        StringBuffer str= new StringBuffer();

        if(condition instanceof BinaryExpr){
            if(comparator.contains(((BinaryExpr)condition).getOperator())){
               return "(toBool "+visit(condition)+") ";

            }
        }

        if(condition instanceof DeclRef || condition instanceof IntLiteral)
            return "(toBool "+visit(condition)+") ";


        return visit(condition);

    }
}
