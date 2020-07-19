import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private int PORT;
	private String dest;	//destination folder
	
	public Server(int port, String dest) {
		this.PORT = port;
		this.dest = dest;
	}
	
	private void init() {
		System.out.println("The Server is running ...");
		try {
			ServerSocket ss = new ServerSocket(PORT);
			
			while(true) {
				Socket s = ss.accept();
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(s.getInputStream());
				new DealWithClient(out, in, dest).start();
				System.out.println("DEAL WITH CLIENT criado");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
		String os = System.getProperty("os.name");
		int p = 8080;
		String dest;
		if (os.equals("Windows 10")) {
			dest = System.getProperty("user.home") + "\\Desktop\\AppDoAntonioSERVER";
		}
		else {
			dest = System.getProperty("user.home") + "/Desktop/AppDoAntonioSERVER";
		}
		new File(dest).mkdirs();
		new Server(p, dest).init();
	}

}
