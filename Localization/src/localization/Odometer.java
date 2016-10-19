package localization;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	
	public enum TURNDIR {CW, CCW};
	public enum LINEDIR {Forward,Backward};
	
	private double x,y,theta;
	private int leftMotorTachoCount,rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	private static final long ODOMETER_PERIOD = 25;
	public static final double WHEEL_RADIUS = 2.141;
	public static final double TRACK = 16.50;

	private Object mutex;

	private double oldltacho, oldrtacho;
	
	private double data;

	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = this.y = 0.0;
		this.theta = 0.5 * Math.PI;
		this.leftMotorTachoCount = this.rightMotorTachoCount = 0;
		this.oldltacho = this.oldrtacho = 0;
		this.leftMotor.resetTachoCount();
		this.rightMotor.resetTachoCount();
		mutex = new Object();
	}

	@Override
	public void run() {
		long updateStart, updateEnd;
		
		while(true) {
			updateStart = System.currentTimeMillis();
			this.leftMotorTachoCount = this.leftMotor.getTachoCount();
			this.rightMotorTachoCount = this.rightMotor.getTachoCount();
								
			double dLeft, dRight, dTheta, dPos; //left wheel movement, right wheel movement, change in heading, and change in position
			dLeft = WHEEL_RADIUS * Math.PI * (this.leftMotorTachoCount - this.oldltacho) / 180.0;
			dRight = WHEEL_RADIUS * Math.PI * (this.rightMotorTachoCount - this.oldrtacho) / 180.0;
			oldltacho = leftMotorTachoCount;
			oldrtacho = rightMotorTachoCount;
			dTheta = (-dLeft + dRight)/TRACK;
			dPos = (dLeft + dRight)/2;
			synchronized (mutex) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. 
				 * Do not perform complex math
				 * 
				 */
				theta = (theta + dTheta) % (2*Math.PI);	//radians - wrap around
				//if(theta < 0.0) theta += 2*Math.PI; //No negative angles
				y += dPos*Math.sin(theta);
				x += dPos*Math.cos(theta);
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}
	
	public EV3LargeRegulatedMotor[] getMotors() {
		EV3LargeRegulatedMotor[] motors = {leftMotor, rightMotor};
		return motors;
	}
	
	public void setMotorSpeeds(int speedL, int speedR) {
		leftMotor.setSpeed(speedL);
		rightMotor.setSpeed(speedR);
	}
	
	public void setMotorSpeed(int speed) {
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
	}
	
	public void forwardMotors() {
		leftMotor.forward();
		rightMotor.forward();
	}
	
	public void backwardMotors() {
		leftMotor.backward();
		rightMotor.backward();
	}
	
	public void spin(TURNDIR dir) {
		if(dir == TURNDIR.CW) {
			leftMotor.forward();
			rightMotor.backward();
		}
		else {
			leftMotor.backward();
			rightMotor.backward();
		}
	}
	
	public void moveCM(LINEDIR dir, double distance, boolean stop) {
		//Moves robot by a certain distance in a given direction
		if(dir == LINEDIR.Forward) forwardMotors();
		else backwardMotors();
		double startpos[] = new double[3];
		double curpos[] = new double[3]; //Current position buffer
		getPosition(startpos,new boolean[] {true,true,true});
		do {
			getPosition(curpos, new boolean[] {true,true,true}); //While hasn't moved far enough, update current position
		}
		while(euclideanDistance(startpos,curpos) < distance); //Stop when you've moved by the appropriate distance (approximately)
		
		if(stop) {
			setMotorSpeed(0);
			forwardMotors();
		}
	}

	public void getPosition(double[] position, boolean[] update) {
		synchronized(mutex) {
			if(update[0]) position[0] = x;
			if(update[1]) position[1] = y;
			if(update[2]) position[2] = theta;
		}
	}
	
	public void getPosition(double[] position) {
		synchronized(mutex) {
			position[0] = x;
			position[1] = y;
			position[2] = theta;
		}
	}

	public double getX() {
		double result;
		synchronized(mutex) {
			result = x;
		}
		return result;
	}

	public double getY() {
		double result;
		synchronized(mutex) {
			result = y;
		}
		return result;
	}

	public double getTheta() {
		double result;
		synchronized(mutex) {
			result = theta;
		}
		return result;
	}

	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (mutex) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (mutex) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (mutex) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (mutex) {
			this.theta = theta;
		}
	}

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (mutex) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (mutex) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}
	
	public double getFilteredData() {
		return data;
	}
	
	public void setData(double data) {
		this.data = data;
	}
	
	public double euclideanDistance(double[] a, double[] b) {
		return Math.sqrt((a[0] - b[0])*(a[0] - b[0]) + (a[1]-b[1])*(a[1]-b[1]));
	}
}

