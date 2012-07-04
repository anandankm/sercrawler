
class Buffer {
	private String contents="0";
	private boolean empty = true;

	public synchronized void put (String i) throws InterruptedException { 
		while (empty == false) { 	//wait till the buffer becomes empty
			try { wait(); }
			catch (InterruptedException e) {throw e;}
		}
		contents = i;
		empty = false;
		notify();
	} 

	public synchronized String get () throws InterruptedException {
		while (empty == true)  {	//wait till something appears in the buffer
			try { 	wait(); }
			catch (InterruptedException e) {throw e;}
		}
		empty = true;
		notify();
		String val = contents;

		return val;
	}

}