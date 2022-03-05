package jline.solvers.ssa;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class FCFSClassBuffer extends StateCell {
    protected Deque<Integer> deque;
    protected int nServers;
    protected int[] inQueue;
    protected int[] inService;
    protected int totalInQueue;
    protected int totalInService;

    public FCFSClassBuffer(int nClasses, int nServers) {
        this.deque = new ArrayDeque<Integer>();
        this.nServers = nServers;
        this.inQueue = new int[nClasses];
        this.inService = new int[nClasses];

        for (int i = 0; i < nClasses; i++) {
            this.inService[i] = 0;
            this.inQueue[i] = 0;
        }
        this.totalInQueue = 0;
        this.totalInService = 0;
    }

    public int peakBuffer() {
        return this.deque.peekFirst();
    }

    public int peakBufferAt(int atPoint) {
        Iterator<Integer> dequeIterator = deque.iterator();
        for (int i = 0; i < atPoint; i++) {
            dequeIterator.next();
        }
        return dequeIterator.next();
    }

    public int popFromBuffer() {
        if (this.isEmpty()) {
            return -1;
        }

        int outClass = this.deque.removeFirst();

        if (this.totalInQueue <= this.nServers) {
            this.inService[outClass]--;
            this.totalInService--;
        }

        this.inQueue[outClass]--;
        this.totalInQueue--;

        return outClass;
    }

    public void addToBuffer(int classIdx) {
        if (this.totalInQueue < this.nServers) {
            this.inService[classIdx]++;
            this.totalInService++;
        }
        this.inQueue[classIdx]++;
        this.totalInQueue++;

        this.deque.addLast(classIdx);
    }

    public void addNToBuffer(int classIdx, int n) {
        if (this.totalInQueue < this.nServers) {
            int shortfall = this.nServers - this.totalInQueue;
            this.inService[classIdx] += Math.min(shortfall, n);
            this.totalInService += Math.min(shortfall, n);
        }
        this.inQueue[classIdx] += n;
        this.totalInQueue += n;

        for (int i = 0; i < n; i++) {
            this.deque.addLast(classIdx);
        }
    }

    public int getInService(int classIdx) {
        Iterator<Integer> dequeIterator = deque.iterator();
        int nCt = 0;
        int acc = 0;
        while ((dequeIterator.hasNext()) && (nCt < this.nServers)) {
            if (dequeIterator.next() == classIdx) {
                acc++;
            }
            nCt++;
        }
        return acc;
        //return this.inService[classIdx];
    }

    public boolean isEmpty() {
        return this.totalInQueue == 0;
    }

    public void removeFirstOfClass(int classIdx) {
        Iterator<Integer> dequeIterator = deque.iterator();
        while (dequeIterator.hasNext()) {
            if (dequeIterator.next() == classIdx) {
                dequeIterator.remove();
                this.totalInQueue--;
                this.inQueue[classIdx]--;
                if (this.totalInQueue < this.nServers) {
                    this.inService[classIdx]--;
                    this.totalInService--;
                }
                return;
            }
        }
    }

    public void removeNClass(int n, int classIdx) {
        Iterator<Integer> dequeIterator = deque.iterator();
        int nRemoved = 0;

        while ((dequeIterator.hasNext()) && (n > 0)) {
            if (dequeIterator.next() == classIdx) {
                dequeIterator.remove();
                n--;
                nRemoved++;
            }
        }

        this.totalInQueue -= nRemoved;
        this.inQueue[classIdx] -= nRemoved;
        int nRemovedFromService = this.inService[classIdx] - Math.min(this.inService[classIdx], this.inQueue[classIdx]);
        this.inService[classIdx] -= nRemovedFromService;
        this.totalInService -= nRemovedFromService;
    }

    public StateCell createCopy() {
        FCFSClassBuffer copyBuffer = new FCFSClassBuffer(this.inQueue.length, this.nServers);
        copyBuffer.deque = new LinkedList<Integer>(this.deque);
        return copyBuffer;
    }

    public int getInQueue(int classIdx) {
        return this.inQueue[classIdx];
    }
}
