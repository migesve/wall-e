import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;
/**
 * Classe qui contient toutes les méthodes de bas niveau concernant les déplacements. Les moteurs
 * gauche et droit sont des <code>EV3LargeRegulatedMotor</code> tandis que le moteur de la pince
 * est un <code>EV3MediumRegulatedMotor</code>. On utilise une instance de la classe <code>Chassis
 * </code> ainsi que <code>MovePilot</code> pour calculer tous les mouvements dont nous avons besoin
 * à partir de ces paramètres :
 * <li>le diamètre des roues en millimètres (l'unité des distances que nous avons choisie).
 * <li>l'offset, c'est à dire le décalage des roues par rapport au centre du 'segment' qui relie les
 * deux roues.
 * <li>l' "angular speed" qui définit la vitesse du robot dans les méthodes de rotation.
 * <li>la "linear speed" qui définit la vitesse du robot dans les méthodes de déplacements avant/arrière
 * <li>la "linear acceleration" qui définit la qualité des phases d'accélération en début de mouvement
 * et de décélération en fin de mouvement pour une vitesse moteur spécifiée.
 * @author Nous <3
 */
public class Actionneur {
	/**
	 * Classe qui contrôle un châssis.
	 */
	private MovePilot mp;
	/**
	 * Moteur de la pince.
	 */
	private EV3MediumRegulatedMotor moteurPince;
	/**
	 * Indique si les pinces sont ouvertes ou non.
	 */
	private boolean pincesOuvertes;
	/**
	 * Direction actuelle du robot. Est toujours compris entre 0 inclus et 360 exclu.
	 */
	private int direction;

	public Actionneur(Port portA, Port portB, Port portC) {
		EV3LargeRegulatedMotor moteurGauche = new EV3LargeRegulatedMotor(portA);
		EV3LargeRegulatedMotor moteurDroit = new EV3LargeRegulatedMotor(portC);
		moteurPince = new EV3MediumRegulatedMotor(portB);
		Wheel wheel1 = WheeledChassis.modelWheel(moteurGauche, 56).offset(-60.2); //unité = mm. L'offset est le décalage de la roue par rapport au centre de l'essieu.
		Wheel wheel2 = WheeledChassis.modelWheel(moteurDroit,  56).offset( 60.2);
		Chassis chassis = new WheeledChassis(new Wheel[] { wheel1, wheel2 }, WheeledChassis.TYPE_DIFFERENTIAL);
		mp = new MovePilot(chassis);
		moteurPince.setSpeed(800); //Vitesse d'ouverture/fermeture des pinces reste inchangée.
	}
	public void update() {
		
	}
	/**
	 * Permet de récupérer le mouvement en cours.
	 * @return un mouvement de type Move.
	 */
	public Move getMouvement() {
		return mp.getMovement();
	}
	/**
	 * Mouvements avancer/reculer.
	 * @param distance Distance à parcourir en mm. Si négatif fait la distance en reculant.
	 * @param speed Vitesse du mouvement en mm/s.
	 * @param nonBloquante Indique si la méthode retourne directement ou non.
	 */
	public void avancer(double distance, double speed, boolean nonBloquante) {
		mp.setLinearAcceleration(speed);
		mp.setLinearSpeed(speed);
		mp.travel(distance,nonBloquante);
	}
	/**
	 * Rotation sur lui-même.
	 * @param angle Angle de rotation en degré. Positif = left ; négatif = right.
	 * @param speed Vitesse de rotation en degré/s.
	 * @param nonBloquante Indique si la méthode retourne directement ou non.
	 */
	public void rotation(double angle, double speed, boolean nonBloquante) {
		mp.setAngularAcceleration(speed);
		mp.setAngularSpeed(speed);
		mp.rotate(angle,nonBloquante);
		updateDirection(angle); //met à jour l'attribut de direction.
	}
	/**
	 * Déplace le robot sur le long du cercle spécifié par son rayon.
	 * Si le rayon est positif, le centre de ce cercle est à gauche du robot ; sinon à droite.
	 * Si la distance est positive, le robot se déplace sur le cercle en avançant, sinon en reculant.
	 * Si le rayon est nul, le robot tourne sur lui-même. Si la distance est nulle, la méthode retourne immédiatement.
	 * @param radius Le rayon du cercle autour duquel le robot va se déplacer.
	 * @param distance La distance à parcourir le long de  ce cercle.
	 */
	public void travelArc(double radius, double distance) {
		//On dirait que le rayon de l'arc sur lequel on tourne * 0.1 + 2300 fait un tour complet .... NO IDEA BRO
		mp.travelArc(radius,distance);
	}
	/**
	 * Stoppe le robot.
	 */
	public void stop() {
		mp.stop();
	}
	/**
	 * Ouvre les pinces du robot, et met à jour l'attribut <code>pincesOuvertes</code>.
	 * Méthode bloquante.
	 */
	public void ouvrirPinces() {
		if (pincesOuvertes) return;
		moteurPince.forward();
		Delay.msDelay(1500);
		moteurPince.stop();
		pincesOuvertes = true;
	}
	/**
	 * Ferme les pinces du robot, et met à jour l'attribut <code>pincesOuvertes</code>.
	 * Méthode bloquante.
	 */
	public void fermerPinces() {
		if (!pincesOuvertes) return;
		moteurPince.backward();
		Delay.msDelay(1500);
		moteurPince.stop();
		pincesOuvertes = false;
	}
	/**
	 * Incrémente la direction actuelle du robot de l'angle en paramètre. On souhaite toujours
	 * récupérer l'angle actuel du robot selon un angle compris entre 0 inclus et 360 exclu.
	 * @param angle L'angle à incrémenter à la direction actuelle.
	 */
	public void updateDirection(double angle) {
		direction += angle;
		direction %= 360;
		if (direction < 0) {
			direction += 360;
		}
	}
	/**
	 * Indique si le robot est actuellement en train d'effectuer un mouvement (de la classe <code>Move</code>).
	 * @return true si le robot effectue un mouvement, false sinon.
	 */
	public boolean isMoving() {
		return mp.isMoving();
	}
	/**
	 * Accesseur public à l'attribut <code>pincesOuvertes</code>.
	 * @return l'état des pinces.
	 */
	public boolean pincesOuvertes() {
		return pincesOuvertes;
	}
	/**
	 * Accesseur public à l'attribut <code>direction</code>.
	 * @return la direction actuelle du robot.
	 */
	public int getDirection() {
		return direction;
	}
}