package xxl.tests.util.reflect;

import java.net.InetAddress;
import java.net.UnknownHostException;

import xxl.core.cursors.Cursor;
import xxl.core.util.reflect.SocketProxy;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SocketProxy.
 */
public class TestSocketProxy {

	/**
	 * Usage Example for SocketProxy.
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String args[]) {
		Cursor cursor;
		int numberOfElements = 1000;
		long t1,t2;
		
		int port=4289;
		if (args.length>0)
			port = Integer.parseInt(args[0]);
		System.out.println("Port: "+port);
		
		switch (args.length) {
			case 2:
			{
				if (!args[1].equalsIgnoreCase("server"))
					System.out.println("Error in parameters!");
				else {
					System.out.println("Starting server");
					SocketProxy.newServerInstance(port, new xxl.core.cursors.sources.Enumerator(numberOfElements),false,System.out);
					System.out.println("Shutdown server");
				}
				break;
			}
			case 3:
			{
				if (!args[1].equalsIgnoreCase("client"))
					System.out.println("Error in parameters!");
				else {
					InetAddress adr = null;
					try {
						adr = InetAddress.getByName(args[2]);
					}
					catch (UnknownHostException e) {}

					Object [] proxy = SocketProxy.newClientInstance(adr,port,"xxl.core.cursors.Cursor");
					cursor = (Cursor) proxy[0];
					SocketProxy sp = (SocketProxy) proxy[1];
					
					System.out.println("Start working...");
					
					t1 = System.currentTimeMillis();
					xxl.core.cursors.Cursors.consume(cursor);
					t2 = System.currentTimeMillis();
					
					System.out.println("Time for consuming "+numberOfElements+" elements of an enumerator via proxy: "+(t2-t1)+"ms");

					System.out.println("Sending shutdown signal");
					sp.shutdown();
					System.out.println("End of program");
				}
				break;
			}
			case 0:
			case 1:
			{
				SocketProxy sp;
				Object [] proxy;
				
				System.out.println("Example for SocketProxy");
				System.out.println();
				System.out.println("This class can optionally be called with 0 to 3 parameters:");
				System.out.println();
				System.out.println("1. port number");
				System.out.println("2. 'client' or 'server'");
				System.out.println("3. (only in 'client'-mode) name of the server (e.g. localhost or ip-address)");
				System.out.println();

				System.out.println("Start a server object");
				SocketProxy.newServerInstance(port, new xxl.core.cursors.sources.Enumerator(10),true,null);
				
				InetAddress adr = null;
				try {
					adr = InetAddress.getLocalHost();
				}
				catch (UnknownHostException e) {}
				
				System.out.println("Start a client that uses this server object");
				proxy = SocketProxy.newClientInstance(adr,port,"xxl.core.cursors.Cursor");
				cursor = (Cursor) proxy[0];
				sp = (SocketProxy) proxy[1];
				// The object fulfils the Cursor interface.
				
				System.out.println("Working with the cursor... (output)");
				while (cursor.hasNext())
					System.out.println(cursor.next());
				cursor.close();
				
				sp.shutdown();
				System.out.println();
				
				// Performance Test
				System.out.println("Performance test");
				cursor = new xxl.core.cursors.sources.Enumerator(numberOfElements);
				
				t1 = System.currentTimeMillis();
				xxl.core.cursors.Cursors.consume(cursor);
				t2 = System.currentTimeMillis();
				System.out.println("Time for consuming "+numberOfElements+" elements of an enumerator: "+(t2-t1)+"ms");
		
				SocketProxy.newServerInstance(port, new xxl.core.cursors.sources.Enumerator(numberOfElements),true,System.out);
				proxy = SocketProxy.newClientInstance(adr,port,"xxl.core.cursors.Cursor");
				cursor = (Cursor) proxy[0];
				sp = (SocketProxy) proxy[1];
				
				t1 = System.currentTimeMillis();
				xxl.core.cursors.Cursors.consume(cursor);
				t2 = System.currentTimeMillis();
				System.out.println("Time for consuming "+numberOfElements+" elements of an enumerator via proxy (locally): "+(t2-t1)+"ms");

				sp.shutdown();
			}
			break;
		}
	}

}
