package SolverSSA;
import StochLib.Node;

public interface NodeEvent {
    public Node getNode();
    public int getNodeStatefulIdx();
    public int getClassIdx();
    public boolean isStateful();
}
