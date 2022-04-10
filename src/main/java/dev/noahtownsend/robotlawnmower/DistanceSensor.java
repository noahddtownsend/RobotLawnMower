package dev.noahtownsend.robotlawnmower;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import io.reactivex.rxjava3.core.Single;

public class DistanceSensor {
    // m/ms
    private static final double SPEED_OF_SOUND = 0.343;

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
                        .pull(PullResistance.PULL_UP)
                        .debounce(1L)
                        .provider("pigpio-digital-input")
        );
    }

    /**
     *
     * @return Single that emits measured distance in CM. If distance in CM is greater than Double.MAX_VALUE, Double.MAX_VALUE is emitted
     */
    public Single<Double> measure() {
        return Single.create(emitter -> {
            trigger.high();
            long start = System.currentTimeMillis();

            echo.addListener(digitalStateChangeEvent -> {
                if (digitalStateChangeEvent.state() == DigitalState.HIGH) {
                    double distanceInM = (System.currentTimeMillis() - start) * SPEED_OF_SOUND / 2.0;

                    if (distanceInM < 0) {
                        distanceInM = Double.MAX_VALUE;
                    }

                    emitter.onSuccess(distanceInM);
                }
            });


            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            trigger.low();
        });

    }

}
