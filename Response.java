import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Response implements Serializable {
	private List<SendableImage> images;
	
	public Response() {
		this.images = new ArrayList<>();
	}
	
	public Response (List<SendableImage> images) {
		this.images = images;
	}
	
	public List<SendableImage> getImages() {
		return images;
	}
	
	public void addSendableImage (SendableImage img) {
		images.add(img);
	}
}
