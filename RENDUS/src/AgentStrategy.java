import lejos.hardware.*;
/**
 * Classe qui ordonne les m�thodes d'Agent, nos strat�gies.
 * 
 * <b> D�pendance : les m�thodes de cette classe sont utilis�es dans <code>Agent</code> uniquement. </b>
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
	 * Strat�gie cod�e en dur pour les 3 premiers palets si on commence ligne rouge.
	 * Vu la variabilit� des moteurs, elle risque de passer toute seule en IA juste apr�s
	 * avoir marqu� le 1er palet.
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
	 * Strat�gie cod�e en dur pour les 3 premiers palets si on commence ligne noire.
	 * Vu la variabilit� des moteurs, elle risque de passer toute seule en IA juste apr�s
	 * avoir marqu� le 1er palet.
	 */
	public void strategyMid() {
		
		int beginDroite = (posOtherRobot == LIGNE_YELLOW && right) || (posOtherRobot == LIGNE_RED && !right) ? 1 : -1;
		//beginDroite vaut -1 si on commence � tourner � droite, on oublie pas 
		//que les angles positifs tournent vers la gauche (sens trigo), donc 
		//si on doit commencer � droite, beginDroite vaut -1.
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
	 * La strat�gie IA qui permet au robot de r�cup�rer des palets � l'aide de ses capteurs uniquement
	 */
	public void strategyAI() {
		//on essaie de marquer 4 autres palets en mode "IA"
		for (int i = 0; i < 4; i++) {
			
			//1er bloc : on se situe � peu pr�s au milieu et on essaie de prendre un palet
			//Si la 3eme tentative �choue, on appelle perdu()
			for (int essai = 0; ; essai++) {
				if (essai == 5) { //au 3eme essai
					perdu();
					return;
				}
				
				float minDist = directionNearestObject();
				/*
				 * Si distance minimum est inf � 30, c'est que on a vu un mur. On recule donc.
				 */
				if (minDist < 30) {
					getAction().avancer(-200, 300, false); //on recule un peu
					continue; //Si la distance est inf�rieure � 30 (faut que ce soit un palet) on refait car �a a pas march�.
				}
				/*
				 * Si �a r�ussit on tente de prendre le palet sur la distance min per�ue + une distance de 100mm de s�curit�.
				 * On onblie pas que la dist per�ue par le capteur � US est en cm, donc *10 pour avoir en mm.
				 */
				boolean essaiPalet = prendrePalet(minDist*10 + 100,200);
				if (essaiPalet) break; 
			}
			
			//2eme bloc, d�s qu'on a un palet on se tourne vers le but et on FONCE.
			resetDirection(); //remet le robot � l'angle 0 initial (donc en face des buts adverses)
			avancerJusquaColor("white",2000,350);
			getAction().ouvrirPinces(false);
			
			//3eme bloc, on referme les pinces tout en ramenant le robot au centre du plateau
			//Pour cela on le fait reculer jusqu'� la ligne noire verticale puis on tourne de 90 et on regarde la distance
			getAction().fermerPinces(true);
			boolean b = avancerJusquaColor("black",-1500,350);
			if (!b) perdu();
			getAction().rotation(90, 300, false);
			float distance = getPerception().distance;
			//on avance de distance - 1000, car si distance affiche 80 cm c'est qu'on est � 800 mm du bord, hors la hauteur 
			//est de 2000mm donc la moiti�e est � 1000mm :)
			getAction().avancer(distance*10 - 1000, 100, false); 
			
			//� pr�sent que nous nous sommes replac�s au milieu, on boucle.
		}
		System.exit(0);
			
	}
	/**
	 * M�thode appel�e si la premi�re partie cod�e en "dur" �choue. Elle permet au robot de
	 * se resituer au centre du plateau pour passer en strat�gie IA.
	 */
	public void staticEchoue() {
		getAction().fermerPinces(true);
		/*
		 * Retourne tr�s vite � la ligne blanche de NOTRE equipe.
		 */
		avancerJusquaColor("white",-2000,350);
		/*
		 * une fois sur la ligne, se tourne vers l'objet le plus proche (donc le mur si distance inf�rieure � 32).
		 */
		float f = directionNearestObject();
		if (f < 32) {
			/*
			 * Red�finit notre attribut de direction � 180.
			 */
			getAction().setDirection(180);
		}
		/**
		 * Tourne de 180 puis va � la ligne noire.
		 */
		getAction().rotation(180, 300, false);
		avancerJusquaColor("black",2000,350);
		/**
		 * Passe en strat�gie IA.
		 */
		strategyAI();
	}
	/**
	 * Si au cours de la strat�gie le robot est perdu, on appelle cette m�thode.
	 * La m�thode tente de recalibrer un angle de 0 face au but adverse et de
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
		 * On tourne de 90� et on se positionne au milieu de l'autre ligne noire mais
		 * avec le capteur � US cette fois.
		 */
		getAction().rotation(90, 300, false);
		float distance = getPerception().distance;
		getAction().avancer(distance*10 - 1000, 100, false);
		/*
		 * On reprend notre strat�gie.
		 */
		strategyAI();
	}
	/**
	 * Message au d�but juste apr�s avoir lanc� le programme.
	 * L'interface demande d'abord de quel c�t� on part, puis sur quelle ligne commence
	 * l'adversaire pour d�terminer de quel c�t� le robot va faire ses rotations.
	 * Ensuite l'interface propose un "r�capitulatif" que nous devons valider.
	 * Enfin, l'interface inique si le robot est pret, et que le code commencer � s'�xecuter d�s l'appui sur une touche.
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
