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
 * Classe qui contient toutes les m�thodes de bas niveau concernant les d�placements. Les moteurs
 * gauche et droit sont des <code>EV3LargeRegulatedMotor</code> tandis que le moteur de la pince
 * est un <code>EV3MediumRegulatedMotor</code>. On utilise une instance de la classe <code>Chassis
 * </code> ainsi que <code>MovePilot</code> pour calculer tous les mouvements dont nous avons besoin
 * � partir de ces param�tres :
 * <p>- le diam�tre des roues en millim�tres (l'unit� des distances que nous avons choisie).
 * <p>- l'offset, c'est � dire le d�calage des roues par rapport au centre du 'segment' qui relie les
 * deux roues.
 * <p>- l' "angular speed" qui d�finit la vitesse du robot dans les m�thodes de rotation.
 * <p>- la "linear speed" qui d�finit la vitesse du robot dans les m�thodes de d�placements avant/arri�re
 * <p>- la "linear acceleration" qui d�finit la qualit� des phases d'acc�l�ration en d�but de mouvement
 * et de d�c�l�ration en fin de mouvement pour une vitesse moteur sp�cifi�e.
 * 
 * <b> D�pendance : les m�thodes de cette classe sont utilis�es dans <code>Agent</code> uniquement. </b>
 * 
 * @author GATTACIECCA Bastien
 * @author DESCOTILS Juliette
 * @author LATIFI Arita
 * @author mig
 */
public class Actionneur {
	/**
	 * Le <code>Thread</code> qui fait ouvrir et fermer les pinces lorsqu'on appelle les
	 * m�thodes ouvrirPinces ou fermerPinces de mani�re non bloquante (pour ne pas bloquer
	 * le <code>Thread</code> principal). Il est important de pr�ciser que les m�thodes
	 * bloquantes/non bloquantes ne fonctionnent qu'avec la classe chassis et pas la classe
	 * <code>EV3MediumRegulatedMotor</code> pour le moteur de la pince directement !
	 */
	private PincesThread pincesThread;
	/**
	 * Indique si on d�sire ouvrir les pinces sans bloquer le thread principal.
	 */
	private boolean requireOuvrirPinces;
	/**
	 * Indique si on d�sire fermer les pinces sans bloquer le thread principal.
	 */
	private boolean requireFermerPinces;
	/**
	 * Indique si les pinces sont ouvertes ou non.
	 */
	private volatile boolean pincesOuvertes;
	/**
	 * Classe qui contr�le un ch�ssis.
	 */
	private MovePilot mp;
	/**
	 * Moteur de la pince.
	 */
	private EV3MediumRegulatedMotor moteurPince;
	/**
	 * Direction actuelle du robot. Est toujours compris entre 0 inclus et 360 exclu.
	 */
	private int direction;

	public Actionneur(Port portA, Port portB, Port portC) {
		/*
		 * On instancie les 3 moteurs.
		 */
		EV3LargeRegulatedMotor moteurGauche = new EV3LargeRegulatedMotor(portA);
		EV3LargeRegulatedMotor moteurDroit = new EV3LargeRegulatedMotor(portC);
		moteurPince = new EV3MediumRegulatedMotor(portB);
		/*
		 * unit� = mm. L'offset est le d�calage de la roue par rapport au centre de l'essieu.
		 * Pour d�terminer l'offset : on fait tourner 10 fois le robot sur lui-m�me et on regarde 
		 * le d�calage par rapport � 0�.
		 */
		Wheel wheel1 = WheeledChassis.modelWheel(moteurGauche, 56).offset(-59.9);
		Wheel wheel2 = WheeledChassis.modelWheel(moteurDroit,  56).offset( 59.9);
		Chassis chassis = new WheeledChassis(new Wheel[] { wheel1, wheel2 }, WheeledChassis.TYPE_DIFFERENTIAL);
		mp = new MovePilot(chassis);
		/*
		 * Vitesse d'ouverture/fermeture des pinces reste inchang�e.
		 */
		moteurPince.setSpeed(1500); 
		pincesThread = new PincesThread();
		pincesThread.start();
	}
	/**
	 * Permet de r�cup�rer le mouvement en cours. On utilisera cette m�thode pour r�cup�rer
	 * par exemple pendant un mouvement de combien est-ce qu'on a avanc� ou de combien est-ce
	 * qu'on a tourn�.
	 * @return un mouvement de type Move.
	 */
	public Move getMouvement() {
		return mp.getMovement();
	}
	/**
	 * Mouvements avancer/reculer.
	 * @param distance Distance � parcourir en mm. Si n�gatif fait la distance en reculant.
	 * @param speed Vitesse du mouvement en mm/s.
	 * @param nonBloquante Indique si la m�thode retourne directement ou non.
	 */
	public void avancer(double distance, double speed, boolean nonBloquante) {
		mp.setLinearAcceleration(speed);
		mp.setLinearSpeed(speed);
		mp.travel(distance,nonBloquante);
	}
	/**
	 * Rotation sur lui-m�me.
	 * @param angle Angle de rotation en degr�. Positif = left ; n�gatif = right.
	 * @param speed Vitesse de rotation en degr�/s.
	 * @param nonBloquante Indique si la m�thode retourne directement ou non.
	 */
	public void rotation(double angle, double speed, boolean nonBloquante) {
		mp.setAngularAcceleration(speed);
		mp.setAngularSpeed(speed);
		mp.rotate(angle,nonBloquante);
		/*
		 * On met � jour l'attribut de direction en incr�mentant l'angle dont le robot va tourner.
		 */
		updateDirection(angle);
	}
	/**
	 * D�place le robot sur le long du cercle sp�cifi� par son rayon.
	 * Si le rayon est positif, le centre de ce cercle est � gauche du robot ; sinon � droite.
	 * Si la distance est positive, le robot se d�place sur le cercle en avan�ant, sinon en reculant.
	 * Si le rayon est nul, le robot tourne sur lui-m�me. Si la distance est nulle, la m�thode retourne imm�diatement.
	 * @param radius Le rayon du cercle autour duquel le robot va se d�placer.
	 * @param distance La distance � parcourir le long de  ce cercle.
	 * @param nonBloquante si nonBloquante est true, la m�thode retourne imm�diatement.
	 */
	public void travelArc(double radius, double distance, boolean nonBloquante) {
		mp.travelArc(radius,distance,nonBloquante);
	}
	/**
	 * Stoppe le robot.
	 */
	public void stop() {
		mp.stop();
	}
	/**
	 * Ouvre les pinces du robot, et met � jour l'attribut <code>pincesOuvertes</code>.
	 * M�thode bloquante si le param�tre est 'false'. Si on appelle ouvrirPinces() alors que 
	 * les pinces sont en train de se fermer. On attend qu'elles soient int�gralement ferm�es 
	 * pour les r�ouvrir pour s'assurer que l'on ouvre toujours les pinces d'autant qu'on les 
	 * ferme. (un d�calage pourrait casser le m�canisme). On a aucun probl�me � bloquer le thread 
	 * principal dans une boucle vu que si une action de fermeture des pinces se fait � ce moment 
	 * pr�cis (le cas o� moteurPince.isMoving() est �valu� � 'true') c'est que cette action se fait 
	 * dans le thread secondaire.
	 * @param nonBloquante Si la m�thode ouvrirPinces est bloquante ou non.
	 */
	public void ouvrirPinces(boolean nonBloquante) {
		/*
		 * Si les pinces bougent, on attend la fin du mouvement.
		 */
		while(moteurPince.isMoving()) {
			Delay.msDelay(Agent.MS_DELAY);
		}
		/*
		 * Si les pinces sont d�j� ouvertes, on ne fait rien et la m�thode retourne
		 * imm�diatement.
		 */
		if (pincesOuvertes) return;
		if (nonBloquante) {
			requireOuvrirPinces = true;
		}else {
			moteurPince.forward();
			Delay.msDelay(1000);
			moteurPince.stop();
			pincesOuvertes = true;
		}
	}
	/**
	 * Ferme les pinces du robot, et met � jour l'attribut <code>pincesOuvertes</code>.
	 * M�thode bloquante si le param�tre est 'false'. Si on appelle fermerPinces() alors 
	 * que les pinces sont en train de s'ouvrir. On attend qu'elles soient int�gralement 
	 * ouvertes pour les fermer, pour s'assurer que l'on ferme toujours les pinces d'autant 
	 * qu'on les ouvre. (un d�calage pourrait casser le m�canisme). On a aucun probl�me � bloquer 
	 * le thread principal dans une boucle vu que si une action d'ouverture des pinces se fait � 
	 * ce moment pr�cis (le cas o� moteurPince.isMoving() est �valu� � 'true') c'est que cette 
	 * action se fait dans le thread secondaire.
	 * @param nonBloquante Si la m�thode fermerPinces est bloquante ou non.
	 */
	public void fermerPinces(boolean nonBloquante) {
		/*
		 * Si les pinces bougent, on attend la fin du mouvement.
		 */
		while(moteurPince.isMoving()) {
			Delay.msDelay(Agent.MS_DELAY);
		}
		/*
		 * Si les pinces sont d�j� ferm�es, on ne fait rien et la m�thode retourne
		 * imm�diatement.
		 */
		if (!pincesOuvertes) return;
		if (nonBloquante) {
			requireFermerPinces = true;
		}else {
			moteurPince.backward();
			Delay.msDelay(1000);
			moteurPince.stop();
			pincesOuvertes = false;
		}
	}
	/**
	 * Incr�mente la direction actuelle du robot de l'angle en param�tre. On souhaite toujours
	 * r�cup�rer l'angle actuel du robot selon un angle compris entre 0 inclus et 360 exclu.
	 * @param angle L'angle � incr�menter � la direction actuelle.
	 */
	public void updateDirection(double angle) {
		/*
		 * On incr�mente.
		 */
		direction += angle;
		/*
		 * On rabaisse � [-359;359].
		 */
		direction %= 360;
		/*
		 * Si n�gatif on incr�mente 360 (on retrouve le m�me angle mais il est positif).
		 * Ca ne fait pas tourner le robot ! �a ne fait que mettre � jour un attribut.
		 */
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
	 * Getter public � l'attribut <code>pincesOuvertes</code>.
	 * @return l'�tat des pinces.
	 */
	public boolean pincesOuvertes() {
		return pincesOuvertes;
	}
	/**
	 * Setter public � l'attribut <code>direction</code>.
	 * On ne red�finit cet attribut que si le robot s'est perdu, apr�s appel
	 * � une m�thode qui consiste � recadrer le robot face � un mur.
	 * @param direction La direction que le robot croit �tre.
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}
	/**
	 * Getter public � l'attribut <code>direction</code>.
	 * @return la direction actuelle du robot.
	 */
	public int getDirection() {
		return direction;
	}
	/**
	 * Thread secondaire. Permet de faire des actions comme ouvrir et fermer les pinces
	 * sans bloquer le thread principal. Rappelons que pour ouvrir les pinces on est oblig�
	 * de proc�der comme suit : on demande au moteur de tourner, on attend un temps t, puis on
	 * stop le moteur. Sachant la vitesse on contr�le ainsi l'ouverture/fermeture des pinces.
	 * Or, "attendre" demande de bloquer le thread, en effet quand on appelle Delay.msDelay(long)
	 * le programme boucle tant que l'intervalle de temps n'est pas �coul�, et donc ne rend pas la
	 * mains pour demander (par exemple) aux moteurs des roues qui eux ne bougent pas pendant l'ouverture
	 * des pinces d'avancer !
	 * @author GATTACIECCA Bastien
	 * @author DESCOTILS Juliette
	 * @author LATIFI Arita
	 * @author mig
	 */
	class PincesThread extends Thread  {
		@SuppressWarnings("static-access")
		@Override
		public void run() {
			/*
			 * Thread qui fonctionne en parall�le du thread principal � tout moment.
			 */
			while(true) {
				try {
					/*
					 * Si on demande d'ouvrir les pinces de mani�re non bloquante, la m�thode
					 * ouvrirPinces(true) ne fera que mettre l'attribut "requireOuvrirPinces"
					 * � 'true'. C'est ici qu'on bloquera le thread, et pas le principal.
					 */
					if(requireOuvrirPinces) {
						moteurPince.forward();
						this.sleep(1000);
						moteurPince.stop();
						pincesOuvertes = true;
						/*
						 * On indique �videment qu'on a plus besoin d'ouvrir les pinces dans ce thread
						 * une fois l'op�ration termin�e.
						 */
						requireOuvrirPinces = false;
					}
					if(requireFermerPinces) {
						moteurPince.backward();
						this.sleep(1000);
						moteurPince.stop();
						pincesOuvertes = false;
						requireFermerPinces = false;
					}
					this.sleep(Agent.MS_DELAY);
				}
				catch (InterruptedException e) {}
			}
		}
	}
}