package jline.solvers.ssa.state;

import java.util.*;

import jline.util.Pair;

import static java.lang.Math.min;

public class ProcessorSharingBuffer extends StateCell {
    protected List<Integer> classBuffer;
    protected Random random;
    protected int nServers;

    public ProcessorSharingBuffer(Random random, int nServers) {
        this.classBuffer = new ArrayList<Integer>();
        this.random = random;
        this.nServers = nServers;
    }

    public int peakBuffer() {
        return this.classBuffer.get(this.random.nextInt(this.classBuffer.size()));
    }

    public int peakBufferAt(int atPoint) {
        Iterator<Integer> dequeIterator = classBuffer.iterator();
        for (int i = 0; i < atPoint; i++) {
            dequeIterator.next();
        }
        return dequeIterator.next();
    }

    public int popFromBuffer() {
        if (this.classBuffer.size() == 0) {
            return -1;
        }

        int bufferIdx = this.random.nextInt(this.classBuffer.size());
        int outClass = this.classBuffer.get(bufferIdx);
        this.classBuffer.remove(outClass);
        return  outClass;
    }

    public void addToBuffer(int classIdx) {
        this.classBuffer.add(classIdx);
    }

    public void addNToBuffer(int classIdx, int n) {
        for (int i = 0; i < n; i++) {
            this.classBuffer.add(classIdx);
        }
    }

    public int getInService(int classIdx) {
        int nInservice = 0;
        Iterator<Integer> dequeIterator = classBuffer.iterator();
        int nIter = 0;

        while (dequeIterator.hasNext()) {
            if (nIter >= this.nServers) {
                break;
            }

            nIter++;

            if (dequeIterator.next() == classIdx) {
                nInservice++;
            }
        }

        return nInservice;
    }

    public boolean isEmpty() {
        return this.classBuffer.isEmpty();
    }

    public void removeFirstOfClass(int classIdx) {
        if (this.getInQueue(classIdx) == 0){
            return;
        }  else if (this.classBuffer.size() == 0) {
            return;
        }
        while (true) {
            int bufferIdx = this.random.nextInt(this.classBuffer.size());
            if (this.classBuffer.get(bufferIdx) != classIdx) {
                continue;
            }
            this.classBuffer.remove(bufferIdx);
            return;
        }
    }

    public void removeNClass(int n, int classIdx) {
        n = min(n, this.classBuffer.size());
        while (n > 0) {
            int bufferIdx = this.random.nextInt(this.classBuffer.size());
            if (this.classBuffer.get(bufferIdx) != classIdx) {
                continue;
            }
            this.classBuffer.remove(bufferIdx);
            n--;
        }
    }

    public StateCell createCopy() {
        ProcessorSharingBuffer copyBuffer = new ProcessorSharingBuffer(this.random, this.nServers);
        copyBuffer.classBuffer = new ArrayList<Integer>(this.classBuffer);
        return copyBuffer;
    }

    public int getInQueue(int classIdx) {
        int acc = 0;
        Iterator<Integer> dequeIterator = classBuffer.iterator();
        while(dequeIterator.hasNext()) {
            if (dequeIterator.next() == classIdx) {
                acc++;
            }
        }

        return acc;
    }
}
