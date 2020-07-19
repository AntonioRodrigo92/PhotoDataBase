import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class Client {
	private int PORT;
	private String add;
	private String dest;						//destination folder
	private Socket socket;
	private JList<String> listaEsquerda;
	private JList<String> listaDireita;
	private HashMap<String, BufferedImage> mapaImagens;
	private File[] files;
	private JFrame frame;
	private JLabel viewer;
	private ObjectOutputStream outServer;
	private ObjectInputStream inServer;
	
	public Client(String add, int port, String dest) {
		this.PORT = port;
		this.add = add;
		this.dest = dest;
		this.mapaImagens = new HashMap<>();
		try {
			ConnectToServer();
			System.out.println("CONNECTED!");
			BuildGui();
			new receiveFromServer().start();
		} catch (IOException e) {
			ErrorWindow.buildWarning("Server is not available... try again later!");
		}
	}
	
	private void BuildGui() {
		frame = new JFrame("File Share App");
		viewer = new JLabel();
		String[] espaco = {"                                         "};
		listaEsquerda = new JList<String>(espaco);
		listaDireita = new JList<String>(espaco);
		frame.setLayout(new BorderLayout());
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		addFrameContent();
	}
	
	private void ConnectToServer() throws IOException {
		InetAddress address = InetAddress.getByName(add);
		socket = new Socket (address, PORT);
		inServer = new ObjectInputStream(socket.getInputStream());
		outServer = new ObjectOutputStream(socket.getOutputStream());
	}
	
	private class receiveFromServer extends Thread {
		public void run() {
			try {
				while (true) {
					Object obj = inServer.readObject();
					if (obj instanceof Response) {
						Response res = (Response) obj;
						for (SendableImage si : res.getImages()) {
							String longName = si.getName();
							String pc;
							if (longName.contains("\\")) {
								pc = "\\";
							}
							else {
								pc = "/";
							}
							String[] name = longName.split(Pattern.quote(pc));
							BufferedImage img = Conversion.toBufferedImage(si.getImage());
							mapaImagens.put(name[name.length - 1], img);
							FileDealer.saveImage(img, name[name.length - 1], dest);
						}
						SwingUtilities.invokeLater(doAdicionarPainelResposta);
						SwingUtilities.invokeLater(doAdicionarImagens);
					}
					else {
						ArrayList<String> availablePhotos = (ArrayList<String>) obj;
						String[] vector = new String[availablePhotos.size()];
						for (int i = 0; i < vector.length; i++) {
							vector[i] = availablePhotos.get(i);
						}
						listaEsquerda.setListData(vector);
					}
				}
			} 
			catch(ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				Reconnect();
			}

		}
	}
	
	private void addFrameContent() {
		JPanel painel = new JPanel();
		frame.add(listaEsquerda, BorderLayout.WEST);
		frame.add(listaDireita, BorderLayout.EAST);
		frame.add(painel, BorderLayout.SOUTH);
		JPanel painelDePastas = new JPanel();	
		
		painel.setLayout(new GridLayout(1, 2));
		painelDePastas.setLayout(new BorderLayout());		
		JButton sch = new JButton("Search");
		JButton ul = new JButton("Upload");
		JButton dl = new JButton("Download");	

		painel.add(dl);
		dl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new downloadFile().start();
			}
		});
		
		painelDePastas.add(sch, BorderLayout.NORTH);
		sch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(doProcurarImagens);
			}
		});
		
		painelDePastas.add(ul,BorderLayout.SOUTH);	
		ul.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new uploadFile().start();
			}
		});
		painel.add(painelDePastas);
	}
	
	private class downloadFile extends Thread {
		public void run() {	
			ArrayList<String> reqList = (ArrayList<String>) listaEsquerda.getSelectedValuesList();
			Request req = new Request(reqList);
			try {
				mapaImagens.clear();
				viewer.setIcon(null);
				sendToServer(req);
			} catch (IOException e) {

			}
		}
	}
	
	final Runnable doProcurarImagens = new Runnable() {
		public void run() {
			JFileChooser jfc = new JFileChooser();
			jfc.setMultiSelectionEnabled(true);
			 jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			 
			 jfc.addChoosableFileFilter(new ImageFilter());
			 jfc.setAcceptAllFileFilterUsed(false);
			 int returnValue = jfc.showOpenDialog(null);
			
			 if (returnValue == JFileChooser.APPROVE_OPTION) {
				 files = jfc.getSelectedFiles();
			 }
		}
	};
	
	final Runnable doAdicionarImagens = new Runnable() {
		public void run() {
			listaDireita.addListSelectionListener(new ListSelectionListener(){
				@Override
				public void valueChanged(ListSelectionEvent e) {
					String selectedValue = listaDireita.getSelectedValue();
					if (selectedValue != null) {
						ImageIcon icon = new ImageIcon (mapaImagens.get(selectedValue));
						viewer.setIcon(icon);
						frame.add(viewer);
					}
				}
			});
		}
	};
	
	final Runnable doAdicionarPainelResposta = new Runnable() {
		public void run() {
			String[] nomes = new String[mapaImagens.size()];
			int i = 0;
			for (String s : mapaImagens.keySet()) {
				nomes[i] = s;
				i++;
			}
		
			listaDireita.setListData(nomes);
		}
	};
	
	private class uploadFile extends Thread {
		public void run() {	
			Response resp = new Response();
			for (int i = 0; i < files.length; i++) {
				try {
					byte[] imagem = Conversion.toByteArray(ImageIO.read(files[i]));
					String nome = files[i].getName();
					SendableImage si = new SendableImage(imagem, nome);
					resp.addSendableImage(si);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			try {
				outServer.writeObject(resp);
			} catch (IOException e1) {
				
			}
		}
	}
	
	private void Reconnect() {
		int i = 0;
		while (i < 10) {
			try {
				Thread.sleep(3000);
				ConnectToServer();
				new receiveFromServer().start();
				break;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
		if (i == 10) {
			ErrorWindow.buildWarning("Server is not available... try again later!");
		}
	}

	private synchronized void sendToServer (Object obj) throws IOException {
		outServer.writeObject(obj);
	}
	
	private void init() {
		frame.setVisible(true);
	}
	
	public static void main (String[] args) {
		String os = System.getProperty("os.name");
		String end = "0.0.0.0";
		int p = 8080;
		String dest;
		if (os.equals("Windows 10")) {
			dest = System.getProperty("user.home") + "\\Desktop\\AppDoAntonio";
		}
		else {
			dest = System.getProperty("user.home") + "/Desktop/AppDoAntonio";
		}
		Client c = new Client(end, p, dest);
		c.init();
	}
}
