package me.baldo3000.model.impl;

import java.util.concurrent.*;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import me.baldo3000.model.api.SerialCommChannel;

/**
 * Comm channel implementation based on serial port.
 *
 * @author aricci, baldo3000
 */
public class SerialCommChannelImpl implements SerialCommChannel, SerialPortEventListener {

    private SerialPort serialPort;
    private final BlockingQueue<String> queue;
    private StringBuffer currentMsg = new StringBuffer("");
    private long lastMsgTimestamp;

    public SerialCommChannelImpl() throws Exception {
        this(DEFAULT_PORT, DEFAULT_BAUD_RATE);
        this.lastMsgTimestamp = System.currentTimeMillis();
    }

    public SerialCommChannelImpl(String port, int rate) throws Exception {
        queue = new ArrayBlockingQueue<String>(100);

        try {
            serialPort = new SerialPort(port);
            serialPort.openPort();

            serialPort.setParams(rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);

            // serialPort.addEventListener(this, SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(this);
        } catch (final SerialPortException e) {
            System.err.println("Serial port " + port + " not found, skipping connection");
        }
    }

    @Override
    public void sendMsg(String msg) {
        char[] array = (msg + "\n").toCharArray();
        byte[] bytes = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = (byte) array[i];
        }
        // System.out.println("Sending over serial: " + msg);
        try {
            synchronized (serialPort) {
                serialPort.writeBytes(bytes);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String receiveMsg() throws InterruptedException {
        return queue.take();
    }

    @Override
    public boolean isMsgAvailable() {
        return !queue.isEmpty();
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public void close() {
        try {
            if (serialPort != null) {
                serialPort.removeEventListener();
                serialPort.closePort();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void serialEvent(SerialPortEvent event) {
        /* if there are bytes received in the input buffer */
        if (event.isRXCHAR()) {
            try {
                String msg = serialPort.readString(event.getEventValue());

                msg = msg.replaceAll("\r", "");

                currentMsg.append(msg);

                boolean goAhead = true;

                while (goAhead) {
                    String msg2 = currentMsg.toString();
                    int index = msg2.indexOf("\n");
                    if (index >= 0) {
                        queue.put(msg2.substring(0, index));
                        currentMsg = new StringBuffer("");
                        if (index + 1 < msg2.length()) {
                            currentMsg.append(msg2.substring(index + 1));
                        }
                    } else {
                        goAhead = false;
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Error in receiving string from COM-port: " + ex);
            }
        }
    }

    @Override
    public boolean isOpen() {
        return this.serialPort.isOpened();
    }
}
