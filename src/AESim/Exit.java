package AESim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import antlr.collections.List;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;

public class Exit extends General {

	private Grid<Object> grid;
	private static int numExit;
	private int initPosX;
	private int initPosY;
	private static  double [] [] patientTimes;
	private static int varI;
	private static int currentRun;
	private static int dataNum;
	
	private FileWriter writer;
	
	public Exit(Grid<Object> grid, int currentRun) throws IOException {
		this.grid = grid;
		Exit.currentRun = currentRun;
		patientTimes = new double [1000][1000];
		varI = 0;
		String fileName1 = "C:\\RepastSimphony-2.1\\AESimModel\\Outputs\\patientTimeInSystemRuns\\";		
		String fileName2 = "PatientTimeInSystemRun" + String.valueOf(currentRun)+ ".csv";
		String filePath = fileName1 + fileName2;
		writer = new FileWriter(filePath);
		writer.append("\n Tick, RunNum,Week,Day,hour,Id,TriageColor,TriageNum, TypeArrival,TimeInSys, isBackInBed, patientDoctor, CurrentQueue\n");
		Exit.dataNum = 0;
	}
	
	
	// TODO for now I will use the exit to keep the results and monitoring
	// things. Change it later
	
	
	@ScheduledMethod(start = 29, interval = 30, priority = 7)
	public double[][] getPatientTimesInSys() {
		double[][] times = new double[1][0];

		// repast.simphony.engine.environment.RunInfo
		String fileName1 = "C:\\RepastSimphony-2.1\\AESimModel\\Outputs\\patientTimesRuns\\";

		String fileName2 = "PatientTimesRun" + String.valueOf(currentRun)
				+ ".csv";

		String filePath = fileName1 + fileName2;
		// String filePath=
		// "C:\\RepastSimphony-2.0\\workspace\\AESim\\src\\AESim\\patientTimesRuns\\patientTimesAllRuns.csv";

		// String filePath=
		// "C:\\RepastSimphony-2.0\\workspace\\AESim\\src\\AESim\\PatientsTimes3.csv";

//		for (Object p : getContext().getObjects(Patient.class)) {
//
//			if (p != null) {
//			
//					int row = 0;
//					
//
//					try {
//						FileWriter writer = new FileWriter(filePath);
//
//						writer.append("\n RunNum,WeekIn,DayIn,hourIn,WeekOut,DayOut,hourOut,TriageColor,TypeArrival,PatientNum,TimeInSys \n");
//
//						Patient patient = (Patient) p;
//
//							if (patient.getIsInSystem()) {
//								dataNum++;
//								row++;
//								
//								writer.append(String.valueOf(currentRun));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getWeekInSystem()));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getDayInSystem()));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getHourInSystem()));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getWeekOutSystem()));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getDayOutSystem()));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getHourOutSystem()));
//								writer.append(",");
//
//								writer.append(patient.getTriage());
//								writer.append(",");
//								writer.append(patient.getTypeArrival());
//								writer.append(",");
//								writer.append(patient.getId());
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getTimeInSystem()));
//								writer.append("\n");
//
//							}						
//
//						writer.flush();
//						writer.close();
//
//					}
//
//					catch (IOException e) {
//						System.out.println("IOException: " + e);
//					}
//
//					// times = new float[queueTrolley.getSize()];
//					// for (int i = 0; i < queueTrolley.getSize(); i++) {
//					// times[i] = queueTrolley.
//					// }
//				}
//			}
		
		return times;

	}

	
	
	

	@ScheduledMethod(start = 10, priority = 5, pick = 1, interval = 30)
	public void getResults() throws IOException {
		 
		int j=0;
//		for (int i = 0; i < 100; i++){
//			writer.append(String.valueOf(i));
//			writer.append("\n");
//		}
		
		for (Object p: getContext().getObjects(Patient.class)){		
			if (p!=null){	
				Patient patient = (Patient)p;
				if (patient.getIsInSystem()){
					j++;
				//	int j= patient.getNumPatient();
				//	patientTimes[varI][j]= patient.getTimeInSystem();					
					writer.append(String.valueOf(getTime()));
					writer.append(",");
					writer.append(String.valueOf(currentRun));
					writer.append(",");
					writer.append(String.valueOf(getWeek()));
					writer.append(",");
					writer.append(String.valueOf(getDay()));
					writer.append(",");
					writer.append(String.valueOf(getHour()));
					writer.append(",");
					writer.append(patient.getId());
					writer.append(",");					
					writer.append(patient.getTriage());
					writer.append(",");
					writer.append(String.valueOf(patient.getTriageNum()));					
					writer.append(",");
					writer.append(patient.getTypeArrival());
					writer.append(",");
					writer.append(String.valueOf(patient
							.getTimeInSystem()));
					writer.append(",");
					writer.append(String.valueOf(patient
							.getBackInBed()));
					writer.append(",");
					if (patient.getMyDoctor() == null){
						writer.append("Null");
					} else {
						writer.append(patient.getMyDoctor().getId());
					}
					
					writer.append(",");					
					writer.append(patient
							.getCurrentQueue().getId());
					
					
					writer.append("\n");
				}				 
			}			
		}
//		varI++;

	}

//	@ScheduledMethod(start = 10079, interval = 10080, priority = 7)
//	public double[][] getPatientTimesInSys() {
//		double[][] times = new double[1][0];
//
//		// repast.simphony.engine.environment.RunInfo
//		String fileName1 = "C:\\RepastSimphony-2.1\\AESimModel\\Outputs\\patientTimesRuns\\";
//
//		String fileName2 = "PatientTimesRun" + String.valueOf(currentRun)
//				+ ".csv";
//
//		String filePath = fileName1 + fileName2;
//		// String filePath=
//		// "C:\\RepastSimphony-2.0\\workspace\\AESim\\src\\AESim\\patientTimesRuns\\patientTimesAllRuns.csv";
//
//		// String filePath=
//		// "C:\\RepastSimphony-2.0\\workspace\\AESim\\src\\AESim\\PatientsTimes3.csv";
//
//		for (Object q : getContext().getObjects(Queue.class)) {
//
//			if (q != null) {
//				Queue queueTrolley = (Queue) q;
//				if (queueTrolley.getId().equals("qTrolley ")) {
//					int x = queueTrolley.getX();
//					int y = queueTrolley.getY();
//					grid = getGrid();
//					int row = 0;
//
//					times = new double[queueTrolley.getSize()][2];
//
//					try {
//						FileWriter writer = new FileWriter(filePath);
//
//						writer.append("\n RunNum,WeekIn,DayIn,hourIn,WeekOut,DayOut,hourOut,TriageColor,TypeArrival,PatientNum,TimeInSys \n");
//
//						for (Object p : grid.getObjectsAt(x, y)) {
//
//							if (p != null && p instanceof Patient) {
//								dataNum++;
//								row++;
//								Patient patient = (Patient) p;
//
//								writer.append(String.valueOf(currentRun));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getWeekInSystem()));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getDayInSystem()));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getHourInSystem()));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getWeekOutSystem()));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getDayOutSystem()));
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getHourOutSystem()));
//								writer.append(",");
//
//								writer.append(patient.getTriage());
//								writer.append(",");
//								writer.append(patient.getTypeArrival());
//								writer.append(",");
//								writer.append(patient.getId());
//								writer.append(",");
//								writer.append(String.valueOf(patient
//										.getTimeInSystem()));
//								writer.append("\n");
//
//							}
//						}
//
//						writer.flush();
//						writer.close();
//
//					}
//
//					catch (IOException e) {
//						System.out.println("IOException: " + e);
//					}
//
//					// times = new float[queueTrolley.getSize()];
//					// for (int i = 0; i < queueTrolley.getSize(); i++) {
//					// times[i] = queueTrolley.
//					// }
//				}
//			}
//		}
//		return times;
//
//	}

	@ScheduledMethod(start = 1440, priority = 4)
	public void printResults() {

	}


	public static double[][] getPatientTimes() {
		return patientTimes;
	}

}
