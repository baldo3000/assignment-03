package me.baldo3000.controller.api;

public interface Controller {

    static final double HOT_THRESHOLD = 20.0;
    static final double TOO_HOT_THRESHOLD = 25.0;
    static final long ALARM_TRIGGER_TIME = 5000;

    static final long NORMAL_SAMPLE_INTERVAL = 3000;
    static final long HOT_SAMPLE_INTERVAL = 1500;

    enum Mode {
        AUTOMATIC, MANUAL
    }

    enum State {
        NORMAL, HOT, TOO_HOT, ALARM
    }

    /**
     * Initialize the application.
     */
    void initialize();

    /**
     * Start the main loop of the application.
     */
    void mainLoop() throws InterruptedException;

    /**
     * Terminate application.
     */
    void terminate();

    /**
     * Changes the state of the application.
     *
     * @param state status to put
     */
    void setState(State state);

    /**
     * Gives the current State of the application.
     *
     * @return the current status
     */
    State getState();
}
