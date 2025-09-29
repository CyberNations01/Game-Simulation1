

public class MyStack {
    final int id;            // 1..11
    final StackRing ring;    // Inner / Middle / Outer
    State state;             // Current state.

    MyStack(int id, StackRing ring, State init) {
        this.id = id;
        this.ring = ring;
        this.state = init;
    }

    @Override
    public String toString() {
        return "MyStack{" + id + "," + ring + "," + state + "}";
    }
}
