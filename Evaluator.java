public class Evaluator {

	//Wrapper that removes space and sanitises the expr
	public static int evaluateIntExpr(String exp) {
		exp=exp.trim().replaceAll(" ","");
		return evaluateIntExp(exp);
	}
	
	//Wrapper that removes space and sanitises the expr
	public static int evaluateBoolExpr(String exp) {
		exp=exp.trim().replaceAll(" ","");
		return evaluateBoolExp(exp);
	}
	
	// A string containing + * and integer number without brackets is evaluated
	public static int evaluateIntExp(String exp) {
		
		// First check for op with highest precedence +
		if(exp.indexOf('+')>=0) {
			int operand=exp.indexOf('+');
			
			//Seperate into two operands, evaluate the operands, add and return
			int op1=evaluateIntExp(exp.substring(0,operand));
			int op2=evaluateIntExp(exp.substring(operand+1,exp.length()));
			return op1+op2;
		// Next op is *	
		} else if(exp.indexOf('*')>=0) {
			int operand=exp.indexOf('*');
			
			//Seperate into two operands, evaluate the operands, mult and return
			int op1=evaluateIntExp(exp.substring(0,operand));
			int op2=evaluateIntExp(exp.substring(operand+1,exp.length()));
			return op1*op2;
		// Finally just an integer		
		} else {
			return Integer.parseInt(exp);
		}
	}

	// A string containing ^ & | >= > <= < = integers can evaluated 
	public static int evaluateBoolExp(String exp) {
		// Not operator
		if(exp.indexOf('^')>=0) {
			int operand=exp.indexOf('^');
			int op=evaluateBoolExp(exp.substring(operand+1,exp.length()));
			int result;
			if(op==0)
				result=1;
			else
				result=0;
			// Evaluate result of not operator and substitute in expr and evaluate the expr
			return evaluateBoolExp(exp.substring(0,operand)+result);
		// And operator	
		} else if(exp.indexOf('&')>=0) {
			int operand=exp.indexOf('&');
			int op1=evaluateBoolExp(exp.substring(0,operand));
			int op2=evaluateBoolExp(exp.substring(operand+1,exp.length()));
			if(op1==0||op2==0)
				return 0;
			else
				return 1;
		// Or operator		
		} else if(exp.indexOf('|')>=0) {
			int operand=exp.indexOf('|');
			int op1=evaluateBoolExp(exp.substring(0,operand));
			int op2=evaluateBoolExp(exp.substring(operand+1,exp.length()));
			if(op1!=0||op2!=0)
				return 1;
			else
				return 0;
		//GEqual		
		} else if(exp.indexOf(">=")>=0) {
			int operand=exp.indexOf(">=");
			int op1=Integer.parseInt(exp.substring(0,operand));
			int op2=Integer.parseInt(exp.substring(operand+2,exp.length()));
			if(op1>=op2)
				return 1;
			else
				return 0;
		//LEqual		
		} else if(exp.indexOf("<=")>=0) {
			int operand=exp.indexOf("<=");
			int op1=Integer.parseInt(exp.substring(0,operand));
			int op2=Integer.parseInt(exp.substring(operand+2,exp.length()));
			if(op1<=op2)
				return 1;
			else
				return 0;
		//Greater than		
		} else if(exp.indexOf(">")>=0) {
			int operand=exp.indexOf(">");
			int op1=Integer.parseInt(exp.substring(0,operand));
			int op2=Integer.parseInt(exp.substring(operand+1,exp.length()));
			if(op1>op2)
				return 1;
			else
				return 0;
		// Less than		
		} else if(exp.indexOf("<")>=0) {
			int operand=exp.indexOf("<");
			int op1=Integer.parseInt(exp.substring(0,operand));
			int op2=Integer.parseInt(exp.substring(operand+1,exp.length()));
			if(op1<op2)
				return 1;
			else
				return 0;
		// Equal		
		} else if(exp.indexOf("=")>=0) {
			int operand=exp.indexOf("=");
			int op1=Integer.parseInt(exp.substring(0,operand));
			int op2=Integer.parseInt(exp.substring(operand+1,exp.length()));
			if(op1==op2)
				return 1;
			else
				return 0;
		// Finally just an integer		
		} else {
			return Integer.parseInt(exp);
		}
	}
			
	public static void main(String args[]) throws Exception {
		System.out.println(evaluateBoolExpr(args[0]));
	}
}
