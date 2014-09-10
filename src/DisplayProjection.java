package projections;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class DisplayProjection {
	private ArrayList<RAPMProjection> rapm_proj = new ArrayList<RAPMProjection>();
	private ArrayList<WPProjection> wp_proj = new ArrayList<WPProjection>();
	private ArrayList<Projection> combined_proj = new ArrayList<Projection>();
	private static ArrayList<RestMetric> rest_values = new ArrayList<RestMetric>();
	
	public final static int TOTAL_GAMES = 82;
	private File sourceDirectory = new File("data");
	
	public DisplayProjection(boolean sorted) throws FileNotFoundException {
		for (File file : sourceDirectory.listFiles()) {
			if (file.isFile() & file.getName().endsWith("RAPM.csv")) rapm_proj.add(new RAPMProjection(file));
			if (file.isFile() & file.getName().endsWith("WP.csv")) wp_proj.add(new WPProjection(file));
		}
		if (sorted) {
			rapm_proj = (ArrayList<RAPMProjection>) sortByRecord(this.rapm_proj);
			wp_proj = (ArrayList<WPProjection>) sortByRecord(this.wp_proj);
		}
		getRestValues();
		combineProjections();
		incRestValue(this.combined_proj);
		combined_proj = (ArrayList<Projection>) sortByRecord(this.combined_proj);
	
	}
	private void getRestValues() throws FileNotFoundException {
		//current season rest values
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
			
			rest_values.add(new RestMetric(teamName));
			rest_values.get(currentTeam++).adjustRV(rVal);
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

			int ownR1 = Integer.parseInt(fileLine[1]), ownR2 = Integer.parseInt(fileLine[2]);
			int ownR3 = Integer.parseInt(fileLine[3]), ownR4 = Integer.parseInt(fileLine[4]);
			int oppR1 = Integer.parseInt(fileLine[6]), oppR2 = Integer.parseInt(fileLine[7]);
			int oppR3 = Integer.parseInt(fileLine[8]), oppR4 = Integer.parseInt(fileLine[9]);
			double rVal = (oppR1 - ownR1) * RestMetric.PEN1;
			rVal += (oppR2 - ownR2) * RestMetric.PEN2;
			rVal += (oppR3 - ownR3) * RestMetric.PEN3;
			rVal += (oppR4 - ownR4) * RestMetric.PEN4;
			
			rest_values.get(currentTeam++).adjustRV(rVal * -1);
		}
		fileScan.close();
		
	}
	//Incorporate rest values
	private static void incRestValue(ArrayList<? extends Projection> projections) {
		for (int i = 0; i < projections.size(); i++) {
			for (int j = 0; j < rest_values.size(); j++) {
				if (projections.get(i).getTeamName().equals(rest_values.get(j).getTeamName())) {
					projections.get(i).weight(rest_values.get(j).getNetRV());
				}
			}
		}
	}
	//sort
	private ArrayList<? extends Projection> sortByRecord(ArrayList<? extends Projection> projections) {
		ArrayList<Projection> sortedList = new ArrayList<Projection>();
		int length = projections.size();

		for (int i = 0; i < length; i++) {
			//set default minimum to first element
			int mindex = 0;
			float min = projections.get(mindex).getWinsGBL();
			for (int j = 1; j < projections.size(); j++) {
				//if current element smaller, set new min
				if (projections.get(j).getWinsGBL() < min) {
					mindex = j;
					min = projections.get(mindex).getWinsGBL();
				}
			}
			sortedList.add(projections.remove(mindex));
		}
		return sortedList;
	}
	
	public float winsGBP(String name, int type) {
		if (type == 0) {
			for (int i = 0; i < rapm_proj.size(); i++) {
				if (rapm_proj.get(i).getWinsGBP().get(name) != null) {
					return rapm_proj.get(i).getWinsGBP().get(name);
				}
			}
		}
		else {
			for (int i = 0; i < wp_proj.size(); i++) {
				if (wp_proj.get(i).getWinsGBP().get(name) != null) {
					return wp_proj.get(i).getWinsGBP().get(name);
				}
			}
		}
		return -1;
	}
	
	//type 0 == RAPM, type 1 == WP
	public int getTotalWins(ArrayList<? extends Projection> projections) {
		int total = 0;
		
		for (int i = 0; i < projections.size(); i++) {
			total += projections.get(i).getWholeWinsGBL();
		}

		return total;
	}
	//only use on sorted projections where both lists contain same teams
	private void combineProjections() {
		if (rapm_proj.size() != wp_proj.size()) return;
		
		for (int i = 0; i < rapm_proj.size(); i++) {
			for (int j = 0; j < wp_proj.size(); j++) {
				if (rapm_proj.get(i).getTeamName().equals(wp_proj.get(j).getTeamName())) {
					combined_proj.add(new Projection(rapm_proj.get(i).getTeamName(), (rapm_proj.get(i).getWinsGBL() + wp_proj.get(j).getWinsGBL()) / 2));
					break;
				}				
			}
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		DisplayProjection records = new DisplayProjection(true);
		ArrayList<Projection> recordsOI = records.combined_proj;
		//display
		for (int i = 0; i < recordsOI.size(); i++) recordsOI.get(i).display(0);
		System.out.println();
		
		System.out.println("Wins: " + records.getTotalWins(recordsOI));
		System.out.println("Losses: " + (recordsOI.size() * TOTAL_GAMES - records.getTotalWins(recordsOI)));
		System.out.println();
		/*
		
		
		//search for player
		Scanner input = new Scanner(System.in);
		//type 0 == RAPM search, 1 == WP search;
		int type = 1;
		while (true) {
			System.out.print("Search for wins generated by player (n to exit): ");
			String playerSearch = input.nextLine();
			if (playerSearch.equals("n")) break;
			System.out.println("Wins generated by " + playerSearch + ": " + records.winsGBP(playerSearch, type));
		}
		input.close();
		*/
		
	}

}
