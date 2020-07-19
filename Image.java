import java.awt.image.BufferedImage;

public class Image {
	private BufferedImage image;
	private String name;
	
	public Image (BufferedImage image, String name) {
		this.image = image;
		this.name = name;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public String getName() {
		return name;
	}
	
}
