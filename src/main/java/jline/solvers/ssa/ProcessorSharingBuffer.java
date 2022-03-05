package jline.solvers.ssa;

import jline.lang.Pair;

import java.util.*;

public class ProcessorSharingBuffer extends StateCell {
    protected PriorityQueue<Pair<Double, Integer>> classBuffer;
    protected Random random;
    protected int nServers;

    public ProcessorSharingBuffer(Random random, int nServers) {
        this.classBuffer = new PriorityQueue<Pair<Double, Integer>>();
        this.random = random;
        this.nServers = nServers;
    }

    public int peakBuffer() {
        return this.classBuffer.peek().getRight();
    }

    public int peakBufferAt(int atPoint) {
        Iterator<Pair<Double, Integer>> dequeIterator = classBuffer.iterator();
        for (int i = 0; i < atPoint; i++) {
            dequeIterator.next();
        }
        return dequeIterator.next().getRight();
    }

    public int popFromBuffer() {
        return this.classBuffer.poll().getRight();
    }

    public void addToBuffer(int classIdx) {
        this.classBuffer.add(new Pair(this.random.nextDouble(), classIdx));
    }

    public void addNToBuffer(int classIdx, int n) {
        for (int i = 0; i < n; i++) {
            this.classBuffer.add(new Pair(this.random.nextDouble(), classIdx));
        }
    }

    public int getInService(int classIdx) {
        int nInservice = 0;
        Iterator<Pair<Double, Integer>> dequeIterator = classBuffer.iterator();
        int nIter = 0;

        while (dequeIterator.hasNext()) {
            if (nIter >= this.nServers) {
                break;
            }

            nIter++;

            if (dequeIterator.next().getRight() == classIdx) {
                nInservice++;
            }
        }

        return nInservice;
    }

    public boolean isEmpty() {
        return this.classBuffer.isEmpty();
    }

    public void removeFirstOfClass(int classIdx) {
        Iterator<Pair<Double, Integer>> cbIterator = classBuffer.iterator();
        while (cbIterator.hasNext()) {
            if (cbIterator.next().getRight() == classIdx) {
                cbIterator.remove();
                return;
            }
        }
    }

    public void removeNClass(int n, int classIdx) {
        Iterator<Pair<Double, Integer>> cbIterator = classBuffer.iterator();

        while ((cbIterator.hasNext()) && (n > 0)) {
            if (cbIterator.next().getRight() == classIdx) {
                cbIterator.remove();
                n--;
            }
        }
    }

    public StateCell createCopy() {
        ProcessorSharingBuffer copyBuffer = new ProcessorSharingBuffer(this.random, this.nServers);
        copyBuffer.classBuffer = new PriorityQueue<Pair<Double, Integer>>(this.classBuffer);
        return copyBuffer;
    }

    public int getInQueue(int classIdx) {
        int acc = 0;
        Iterator<Pair<Double, Integer>> dequeIterator = classBuffer.iterator();
        while(dequeIterator.hasNext()) {
            if (dequeIterator.next().getRight() == classIdx) {
                acc++;
            }
        }

        return acc;
    }
}
