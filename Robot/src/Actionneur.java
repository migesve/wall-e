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

	private boolean pincesOuvertes;
	private int direction;

	public Actionneur(Port portA, Port portB, Port portC) {
		moteurGauche = new EV3LargeRegulatedMotor(portA);
		moteurPince = new EV3MediumRegulatedMotor(portB);
		moteurDroit = new EV3LargeRegulatedMotor(portC);
		Wheel wheel1 = WheeledChassis.modelWheel(moteurGauche, 56).offset(-60);
		Wheel wheel2 = WheeledChassis.modelWheel(moteurDroit,  56).offset( 60);
		chassis = new WheeledChassis(new Wheel[] { wheel1, wheel2 }, WheeledChassis.TYPE_DIFFERENTIAL);
		mp = new MovePilot(chassis);
		mp.setAngularSpeed(150);
		mp.setLinearAcceleration(100);
		mp.setLinearSpeed(150);
		moteurPince.setSpeed(800);
	}
	public void update() {

	}
	public Move getMouvement() {
		return mp.getMovement();
	}
	public void avancer(double distance, boolean immediateReturn) {
		mp.travel(distance,false);
	}
	public void stop() {
		mp.stop();
	}
	public void rotation(double angle, boolean immediateReturn) {
		
		updateDirection(angle);
		mp.rotate(angle);
	}
	public void travelArc(double radius, double distance, boolean immediateReturn) {
		mp.setAngularSpeed(100);
		mp.travelArc(radius,distance,false);
	}
	public void ouvrirPinces() {
		moteurPince.forward();
		Delay.msDelay(800);
		moteurPince.stop();
		pincesOuvertes = true;
	}
	public void fermerPinces() {
		moteurPince.backward();
		Delay.msDelay(800);
		moteurPince.stop();
		pincesOuvertes = false;
	}
	// b = true --> rotating left
	// b = false -> rotating right
	public void startRotating(boolean b) {
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
	public boolean pincesOuvertes() {
		return pincesOuvertes;
	}
	public boolean isMoving() {
		return mp.isMoving();
	}
}