import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class TripPoint {

	private double lat;	// latitude
	private double lon;	// longitude
	private int time;	// time in minutes
	
	private static ArrayList<TripPoint> trip;	// ArrayList of every point in a trip
	private static ArrayList<TripPoint> movingTrip;

	/**
	 * default constructor for TripPoint
	 */
	public TripPoint() {
		time = 0;
		lat = 0.0;
		lon = 0.0;
	}
	
	/**
	 * Constructor for TripPoint
	 * @param time
	 * @param lat
	 * @param lon
	 */
	public TripPoint(int time, double lat, double lon) {
		this.time = time;
		this.lat = lat;
		this.lon = lon;
	}
	
	/**
	 * getter for time
	 * @return time in minutes
	 */
	public int getTime() {
		return time;
	}
	
	/**
	 * getter for latitude
	 * @return latitude in degrees
	 */
	public double getLat() {
		return lat;
	}
	
	/**
	 * getter for longitude
	 * @return longitude in degrees
	 */
	public double getLon() {
		return lon;
	}
	
	/**
	 * getter for array list of TripPoints
	 * @return array list of TripPoints
	 */
	public static ArrayList<TripPoint> getTrip() {
		return new ArrayList<>(trip);
	}
	
	/**
	 * calculates the distance between two trip points using haversine
	 * @param a
	 * @param b
	 * @return distance between the two parameters in kilometers; given as a double
	 */
	public static double haversineDistance(TripPoint first, TripPoint second) {
		// distance between latitudes and longitudes
		double lat1 = first.getLat();
		double lat2 = second.getLat();
		double lon1 = first.getLon();
		double lon2 = second.getLon();
		
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) *
                   Math.cos(lat1) *
                   Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
	}
	
	/**
	 * calculates average speed between two points
	 * @param a
	 * @param b
	 * @return average speed between two points in km/hr; given as a double
	 */
	public static double avgSpeed(TripPoint a, TripPoint b) {
		
		int timeInMin = Math.abs(a.getTime() - b.getTime());
		
		double dis = haversineDistance(a, b);
		
		double kmpmin = dis / timeInMin;
		
		return kmpmin*60;
	}
	
	/**
	 * calculates total time of trip in hours
	 * @return total time of trip in hours; given as a double
	 */
	public static double totalTime() {
		int minutes = trip.get(trip.size()-1).getTime();
		double hours = minutes / 60.0;
		return hours;
	}
	
	/**
	 * calculates the total distance of the trip 
	 * @return total distance of trip in km; given as a double
	 */
	public static double totalDistance() throws FileNotFoundException, IOException {
		
		double distance = 0.0;
		
		if (trip.isEmpty()) {
			readFile("triplog.csv");
		}
		
		for (int i = 1; i < trip.size(); ++i) {
			distance += haversineDistance(trip.get(i-1), trip.get(i));
		}
		
		return distance;
	}
	
	/**
	 * Overrides toString
	 * @return null/nothing
	 */
	public String toString() {
		
		return null;
	}

	/**
	 * parses the file into TripPoint objects and adds it to ArrayList trip
	 * @param filename
	 * @throws IOException
	 */
	public static void readFile(String filename) throws FileNotFoundException, IOException {

		// construct a file object for the file with the given name.
		File file = new File(filename);

		// construct a scanner to read the file.
		Scanner fileScanner = new Scanner(file);
		
		// initiliaze trip
		trip = new ArrayList<TripPoint>();

		// create the Array that will store each lines data so we can grab the time, lat, and lon
		String[] fileData = null;

		// grab the next line
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();

			// split each line along the commas
			fileData = line.split(",");

			// only write relevant lines
			if (!line.contains("Time")) {
				// fileData[0] corresponds to time, fileData[1] to lat, fileData[2] to lon
				trip.add(new TripPoint(Integer.parseInt(fileData[0]), Double.parseDouble(fileData[1]), Double.parseDouble(fileData[2])));
			}
		}

		// close scanner
		fileScanner.close();
	}
	
	/**
	 * creates a copy of movingTrip and returns it
	 * @return a copy of the moving trip arraylist
	 */
	public static ArrayList<TripPoint> getMovingTrip() {
		return new ArrayList<>(movingTrip);
	}
	
	/**
	 * find the average speed while moving
	 * @return average moving speed
	 */
	public static double avgMovingSpeed() {
		double total = 0;
		
		//finds total distance
		for (int i = 1; i < movingTrip.size(); i++) {
			total += haversineDistance(movingTrip.get(i-1), movingTrip.get(i));
		}
		
		return total/movingTime();
	}
	
	/**
	 * calculates time spent moving in hours
	 * @return time spent moving in hours
	 */
	public static double movingTime() {
		System.out.println(movingTrip.size());
		return ((movingTrip.size()-1)*5)/60.0;
	}
	
	/**
	 * find the amount of time spent stopped
	 * @return time stopped in hours
	 */
	public static double stoppedTime() {
		return totalTime()-movingTime();
	}
	
	/**
	 * heuristic that finds and returns the number of stops in the trip. It uses a displacement threshold of 0.6km and initializes movingtrip array
	 * @return number of stops according to heuristic 1
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static int h1StopDetection() throws FileNotFoundException, IOException {
		
		if (trip == null || trip.isEmpty()) {
			readFile("triplog.csv");
		}
		
		int trips = 0;
		movingTrip = new ArrayList<TripPoint>();
		movingTrip.add(trip.get(0));
		
		for (int i = 1; i < trip.size(); i++) {
			if (haversineDistance(trip.get(i), trip.get(i-1)) <= 0.6) {
				trips++;
			} else {
				movingTrip.add(trip.get(i));
			}
		}
		
		return trips;
	}
	
	/**
	 * heuristic that finds and returns the number of stops in the trip. It uses a displacement threshold of 0.5km and compares three points; also initializes movingtrip array
	 * @return number of stops according to heuristic 2
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static int h2StopDetection() throws FileNotFoundException, IOException {
		
		if (trip == null || trip.isEmpty()) {
			readFile("triplog.csv");
		}
		
		movingTrip = new ArrayList<TripPoint>();
		ArrayList<TripPoint> stopTrips = new ArrayList<TripPoint>();
		int trips = 0;
		
		for (int i = 1; i < trip.size()-1; i++) {
			
			//compares 1 & 3, and 1 & 2 & 3 where 2 is the og index
			if ((haversineDistance(trip.get(i), trip.get(i+1)) <= 0.5 && haversineDistance(trip.get(i), trip.get(i-1)) <= 0.5) ||
					haversineDistance(trip.get(i-1), trip.get(i+1)) <= 0.5) {
				
				
				if (!(stopTrips.contains(trip.get(i-1)))) {
					stopTrips.add(trip.get(i-1));
				}
				
				if (!(stopTrips.contains(trip.get(i)))) {
					stopTrips.add(trip.get(i));
				}
				if (!(stopTrips.contains(trip.get(i+1)))) {
					stopTrips.add(trip.get(i+1));
				}
				
			}
				
		}
		
		for (int j = 0; j < trip.size(); j++) {
			if (!(stopTrips.contains(trip.get(j)))) {
				movingTrip.add(trip.get(j));
			}
				
		}
			
		return stopTrips.size();
		
	}
		
		
}

