public enum FeedbackToken {
    WILDS, WASTES, DEVA, DEVB;

    void resolveOn(MyStack myStack) {
        resolveOn(myStack, null);
    }
    
    void resolveOn(MyStack myStack, DisruptionCardManager disruptionManager) {
        switch (this) {
            case WILDS -> myStack.state = State.WILDS;   // set to wilds.
            case DEVA -> myStack.state = State.WASTES;  // DevA token: set the state to wastes.
            case WASTES -> {
                // No effect at this stage.
            }
            case DEVB -> {
                // DEVB token: draw disruption card and apply tile change effects
                if (disruptionManager != null && !disruptionManager.isDeckEmpty()) {
                    DisruptionCard card = disruptionManager.drawCard();
                    System.out.println("DEVB token drew disruption card: " + card.getName());
                    
                    // Only apply tile change effects
                    if (card.hasTileChangeEffect()) {
                        applyDisruptionCardEffects(card, myStack);
                    } else {
                        System.out.println("Card has no tile change effects, nothing happens");
                    }
                } else if (disruptionManager != null) {
                    System.out.println("Disruption card deck is empty, DEVB token has no effect");
                }
            }
        }
    }
    
    private void applyDisruptionCardEffects(DisruptionCard card, MyStack myStack) {
        // Apply effects to target stacks
        for (int targetId : card.getStackTarget()) {
            // Ensure target stack ID is valid (1-11)
            if (targetId >= 1 && targetId <= 11) {
                // Apply each effect
                for (var effect : card.getEffects()) {
                    switch (effect.getKey()) {
                        case DISRUPTION_TURN_WILD:
                            System.out.println("Convert stack " + targetId + " to WILD");
                            myStack.state = State.WILDS;
                            break;
                        case DISRUPTION_TURN_WASTE:
                            System.out.println("Convert stack " + targetId + " to WASTE");
                            myStack.state = State.WASTES;
                            break;
                        case DISRUPTION_TURN_DEVA:
                            System.out.println("Convert stack " + targetId + " to DEVA");
                            myStack.state = State.DEVA;
                            break;
                        case DISRUPTION_TURN_DEVB:
                            System.out.println("Convert stack " + targetId + " to DEVB");
                            myStack.state = State.DEVB;
                            break;
                        default:
                            // Ignore other effects for now
                            break;
                    }
                }
            }
        }
    }
}
