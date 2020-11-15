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
	private boolean requestPrendrePalet;

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
	public void prendrePalet() {
		if (!action.getStatePinces()) {
			action.ouvrirPinces();
		}
		action.avancer(100, true);
		requestPrendrePalet = true;
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
				requestPerpendiculaire = false;
				action.stop();
				action.updateDirection(action.getMouvement().getAngleTurned());
			}
			if(requestLineFollower && perception.currentColor != previousColor) {
				requestLineFollower = false;
				action.stop();
			}
			if (requestPrendrePalet && perception.touch) {
				requestPrendrePalet = false;
				action.stop();
				action.fermerPinces();
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