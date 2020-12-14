import lejos.hardware.*;
/**
 * Classe qui ordonne les méthodes d'Agent, nos stratégies.
 * 
 * <b> Dépendance : les méthodes de cette classe sont utilisées dans <code>Agent</code> uniquement. </b>
 * 
 * @author GATTACIECCA Bastien
 * @author DESCOTILS Juliette
 * @author LATIFI Arita
 * @author mig
 */
public class AgentStrategy extends Agent {
	/**
	 * Constance pour la ligne rouge.
	 */
	public static final int LIGNE_RED = -1;
	/**
	 * Constance pour la ligne noire.
	 */
	public static final int LIGNE_BLACK = 0;
	/**
	 * Constance pour la ligne bleue.
	 */
	public static final int LIGNE_YELLOW = 1;
	public boolean right;
	int posOtherRobot;
	
	public AgentStrategy() {
		super();
		messageDebut();
		if (posOtherRobot == LIGNE_BLACK) {
			strategyRed();
		}else {
			strategyMid();
		}
	}
	/**
	 * Stratégie codée en dur pour les 3 premiers palets si on commence ligne rouge.
	 * Vu la variabilité des moteurs, elle risque de passer toute seule en IA juste après
	 * avoir marqué le 1er palet.
	 */
	public void strategyRed() {
		if (!prendrePalet(600,200)) staticEchoue();
		getAction().rotation(right ? -45 : 45, 300, false);
		getAction().avancer(300, 300, false);
		getAction().rotation(right ? 45 : -45, 300, false);
		avancerJusquaColor("white",2000,350);
		getAction().ouvrirPinces(false);
		getAction().fermerPinces(true);
		getAction().avancer(-1200, 300, false);
		getAction().rotation(right ? 90 : -90, 300, false);
		if (!prendrePalet(600,200)) staticEchoue();
		getAction().avancer(-250, 300, false);
		getAction().rotation(right ? -90 : 90, 300, false);
		avancerJusquaColor("white",2000,350);
		getAction().ouvrirPinces(false);
		getAction().fermerPinces(true);
		getAction().avancer(-600, 300, false);
		getAction().rotation(right ? 90 : -90, 300, false);
		if (!prendrePalet(600,200)) staticEchoue();
		getAction().rotation(right ? -90 : 90, 300, false);
		avancerJusquaColor("white",2000,350);
		getAction().ouvrirPinces(false);
		getAction().fermerPinces(true);
		getAction().travelArc(right ? 1200 : -1200, -400,false);
		strategyAI();
	}
	/**
	 * Stratégie codée en dur pour les 3 premiers palets si on commence ligne noire.
	 * Vu la variabilité des moteurs, elle risque de passer toute seule en IA juste après
	 * avoir marqué le 1er palet.
	 */
	public void strategyMid() {
		
		int beginDroite = (posOtherRobot == LIGNE_YELLOW && right) || (posOtherRobot == LIGNE_RED && !right) ? 1 : -1;
		//beginDroite vaut -1 si on commence à tourner à droite, on oublie pas 
		//que les angles positifs tournent vers la gauche (sens trigo), donc 
		//si on doit commencer à droite, beginDroite vaut -1.
		if (!prendrePalet(600,200)) staticEchoue();
		getAction().rotation(beginDroite * 45, 300, false);
		getAction().avancer(300, 300, false);
		getAction().rotation(beginDroite * -45, 300, false);
		avancerJusquaColor("white",2000,350);
		getAction().ouvrirPinces(false);
		getAction().fermerPinces(true);
		getAction().avancer(-1200, 300, false);
		getAction().rotation(beginDroite * -90, 300, false);
		if (!prendrePalet(600,200)) staticEchoue();
		getAction().avancer(-250, 300, false);
		getAction().rotation(beginDroite * 90, 300, false);
		avancerJusquaColor("white",2000,350);
		getAction().ouvrirPinces(false);
		getAction().fermerPinces(true);
		getAction().avancer(-600, 300, false);
		getAction().rotation(beginDroite * -90, 300, false);
		if (!prendrePalet(600,200)) staticEchoue();
		getAction().rotation(beginDroite * 90, 300, false);
		avancerJusquaColor("white",2000,350);
		getAction().ouvrirPinces(false);
		getAction().fermerPinces(true);
		getAction().avancer(-1000,300,false);
		strategyAI();
	}
	/**
	 * La stratégie IA qui permet au robot de récupérer des palets à l'aide de ses capteurs uniquement
	 */
	public void strategyAI() {
		//on essaie de marquer 4 autres palets en mode "IA"
		for (int i = 0; i < 4; i++) {
			
			//1er bloc : on se situe à peu près au milieu et on essaie de prendre un palet
			//Si la 3eme tentative échoue, on appelle perdu()
			for (int essai = 0; ; essai++) {
				if (essai == 5) { //au 3eme essai
					perdu();
					return;
				}
				
				float minDist = directionNearestObject();
				/*
				 * Si distance minimum est inf à 30, c'est que on a vu un mur. On recule donc.
				 */
				if (minDist < 30) {
					getAction().avancer(-200, 300, false); //on recule un peu
					continue; //Si la distance est inférieure à 30 (faut que ce soit un palet) on refait car ça a pas marché.
				}
				/*
				 * Si ça réussit on tente de prendre le palet sur la distance min perçue + une distance de 100mm de sécurité.
				 * On onblie pas que la dist perçue par le capteur à US est en cm, donc *10 pour avoir en mm.
				 */
				boolean essaiPalet = prendrePalet(minDist*10 + 100,200);
				if (essaiPalet) break; 
			}
			
			//2eme bloc, dès qu'on a un palet on se tourne vers le but et on FONCE.
			resetDirection(); //remet le robot à l'angle 0 initial (donc en face des buts adverses)
			avancerJusquaColor("white",2000,350);
			getAction().ouvrirPinces(false);
			
			//3eme bloc, on referme les pinces tout en ramenant le robot au centre du plateau
			//Pour cela on le fait reculer jusqu'à la ligne noire verticale puis on tourne de 90 et on regarde la distance
			getAction().fermerPinces(true);
			boolean b = avancerJusquaColor("black",-1500,350);
			if (!b) perdu();
			getAction().rotation(90, 300, false);
			float distance = getPerception().distance;
			//on avance de distance - 1000, car si distance affiche 80 cm c'est qu'on est à 800 mm du bord, hors la hauteur 
			//est de 2000mm donc la moitiée est à 1000mm :)
			getAction().avancer(distance*10 - 1000, 100, false); 
			
			//à présent que nous nous sommes replacés au milieu, on boucle.
		}
		System.exit(0);
			
	}
	/**
	 * Méthode appelée si la première partie codée en "dur" échoue. Elle permet au robot de
	 * se resituer au centre du plateau pour passer en stratégie IA.
	 */
	public void staticEchoue() {
		getAction().fermerPinces(true);
		/*
		 * Retourne très vite à la ligne blanche de NOTRE equipe.
		 */
		avancerJusquaColor("white",-2000,350);
		/*
		 * une fois sur la ligne, se tourne vers l'objet le plus proche (donc le mur si distance inférieure à 32).
		 */
		float f = directionNearestObject();
		if (f < 32) {
			/*
			 * Redéfinit notre attribut de direction à 180.
			 */
			getAction().setDirection(180);
		}
		/**
		 * Tourne de 180 puis va à la ligne noire.
		 */
		getAction().rotation(180, 300, false);
		avancerJusquaColor("black",2000,350);
		/**
		 * Passe en stratégie IA.
		 */
		strategyAI();
	}
	/**
	 * Si au cours de la stratégie le robot est perdu, on appelle cette méthode.
	 * La méthode tente de recalibrer un angle de 0 face au but adverse et de
	 * ramener le robot au centre du plateau puis de repartir en mode IA.
	 */
	public void perdu() {
		getAction().fermerPinces(true);
		/*
		 * On essaie de retrouver une ligne noire
		 */
		boolean b = avancerJusquaColor("black",-1500,350);
		if (!b) avancerJusquaColor("black",1500,350);
		/*
		 * On tourne de 90° et on se positionne au milieu de l'autre ligne noire mais
		 * avec le capteur à US cette fois.
		 */
		getAction().rotation(90, 300, false);
		float distance = getPerception().distance;
		getAction().avancer(distance*10 - 1000, 100, false);
		/*
		 * On reprend notre stratégie.
		 */
		strategyAI();
	}
	/**
	 * Message au début juste après avoir lancé le programme.
	 * L'interface demande d'abord de quel côté on part, puis sur quelle ligne commence
	 * l'adversaire pour déterminer de quel côté le robot va faire ses rotations.
	 * Ensuite l'interface propose un "récapitulatif" que nous devons valider.
	 * Enfin, l'interface inique si le robot est pret, et que le code commencer à s'éxecuter dès l'appui sur une touche.
	 */
	public void messageDebut() {
		System.out.println("Cote de depart ?");
		System.out.println("rappel :");
		System.out.println("gauche = ligne   bleue");
		System.out.println("droite = ligne   vert");
		int id = Button.waitForAnyPress();
		switch (id) {
		case Button.ID_LEFT : right = false; break;
		case Button.ID_RIGHT : right = true; break;
		default: System.exit(0);
		}
		System.out.println("\n\n\n\n\n");
		System.out.println("Position autre   robot ?");
		System.out.println("rappel :");
		System.out.println("gauche = rouge");
		System.out.println("centre = noire");
		System.out.println("droit = jaune");
		int id2 = Button.waitForAnyPress();
		switch (id2) {
		case Button.ID_LEFT : posOtherRobot = LIGNE_RED; break;
		case Button.ID_ENTER : posOtherRobot = LIGNE_BLACK; break;
		case Button.ID_RIGHT : posOtherRobot = LIGNE_YELLOW; break;
		default: System.exit(0);
		}
		System.out.println("\n\n\n\n\n");
		System.out.println("cc commence cote "+ (right ? "droit" : "gauche") + " et le robot");
		System.out.println("adverse est sur");
		System.out.println("la ligne "+ (posOtherRobot == LIGNE_RED ? "rouge." : posOtherRobot == LIGNE_BLACK ? "noire." : "jaune."));
		System.out.println("OK pour valider");
		System.out.println("ESCAPE pour quit");
		int id3 = Button.waitForAnyPress();
		if (id3 != Button.ID_ENTER) System.exit(0);
		System.out.println("\n\n\n\n\n");
		System.out.println("Le robot est en");
		System.out.println("pause. Appuyer");
		System.out.println("une touche pour");
		System.out.println("lancer !");
		Button.waitForAnyPress();
		System.out.println("\n\n\n\n\n");
		System.out.println("GO CEDRIC GOOOO");
	}

	
}
