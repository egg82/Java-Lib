package ninja.egg82.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class FifoThreadFactory implements ThreadFactory {
    // vars
    private AtomicInteger dec = new AtomicInteger(Thread.MAX_PRIORITY);
    private ThreadFactory coreFactory = null;

    // constructor
    public FifoThreadFactory(ThreadFactory coreFactory) {
        this.coreFactory = coreFactory;
    }

    // public
    public Thread newThread(Runnable r) {
        Thread t = coreFactory.newThread(r);
        t.setPriority(dec.getAndUpdate(update));
        return t;
    }

    // private
    private IntUnaryOperator update = new IntUnaryOperator() {
        public int applyAsInt(int operand) {
            return (operand > Thread.MIN_PRIORITY) ? operand - 1 : Thread.MAX_PRIORITY;
        }
    };
}
