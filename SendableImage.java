import java.io.Serializable;

public class SendableImage implements Serializable {
	private byte[] image;
	private String name;
	
	public SendableImage (byte[] image, String name) {
		this.image = image;
		this.name = name;
	}
	
	public byte[] getImage() {
		return image;
	}
	
	public String getName() {
		return name;
	}

}
