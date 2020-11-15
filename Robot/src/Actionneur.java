import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

public class Actionneur {
	private Chassis chassis;
	private MovePilot mp;
	private EV3LargeRegulatedMotor moteurGauche;
	private EV3LargeRegulatedMotor moteurDroit;
	private EV3MediumRegulatedMotor moteurPince;

	private boolean statePinces;

	public int direction;

	public Actionneur(Port portA, Port portB, Port portC) {
		moteurGauche = new EV3LargeRegulatedMotor(portA);
		moteurPince = new EV3MediumRegulatedMotor(portB);
		moteurDroit = new EV3LargeRegulatedMotor(portC);
		Wheel wheel1 = WheeledChassis.modelWheel(moteurGauche, 56).offset(-60);
		Wheel wheel2 = WheeledChassis.modelWheel(moteurDroit,  56).offset( 60);
		chassis = new WheeledChassis(new Wheel[] { wheel1, wheel2 }, WheeledChassis.TYPE_DIFFERENTIAL);
		mp = new MovePilot(chassis);
		moteurPince.setSpeed(800);
	}
	public void update() {

	}
	public Move getMouvement() {
		return mp.getMovement();
	}
	public void avancer(int speed, boolean immediateReturn) {
		mp.setAngularSpeed(Math.abs(speed));
		if (speed > 0) {
			mp.travel(Double.POSITIVE_INFINITY, immediateReturn);
		}else if (speed < 0) {
			mp.travel(Double.NEGATIVE_INFINITY, immediateReturn);
		}else {
			stop();
		}
	}
	public void stop () {
		mp.stop();
	}
	public void rotation(double angle, boolean immediateReturn) {
		mp.setAngularSpeed(100);
		updateDirection(angle);
		mp.rotate(angle,immediateReturn);
	}
	// b = true --> rotating left
	// b = false -> rotating right
	public void startRotating(boolean b) {
		mp.setAngularSpeed(100);
		if (b) {
			mp.rotateLeft();
		}else {
			mp.rotateRight();
		}
	}
	public void updateDirection(double angle) {
		direction += angle;
		direction %= 360;
		if (direction < 0) {
			direction += 360;
		}
	}
	public void travelArc(double radius, double distance) {
		mp.travelArc(radius,distance,true);
	}
	public boolean isMoving() {
		return mp.isMoving();
	}
	public void ouvrirPinces() {
		moteurPince.forward();
		Delay.msDelay(800);
		moteurPince.stop();
		statePinces = true;
	}
	public void fermerPinces() {
		moteurPince.backward();
		Delay.msDelay(800);
		moteurPince.stop();
		statePinces = false;
	}
	public boolean getStatePinces() {
		return statePinces;
	}
}