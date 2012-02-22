package eu.powet.SerialUSB.jna;


import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import eu.powet.SerialUSB.SerialPort.SerialPortException;


/*
 * 
 * Callbacks c functions
 */
public interface SerialEvent extends Callback {

	void serial_reader_callback(int taille,Pointer data) throws SerialPortException;

} 


