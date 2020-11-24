
public class AgentStrategy extends Agent {
	
	public static final int LIGNE_ROUGE = -1;
	public static final int LIGNE_BLACK = 0;
	public static final int LIGNE_YELLOW = 1;
	public boolean rigth;
	int posOtherRobot;
	public AgentStrategy() {
		super();
		
		posOtherRobot = LIGNE_ROUGE;
		rigth = true;
		strategyMilieu();
		System.exit(0);
	}
	/**
	 * 
	 */
	public void strategyRed() {
		prendrePalet(600,200);
		getAction().rotation(rigth ? -45 : 45, 300, false);
		getAction().avancer(300, 300, false);
		getAction().rotation(rigth ? 45 : -45, 300, false);
		avancerJusquaColor("white",350);
		getAction().ouvrirPinces(false);
		getAction().avancer(-1200, 300, false);
		getAction().rotation(rigth ? 90 : -90, 300, false);
		prendrePalet(600,200);
		getAction().avancer(-250, 300, false);
		getAction().rotation(rigth ? -90 : 90, 300, false);
		avancerJusquaColor("white",350);
		getAction().ouvrirPinces(false);
		getAction().avancer(-600, 300, false);
		getAction().rotation(rigth ? 90 : -90, 300, false);
		prendrePalet(600,200);
		getAction().rotation(rigth ? -90 : 90, 300, false);
		avancerJusquaColor("white",350);
		getAction().ouvrirPinces(false);
		getAction().avancer(-600, 300, false);
		getAction().fermerPinces(false);
		System.exit(0);
	}
	
	/**
	 * 
	 */
	public void strategyMilieu() {
		prendrePalet(600,200);
		getAction().rotation(posOtherRobot * 45, 300, false);
		getAction().avancer(300, 300, false);
		getAction().rotation(posOtherRobot * -45, 300, false);
		avancerJusquaColor("white",350);
		getAction().ouvrirPinces(false);
		getAction().fermerPinces(true);
		getAction().avancer(-1200, 300, false);
		getAction().rotation(posOtherRobot * -90, 300, false);
		prendrePalet(600,200);
		getAction().avancer(-250, 300, false);
		getAction().rotation(posOtherRobot * 90, 300, false);
		avancerJusquaColor("white",350);
		getAction().ouvrirPinces(false);
		getAction().fermerPinces(true);
		getAction().avancer(-600, 300, false);
		getAction().rotation(posOtherRobot * -90, 300, false);
		prendrePalet(600,200);
		getAction().rotation(posOtherRobot * 90, 300, false);
		avancerJusquaColor("white",350);
		getAction().ouvrirPinces(false);
		getAction().fermerPinces(true);
		getAction().avancer(-600, 300, false);
		System.exit(0);
	}
	
	
}
