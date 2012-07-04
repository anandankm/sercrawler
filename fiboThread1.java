
public class fiboThread1 extends Thread {
    Buffer upperBuf;
	public String upperfibval="1";
	static String toprint1=null;
	//GoldenRatio sr=new GoldenRatio();
	//test sr=new test();
	public fiboThread1 (Buffer buf) throws InterruptedException {
		upperBuf = buf;
	
		}

	public void run() {
		
		
		try {
			upperBuf.put("1");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while(true){
			try {
				Thread.sleep(server.fiboThreadSleep); // sleep for a randomly chosen time
			} catch (InterruptedException e) {return;}

			try {
				String val= upperBuf.get(); //starting from 1, not 0
		//		String upperfibval=addString(upperfibval,val);
//				System.out.println(upperfibval);
				upperfibval=addStringhex(val,upperfibval);
				
				server.Lockupperfib.writeLock().lock();
				
				try {
					
					server.upperfib=upperfibval;
					
				} finally {
					
					server.Lockupperfib.writeLock().unlock();
				}
				
				
				
				//t.fiblow=upperfibval;
				//System.out.println("Upper fibo Val:..." + upperfibval);
				upperBuf.put(upperfibval); 
			} catch (InterruptedException e) {return;}
			
		

		}
	}

	private String addStringhex(String a, String b) {
		StringBuffer sb=new StringBuffer();
        int len=0;
		
		
		if(a.length()>=b.length()){
		   len=a.length()+1;
		}
		else{
			len=b.length()+1;
		}
		String[] g=new String[len];
		
		//String str="";
		  
		String val1=padd(a,len);
		String val2=padd(b,len);
	//	System.out.println(len);
	//	System.out.println(val1.length());
		for (int j = len-1; j >=0; j--) {
			
			int temp1;
			int temp2;
			int temp3;		
			int tocarry=0;
			String carry=null;
			String v1=(new Character(val1.charAt(j))).toString();
			String v2=(new Character(val2.charAt(j))).toString();
			
			temp1=Integer.parseInt(v1,16);
		//	System.out.println(temp1);

			temp2=Integer.parseInt(v2,16);
		//	System.out.println(temp2);
			try{
			tocarry=Integer.parseInt(carry,16);}
			catch(NumberFormatException y){
				tocarry=0;
			}
			//System.out.println(tocarry);
			
			temp3=temp1+temp2+tocarry;
			
			String str= (Integer.toHexString(temp3) );
		//	System.out.println(str);
			
			if(((str.length()%2))==0){
				 carry =(new Character(str.charAt(0))).toString();
				g[j] = (new Character(str.charAt(1))).toString();
				}
			else{	g[j] = (new Character(str.charAt(0))).toString();
				}
		
			if(g[j].equalsIgnoreCase(null))
				g[j]="0";
		//System.out.println(g[j]);
	
		sb.append(g[j]);
		}

		String toreverse=sb.toString();
	//	System.out.println("aj''");
	//	System.out.println((toreverse.charAt(len-1)));
		if((toreverse.charAt(len-1))=='0'){
			toreverse=toreverse.substring(0,(len-1));
		}
		 int i, length = toreverse.length();
		    StringBuffer dest = new StringBuffer(length);
		    for (i = (length - 1); i >= 0; i--)
		      dest.append(toreverse.charAt(i));
		
		
//System.out.println(dest.toString());
return dest.toString();

	}
	
	
	private static String padd(String a, int len) {
		int temp;
	      temp = len-a.length();
	   //     System.out.println("Length: "+temp);
	         for(int i=0;i<temp;i++)
	         {
	            a="0"+a;
	         }
	     //    System.out.println(a);
		return a;
	}
	


}