package eu.powet.SerialUSB;


import eu.powet.SerialUSB.SerialPort.*;

public class Test {

    /**
     * @param args
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {


        System.out.println(SerialPort.getPortIdentifiers());


        final SerialPort serial = new SerialPort("/dev/ttyACM0", 115200);
        serial.open();


        serial.addEventListener(new SerialPortEventListener() {
            public void incomingDataEvent(SerialPortEvent evt)
            {
                System.out.println("event=" + evt.getSize() + "/" + new String(evt.read()));
            }

            public void disconnectionEvent(SerialPortDisconnectionEvent evt)
            {
                System.out.println("device " + serial.getPort_name() + " is not connected anymore ");

                try {
                    serial.autoReconnect(20, this);
                } catch (SerialPortException e) {
                }
            }
        });

        Thread.currentThread().sleep(3000000);

    }

}
