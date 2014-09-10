package projections;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * Rest Value is the expected number of additional wins granted by the team's rest schedule
 * A positive rest value means the team is expected to win that many more wins, and vice versa for negative
 */
public class RestMetric {

	public static final File CURR_REST_FILE = new File("data/Schedule/rest_analysis_15.csv");
	public static final File PREV_REST_FILE = new File("data/Schedule/rest_analysis_14.csv");
	
	//penalties based on study indicating 7% reduced winrate on back to backs
	public static final double PEN1 = 0.14, PEN2 = 0.105, PEN3 = 0.07, PEN4 = 0.035;
	private String teamName;
	//RV = Rest Value
	private double netRV = 0;
	
	public RestMetric(String teamName) {
		this.teamName = teamName;
	}
	
	public void adjustRV (double value) {
		netRV += value;
	}
	public String getTeamName() {
		return teamName;
	}
	
	public double getNetRV() {
		return netRV;
	}
	
	public void display() {
		System.out.println(this.teamName + " RV: " + this.netRV);
	}
	public static void main(String[] args) throws FileNotFoundException {
		ArrayList<RestMetric> teams = new ArrayList<RestMetric>();
		int currentTeam = 0;
		Scanner fileScan = new Scanner(RestMetric.CURR_REST_FILE);
		//clear first line of file
		String[] fileLine = fileScan.nextLine().split(",");
		
		while (fileScan.hasNext()) {
			//initialize
			fileLine = fileScan.nextLine().split(",");
			
			String teamName = fileLine[0];
			/*
			 * 1 (+ 5)= 4IN5-B2B 
			 * 2 (+ 5)= 3IN4-B2B
			 * 3 (+ 5)= B2B
			 * 4 (+ 5)= 3IN4
			 */
			int ownR1 = Integer.parseInt(fileLine[1]), ownR2 = Integer.parseInt(fileLine[2]);
			int ownR3 = Integer.parseInt(fileLine[3]), ownR4 = Integer.parseInt(fileLine[4]);
			int oppR1 = Integer.parseInt(fileLine[6]), oppR2 = Integer.parseInt(fileLine[7]);
			int oppR3 = Integer.parseInt(fileLine[8]), oppR4 = Integer.parseInt(fileLine[9]);
			double rVal = (oppR1 - ownR1) * RestMetric.PEN1;
			rVal += (oppR2 - ownR2) * RestMetric.PEN2;
			rVal += (oppR3 - ownR3) * RestMetric.PEN3;
			rVal += (oppR4 - ownR4) * RestMetric.PEN4;
			
			teams.add(new RestMetric(teamName));
			teams.get(currentTeam++).adjustRV(rVal);
		}
		fileScan.close();
		
		
		//subtract previous season rest values for delta
		currentTeam = 0;
		fileScan = new Scanner(RestMetric.PREV_REST_FILE);
		//clear first line of file
		fileLine = fileScan.nextLine().split(",");
		
		while (fileScan.hasNext()) {
			//initialize
			fileLine = fileScan.nextLine().split(",");
			
			String teamName = fileLine[0];

			int ownR1 = Integer.parseInt(fileLine[1]), ownR2 = Integer.parseInt(fileLine[2]);
			int ownR3 = Integer.parseInt(fileLine[3]), ownR4 = Integer.parseInt(fileLine[4]);
			int oppR1 = Integer.parseInt(fileLine[6]), oppR2 = Integer.parseInt(fileLine[7]);
			int oppR3 = Integer.parseInt(fileLine[8]), oppR4 = Integer.parseInt(fileLine[9]);
			double rVal = (oppR1 - ownR1) * RestMetric.PEN1;
			rVal += (oppR2 - ownR2) * RestMetric.PEN2;
			rVal += (oppR3 - ownR3) * RestMetric.PEN3;
			rVal += (oppR4 - ownR4) * RestMetric.PEN4;
			
			teams.get(currentTeam++).adjustRV(rVal * -1);
		}
		fileScan.close();
		
		//sort
		ArrayList<RestMetric> sortedList = new ArrayList<RestMetric>();
		int length = teams.size();

		for (int i = 0; i < length; i++) {
			//set default minimum to first element
			int mindex = 0;
			double min = teams.get(mindex).getNetRV();
			for (int j = 1; j < teams.size(); j++) {
				//if current element smaller, set new min
				if (teams.get(j).getNetRV() < min) {
					mindex = j;
					min = teams.get(mindex).getNetRV();
				}
			}
			sortedList.add(teams.remove(mindex));
		}
		for (int i = 0; i < sortedList.size(); i++) {
			sortedList.get(i).display();
		}
	}
	
}
