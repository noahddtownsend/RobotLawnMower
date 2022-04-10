package dev.noahtownsend.robotlawnmower;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

public class DistanceSensor {
    private static final double SPEED_OF_SOUND = 34300.0;

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
            trigger.low();

            echo.addListener(digitalStateChangeEvent -> {
                System.out.println("Received event");
                if (digitalStateChangeEvent.state() == DigitalState.HIGH) {
                    double distanceInCm = (System.currentTimeMillis() - start) * SPEED_OF_SOUND / 2.0;

                    if (distanceInCm < 0) {
                        distanceInCm = Double.MAX_VALUE;
                    }

                    emitter.onSuccess(distanceInCm);
                } else {
                    double distanceInCm = (System.currentTimeMillis() - start) * SPEED_OF_SOUND / 2.0;

                    if (distanceInCm < 0) {
                        distanceInCm = Double.MAX_VALUE;
                    }

                    emitter.onSuccess(distanceInCm);
                }
            });
        });

    }

}
