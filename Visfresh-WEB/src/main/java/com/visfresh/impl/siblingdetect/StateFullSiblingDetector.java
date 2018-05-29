/**
 *
 */
package com.visfresh.impl.siblingdetect;

import com.visfresh.io.TrackerEventDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class StateFullSiblingDetector {
    protected enum State {
        Siblings,
        NotSiblings,
        Checking
    }

    private State state = State.Checking;
    private boolean isFinished;

    /**
     *
     */
    public StateFullSiblingDetector() {
        super();
    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }
    /**
     * @param state the state to set
     */
    public void setState(final State state) {
        this.state = state;
    }
    /**
     * @param e1 first event.
     * @param e2 next event.
     * @return
     */
    public final State next(final TrackerEventDto e1, final TrackerEventDto e2) {
        if (!isFinished) {
            doNext(e1, e2);
        }
        return getState();
    }
    /**
     * @param e1 first event.
     * @param e2 next event.
     */
    protected abstract void doNext(TrackerEventDto e1, TrackerEventDto e2);
    /**
     * Finishes the calculation.
     */
    public final void finish() {
        isFinished = true;
        doFinish();
    }
    /**
     * Finishes the calculation.
     */
    protected void doFinish() {};
}
