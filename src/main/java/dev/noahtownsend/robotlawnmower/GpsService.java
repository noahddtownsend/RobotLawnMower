package dev.noahtownsend.robotlawnmower;

import com.pi4j.context.Context;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.StopBits;

public class GpsService {
    private static final String SERIAL_ADDRESS = "/dev/ttyS0";

    private Context pi4j;

    public GpsService(Context context) throws InterruptedException {
        this.pi4j = context;
    }

    public void init() throws InterruptedException {
        Serial serial = pi4j.create(Serial.newConfigBuilder(pi4j)
                .use_9600_N81()
                .dataBits_8()
                .parity(Parity.NONE)
                .stopBits(StopBits._1)
                .flowControl(FlowControl.NONE)
                .id("gps-serial")
                .device(SERIAL_ADDRESS)
                .provider("pigpio-serial")
                .build());
        serial.open();

        while (!serial.isOpen()) {
            Thread.sleep(250);
            System.out.println("Waiting for serial to open...");
        }

        SerialReader serialReader = new SerialReader(serial);
        Thread serialReaderThread = new Thread(serialReader, "SerialReader");
        serialReaderThread.setDaemon(true);
        serialReaderThread.start();

        while (serial.isOpen()) {
            Thread.sleep(500);
        }

        serialReader.stopReading();
    }

}
