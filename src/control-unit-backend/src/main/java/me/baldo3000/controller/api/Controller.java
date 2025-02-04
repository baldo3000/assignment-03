package me.baldo3000.engine.api;

public interface Controller {

    public static final double HOT_THRESHOLD = 20.0;
    public static final double TOO_HOT_THRESHOLD = 25.0;
    public static final long ALARM_TRIGGER_TIME = 5000;

    public static final long NORMAL_SAMPLE_INTERVAL = 3000;
    public static final long HOT_SAMPLE_INTERVAL = 1000;

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
    void mainLoop();

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
