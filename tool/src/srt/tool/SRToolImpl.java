package srt.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import srt.ast.Program;
import srt.ast.visitor.impl.PrinterVisitor;
import srt.exec.ProcessExec;
import srt.tool.exception.ProcessTimeoutException;

public class SRToolImpl implements SRTool {


    private Program program;
    private CLArgs clArgs;
    private Program origprog;
    boolean skip=false;
    int count=0;
    boolean skiphoudini= false;

    HashMap<String, String> modeResultMap = new HashMap <String, String>(); //Mode, Result
    String queryResultMult;

	public SRToolImpl(Program p, CLArgs clArgs) {

		this.program = p;
        this.origprog=p;
		this.clArgs = clArgs;

	}
    public SRToolResult go(Integer inv) throws IOException, InterruptedException{

        List<Integer> invs= new ArrayList<Integer>();
        invs.add(inv);
        return go(invs);
    }

    public SRToolResult go(List<Integer> list) throws IOException, InterruptedException{

        Program prog= origprog;
        if(count==0)
            prog.setInvariants(list);
        if(count>=1)
            prog.setInvariants1(list);
        count++;
        program = (Program) new HoudiniVisitor().visit(prog);

        //program.setInvariants(list);
        program = (Program) new PredicationVisitor().visit(program);
        program = (Program) new SSAVisitor().visit(program);

        // Output the program as text after being transformed (for debugging).
        if (clArgs.verbose) {
            String programText = new PrinterVisitor().visit(program);
            System.out.println(programText);
        }


        // Collect the constraint expressions and variable names.
        CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
        ccv.visit(program);

        // Convert constraints to SMTLIB String.
        SMTLIBQueryBuilder builder = new SMTLIBQueryBuilder(ccv);


        builder.buildQuery();

        String smtQuery = builder.getQuery();

        // Output the query for debugging
        if (clArgs.verbose) {
            System.out.println(smtQuery);
        }

        // Submit query to SMT solver.
        // You can use other solvers.
        // E.g. The command for cvc4 is: "cvc4", "--lang", "smt2"
        ProcessExec process = new ProcessExec("z3", "-smt2", "-in");
        String queryResult;
        try {
            queryResult = process.execute(smtQuery, clArgs.timeout);

        } catch (ProcessTimeoutException e) {
            if (clArgs.verbose) {
                System.out.println("Timeout!");
            }
            return SRToolResult.UNKNOWN;
        }

        // output query result for debugging
        if (clArgs.verbose) {
            System.out.println(queryResult);
        }

        if (queryResult.contains("unsat")) {
            return SRToolResult.CORRECT;
        }
        else
        if(clArgs.mode.equals(CLArgs.HOUDINI)&& !skiphoudini){
            count++;
            skiphoudini=true;
            String[] lines=queryResult.split("\\r?\\n");
            String inv;
            ArrayList<Integer> correctInvariants= new ArrayList<Integer>();
            for(String line: lines){
                if(line.contains("#x00000001")){
                    System.out.println(line);

                    inv= line.split("\\(+")[1];

                    inv= inv.split("inv")[1];

                    inv= inv.split("\\$")[0];



                    correctInvariants.add(Integer.parseInt(inv));
                }
            }

            return go(correctInvariants);


        }
        if(!skip && list.size()>1 && !queryResult.contains("unsat")){
            if(count>1){ //after we get satisfiable invariants
            skip=true;

            ArrayList<Integer> invs= new ArrayList<Integer>();
            for (Integer inv: list){

                if(go(inv).equals(SRToolResult.CORRECT))
                {

                invs.add(inv);
                }
        }
        System.out.println(invs);
        return go(invs);
            }
            else{
                count++;
                String[] lines=queryResult.split("\\r?\\n");
                String inv;
                ArrayList<Integer> correctInvariants= new ArrayList<Integer>();
                for(String line: lines){
                    if(line.contains("#x00000001")){
                        System.out.println(line);

                        inv= line.split("\\(+")[1];

                        inv= inv.split("inv")[1];

                        inv= inv.split("\\$")[0];



                        correctInvariants.add(Integer.parseInt(inv));
                    }
                }

                return go(correctInvariants);

            }
        }
        else{
            if (queryResult.startsWith("sat")) {
                //if(list.size()<=1 || skip) {
                    return SRToolResult.INCORRECT;
            }
            // query result started with something other than "sat" or "unsat"
            return SRToolResult.UNKNOWN;
        }
        }


	public SRToolResult go() throws IOException, InterruptedException {

		//  Transform program using Visitors here.
        ArrayList <String> modes = new ArrayList<String>();
        if(clArgs.mode.equals(CLArgs.COMP)){
            modes = new CompetitionVisitor(program).getMultipleModes();
            System.out.println("Mode size: "+modes.size()+"\n");
        }
        if(modes.size()>1 && clArgs.mode.equals(CLArgs.COMP)){
            System.out.println("MULTIPLE MODES :)\n");
            goMultipleModes(modes);
            //break;
        }
        //ONLY ONE MODE or competition mode
        else  {
            if(clArgs.mode.equals(CLArgs.COMP))clArgs.mode = modes.get(0);
            if( modes.size()<=1){

                System.out.println("Clargs mode " + clArgs.mode + "\n");

		if (clArgs.mode.equals(CLArgs.BMC)) {
			program = (Program) new LoopUnwinderVisitor(clArgs.unsoundBmc,
					clArgs.unwindDepth).visit(program);
		} else if (clArgs.mode.equals(CLArgs.HOUDINI)){
            program = (Program) new HoudiniVisitor().visit(program);
        } else if (clArgs.mode.equals(CLArgs.INVGEN)){
            program= (Program)new InvGenerationVisitor().visit(program);

            program = (Program) new HoudiniVisitor().visit(program);

        }else if (clArgs.mode.compareTo("CORRECT")==0){
                System.out.println("Mode: "+clArgs.mode+"\n");
            }
        else {
			program = (Program) new LoopAbstractionVisitor().visit(program);
		}
		program = (Program) new PredicationVisitor().visit(program);
		program = (Program) new SSAVisitor().visit(program);

		// Output the program as text after being transformed (for debugging).
		if (clArgs.verbose) {
			String programText = new PrinterVisitor().visit(program);
			System.out.println(programText);
		}

		// Collect the constraint expressions and variable names.
		CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
		ccv.visit(program);

		//  Convert constraints to SMTLIB String.
		SMTLIBQueryBuilder builder = new SMTLIBQueryBuilder(ccv);


		builder.buildQuery();

		String smtQuery = builder.getQuery();

		// Output the query for debugging
		if (clArgs.verbose) {
			System.out.println(smtQuery);
		}

		// Submit query to SMT solver.
		// You can use other solvers.
		// E.g. The command for cvc4 is: "cvc4", "--lang", "smt2"
		ProcessExec process = new ProcessExec("z3", "-smt2", "-in");
		String queryResult;
		try {
			queryResult = process.execute(smtQuery, clArgs.timeout);
            //System.out.println("------"+queryResult);
		} catch (ProcessTimeoutException e) {
			if (clArgs.verbose) {
				System.out.println("Timeout!");
			}
			return SRToolResult.UNKNOWN;
		}

                if(clArgs.mode.compareTo("CORRECT")==0){
                    System.out.println("query is CORRECT because there are no assertions or loops.\n");
                    return SRToolResult.CORRECT;
                }
                else{
		// output query result for debugging
		if (clArgs.verbose) {
			System.out.println(queryResult);
		}
        if((clArgs.mode.equals(CLArgs.HOUDINI) || clArgs.mode.equals(CLArgs.INVGEN) || queryResult.startsWith("MODSET")) && (queryResult.startsWith("sat") || queryResult.startsWith("MODSET"))){
            String inv;
            String[] lines= queryResult.split("\\r?\\n");
            ArrayList<Integer> correctInvariants= new ArrayList<Integer>();

            for(String line: lines){
                if(line.contains("#x00000001")){
                    System.out.println(line);

                     inv= line.split("\\(+")[1];

                     inv= inv.split("inv")[1];

                     inv= inv.split("\\$")[0];



                    correctInvariants.add(Integer.parseInt(inv));
                }
            }
            System.out.println(correctInvariants);
        return go(correctInvariants);
        }

        else{

		if (queryResult.startsWith("unsat")) {
			return SRToolResult.CORRECT;
		}

		if (queryResult.startsWith("sat")) {
			return SRToolResult.INCORRECT;
		}
		// query result started with something other than "sat" or "unsat"
		return SRToolResult.UNKNOWN;
        }
                }
        }//end else
        }
        if( modeResultMap!=null){
            if(modes.contains(CLArgs.BMC) && modes.contains(CLArgs.VERIFIER)){
                for(int i=0; i<modeResultMap.size(); i++){
                    System.out.println("PRINTING MODES "+modeResultMap.get(modes.get(i)));

                }

                if(modeResultMap.get(modes.get(modes.indexOf(CLArgs.VERIFIER))).compareTo("Correct")==0 || modeResultMap.get(modes.get(modes.indexOf(CLArgs.BMC))).compareTo("Correct")==0){
                    return SRToolResult.CORRECT;
                }else if(modeResultMap.get(modes.get(modes.indexOf(CLArgs.BMC))).compareTo("Incorrect")==0){
                    return SRToolResult.INCORRECT;
                }

            }

        }


        return SRToolResult.UNKNOWN;
	}


    public SRToolResult goMultipleModes(ArrayList<String> modesList) throws IOException, InterruptedException {


        for (String aModesList : modesList) {

            program = origprog;

            System.out.println("Modes in array: " + aModesList + "\n");
            clArgs.mode = aModesList;
            if (clArgs.mode.equals(CLArgs.BMC)) {
                System.out.println("Mode: " + clArgs.mode + "\n");
                program = (Program) new LoopUnwinderVisitor(clArgs.unsoundBmc,
                        clArgs.unwindDepth).visit(program);
            } else {
                if (clArgs.mode.equals(CLArgs.HOUDINI)) {
                    System.out.println("Mode: " + clArgs.mode + "\n");
                    program = (Program) new HoudiniVisitor().visit(program);
                } else {
                    if (clArgs.mode.equals(CLArgs.INVGEN)) {
                        System.out.println("Mode: " + clArgs.mode + "\n");
                        program  = (Program) new InvGenerationVisitor().visit(program);

                        program = (Program) new HoudiniVisitor().visit(program);
                    } else if (clArgs.mode.equals("CORRECT")) {
                        System.out.println("Mode: " + clArgs.mode + "\n");
                    } else
                    {//verifier mode
                        System.out.println("Mode: " + clArgs.mode + "\n");
                        program = (Program) new LoopAbstractionVisitor().visit(program);
                    }
                }
            }

            program = (Program) new PredicationVisitor().visit(program);
            program = (Program) new SSAVisitor().visit(program);

            // Output the program as text after being transformed (for debugging).
       /* if (clArgs.verbose) {
            String programText = new PrinterVisitor().visit(program);
            System.out.println(programText);
        } */

            // Collect the constraint expressions and variable names.
            CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
            ccv.visit(program);

            //  Convert constraints to SMTLIB String.
            SMTLIBQueryBuilder builder = new SMTLIBQueryBuilder(ccv);


            builder.buildQuery();

            String smtQuery = builder.getQuery();

            // Output the query for debugging
            if (clArgs.verbose) {
                System.out.println(smtQuery);
            }

            // Submit query to SMT solver.
            // You can use other solvers.
            // E.g. The command for cvc4 is: "cvc4", "--lang", "smt2"
            ProcessExec process = new ProcessExec("z3", "-smt2", "-in");
            queryResultMult = "";

            try {
                queryResultMult = process.execute(smtQuery, clArgs.timeout);

            } catch (ProcessTimeoutException e) {
                if (clArgs.verbose) {
                    System.out.println("Timeout!");
                }
                //return SRToolResult.UNKNOWN;
                modeResultMap.put(aModesList, "unknown");
            }

            // output query result for debugging

            if (clArgs.mode.contains("CORRECT")) {
                System.out.println("query is CORRECT because there are no assertions or loops.\n");
                modeResultMap.put(aModesList, "correct");
            } else {
                if (clArgs.verbose) {
                    //System.out.println(queryResult);
                    modeResultMap.put(aModesList, queryResultMult);
                }
                if ((clArgs.mode.equals(CLArgs.HOUDINI) || clArgs.mode.equals(CLArgs.INVGEN) || queryResultMult.startsWith("MODSET")) && (queryResultMult.startsWith("sat") || queryResultMult.startsWith("MODSET"))) {
                    String result = queryResultMult;
                    String inv;
                    String[] lines = result.split("\\r?\\n");
                    ArrayList<Integer> correctInvariants = new ArrayList<Integer>();

                    for (String line : lines) {
                        if (line.contains("#x00000001")) {
                            System.out.println(line);

                            inv = line.split("\\(+")[1];

                            inv = inv.split("inv")[1];

                            inv = inv.split("\\$")[0];

                            correctInvariants.add(Integer.parseInt(inv));
                        }
                    }
                    System.out.println(correctInvariants);
                    return go(correctInvariants);
                } else {
                    System.out.println("RESULT: " + queryResultMult);
                    if (queryResultMult.startsWith("unsat")) {
                        modeResultMap.put(aModesList, "Correct");
                    } else if (queryResultMult.startsWith("sat")) {
                        modeResultMap.put(aModesList, "Incorrect");
                    }
                    // query result started with something other than "sat" or "unsat"
                    else {
                        modeResultMap.put(aModesList, "Unknown");
                    }
                }
            }//end else

        }//end loop

        if(modesList.contains(CLArgs.BMC) && modesList.contains(CLArgs.VERIFIER)){
            if(modeResultMap.get(modesList.get(modesList.indexOf(CLArgs.VERIFIER))).compareTo("Correct")==0 || modeResultMap.get(modesList.get(modesList.indexOf(CLArgs.BMC))).compareTo("Correct")==0){
                return SRToolResult.CORRECT;
            }else if(modeResultMap.get(modesList.get(modesList.indexOf(CLArgs.BMC))).compareTo("Incorrect")==0){
                return SRToolResult.INCORRECT;
            }
        }
        return SRToolResult.UNKNOWN;

    }

}
