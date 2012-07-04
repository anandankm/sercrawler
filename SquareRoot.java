import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;

public class SquareRoot implements Square_Root{
	/*
	 * First or Second arguments must not be null or any character or string that is not
	 * an integer.
	 * (non-Javadoc)
	 * @see serverAddInterface#add_number(java.lang.String, java.lang.String)
	 */
	public String serviceAgreement() throws RemoteException {
		String cost=null;
		String S=null;
		try {
			Properties tempobj3 = new Properties();
			InputStream tempfileobj = new FileInputStream("squarerootconfig.ini");
			tempobj3.load(tempfileobj);
			cost= tempobj3.getProperty("cost");
			S= tempobj3.getProperty("servicegurantee");
			tempfileobj.close();
		}
		catch(IOException e){
			System.out.println("connot open config squarerootconfig.ini file");
		}
		return (cost+":"+S);
	}
	
	public String squareRoot(String input, int precision)
			throws RemoteException {
	
		try {
		String sqroot1=null;
		int p=0;
		int tempp=0;
		try{
		//read precsion from config file and check with the user value
		Properties tempobj = new Properties();
		InputStream tempfileobj = new FileInputStream("squarerootconfig.ini");
		tempobj.load(tempfileobj);
		String temppre= tempobj.getProperty("squarerootprecision");
		tempfileobj.close();
		tempp = Integer.parseInt(temppre);
		}
		catch(IOException e){
			System.out.println("squarerootconfig file not found");
		}
 if(precision>tempp)
	 p=tempp;
  else
	p=precision;
//		String input=args[0];
//		int precision=Integer.parseInt(args[1]);
		int dotindex=input.indexOf(".");
		if (dotindex>0){
		int decimallength=input.length()-dotindex-1;
		float wholenumber = 0;
		try {
			
			wholenumber=Float.parseFloat(input);
			
		} catch (Exception e) {
			
			return "Please enter a integer or a float input";
			
		}
		
		if (wholenumber < 0) {
			
			return "Please enter a positive value";
			
		}
		
		int dividingno=(int) Math.pow(10, decimallength);
		int newno=(int) (wholenumber*dividingno);
		float part1=calculateroot(newno);
		if (part1 == -12) {
			return "Enter 10 or less digits";
		}
		float part2=calculateroot(dividingno);
		float sqroot=part1/part2;
		sqroot1=round(sqroot,precision);
		//System.out.println(sqroot);
		return sqroot1;
		
		}
		else{
			int num = 0;
			try {
				
				num=Integer.parseInt(input);
				
			} catch (Exception e) {
				
				return "Please enter a integer or a float input";
				
			}
			
			if (num < 0) {
				
				return "Please enter a positive value";
				
			}
			
			float sqroot=calculateroot(num);
			
			if (sqroot == -12) {
			
				return "Enter 10 or less digits";
				
			}
			sqroot1=round(sqroot,precision);
			//System.out.println(sqroot);
			return sqroot1;
		}
		
		} catch (Exception e) {
			
			return "Square Root: Input Error";
			
		}
		
		
	}//main

	static float calculateroot(int num){
	
	int cnum=0;
	int temp=0;
	int n=num;
	int[] digArray=new int[100];
	
	float digit=0;
	float xi=0;
	
	float pi=1;
	float tosub=0;
while (n!=0){
digArray[(int) digit]=(int) (n%10);
digit++;
n=n/10;
}
//System.out.println(digit);
if (digit>10) {
	float f = -12;
	return f;
}

if((digit%2)==0){
 cnum=(digArray[(int) (digit-1)]*10)+digArray[(int) (digit-2)];}
else{		
	 cnum=digArray[(int) (digit-1)];}
//System.out.println(cnum);

	if(cnum==1) {
		temp=1;
		
	} else
	{
		
		for (int i = 1; i <=cnum/2; i++) {
			if ((i*i)<= cnum){
				temp=i;
			}else{
				break;}
		}
	}
	//System.out.println(temp);		
float digitfloat=digit/2;
int newdigit=(int) Math.ceil((digitfloat));
	
	double temp1;
	if(newdigit>0){
		temp1= Math.pow(10, (newdigit-1));
    xi= (float) (temp * temp1);}
	else
		xi=temp;
	//System.out.println(xi);
	
float remainder=(float) (num-Math.pow(xi, 2));
//System.out.println(remainder);


while(pi>0.05){
pi=remainder/(2*xi);

int tocmppi=(int) pi;
if (tocmppi>0){
  pi=tocmppi; }
else{
   pi=pi;}
//System.out.println(pi);

tosub=remainder-(pi*((2*xi)+pi));
xi= (xi+pi);
remainder=tosub;
//System.out.println(tosub);

}//while
//System.out.println(xi);
	return xi;
}//class
	
	
	public static String round(double sqroot, int precision) {
		String st=Double.toString(sqroot);
        int total=st.length();
        int stind=st.indexOf(".");
        String decstring=st.substring(stind,total);
        if(decstring.length()<precision)
        {
            int num=precision-decstring.length();
            for(int i=0;i<num+1;i++){
                decstring=decstring+"0";
            }
        String toreturn=st.substring(0, stind)+decstring;
        return toreturn;
        }
        String decimalpre=decstring.substring(0,precision+1);
        String toreturn=st.substring(0, stind)+decimalpre;
        return toreturn;
	}
}
