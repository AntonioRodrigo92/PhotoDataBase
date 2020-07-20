import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class DealWithClient extends Thread {
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String dest;
	private BloquingQueue<Request> requestQueue;
	
	public DealWithClient(ObjectOutputStream out, ObjectInputStream in, String dest) {
		this.out = out;
		this.in = in;
		this.dest = dest;
		this.requestQueue = new BloquingQueue<>();
	}
	
	public void run() {
		new sendAvailablePhotos().start();
		new ClientRequest().start();
		new ClientResponse().start();
	}
	
	private class sendAvailablePhotos extends Thread {
		public void run() {
			try {
				while (true) {
					List<String> available = FileDealer.getAvailableFiles(dest);
					sendToClient(available);
					sleep(30000);
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//receber do Cliente
	private class ClientRequest extends Thread {
		public void run() {
			try {
				while (true) {
					Object obj = (Object)in.readObject();
					if (obj instanceof Request) {
						Request r = (Request)obj;
						requestQueue.offer(r);
					}
					if (obj instanceof Response) {
						Response res = (Response) obj;
						for (SendableImage si : res.getImages()) {
							String longName = si.getName();
							String[] name;
							if (longName.contains("\\")) {
								name = longName.split(Pattern.quote("\\"));
							}
							else {
								name = longName.split(Pattern.quote("/"));
							}
							BufferedImage img = Conversion.toBufferedImage(si.getImage());
							FileDealer.saveImage(img, name[name.length - 1], dest);
						}
					}
				}
			}
			catch (IOException | ClassNotFoundException e) {
				System.out.println("Cliente desligou-se");
			}
		}
	}
	
	//enviar Ficheiro ao Cliente
	private class ClientResponse extends Thread {
		public void run() {
			try {
				while (true) {
					try {
						Request req = requestQueue.poll();
						List<File> objectList = FileDealer.getFiles(req, dest);	//lista de objetos	
						Response res = new Response();
						for (File s : objectList) {
							BufferedImage img = ImageIO.read(s);
							SendableImage si = new SendableImage(Conversion.toByteArray(img), s.toString());
							res.addSendableImage(si);
						}
						sendToClient(res);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			catch (IOException e) {
				System.out.println("Cliente desligou-se");
			}
		}
	}
	
	private synchronized void sendToClient (Object obj) throws IOException {
		out.writeObject(obj);
	}

}
