package jline.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import jline.solvers.ssa.*;
import jline.solvers.ssa.state.PhaseList;

class PhaseListTest {
    private PhaseList phaseList;
    @BeforeEach
    void setUp() {
        phaseList = new PhaseList(5, new Random());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testPhaseUpdate() {

    }
}