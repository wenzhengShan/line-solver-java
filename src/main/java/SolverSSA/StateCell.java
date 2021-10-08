package SolverSSA;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class StateCell {

    public StateCell() {
    }

    public abstract int peakBuffer();
    public abstract int peakBufferAt(int atPoint);
    public abstract int popFromBuffer();
    public abstract void addToBuffer(int classIdx);
    public abstract void addNToBuffer(int classIdx, int n);
    public abstract int getInService(int classIdx);
    public abstract boolean isEmpty();
    public abstract void removeFirstOfClass(int classIdx);
    public abstract void removeNClass(int n, int classIdx);
    public abstract StateCell createCopy();
    public abstract int getInQueue(int classIdx);
}
