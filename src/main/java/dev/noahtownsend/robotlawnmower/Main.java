package dev.noahtownsend.robotlawnmower;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

public class Main {
    private static int DIGITAL_OUTPUT_PIN = 2;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello world");

        blink(DIGITAL_OUTPUT_PIN);
    }

    public static void blink(int ledNum) throws InterruptedException {
        Context pi4j = Pi4J.newAutoContext();
        System.out.println("Hi there!");


        // create a digital output instance using the default digital output provider
        DigitalOutput output = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .id("led")
                .name("LED Flasher")
                .address(ledNum)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("pigpio-digital-output"));

// setup a digital output listener to listen for any state changes on the digital output
        output.addListener(System.out::println);

// lets invoke some changes on the digital output
        output.low();
        Thread.sleep(5000);
        output.high();
        Thread.sleep(5000);

// lets read the digital output state
        System.out.print("CURRENT DIGITAL OUTPUT [" + output + "] STATE IS [");
        System.out.println(output.state() + "]");


// shutdown Pi4J
        pi4j.shutdown();
    }
}
