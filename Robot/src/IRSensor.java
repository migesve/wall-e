import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.Button;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;

public class IRSensor {

`
    private static float[] distance;
    private static SampleProvider average;
    private static EV3IRSensor distanceSensor;
    
    public IRSensor(Port port){
    	distanceSensor = new EV3IRSensor(port);
    	average = new MeanFilter(distanceSensor.getDistanceMode(), 1);
    	distance=new float[average.sampleSize()];
    }
    
	
}
