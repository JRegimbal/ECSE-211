package chassis;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import utilities.Odometer;

public class Lab5 {
	//Constants (measurements, frequencies, etc)
	private static final long ODOMETER_PERIOD = 25;
	private static final double WHEEL_RADIUS = 2.141; //cm
	private static final double TRACK = 16.50; //cm
	//Resources (motors, sensors)
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static final Port colorPort = LocalEV3.get().getPort("S2");
	
	public static void main(String[] args) {
		//Setup sensors
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("Red");
		float[] colorData = new float[colorValue.sampleSize()];
		
		//Setup threads
		Odometer odo = new Odometer(leftMotor, rightMotor, ODOMETER_PERIOD, WHEEL_RADIUS, TRACK);
		
		//Start threads
		odo.start();
		
		//Wait for escape to exit
		while(Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}