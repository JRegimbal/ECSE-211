package chassis;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

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
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static final Port colorPort = LocalEV3.get().getPort("S2");
	private static TextLCD textLCD = LocalEV3.get().getTextLCD();
	
	public static RobotState state = RobotState.k_Disabled;
	public static DemoState demo = DemoState.k_Default;
	
	public enum RobotState {k_Setup, k_Localization, k_Search, k_Capture, k_Disabled};
	public enum DemoState {k_Part1, k_Part2, k_Default};
	
	public static void main(String[] args) {
		state = RobotState.k_Setup;
		//Setup sensors
		USSensor usSensor = new USSensor(usPort);
		
		ColorSensor colorSensor = new ColorSensor(colorPort);
		//Setup threads
		Odometer odo = new Odometer(leftMotor, rightMotor, ODOMETER_PERIOD, WHEEL_RADIUS, TRACK);
		LCDInfo lcd = new LCDInfo(odo, textLCD, false);	//do not start on creation
		USLocalizer localizer = new USLocalizer(odo, usSensor, USLocalizer.LocalizationType.FALLING_EDGE);
		Search search = new Search(odo, colorSensor, usSensor);
		Capture capture = new Capture(odo);
		
		utilities.Navigator.setOdometer(odo);
		
		textLCD.drawString("<-Part 1 Part 2->", 0, 0);
		int input = Button.waitForAnyPress();
		switch(input) {
		case Button.ID_RIGHT:
			demo = DemoState.k_Part2;
			state = RobotState.k_Search;
			odo.start();	//start threads
			search.start();
			capture.start();
			//lcd.resume();
			//localizer.doLocalization();
			break;
		case Button.ID_LEFT:
			demo = DemoState.k_Part1;
			state = RobotState.k_Search;
			search.start();
			break;
		default:
			//invalid input
			System.exit(-1);
		}
		//Wait for escape to exit
		while(Button.waitForAnyPress() != Button.ID_ESCAPE);
		/*if(state == RobotState.k_Disabled) {	//execution has normally exited
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
		}*/
		odo.interrupt();;
		search.interrupt();
		capture.interrupt();
		localizer.interrupt();
		System.exit(0);
	}
}