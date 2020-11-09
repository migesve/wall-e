import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;


public class Actionneur 
    public static final int OUVERTURE_MAX=0, FERMETURE_MAX=1;
    private Chassis chassis;
    private MovePilot mp;
    private EV3LargeRegulatedMotor moteurGauche;
    private EV3LargeRegulatedMotor moteurDroit;
    private EV3MediumRegulatedMotor moteurPince;
    
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
    public void avancer(int speed) {
        mp.setAngularSpeed(Math.abs(speed));
        if (speed > 0) {
            mp.forward();
        }else if (speed < 0) {
            mp.backward();
        }else {
            stop();
        }
    }
    public void stop () {
        mp.stop();
    }
    public void rotation(int angle) {
    	direction += angle;
        mp.setAngularSpeed(100);
        mp.rotate(angle);
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
    }
    public void fermerPinces() {
        moteurPince.backward();
        Delay.msDelay(800);
        moteurPince.stop();
    }
}