package me.baldo3000.engine.api;

public interface Engine {

    public static final double HOT_THRESHOLD = 20.0;
    public static final double TOO_HOT_THRESHOLD = 30.0;

    enum State {
        NORMAL,
        HOT,
        TOO_HOT,
        ALARM
    }

    void initialize();

    /**
     * starts the main loop of the application.
     */
    void mainLoop();

    /**
     * terminate engine.
     */
    void terminate();

    /**
     * Changes the state of the engine.
     *
     * @param state status to put
     */
    void setState(State state);

    /**
     * Gives the current State of the engine.
     *
     * @return the current status
     */
    State getState();
}
