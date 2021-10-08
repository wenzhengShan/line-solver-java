package SolverSSA;

import SolverSSA.EventStack;
import SolverSSA.StateMatrix;

import javax.swing.plaf.nimbus.State;
import java.io.Serializable;
import java.util.Random;

public class Event implements Serializable {
    public Event() {
    }

    public double getRate(StateMatrix stateMatrix) {
        return Double.NaN;
    }

    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        /*
            stateUpdate -
                Attempt to apply an event to the stateMatrix

            Returns: (boolean) - whether the update was successful or not
         */
        return true;
    }

    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        /*
            stateUpdateN -
                Attempt to apply N repetitions of an event to the stateMatrix

            Returns: (int) - number of repetitions left unapplied
         */
        int rem = n;
        for (int i = 0; i < n; i++) {
            if (this.stateUpdate(stateMatrix, random, timeline)) {
                rem--;
            }
        }

        return rem;
    }

    public void printSummary() {
        System.out.format("Generic event\n");
    }

    public int getMaxRepetitions(StateMatrix stateMatrix) {
        return Integer.MAX_VALUE;
    }
}
