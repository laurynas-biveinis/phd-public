package xxl.tests.collections.queues;

import xxl.core.collections.queues.MemoryManageableListQueue;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MemoryManageableListQueue.
 */
public class TestMemoryManageableListQueue {
    
    public static void main(String[] args) {
        MemoryManageableListQueue<Integer> queue = new MemoryManageableListQueue<Integer>(8);
        queue.open();
        queue.assignMemSize(400);
        for (int i = 0; i < 100; i++) {
            queue.enqueue(new Integer(i));
            System.out.println(queue.size());
        }
        queue.close();
    }

}
