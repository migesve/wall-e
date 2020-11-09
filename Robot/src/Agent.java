import java.awt.Color;

import lejos.hardware.ev3.LocalEV3;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class Agent {
	private Actionneur action;
	private Perception perception;
	private Timer timer;

	private boolean requestPerpendiculaire;
	private boolean requestLineFollower;

	public Agent() {
		action = new Actionneur(
				LocalEV3.get().getPort("A"),
				LocalEV3.get().getPort("B"),
				LocalEV3.get().getPort("C"));
		perception = new Perception(
				LocalEV3.get().getPort("S1"),
				LocalEV3.get().getPort("S2"),
				LocalEV3.get().getPort("S3"));
		timer = new Timer(20,new Boucle());
		timer.start();
	}
	//Suit la ligne de couleur c
	public void lineFollower(Color c) {
		if (perception.currentColor == c) {
			if (!action.isMoving()) action.avancer(200,true);
		}
		requestLineFollower = true;
	}
	public void perpendiculaire() {
		float dist = perception.distance;
		action.rotation(20,false);
		action.startRotating(dist < perception.distance);
		requestPerpendiculaire = true;
	}
	class Boucle implements TimerListener {
		private float previousDist;
		private Color previousColor;
		@Override
		public void timedOut() {
			action.update();
			perception.update();
			if(requestPerpendiculaire && perception.distance > previousDist) {
				action.stop();
				action.updateDirection(action.getMouvement().getAngleTurned());
				requestPerpendiculaire = false;
			}
			if(requestLineFollower && perception.currentColor != previousColor) {
				action.stop();
				requestLineFollower = false;
			}
			//les variables "previous" prennent la valeur des variables de perception actuelles
			//à la fin de la méthode. Ainsi, lorsqu'on appelera cette méthode dans 20s, on commencera
			//par appeler les méthodes update() pour mettre à jour les nouvelles variables, et on aura
			//toujours les valeurs des anciennes variables dans les "previous".
			previousDist = perception.distance;
			previousColor = perception.currentColor;
		}
	}
}