import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.utility.Delay;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * Classe qui contient toutes les m�thodes de haut niveau pour notre robot.
 * 
 * <b> D�pendance : les m�thodes de cette classe sont utilis�es dans <code>AgentStrategy</code> uniquement. </b>
 * 
 * @author GATTACIECCA Bastien
 * @author DESCOTILS Juliette
 * @author LATIFI Arita
 * @author mig
 */
public class Agent {
	/*
	 * On update toutes les 20 ms.
	 */
	public static final int MS_DELAY = 20;
	/**
	 * Les actions du robot.
	 */
	private Actionneur action;
	/**
	 * Les perceptions du robot.
	 */
	private Perception perception;
	/**
	 * Timer qui permet d'it�rer pour mettre � jour les donn�es des capteurs.
	 */
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
	 * D�s qu'on pense avoir d�t�ct� un palet, on appelle cette m�thode pour le r�cup�rer.
	 * Si les pinces sont ferm�es elles s'ouvrent en m�me temps que le robot va avancer pour
	 * r�cup le palet.
	 * @param tryDistance Distance sur laquelle le robot va essayer d'avancer.
	 * @param speed Vitesse � laquelle le robot va s'avancer pinces ouvertes. Apr�s tests, si vitesse sup�rieure 300 le palet rebondit sur le balancier.
	 * @return Retourne true ou false selon que l'on a r�ussi � r�cup�rer un palet ou non.
	 */
	public boolean prendrePalet(double tryDistance, int speed) {
		action.ouvrirPinces(true); //...on ouvre les pinces de mani�re non bloquante !
		//On fait avancer le robot sur cette distance, avec un retour imm�diat.
		action.avancer(tryDistance,speed,true);
		//Tant que le capteur tactile renvoie false ...
		while(!perception.touch) {
			// ...on attend 20ms, histoire de r�duire le nombre d'appels d'une boucle true.
			Delay.msDelay(MS_DELAY);
			//Si pendant le trajet de 'tryDistance' le robot n'a toujours rien trouv� (s'il
			//s'est arr�t�) c'est qu'il n'a pas trouv� de palet, on return false.
			if(!action.isMoving()) {
				action.fermerPinces(false);
				return false;
			}
			//Si on aper�oit un objet tr�s proche (donc trop proche pour que ce soit un palet)
			//on recule en fermant les pinces de la distance que l'on a avanc�.
			if (perception.distance < 10) {
				action.fermerPinces(true);
				action.avancer(-action.getMouvement().getDistanceTraveled(), 200, false);
				return false;
			}
		}
		//Si perception.touch est pass� � 'true' :
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
	 * Tourne vers le mur ; s'arr�te d�s que la distance augmente. Permet de se remettre perpendiculaire
	 * au mur.
	 * @return true � la fin de l'op�ration.
	 */
	public boolean perpendiculaire() {
		float minDist = 300;
		action.rotation(360,60,true); //on commencer � tourner (sur la gauche).
		while(action.isMoving()) {
			if (perception.distance < minDist) { //cas de base, on trouve de plus petites valeurs en tournant vers le mur.
				minDist = perception.distance;
			}
			Delay.msDelay(MS_DELAY); //temps d'update 'perception.distance'.
			if (perception.distance > minDist) { //Si la distance devient plus grance que la plus petite qu'on avait jusque l�...
				action.stop();//...c'est qu'on vient de passer les 90�, donc on s'arr�te.
				action.updateDirection(action.getMouvement().getAngleTurned());//on actualise notre direction.
			}
		} 
		return true;
	}
	/**
	 * Se repositionne � 0�, en fonction de la direction actuelle. Donc dans la m�me direction que le robot
	 * �tait dans sa position de d�part.
	 * @return true � la fin de l'op�ration.
	 */
	public boolean resetDirection() {
		int angle = action.getDirection(); //on r�cup�re la direction actuelle.
		/*
		 * On tourne donc de l'oppos� de cet angle.
		 * Si l'angle oppos� est sup � 180, on tournera n�gativement (plus rapide).
		 */
		if (angle > 180) {
			angle = 360 - angle;
		}else if(angle < 180) {
			angle = - angle;
		}
		action.rotation(angle,150,false); //et on tourne de cet angle.
		return true;
	}
	/**
	 * "Scanne" les alentours sur 360� lentement pour obtenir le plus de valeurs possibles
	 * puis se pointe vers l'objet qui �tait le plus proche.
	 * @return la plus petite distance en cm.
	 */
	public float directionNearestObject() {
		float minDist = 300;
		float minDistAngle = 0;
		action.rotation(360,50,true); //on scanne sur 360� � une vitesse retenue.
		while(action.isMoving()) {
			if (perception.distance < minDist) {
				minDist = perception.distance; //minDist prend toujours la valeur de la plus petite distance per�ue.
				minDistAngle = action.getMouvement().getAngleTurned();//pour cette valeur de minDist, on regarde � quel angle on se trouve.
			}
			Delay.msDelay(100);
		}
		//D�s qu'on sort de la boucle, on a notre attribut minDistAngle qui repr�sente
		//l'angle de l'objet le plus proche de nous.
		//Si cet angle est sup�rieur � 180�, alors il sera pr�f�rable de tourner n�gativement :)
		if (minDistAngle > 180) {
			minDistAngle = - (minDistAngle % 180);
		}		
		//on se tourne vers l� o� la distance �tait la plus petite avec une vitesse plus soutenue, m�thode blocante of course.
		action.rotation(minDistAngle,160,false);

		return minDist;
	}
	/**
	 * Le robot avance jusqu'� ce qu'il d�tecte la couleur en param�tre.
	 * @param c Le String de la couleur.
	 * @param tryDistance La distance d'essai pour aller jusqu'� la ligne de couleur en param�tre.
	 * @param speed La vitesse � laquelle le robot va avancer.
	 * @return true si le robot s'est arret� car il a per�u la ligne, 
	 * false s'il a du s'arr�ter au bout d'une distance d'essai en param�tre.
	 */
	public boolean avancerJusquaColor(String c, double tryDistance, double speed) {
		if (!Perception.isAColor(c)) return false; //throw une exception ?
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
	 * Le robot ne fait qu'avancer tant qu'il suit la couleur pass�e en param�tre sur une
	 * distance donn�e. Il "t�tonne" � gauche et/ou droite pour retrouver la ligne d�s qu'il l'a perdue.
	 * @param c La couleur � suivre.
	 * @param distance La distance � parcourir.
	 * @return true d�s que la distance a �t� parcourue.
	 */
	public boolean suivreColor(String c, int distance) {
		if (!Perception.isAColor(c)) return false; //throw une exception ?
		int dist = 0;
		boolean positif = true;
		while(true) {
			//			if (Button.ESCAPE.isDown()) {
			//				System.exit(0);
			//			}
			action.avancer(3000, 80, true);
			while(perception.color.equals(c)) {
				dist += action.getMouvement().getDistanceTraveled();
				if (dist >= distance) {
					action.stop();
					return true;
				}
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
	 * Classe qui impl�mente l'interface TimerListener.
	 * Un Timer utilise un thread secondaire pour faire un appel it�ratif de la m�thode
	 * timedOut de l'interface TimerListener. On utilise cette it�ration pour mettre � jour
	 * nos attributs de perception.
	 * @author GATTACIECCA Bastien
	 * @author DESCOTILS Juliette
	 * @author LATIFI Arita
	 * @author mig
	 *
	 */
	class Boucle implements TimerListener {
		@Override
		public void timedOut() {
			perception.update();
		}
	}
}