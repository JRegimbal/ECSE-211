package navigator;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	private double x,y,theta;
	private int leftMotorTachoCount,rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	private static final long ODOMETER_PERIOD = 25;

	private Object mutex;

	private double oldltacho, oldrtacho;

	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = this.y = this.theta = 0.0;
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
			dLeft = Lab3.WHEEL_RADIUS * Math.PI * (this.leftMotorTachoCount - this.oldltacho) / 180.0;
			dRight = Lab3.WHEEL_RADIUS * Math.PI * (this.rightMotorTachoCount - this.oldrtacho) / 180.0;
			oldltacho = leftMotorTachoCount;
			oldrtacho = rightMotorTachoCount;
			dTheta = (dRight - dLeft)/Lab3.TRACK;
			dPos = (dLeft + dRight)/2;
			synchronized (mutex) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. 
				 * Do not perform complex math
				 * 
				 */
				theta = (theta + dTheta) % (2*Math.PI);	//radians - wrap around
				if(theta < 0.0) theta += 2*Math.PI; //No negative angles
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

	public void getPosition(double[] position, boolean[] update) {
		synchronized(mutex) {
			if(update[0]) position[0] = x;
			if(update[1]) position[1] = x;
			if(update[2]) position[2] = x;
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
}
