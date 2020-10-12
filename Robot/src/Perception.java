
public class Perception {
	private boolean touch;
	EV3TouchSensor touchSensor;
	EV3ColorSensor colorSensor;

	public int getCouleur() {
		Port capteurCouleur = LocalEV3.get().getPort("D");
		colorSensor=new EV3ColorSensor(capteurCouleur);
		return colorSensor.getColorIDMode();
	}


	public boolean getTouche() {
		Port capteurTouche = LocalEV3.get().getPort(1);
		touchSensor=new EV3TouchSensor(capteurTouch);
		if ( touchSensor.getTouchMode == 1)
			this.b=true;
		else
			this.b=false
			return b ;




	}
