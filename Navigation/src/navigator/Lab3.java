package navigator;

import lejos.hardware.sensor.*;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class Lab3 {

	private static final int bandCenter = 45;
	private static final int bandWidth = 10;
	private static final int motorLow = 100;
	private static final int motorHigh = 200;
	
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final EV3LargeRegulatedMotor sensorMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));

	public static final double WHEEL_RADIUS = 2.141;
	public static final double TRACK = 14.73;

	public static void main(String [] args) {
		int option = 0;
		Printer.printMainMenu();

		while(option == 0) option = Button.waitForAnyPress();

		final TextLCD t = LocalEV3.get().getTextLCD();

		BasicController controller = new BasicController(leftMotor,rightMotor,bandCenter,bandWidth);
		Driver driver = null;
		Odometer odometer = new Odometer(leftMotor,rightMotor);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer,t);

		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usDistance = usSensor.getMode("Distance");
		float[] usData = new float[usDistance.sampleSize()];

		Printer printer = null;

		UltrasonicPoller usPoller = null;
		
		Dexter morgan = new Dexter();

		double waypoints[] = {	60.0,30.0,
								30.0,30.0,
								30.0,60.0,
								60.0,0.0};
		/*double waypoints[] = {	0.0,30.0,
								30.0,30.0,
								0.0,30.0,
								0.0,0.0};*/
		
		double waypoints2[] = {0.0,60.0,60.0,0.0};
		

		switch(option) {
			case Button.ID_LEFT:
				//TODO Implement part 1
				//(0,0) -> (0,60) -> (30,30) -> (30,60) -> (60,0)
				driver = new BlindPathDriver(waypoints,leftMotor,rightMotor,odometer,TRACK,false);
				printer = new Printer(option,driver);
				usPoller = new UltrasonicPoller(usDistance,usData,driver);
				odometer.start();
				odometryDisplay.start();

				usPoller.start();
				//printer.start();
				driver.drive();
				Button.waitForAnyPress();
				System.exit(0);
				break;
			case Button.ID_RIGHT:
				//TODO Implement part 2
				driver = new BlindPathDriver(waypoints2,leftMotor,rightMotor,odometer,TRACK,true);
				usPoller = new UltrasonicPoller(usDistance,usData,driver);
				printer = new Printer(option,driver);
				odometer.start();
				//odometryDisplay.start();
				//printer.start();
				usPoller.start();
				morgan.start();
				driver.drive();
				Button.waitForAnyPress();
				System.exit(0);
				break;
			default:
				System.out.println("Error - invalid button.");
				System.exit(-1);
				break;
		}

		//driver.drive();

		
	}
}
