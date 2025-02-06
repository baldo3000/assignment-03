package me.baldo3000.model.api;

/**
 * Simple interface for an async msg communication channel
 *
 * @author aricci, baldo3000
 */
public interface SerialCommChannel {

    static final String DEFAULT_PORT = "COM9";
    static final int DEFAULT_BAUD_RATE = 115200;

    /**
     * Send a message represented by a string (without new line).
     * <p>
     * Asynchronous model.
     *
     * @param msg message to send
     */
    void sendMsg(String msg);

    /**
     * To receive a message.
     * <p>
     * Blocking behaviour.
     */
    String receiveMsg() throws InterruptedException;

    /**
     * To check if a message is available.
     *
     * @return true if a message is available, false otherwise
     */
    boolean isMsgAvailable();

}
