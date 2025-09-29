public enum FeedbackToken {
    WILDS, WASTES, DEVA, DEVB;

    void resolveOn(MyStack myStack) {
        switch (this) {
            case WILDS -> myStack.state = State.WILDS;   // set to wilds.
            case DEVA -> myStack.state = State.WASTES;  // DevA token: set the state to wastes.
            case WASTES, DEVB -> {
                // No effect at this stage.
            }
        }
    }
}
