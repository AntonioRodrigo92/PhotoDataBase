import java.util.ArrayDeque;
import java.util.Deque;

public class BloquingQueue <T> {
	private Deque<T> queue;
	
	public BloquingQueue() {
		queue = new ArrayDeque<>();
	}
	
	public synchronized void offer (T t) {
		queue.addLast(t);
		notifyAll();
	}
	
	public synchronized T poll () throws InterruptedException {
		while (queue.size() <= 0) {
			wait();
		}
		return queue.poll();
	}

}
