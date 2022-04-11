package dev.noahtownsend.robotlawnmower;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

import java.util.UUID;

public class StepMotor {
    private Context context;
    private DigitalOutput pin1;
    private DigitalOutput pin2;

    public StepMotor(Context context, int pin1, int pin2) {
        this.context = context;
        init(pin1, pin2);
    }

    private void init(int pin1, int pin2) {
        UUID uuid = UUID.randomUUID();
        String id1 = "motor-" + uuid + "-" + pin1;
        String id2 = "motor-" + uuid + "-" + pin2;
        this.pin1 = context.create(
                DigitalOutput.newConfigBuilder(context)
                        .id(id1)
                        .name(id1)
                        .address(pin1)
                        .shutdown(DigitalState.LOW)
                        .initial(DigitalState.LOW)
                        .provider("pigpio-digital-output")
        );

        this.pin2 = context.create(
                DigitalOutput.newConfigBuilder(context)
                        .id(id2)
                        .name(id2)
                        .address(pin2)
                        .shutdown(DigitalState.LOW)
                        .initial(DigitalState.LOW)
                        .provider("pigpio-digital-output")
        );

        context.create(
                DigitalOutput.newConfigBuilder(context)
                        .id(id2 + 1)
                        .name(id2 + 1)
                        .address(22)
                        .shutdown(DigitalState.LOW)
                        .initial(DigitalState.LOW)
                        .provider("pigpio-digital-output")
        );
        context.create(
                DigitalOutput.newConfigBuilder(context)
                        .id(id2 + 7)
                        .name(id2 + 7)
                        .address(27)
                        .shutdown(DigitalState.LOW)
                        .initial(DigitalState.LOW)
                        .provider("pigpio-digital-output")
        );
    }

    public void setStepLevel(StepLevel stepLevel) {
        switch (stepLevel) {
            case LOW:
                pin2.low();
                pin1.high();
                break;
            case MID:
                pin2.high();
                pin1.low();
                break;
            case HIGH:
                pin2.high();
                pin1.high();
                break;
            default:
                pin2.low();
                pin1.low();
                break;
        }
    }

    enum StepLevel {
        OFF,
        LOW,
        MID,
        HIGH
    }
}
