package projections;

import java.io.File;
import java.util.Scanner;

public class Projection {
	protected File statsheet;
	private static final File WSV_FILE = new File("data/weights/winStabilizationValues.csv");
	protected final int POSSIBLE_MINUTES = 3936, TOTAL_GAMES = 82;
	protected String teamName;
	protected String record;
	protected float winsGBL = 0; 
	protected int wholeWinsGBL;
	
	public Projection(File statsheet) {
		this.statsheet = statsheet;
	}
	
	public Projection(String teamName, float winsGBL) {
		this.teamName = teamName;
		this.winsGBL = winsGBL;
		this.wholeWinsGBL = Math.round(this.winsGBL);
		this.record = wholeWinsGBL + " - " + (82 - wholeWinsGBL);
	}
	
	public String getRecord() {
		return record;
	}
	
	public float getWinsGBL() {
		return winsGBL;
	}
	
	public int getWholeWinsGBL() {
		return wholeWinsGBL;
	}
	public void weight(double value) {
		winsGBL += value;
		this.wholeWinsGBL = Math.round(this.winsGBL);
		this.record = wholeWinsGBL + " - " + (82 - wholeWinsGBL);
	}
	//type == 0 -> Provide whole number record, type == 1 -> provide float wins
	public void display(int type) {
		if (type == 0) {
			System.out.print(this.teamName + "'s Projected Record: ");
			System.out.println(this.record == null ? "Not enough information given" : this.record);			
		}
		else {
			System.out.print(this.teamName + "'s Projected Wins: ");
			System.out.println(this.record == null ? "Not enough information given" : this.winsGBL);		
		}
	}
	public String getTeamName() {
		return teamName;
	}
	protected void applyWinStabilization() throws Exception{
		Scanner fileScan = new Scanner(Projection.WSV_FILE);
		while (fileScan.hasNext()) {
			String[] fileLine = fileScan.nextLine().split(",");
			String teamNameWSV = fileLine[0];
			double wSV = Double.parseDouble(fileLine[1]);
		
			if (this.teamName.equalsIgnoreCase(teamNameWSV)) {
				this.weight(wSV);
				break;
			}
		}
		fileScan.close();
	}
}
