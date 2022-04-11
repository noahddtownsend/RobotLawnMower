package dev.noahtownsend.robotlawnmower;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.util.concurrent.atomic.AtomicLong;

public class DistanceSensor {
    // m/ms
    private static final double SPEED_OF_SOUND = 0.343;

    private final AtomicLong TRIGGER_TIME = new AtomicLong(0);
    private int TRIGGER_PIN;
    private int ECHO_PIN;
    private Context context;
    private DigitalOutput trigger;
    private DigitalInput echo;

    public DistanceSensor(Context context) {
        this(context, 18, 24);
    }

    public DistanceSensor(Context context, int triggerPin, int echoPin) {
        this.context = context;
        TRIGGER_PIN = triggerPin;
        ECHO_PIN = echoPin;
        init();
    }

    private void init() {
        trigger = context.create(
                DigitalOutput.newConfigBuilder(context)
                        .id("sonic-dist-trigger")
                        .name("sonic-dist-trigger")
                        .address(TRIGGER_PIN)
                        .shutdown(DigitalState.LOW)
                        .initial(DigitalState.LOW)
                        .provider("pigpio-digital-output")
        );

        echo = context.create(
                DigitalInput.newConfigBuilder(context)
                        .id("sonic-dist-echo")
                        .name("sonic-dist-echo")
                        .address(ECHO_PIN)
                        .pull(PullResistance.PULL_DOWN)
                        .debounce(1L)
                        .provider("pigpio-digital-input")
        );
    }

    /**
     * @return Single that emits measured distance in CM. If distance in CM is greater than Double.MAX_VALUE, Double.MAX_VALUE is emitted
     */
    public Observable<Double> measure() {
        return PublishSubject.create(emitter -> {
            trigger.high();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            echo.addListener(digitalStateChangeEvent -> {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - TRIGGER_TIME.get();
                trigger();
                if (digitalStateChangeEvent.state() == DigitalState.HIGH) {
                    System.out.println("Time elapsed (high): " + elapsedTime);
                    double distanceInM = elapsedTime * SPEED_OF_SOUND / 2.0;

                    if (distanceInM < 0) {
                        distanceInM = Double.MAX_VALUE;
                    }

                    emitter.onNext(distanceInM);
                } else {
                    System.out.println("Time elapsed (low): " + elapsedTime);
                    double distanceInM = elapsedTime * SPEED_OF_SOUND / 2.0;

                    if (distanceInM < 0) {
                        distanceInM = Double.MAX_VALUE;
                    }

                    emitter.onNext(distanceInM);
                }
            });

            TRIGGER_TIME.set(System.currentTimeMillis());
            trigger.low();
            TRIGGER_TIME.set(System.currentTimeMillis());

        });
    }

    private void trigger() {
        trigger.high();
        TRIGGER_TIME.set(System.currentTimeMillis());
        trigger.low();
        TRIGGER_TIME.set(System.currentTimeMillis());
    }

}
