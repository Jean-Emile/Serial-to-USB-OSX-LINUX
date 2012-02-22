package eu.powet.SerialUSB.SerialPort;

import java.util.List;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 22/02/12
 * Time: 10:50
 */
public interface ISerialPort {
    public abstract void open()throws SerialPortException;
    public abstract void close()throws SerialPortException;
    public abstract void write(byte[] data) throws SerialPortException, SerialPortException;
    public abstract boolean autoReconnect (int tentative,SerialPortEventListener event) throws SerialPortException;
    public abstract void addEventListener (SerialPortEventListener listener);
    public abstract void removeEventListener (SerialPortEventListener listener);
}


