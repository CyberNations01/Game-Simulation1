public enum FeedbackToken {
    WILDS, WASTES, DEVA, DEVB;

    void resolveOn(MyStack myStack) {
        switch (this) {
            case WILDS -> myStack.state = State.WILDS;   // 置为 WILDS
            case DEVA -> myStack.state = State.WASTES;  // DEVA 令牌：把状态改成 WASTES
            case WASTES, DEVB -> {
                // 无效果 —— 保持现状
            }
        }
    }
}
