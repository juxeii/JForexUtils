package com.jforex.programming.position;

import com.jforex.programming.order.OrderDirection;

public enum PositionSwitcherFSM {

    FLAT {
        @Override
        PositionSwitcherFSM sendBuySignal(final PositionSwitcher positionSwitcher) {
            positionSwitcher.executeOrderCommandSignal(OrderDirection.LONG);
            return BUSY;
        }

        @Override
        PositionSwitcherFSM sendSellSignal(final PositionSwitcher positionSwitcher) {
            positionSwitcher.executeOrderCommandSignal(OrderDirection.SHORT);
            return BUSY;
        }
    },
    LONG {
        @Override
        PositionSwitcherFSM sendFlatSignal(final PositionSwitcher positionSwitcher) {
            positionSwitcher.startClose();
            return BUSY;
        }

        @Override
        PositionSwitcherFSM sendSellSignal(final PositionSwitcher positionSwitcher) {
            positionSwitcher.executeOrderCommandSignal(OrderDirection.SHORT);
            return BUSY;
        }
    },
    SHORT {
        @Override
        PositionSwitcherFSM sendFlatSignal(final PositionSwitcher positionSwitcher) {
            positionSwitcher.startClose();
            return BUSY;
        }

        @Override
        PositionSwitcherFSM sendBuySignal(final PositionSwitcher positionSwitcher) {
            positionSwitcher.executeOrderCommandSignal(OrderDirection.LONG);
            return BUSY;
        }
    },
    BUSY {
        @Override
        PositionSwitcherFSM triggerSubmitDone(final PositionSwitcher positionSwitcher) {
            positionSwitcher.startMerge();
            return BUSY;
        }

        @Override
        PositionSwitcherFSM triggerMergeDone(final OrderDirection orderDirection) {
            if (orderDirection == OrderDirection.FLAT)
                return FLAT;
            if (orderDirection == OrderDirection.LONG)
                return LONG;
            return SHORT;
        }

        @Override
        PositionSwitcherFSM triggerCloseDone() {
            return FLAT;
        }
    };

    PositionSwitcherFSM sendBuySignal(final PositionSwitcher positionSwitcher) {
        return this;
    }

    PositionSwitcherFSM sendSellSignal(final PositionSwitcher positionSwitcher) {
        return this;
    }

    PositionSwitcherFSM sendFlatSignal(final PositionSwitcher positionSwitcher) {
        return this;
    }

    PositionSwitcherFSM triggerSubmitDone(final PositionSwitcher positionSwitcher) {
        return this;
    }

    PositionSwitcherFSM triggerMergeDone(final OrderDirection orderDirection) {
        return this;
    }

    PositionSwitcherFSM triggerCloseDone() {
        return this;
    }
}
