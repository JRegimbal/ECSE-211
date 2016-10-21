package chassis;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

import utilities.Odometer;
import utilities.USLocalizer;
import utilities.Search;
import utilities.Capture;

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
	private static TextLCD textLCD = LocalEV3.get().getTextLCD();
	
	public static RobotState state = RobotState.k_Disabled;
	
	public enum RobotState {k_Setup, k_Localization, k_Search, k_Capture, k_Disabled};
	
	public static void main(String[] args) {
		state = RobotState.k_Setup;
		//Setup sensors
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		
		@SuppressWarnings("resource")
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("Red");
		float[] colorData = new float[colorValue.sampleSize()];
		
		//Setup threads
		Odometer odo = new Odometer(leftMotor, rightMotor, ODOMETER_PERIOD, WHEEL_RADIUS, TRACK);
		LCDInfo lcd = new LCDInfo(odo, textLCD, false);	//do not start on creation
		USLocalizer localizer = new USLocalizer(odo, usSensor, usData, USLocalizer.LocalizationType.FALLING_EDGE);
		Search search = new Search(odo, colorValue, colorData, usValue, usData);
		Capture capture = new Capture(odo);
		
		textLCD.drawString("Press any key to start.", 0, 0);
		Button.waitForAnyPress();
		state = RobotState.k_Localization;
		//Start threads
		odo.start();
		search.start();
		capture.start();
		lcd.resume();
		localizer.run();
		
		
		//Wait for escape to exit
		while(Button.waitForAnyPress() != Button.ID_ESCAPE);
		if(state == RobotState.k_Disabled) {	//execution has normally exited
			try {
				odo.join();
				search.join();
				capture.join();
				localizer.join();
			} catch (Exception e) {}
		} else {								//cancelled while still running
			try {
				odo.interrupt();
				search.interrupt();
				capture.interrupt();
				localizer.interrupt();
			} catch (Exception e) {}
		}
		System.exit(0);
	}
}