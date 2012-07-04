
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Readwritelock {

	static synchronized public BufferedReader readfile(File afile) throws IOException {
		BufferedReader input = null;
		try{
			input =  new BufferedReader(new FileReader(afile));
		}
		catch( IOException e){
			System.out.println("Cannot open file ");
		}
		return input;
	}


	static synchronized public void writefile(File fname, String aContents, boolean append )
	throws FileNotFoundException, IOException, InterruptedException {

		BufferedWriter output = null;
		try {
			if (fname == null) {
				throw new IllegalArgumentException("File should not be null.");
			}
			if (!fname.exists()) {
				throw new FileNotFoundException ("File not exist: " + fname);
			}
			if (!fname.isFile()) {
				throw new IllegalArgumentException("Should not be a directory: " + fname);
			}
			if (!fname.canWrite()) {
				throw new IllegalArgumentException("File cannot be written: " + fname);
			}

			//write with default encoding
			output = new BufferedWriter(new FileWriter(fname,append));
			output.write(aContents );
			output.newLine(); 
		}
		finally {
			if(output!=null){
				output.close();
			}
		}		
	}
}
