import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class FileDealer {
	
	
	public static List<String> getAvailableFiles (String dest) {
		List<String> lista = new ArrayList<>();
		
		File folder = new File(dest);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile()) {
		        lista.add(file.getName());
		    }
		}
		return lista;
	}
	
	public static List<File> getFiles (Request req, String dest) {
		List<File> lista = new ArrayList<>();

		File folder = new File(dest);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile() && req.contains(file.getName())) {
		        lista.add(file);
		    }
		}
		
		return lista;
	}
	
	public synchronized static void saveImage (BufferedImage img, String name, String dest) {
		new File(dest).mkdirs();
		File file;
		if (dest.contains("\\")) {
			file = new File(dest + "\\" + name);
		}
		else {
			file = new File(dest + "/" + name);
		}
		try {
			ImageIO.write(img, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
