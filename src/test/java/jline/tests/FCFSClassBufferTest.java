package jline.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import jline.solvers.ssa.*;
import jline.util.Pair;

class FCFSClassBufferTest {
    private FCFSClassBuffer classBuffer;
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        this.classBuffer = new FCFSClassBuffer(50,10);
    }


    @org.junit.jupiter.api.Test
    void testAddToBuffer() {
        this.classBuffer.addToBuffer(1);
        this.classBuffer.addToBuffer(2);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.popFromBuffer(),1);
        assertEquals(this.classBuffer.popFromBuffer(),2);
        assertEquals(this.classBuffer.popFromBuffer(),3);
    }


    @org.junit.jupiter.api.Test
    void testAddNToBuffer() {
        Random random = new Random();
        List<Pair<Integer, Integer>> addList = new ArrayList<Pair<Integer,Integer>>();
        for (int i = 0; i < 10; i++) {
            int classIdx = random.nextInt(50);
            int nToAdd = random.nextInt(50);
            addList.add(new Pair<Integer, Integer>(classIdx, nToAdd));
            this.classBuffer.addNToBuffer(classIdx, nToAdd);
        }

        //Collections.reverse(addList);

        for (Pair<Integer, Integer> curPair : addList) {
            int classIdx = curPair.getLeft();
            int nAdded = curPair.getRight();

            for (int i = 0; i < nAdded; i++) {
                assertEquals(this.classBuffer.popFromBuffer(), classIdx);
            }
        }

        assertTrue(this.classBuffer.deque.size() == 0);
    }



    @org.junit.jupiter.api.Test
    void testGetInService() {
        this.classBuffer.addToBuffer(1);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),0);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(2);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),1);
        assertEquals(this.classBuffer.getInService(3),0);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(2);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),0);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),1);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),2);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),3);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),4);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),5);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),6);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),7);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),7);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),7);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),7);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInService(1),1);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),7);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),2);
        assertEquals(this.classBuffer.getInService(3),8);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),1);
        assertEquals(this.classBuffer.getInService(3),9);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),10);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),10);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),9);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),8);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),7);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),6);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),5);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),4);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),3);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),2);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),1);
        assertEquals(this.classBuffer.getInService(4),0);
        this.classBuffer.popFromBuffer();
        assertEquals(this.classBuffer.getInService(1),0);
        assertEquals(this.classBuffer.getInService(2),0);
        assertEquals(this.classBuffer.getInService(3),0);
        assertEquals(this.classBuffer.getInService(4),0);
    }

    @org.junit.jupiter.api.Test
    void testCreateCopy() {
        /*this.classBuffer.addToBuffer(4);
        this.classBuffer.addToBuffer(4);
        this.classBuffer.addToBuffer(4);
        this.classBuffer.addToBuffer(4);
        this.classBuffer.addToBuffer(3);
        this.classBuffer.addToBuffer(3);
        this.classBuffer.addToBuffer(3);
        this.classBuffer.addToBuffer(2);
        this.classBuffer.addToBuffer(2);
        this.classBuffer.addToBuffer(1);
        assertEquals(this.classBuffer.getInService(1, 15),1);
        assertEquals(this.classBuffer.getInService(2, 15),2);
        assertEquals(this.classBuffer.getInService(3, 15),3);
        assertEquals(this.classBuffer.getInService(4, 15),4);

        JobClassBuffer classBufferCopy = this.classBuffer.createCopy();
        classBufferCopy.addToBuffer(1);
        assertEquals(this.classBuffer.getInService(1, 15),1);
        assertEquals(this.classBuffer.getInService(2, 15),2);
        assertEquals(this.classBuffer.getInService(3, 15),3);
        assertEquals(this.classBuffer.getInService(4, 15),4);
        assertEquals(classBufferCopy.getInService(1, 15),2);
        assertEquals(classBufferCopy.getInService(2, 15),2);
        assertEquals(classBufferCopy.getInService(3, 15),3);
        assertEquals(classBufferCopy.getInService(4, 15),4);

        this.classBuffer.addToBuffer(4);

        assertEquals(this.classBuffer.getInService(1, 15),1);
        assertEquals(this.classBuffer.getInService(2, 15),2);
        assertEquals(this.classBuffer.getInService(3, 15),3);
        assertEquals(this.classBuffer.getInService(4, 15),5);
        assertEquals(classBufferCopy.getInService(1, 15),2);
        assertEquals(classBufferCopy.getInService(2, 15),2);
        assertEquals(classBufferCopy.getInService(3, 15),3);
        assertEquals(classBufferCopy.getInService(4, 15),4);*/
    }


    @org.junit.jupiter.api.Test
    void testGetInQueue() {
        this.classBuffer.addToBuffer(4);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),0);
        assertEquals(this.classBuffer.getInQueue(4),1);
        this.classBuffer.addToBuffer(4);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),0);
        assertEquals(this.classBuffer.getInQueue(4),2);
        this.classBuffer.addToBuffer(4);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),0);
        assertEquals(this.classBuffer.getInQueue(4),3);
        this.classBuffer.addToBuffer(4);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),0);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),1);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),2);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),3);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(2);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),1);
        assertEquals(this.classBuffer.getInQueue(3),3);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(2);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),2);
        assertEquals(this.classBuffer.getInQueue(3),3);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(1);
        assertEquals(this.classBuffer.getInQueue(1),1);
        assertEquals(this.classBuffer.getInQueue(2),2);
        assertEquals(this.classBuffer.getInQueue(3),3);
        assertEquals(this.classBuffer.getInQueue(4),4);
    }
}