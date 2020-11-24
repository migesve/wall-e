import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.utility.Delay;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class Agent {
	public static final int MS_DELAY = 20;
	private Actionneur action;
	private Perception perception;
	private Timer timer;
	public Actionneur getAction() {
		return action;
	}
	public Perception getPerception() {
		return perception;
	}

	public Agent() {
		perception = new Perception(
				LocalEV3.get().getPort("S1"),
				LocalEV3.get().getPort("S2"),
				LocalEV3.get().getPort("S3"));
		action = new Actionneur(
				LocalEV3.get().getPort("A"),
				LocalEV3.get().getPort("B"),
				LocalEV3.get().getPort("C"));
		timer = new Timer(MS_DELAY,new Boucle());
		timer.start();
	}
	/**
	 * Dès qu'on pense avoir détécté un palet, on appelle cette méthode pour le récupérer.
	 * @param tryDistance Distance sur laquelle le robot va essayer d'avancer.
	 * @param speed Vitesse à laquelle le robot va s'avancer pinces ouvertes. Après tests, si vitesse > 300 le palet rebondit sur le balancier.
	 * @return Retourne true ou false selon que l'on a réussi à récupérer un palet ou non.
	 */
	public boolean prendrePalet(int tryDistance, int speed) {
		if (!action.pincesOuvertes()) { //Si les pinces sont fermées ...
			action.ouvrirPinces(true); //...on les ouvre !
		}
		//On fait avancer le robot sur cette distance, avec un retour immédiat.
		action.avancer(tryDistance,speed,true);
		//Tant que le capteur tactile renvoie false ...
		while(!perception.touch) {
			// ...on attend 20ms, histoire de réduire le nombre d'appels d'une boucle true.
			Delay.msDelay(MS_DELAY);
			//Si pendant le trajet de 'tryDistance' le robot n'a toujours rien trouvé (s'il
			//s'est arrêté) c'est qu'il n'a pas trouvé de palet, on return false.
			if(!action.isMoving()) {
				action.fermerPinces(false);
				return false;
			}
		}
		//Si perception.touch est passé à 'true' :
		action.stop(); //on stop le robot
		action.fermerPinces(false); //on serre les pinces
		return true; 
	}
	/**
	 * Test les valeurs du capteur distance.
	 */
	public void testDistance() {
		while(true) {
			System.out.println(perception.distance);
			Delay.msDelay(MS_DELAY);
			if (Button.ESCAPE.isDown()) {
				System.exit(0);
			}
		}
	}
	/**
	 * Test les valeurs du capteur couleur.
	 */
	public void testCouleur() {
		while(true) {
			System.out.println(perception.color);
			Delay.msDelay(MS_DELAY);
			if (Button.ESCAPE.isDown()) {
				System.exit(0);
			}
		}
	}
	/**
	 * Tourne vers le mur ; s'arrête dès que la distance augmente.
	 * @return true à la fin de l'opération.
	 */
	public boolean perpendiculaire() {
		float minDist = 300;
		action.rotation(360,60,true); //on commencer à tourner (sur la gauche).
		while(action.isMoving()) {
			if (perception.distance < minDist) { //cas de base, on trouve de plus petites valeurs en tournant vers le mur.
				minDist = perception.distance;
			}
			Delay.msDelay(MS_DELAY); //temps d'update 'perception.distance'.
			if (perception.distance > minDist) { //Si la distance devient plus grance que la plus petite qu'on avait jusque là...
				action.stop();//...c'est qu'on vient de passer les 90°, donc on s'arrête.
				action.updateDirection(action.getMouvement().getAngleTurned());//on actualise notre direction.
			}
		} 
		return true;
	}
	/**
	 * Se repositionne à 0°, en fonction de la direction actuelle. Donc dans la même direction que le robot
	 * était dans sa position de départ.
	 * @return true à la fin de l'opération.
	 */
	public boolean resetDirection() {
		int angle = action.getDirection(); //on récupère l'opposé de la direction actuelle.
		if (angle > 180) {
			angle = 360 - angle;
		}else if(angle < 180) {
			angle = - angle;
		}
		action.rotation(angle,150,false); //et on tourne de cet angle.
		return true;
	}
	/**
	 * "Scanne" les alentours sur 360° puis se pointe vers l'objet qui était
	 * le plus proche.
	 * @return true à la fin de l'opération.
	 */
	public boolean directionNearestObject() {
		float minDist = 300;
		float minDistAngle = 0;
		action.rotation(360,60,true); //on scanne sur 360° à une vitesse retenue.
		while(action.isMoving()) {
			if (perception.distance <= minDist) { 
				minDist = perception.distance; //minDist prend toujours la valeur de la plus petite distance perçue.
				minDistAngle = action.getMouvement().getAngleTurned();//pour cette valeur de minDist, on regarde à quel angle on se trouve.
			}
		}
		//Dès qu'on sort de la boucle, on a notre attribut minDistAngle qui représente
		//l'angle de l'objet le plus proche de nous.
		Delay.msDelay(200);
		//Si cet angle est supérieur à 180°, alors il sera préférable de tourner négativement :)
		if (minDistAngle > 180) {
			minDistAngle = - (minDistAngle % 180);
		}
		action.rotation(minDistAngle,160,false); //on se dirige vers cet objet avec une vitesse plus soutenue, méthode blocante of course. 
		return true;
	}
	/**
	 * Le robot avance jusqu'à ce qu'il détecte la couleur en paramètre.
	 * @param c Le String de la couleur.
	 * @param speed La vitesse à laquelle le robot va avancer.
	 * @return true si le robot s'est arreté car il a perçu la ligne, 
	 * false s'il a du s'arrêter au bout d'une distance d'essai initialisée localement à 2000 mm.
	 */
	public boolean avancerJusquaColor(String c, int speed) {
		if (!Perception.isAColor(c)) return false; //throw une exception ?
		int tryDistance = 2000;
		action.avancer(tryDistance, speed, true);
		while(!perception.color.equals(c)) {
			Delay.msDelay(MS_DELAY);
			if (!action.isMoving()) {
				return false;
			}
		}
		action.stop();
		return true;
	}
	/**
	 * Le robot ne fait qu'avancer tant qu'il suit la couleur passée en paramètre.
	 * @param color La couleur à suivre.
	 */
	public boolean suivreColor(String c) {
		if (!Perception.isAColor(c)) return false; //should we throw an exception ?
		boolean positif = true;
		while(true) {
			if (Button.ESCAPE.isDown()) {
				System.exit(0);
			}
			action.avancer(3000, 80, true);
			while(perception.color.equals(c)) {
				Delay.msDelay(MS_DELAY);
			}
			action.stop();
			action.rotation(positif ? 10 : -10, 80,false);
			if (!perception.color.equals(c)) {
				positif = !positif;
				action.rotation(positif ? 20 : -20, 80,false);
			}
		}
	}
	/**
	 * Classe qui implémente l'interface TimerListener.
	 * Un Timer utilise un thread secondaire pour faire un appel itératif de la méthode
	 * timedOut de l'interface TimerListener. On utilise cette itération pour mettre à jour
	 * nos attributs de perception.
	 * @author moi <3
	 *
	 */
	class Boucle implements TimerListener {
		@Override
		public void timedOut() {
			action.update();
			perception.update();
		}
	}
}