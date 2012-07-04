import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;

public class CubeRoot implements Cube_Root{
	/*
	 * First or Second arguments must not be null or any character or string that is not
	 * an integer.
	 * (non-Javadoc)
	 * @see serverAddInterface#add_number(java.lang.String, java.lang.String)
	 */
	public String cubeRoot(String input, int precision) throws RemoteException {
		try {
		boolean negativeflag = false;
		int tempp=0;
		String sqroot1=null;
		try{
			//read precsion from config file and check with the user value
			Properties tempobj = new Properties();
			InputStream tempfileobj = new FileInputStream("cuberootconfig.ini");
			tempobj.load(tempfileobj);
			String temppre= tempobj.getProperty("cuberootprecision");
			tempfileobj.close();
			tempp = Integer.parseInt(temppre);
		}
		catch(IOException e){
			System.out.println("cubeconfig file not found");
		}
		if(precision>tempp)
			precision=tempp;
		else
			precision=precision;

		int dotindex=input.indexOf(".");
		if (dotindex>0){
			int decimallength=input.length()-dotindex-1;
			float wholenumber = 0;
			try {
				
				wholenumber=Float.parseFloat(input);
				
			} catch (Exception e) {
				
				return "Please enter a integer or a float input";
				
			}
			
			if (wholenumber<0) {
				wholenumber = wholenumber*(-1);
				negativeflag = true;
			}
			
			int dividingno=(int) Math.pow(10, decimallength);
			int newno=(int) (wholenumber*dividingno);
			double part1=calculateroot(newno);
			if (part1 == -12) {
				return "Enter 10 or less digits";
			}
			double part2=calculateroot(dividingno);
			double sqroot=part1/part2;
		//	System.out.println(sqroot);
			sqroot1=round(sqroot,precision);
//System.out.println(sqroot1);
		}
		else{
			int num = 0;
			try {
				
				num=Integer.parseInt(input);
				
			} catch (Exception e) {
				
				return "Please enter a integer or a float input";
				
			}
			
			if (num<0) {
				num = num*(-1);
				negativeflag = true;
			}
			
			double sqroot=calculateroot(num);
			if (sqroot== -12) {
				return "Enter 10 or less digits";
			}
			int lentemp=(Integer.toString((int) sqroot)).length();
	//		System.out.println(sqroot);
			sqroot1=round(sqroot,precision);
//			System.out.println(sqroot1);

		}
		if (negativeflag) {
			return "-"+ sqroot1;
		} else {
			return sqroot1;
		}
		
		} catch (Exception e) {
			
			return "CubeRoot: Input Error";
			
		}

	}//main

	private static String insertCommas(String str)
	{
		if(str.length() < 4){
			return str;
		}
		return insertCommas(str.substring(0, str.length() - 3)) + "," + str.substring(str.length() - 3, str.length());
	}

	static double calculateroot(int num){
		int cnum=0;
		double temp=0;
		int n=num;
		int[] digArray=new int[100];
		float digit=0;
		long remainder=0;
		int divisor=0;
		long[] fromfunc=new long[2];
		String commastr=insertCommas(Integer.toString(num));
		String[] commaArray=commastr.split(",");
		while (n!=0){
			digArray[(int) digit]=(int) (n%10);
			digit++;
			n=n/10;
		}
		
		if (digit>10) {
			double d = -12;
			return d;
		} 
		
		if((digit%3)==0){
			cnum=(digArray[(int) (digit-1)]*100)+(digArray[(int) (digit-2)]*10)+digArray[(int) (digit-3)];}
		else if ((digit%3==1))
		{
			cnum=(digArray[(int) (digit-1)]);}
		else {
			cnum=(digArray[(int) (digit-1)]*10)+digArray[(int) (digit-2)];
		}
		
		if (cnum==1) {
			temp = 1;
		} else {
			
			for (int i = 1; i <=cnum/2; i++) {
				if ((i*i*i)<= cnum){
					temp=i;
				}else{
					break;}
			}
			
		}

		
		remainder= (int) (cnum-Math.pow(temp, 3));

		for (int j = 1; j < commaArray.length; j++) {
			remainder=(((int)remainder)*1000)+Integer.parseInt(commaArray[j]);
			divisor=(int) (3*Math.pow(temp, 2)*100);
			fromfunc=tosubtract(divisor,remainder,temp);
			temp= ((temp*10)+fromfunc[1]);
			remainder=fromfunc[0];
		}
		int c=0;
		int lentemp=(Integer.toString((int) temp)).length();
		n=4;
		while (n>0){
			c++;
			String commandArray ="000";
			String newremainder=Long.toString(remainder)+commandArray;
			remainder=Long.parseLong(newremainder);
			divisor=(int) (3*Math.pow(temp, 2)*100);
			fromfunc=tosubtract(divisor,remainder,temp);
			temp= ((temp*10)+fromfunc[1]);
			remainder=fromfunc[0];
			lentemp++;
			n--;
		}

		if(c>0){
			temp=(float) (temp/Math.pow(10, c));
		}

		else
		{
			temp=temp;
		}
		return temp;



	}//main


	private static long[] tosubtract(int divisor,long remainder,double temp) {
		int d=1;
		double div=0;
		long[] toreturn=new long[2];
		int tocmp=(int) (remainder/divisor);
		int originaldivisor=divisor;
		while ((d<tocmp) &&(d<=9)){
			d++;
			if(d==10){
				d=9;
				break;
			}
		}
		div=3*temp*d*10;
		divisor=(int) (divisor+div+Math.pow(d, 2));

		while ((d*divisor)>remainder){
			d=d-1;
			div=3*temp*d*10;
			divisor=(int) (originaldivisor+div+Math.pow(d, 2));
		}
		remainder=remainder-(d*divisor);
		toreturn[0]=remainder;
		toreturn[1]=d;
		return toreturn;
	}


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


	public String serviceAgreement() throws RemoteException {
		String cost=null;
		String S=null;
		try {
			Properties tempobj3 = new Properties();
			InputStream tempfileobj = new FileInputStream("cuberootconfig.ini");
			tempobj3.load(tempfileobj);
			cost= tempobj3.getProperty("cost");
			S= tempobj3.getProperty("servicegurantee");
			tempfileobj.close();
		}
		catch(IOException e){
			System.out.println("connot open config cuberootconfig.ini file");
		}
		return (cost+":"+S);
	}
	
}
