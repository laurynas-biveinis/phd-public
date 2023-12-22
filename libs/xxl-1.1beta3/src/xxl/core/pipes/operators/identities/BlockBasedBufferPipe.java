/*
 *
 */
package xxl.core.pipes.operators.identities;

import java.util.Arrays;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.pipes.memoryManager.MemoryMonitorable;
import xxl.core.pipes.processors.SchedulableProcessor;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;


/**
 *
 */
public class BlockBasedBufferPipe<I> extends AbstractBufferPipe<I,I[]>{

	protected I[] next;
	protected boolean signalledDone = false;
	
	public BlockBasedBufferPipe(Queue<I> buffer, int batchSize, int objectSize) {
		super(buffer, batchSize, objectSize);
	}
	
	public BlockBasedBufferPipe(Source<? extends I>[] sources, int[] sourceIDs, Queue<I> buffer, int batchSize, int objectSize) {
		super(sources, sourceIDs, buffer, batchSize, objectSize);
	}

	@SuppressWarnings("unchecked")
	public BlockBasedBufferPipe(Source<? extends I> source, int sourceID, Queue<I> buffer, int batchSize, int objectSize) {
		this(new Source[]{source}, new int[]{sourceID}, buffer, batchSize, objectSize);
	}
	
	@SuppressWarnings("unchecked")
	public BlockBasedBufferPipe(Source<? extends I> source, int sourceID, int batchSize, int objectSize) {
		this(new Source[]{source}, new int[]{sourceID}, new ListQueue<I>(), batchSize, objectSize);
	}
	
	@SuppressWarnings("unchecked")
	public BlockBasedBufferPipe(Source<? extends I> source, int sourceID, int batchSize) {
		this(new Source[]{source}, new int[]{sourceID}, new ListQueue<I>(), batchSize, MemoryMonitorable.SIZE_UNKNOWN);
	}
	
	@SuppressWarnings("unchecked")
	public BlockBasedBufferPipe(Source<? extends I> source, int batchSize) {
		this(new Source[]{source}, new int[]{DEFAULT_ID}, new ListQueue<I>(), batchSize, MemoryMonitorable.SIZE_UNKNOWN);
	}
	
	@SuppressWarnings("unchecked")
	public BlockBasedBufferPipe(Source<? extends I> source) {
		this(new Source[]{source}, new int[]{DEFAULT_ID}, new ListQueue<I>(), 1, MemoryMonitorable.SIZE_UNKNOWN);
	}	
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.identities.AbstractBufferPipe#execute()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		if (isClosed) return;
		synchronized(buffer) {
			int size;
			if (buffer.size() >= batchSize)
				size = batchSize;
			else 
				if (isDone) size = buffer.size();
				else return;
			if (size > 0) {
				next = (I[])new Object[size];
				for (int i = 0; i < size; i++) 
					next[i] = buffer.dequeue();
				transfer(next);
			}
			if (isDone && buffer.size() == 0) {
				signalDone();
				signalledDone = true;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.identities.AbstractBufferPipe#isFinished()
	 */
	@Override
	public boolean isFinished() {
		processingRLock.lock();
		try {
			return isClosed || signalledDone;
		}
		finally {
			processingRLock.unlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		final BlockBasedBufferPipe<Double> buffer = new BlockBasedBufferPipe(
				new Enumerator(100, 0), 4);

		AbstractSink print = new AbstractSink(buffer) {
			@Override
			public void processObject(Object o, int sourceID)
					throws IllegalArgumentException {
				System.out.println(Arrays.toString((Object[]) o));
			}
		};

		SchedulableProcessor sp = new SchedulableProcessor(0) {
			@Override
			public void process() {
				buffer.execute();
				if (buffer.isFinished())
					terminate();
			}

		};
		sp.start();

		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(print);
		exec.startQuery(print);
	}
	
}
