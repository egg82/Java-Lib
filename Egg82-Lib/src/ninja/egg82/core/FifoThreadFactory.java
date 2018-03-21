package ninja.egg82.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class FifoThreadFactory implements ThreadFactory {
	//vars
	private AtomicInteger dec = new AtomicInteger(10);
	private ThreadFactory coreFactory = null;
	
	//constructor
	public FifoThreadFactory(ThreadFactory coreFactory) {
		this.coreFactory = coreFactory;
	}
	
	//public
	public Thread newThread(Runnable r) {
		Thread t = coreFactory.newThread(r);
		t.setPriority(dec.getAndUpdate(update));
		return t;
	}
	
	//private
	private IntUnaryOperator update = new IntUnaryOperator() {
		public int applyAsInt(int operand) {
			return (operand > 1) ? operand - 1 : 10;
		}
	};
}
