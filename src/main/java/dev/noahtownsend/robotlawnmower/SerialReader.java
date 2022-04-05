package dev.noahtownsend.robotlawnmower;

import com.pi4j.io.serial.Serial;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SerialReader implements Runnable {
    private final Serial serial;

    private boolean continueReading = true;

    public SerialReader(Serial serial) {
        this.serial = serial;
    }

    public void stopReading() {
        continueReading = false;
        publishSubject.onComplete();
    }

    private final PublishSubject<String> publishSubject = PublishSubject.create();

    public Observable<String> getData() {
        return Observable.wrap(publishSubject);
    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(serial.getInputStream()));

        try {
            StringBuilder line = new StringBuilder();

            while (continueReading) {
                var available = serial.available();
                if (available > 0) {
                    for (int i = 0; i < available; i++) {
                        byte b = (byte) br.read();
                        if (b < 32) {
                            // All non-printable/whitespace chars treated as EOL
                            if (line.length() > 0) {
                                publishSubject.onNext(line.toString());
                                line.setLength(0);
                            }
                        } else {
                            line.append((char) b);
                        }
                    }
                } else {
                    Thread.sleep(10);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading data from serial: " + e.getMessage());
            System.out.println(e.getStackTrace());
        }
    }
}
