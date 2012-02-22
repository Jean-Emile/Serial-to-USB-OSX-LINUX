package eu.powet.SerialUSB.SerialPort;

import com.sun.jna.Memory;
import com.sun.jna.ptr.PointerByReference;
import eu.powet.SerialUSB.Constants;
import eu.powet.SerialUSB.CommPort;
import eu.powet.SerialUSB.Utils.ByteFIFO;
import eu.powet.SerialUSB.jna.NativeLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 26/01/12
 * Time: 17:29

 * SerialPort
 */

public class SerialPort extends CommPort {

    private SerialPortEvent SerialPortEvent;
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    private int sizefifo = 1024;
    private ByteFIFO fifo_out = new ByteFIFO(sizefifo);


    public SerialPort (String portname, int bitrate) throws Exception {
        this.setPort_bitrate(bitrate);
        this.setPort_name(portname);
    }


    public void addEventListener (SerialPortEventListener listener) {
        listenerList.add(SerialPortEventListener.class, listener);
    }

    public void removeEventListener (SerialPortEventListener listener) {
        listenerList.remove(SerialPortEventListener.class, listener);
    }

    public static List<String> getPortIdentifiers() {
        ArrayList<String> ports = new ArrayList<String>();
        File file = new File("/dev");
        File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory() == false)
                {
                    if(files[i].getName().startsWith("tty") || files[i].getName().startsWith("cu"))
                    {
                        if(files[i].getName().contains("USB") || files[i].getName().contains("usb") || files[i].getName().contains("AC") )
                        {
                            String device_name =   "/dev/"+files[i].getName();
                            if(eu.powet.SerialUSB.jna.NativeLoader.getINSTANCE_SerialPort().verify_fd(device_name) == 0)
                            {
                                ports.add(device_name);
                            }
                        }
                    }
                }

            }
        }
        return ports;
    }


    public void fireSerialEvent (SerialPortEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == SerialPortEventListener.class) {
                if (evt instanceof SerialPortDisconnectionEvent) {
                    ((SerialPortEventListener) listeners[i + 1]).disconnectionEvent((SerialPortDisconnectionEvent)evt);
                } else {
                    ((SerialPortEventListener) listeners[i + 1]).incomingDataEvent(evt);
                }
            }
        }
    }

    @Override
    public void write (byte[] bs) throws SerialPortException {

        if (this.getFd() > 0) {
            Memory mem = new Memory(Byte.SIZE * bs.length + 1);
            mem.clear();

            PointerByReference inipar = new PointerByReference();
            inipar.setPointer(mem);
            for (int i = 0; i < bs.length; i++) {
                inipar.getPointer().setByte(i * Byte.SIZE / 8, bs[i]);
            }
            byte c = '\n';
            inipar.getPointer().setByte((bs.length + 1) * Byte.SIZE / 8, c);
            System.out.println("fd  : "+getFd());
            if (NativeLoader.getINSTANCE_SerialPort().serialport_write(getFd(), inipar) != 0) {
                throw new SerialPortException("Write " + bs);
            }

        } else {
            throw new SerialPortException("not open " + bs);
        }


    }

    @Override
    public void open () throws SerialPortException {
        setFd(NativeLoader.getINSTANCE_SerialPort().open_serial(this.getPort_name(), this.getPort_bitrate()));

        if (getFd() < 0) {
            NativeLoader.getINSTANCE_SerialPort().close_serial(getFd());
            throw new SerialPortException(this.getPort_name()+"- [" + getFd() + "] " + Constants.messages.get(getFd())+" Ports : "+ getPortIdentifiers());
        }
        else
        {
            SerialPortEvent = new SerialPortEvent(this);
        }


    }

    public boolean autoReconnect (int tentative,SerialPortEventListener event) throws SerialPortException {
        close();
        removeEventListener(event);
        int i=0;
        boolean echec=true;
        while(i < tentative && echec)   {
            try
            {
                System.out.print("Try reconnect N°"+i);
                try
                {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                open();
                echec=false;
                addEventListener(event);
                return true;
            }catch (Exception e){
                System.out.println(" [FAIL]");
            }
            i++;
        }
        addEventListener(event)  ;
        close();
        return false;
    }

    @Override
    public void close () throws SerialPortException {
        NativeLoader.getINSTANCE_SerialPort().close_serial(getFd());

    }

}
