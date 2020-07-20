import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Conversion {

	public static byte[] toByteArray (BufferedImage image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", baos);
			baos.flush();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] imagemEmBytes = baos.toByteArray();
		return imagemEmBytes;
	}
	
	public static BufferedImage toBufferedImage (byte[] image) throws IOException {
		InputStream in = new ByteArrayInputStream(image);
		BufferedImage buff = ImageIO.read(in);
		return buff;
	}
}
