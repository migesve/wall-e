import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
	 * @return Retourne true ou false selon que l'on a réussi à récupérer un palet ou non.
	 */
	public boolean prendrePalet() {
		if (!action.pincesOuvertes()) { //Si les pinces sont fermées ...
			action.ouvrirPinces(); //...on les ouvre !
		}
		//Distance sur laquelle le robot va avancer pour tenter de récupérer un palet.
		int tryDistance = 600;
		//On fait avancer le robot sur cette distance, avec un retour immédiat.
		action.avancer(tryDistance,200,true);
		//Tant que le capteur tactile renvoie false ...
		while(!perception.touch) {
			// ...on attend 20ms, histoire de réduire le nombre d'appels d'une boucle true.
			Delay.msDelay(20);
			//Si pendant le trajet de 'tryDistance' le robot n'a toujours rien trouvé (s'il
			//s'est arrêté) c'est qu'il n'a pas trouvé de palet, on return false.
			if(!action.isMoving()) {
				action.fermerPinces();
				return false;
			}
		}
		//Si perception.touch est passé à 'true' :
		action.stop(); //on stop le robot
		action.fermerPinces(); //on serre les pinces
		System.out.println(action.getDirection());
		Delay.msDelay(10000);
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
				return;
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
				return;
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
		Delay.msDelay(1000);
		//Si cet angle est supérieur à 180°, alors il sera préférable de tourner négativement :)
		if (minDistAngle > 180) {
			minDistAngle = - (minDistAngle % 180);
		}
		System.out.println(minDistAngle); //test
		Delay.msDelay(6000);
		action.rotation(minDistAngle,120,false); //on se dirige vers cet objet avec une vitesse plus soutenue, méthode blocante of course. 
		return true;
	}
	/**
	 * Le robot ne fait qu'avancer tant qu'il suit la couleur passée en paramètre.
	 * @param c La couleur à suivre.
	 */
	public void suivreColor(Color c) {
//		//action.avancer(3000);
//		while (perception.color.equals(c)) {
//			Delay.msDelay(20);
//		}
//		action.stop();
	}
	class Boucle implements TimerListener {
		@Override
		public void timedOut() {
			action.update();
			perception.update();
		}
	}
}