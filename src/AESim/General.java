package AESim;

import java.io.BufferedReader;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import cern.jet.random.Exponential;
import cern.jet.random.Uniform;

public class General {
	private String id;
	private static Grid<Object> grid;
	private GridPoint loc;
	private int x;
	private int y;
	private double averageTimeAllPatients;
	private static double minute = 0;
	private static int hour = 0;
	private static int day = 0;
	private static int week = 0;
	private static int dayTotal = 0; /*
									 * the total number of days simulation is
									 * running
									 */
	private static float[][] matrixNurse;
	private static float[][] matrixNurse1;
	private static float[][] matrixNurse2;
	private static float[][] matrixNurse3;
	private static float[][] matrixNurse4;
	private static float[][] matrixNurse5;
	private static float[][] arrayDNW; 

	private static float[][] matrixClerk1;
	private static float[][] matrixClerk2;

	private static float[][] matrixRecepcionist; // The consultant doctor
													// doesn't have matrix
													// because the number is
													// constant
	private static float[][] matrixArrivalWalkIn;
	private static float[][] matrixArrivalAmbulance;
	private static float[][] matrixTriagePropByArrival;
	private static float[][] matrixPropTest;

	private static float[][] matrixSHO;
	private static float[][] matrixSHOD0;
	private static float[][] matrixSHOD1;
	private static float[][] matrixSHOD2;
	private static float[][] matrixSHOD3;
	private static float[][] matrixSHOD4;
	private static float[][] matrixSHOD5;
	private static float[][] matrixSHOD6;
	private static float[][] matrixSHOD7;
	private static float[][] matrixSHOD8;
	private static float[][] matrixSHOD9;

	private double zLinear;
	private double zLogistic;
	private double zBipolar;
	private double zHyperTan;
	private double vPhysis;
	private double vEmotion;
	private double vCognition;
	private double vSocial;
	private double[] tTreatment = new double[3];
	private double[] tReasesment = new double[3];
	private double[] tTriage = new double[3];
	private double[] tRegistration = new double[3];
	
	private ArrayList<Double> obsLogArray = new ArrayList<>();
	private ArrayList<Double> obsTriangArray = new ArrayList<>();
	private ArrayList<Double> obsExponentialArray = new ArrayList<>();
	
	private ArrayList<Double> triangularData = new ArrayList<Double>();
	
	protected static  ArrayList<Patient> patientsWaitingForCubicle = new ArrayList<Patient>();

	public General() {
		
	}

	public Context<Object> getContext() {
		Context<Object> context = ContextUtils.getContext(this);
		return context;
	}

	public Grid<Object> getGrid() {
		Grid<Object> grid = (Grid) ContextUtils.getContext(this).getProjection(
				"grid");

		return grid;
	}

	public Uniform uniform(double min, double max) {
		Uniform uniform = RandomHelper.createUniform(min, max);
		return uniform;
	}

	public Exponential exponential(double mean) {
		double lambda = 1 / mean;
		Exponential exponential = RandomHelper.createExponential(lambda);
		return exponential;
	}

	public double distTriangular(double min, double mode, double max) {
		/*
		 * This method works fine. Data is fitted in StatFit and works fine
		 */
		// for the triangular distribution with domain [a, b] and mode (or shape
		// parameter) m, where a <= m <= b.
		// F-1(u) = a + ((b - a)(m - a)u)1/2 if 0 <= u <= (m - a)/(b - a),
		// F-1(u) = b - ((b - a)(b - m)(1 - u))1/2 if (m - a)/(b - a <= u <= 1.

		// double constant= (mode-min)/(max-min);
		// double random = Math.random();
		//
		// double inverseF1= (min+Math.sqrt((constant*random)));
		// double inverseF2= max-Math.sqrt(constant*(1-random));
		//
		// double triangularObs=0 ;
		// if (random <= constant)
		// triangularObs=inverseF1;
		// else
		// triangularObs= inverseF2;
		//
		// return triangularObs;
		// double mode= 3*mean-min+max;
		double T = 0;
		double obs = 0;
		double constant = (mode - min) / (max - min);
		double random = Math.random();
		double T1 = (Math.sqrt((constant * random)));
		double T2 = 1 - Math.sqrt((1 - constant) * (1 - random));

		if (random < (constant))
			T = T1;
		else
			T = T2;

		obs = min + (max - min) * T;

		return obs;

	}

	public double distLognormal(double min, double mean, double max) {
		// i have checked and this method works well
		double average = mean;
		double stdDev = max;

		// double average=10;
		//
		// double stdDev=1;

		double mcuadrado = Math.pow(average, 2);

		double v = stdDev;

		double a = mcuadrado / (Math.sqrt(v + mcuadrado));

		double scale = Math.log(a);

		double shape = Math.sqrt(Math.log(1 + (v / mcuadrado)));

		LogNormalDistribution log = new LogNormalDistribution(scale, shape,
				0.95);

		double obs = (log.sample());

		// obsLogArray.add(obs);
		// TODO CHANGE OBS SIN LOGARITMO
		// System.out.println(" obs lognormales array " + obsLogArray+ " " +
		// this.getId());

		return obs;
	}

	@ScheduledMethod(start = 10, interval = 10, pick = 1)
	public double calculateAverageTimeAllPatients() {
		double sumTimeInSys = 0;
		double totalPatients = 0;

		Context<Object> context = this.getContext();
		Patient patient = null;
		for (Object o : context.getObjects(Patient.class)) {
			if (o != null) {
				patient = (Patient) o;
				if (patient.getIsInSystem()) {
					sumTimeInSys += patient.getTimeInSystem();
					totalPatients++;

				}
			}

		}
		if (totalPatients != 0) {
			averageTimeAllPatients = sumTimeInSys / totalPatients;
		}
		return averageTimeAllPatients;

	}

	public void printTime() {
		System.out
				.println("                                                                              tick: "
						+ getTime()
						+ " (week: "
						+ getWeek()
						+ " day: "
						+ getDay()
						+ " hour: "
						+ getHour()
						+ " minute: "
						+ getMinute() + ")");
	}

	public void printElementsQueue(PriorityQueue<Patient> queueToPrint,
			String name) {

		Patient patientQueuing = null;
		Iterator<Patient> iter = queueToPrint.iterator();

		String a = "[";
		while (iter.hasNext()) {
			Patient elementInQueue = iter.next();

			if (elementInQueue instanceof Patient) {
				patientQueuing = elementInQueue;
				a = a + patientQueuing.getId() + ", ";
			}

		}
		if (a.length() > 2)
			System.out.println("" + this.getId() + " " + name + ": "
					+ a.substring(0, a.length() - 2) + "]");
	}

	public void printElementsArray(ArrayList<Patient> arrayToPrint, String name) {

		Patient patientQueuing = null;
		Iterator<Patient> iter = arrayToPrint.iterator();

		String a = "[";
		while (iter.hasNext()) {
			Patient elementInQueue = iter.next();

			if (elementInQueue instanceof Patient) {
				patientQueuing = elementInQueue;
				a = a + patientQueuing.getId() + ", ";
			}

		}
		if (a.length() > 2)
			System.out.println("" + this.getId() + " " + name + ": "
					+ a.substring(0, a.length() - 2) + "]");
	}

	@ScheduledMethod(start = 60, interval = 60, pick = 1, priority = 90)
	public void increaseTime() {
		/* Increase hour of the day by 1 */

		hour++;
		if (hour == 24) {
			hour = 0;
			day++;
			if (day == 7) {
				day = 0;
				// Day 0: Monday ... Day=6: Sunday
				week++;
				if (week == 52)
					week = 0;
			}
		}

	}

	public final String getId() {
		return id;
	}

	public final GridPoint getLoc(Grid grid) {
		loc = grid.getLocation(this);
		return loc;
	}

	public GridPoint getQueueLocation(String name, Grid grid) {
		GridPoint queueLoc = null;
		Queue queueR = null;
		Context<Object> context = ContextUtils.getContext(this);

		for (Object o : context.getObjects(Queue.class)) {
			queueR = (Queue) o;
			if (queueR.getName() == name) {
				queueLoc = grid.getLocation(o);
				// System.out.println("**** "+ queueR.getId()+ " "
				// + queueLoc);
				break;
			}

		}
		return queueLoc;
	}

	public Resource findResourceAvailable(String resourceType) {
		Resource rAvailable = null;
		Context<Object> context = getContext();
		for (Object o : context.getObjects(Resource.class)) {
			Resource resource = (Resource) o;
			if (resource.getResourceType() == resourceType) {
				System.out.println("resource type? " + resourceType
						+ " is required here, looking if " + resource.getId()
						+ " is available? " + resource.getAvailable());
				if (resource.getAvailable() == true) {
					rAvailable = resource;
					break;
				} else {
					General g= resource.checkWhoInResource();
				}
			}
		}
		return rAvailable;
	}

	public double getTime() {
		double time = (RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount());
		return time;
	}

	public final int getX() {

		return this.loc.getX();
	}

	public final int getY() {
		return this.loc.getY();
	}

	public GridPoint moveTo(Grid<Object> grid, GridPoint newLoc) {
		GridPoint loc = newLoc;
		grid.moveTo(this, loc.getX(), loc.getY());
		this.setLoc(grid.getLocation(this));
		return this.getLoc(grid);

	}

	/**
	 * Hola Paula Que mas
	 * 
	 * @throws IOException
	 */

	@ScheduledMethod(start = 0, pick = 1, priority = 120)
	public void readAllData() throws IOException {
		System.out.println("data worked? ");
		setMatrixArrivalWalkIn(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\DatosIn.txt",
				24, 7));
		setMatrixArrivalAmbulance(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\DatosAmbulance.txt",
				24, 7));
		setMatrixClerk1(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosClerk1.txt",
				24, 7));
		setMatrixClerk2(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosClerk2.txt",
				24, 7));
		setMatrixNurse(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosNurse.txt",
				24, 7));
		setMatrixNurse1(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosNurse1.txt",
				24, 7));
		setMatrixNurse2(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosNurse2.txt",
				24, 7));
		setMatrixNurse3(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosNurse3.txt",
				24, 7));
		setMatrixNurse4(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosNurse4.txt",
				24, 7));
		setMatrixNurse5(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosNurse5.txt",
				24, 7));
		setArrayDNW(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosDNW.txt",
				24,1));
		setMatrixSHO(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO.txt",
				24, 7));

		setMatrixSHOD0(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO_D0.txt",
				24, 7));
		setMatrixSHOD1(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO_D1.txt",
				24, 7));
		setMatrixSHOD2(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO_D2.txt",
				24, 7));
		setMatrixSHOD3(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO_D3.txt",
				24, 7));
		setMatrixSHOD4(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO_D4.txt",
				24, 7));
		setMatrixSHOD5(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO_D5.txt",
				24, 7));
		setMatrixSHOD6(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO_D6.txt",
				24, 7));
		setMatrixSHOD7(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO_D7.txt",
				24, 7));
		setMatrixSHOD8(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO_D8.txt",
				24, 7));
		setMatrixSHOD9(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosSHO_D9.txt",
				24, 7));

		setMatrixRecepcionist(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosrecepcionist.txt",
				24, 7));
		setMatrixTriagePropByArrival(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosTriageByArrival.txt",
				5, 2));
		// En la MatrixTriagePropArrival la primera columna corresponde a WalkIn
		// y la segunda a Ambulance, son las distribuciones acumuladas
		setMatrixPropTest(readFileIn(
				"C:\\RepastSimphony-2.1\\AESimModel\\src\\AESim\\datosProportionsTests.txt",
				5, 2));
		// En la MatrixPropTest la primera columna corresponde a XRay y la
		// segunda a Test
	}

	public float findMax(float matrix[][]) {
		float max = matrix[0][0];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (matrix[i][j] > max) {
					max = matrix[i][j];
				}
			}
		}
		return max;
	}

	public float[][] readFileIn(String fileName, int rows, int cols)
			throws IOException {
		String arrivalMatrix[][] = new String[rows][cols];
		float[][] arrivalMatrixfloat = new float[rows][cols];
		String result = "";
		String line, token, delimiter = ",";

		StringTokenizer tokenizer;

		BufferedReader input = null;
		int i = 0;
		int j = 0;
		try {

			input = new BufferedReader(new FileReader(fileName));
			line = input.readLine(); // when printed gives first line in file

			// outer while (process lines)
			while (line != null) { // doesn't seem to start from first line
				tokenizer = new StringTokenizer(line, delimiter);

				while (tokenizer.hasMoreTokens()) {// process tokens in line
					token = tokenizer.nextToken();
					arrivalMatrix[i][j] = token;
					j++;
				}// close inner while
				j = 0;
				line = input.readLine(); // next line
				i++;
			}// close outer while

		} catch (FileNotFoundException e) {
			System.out.println("Unable to open file " + fileName);
		} catch (IOException e) {
			System.out.println("Unable to read from file " + fileName);
		} finally {

			// Close the file
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				System.out.println("Unable to close file " + fileName);
			}

		}

		// float pos1;

		// for (int a = 0; a < 24; a++) {
		// for (int b = 0; b < 7; b++) {
		// System.out.print(arrivalMatrix[a][b] + '\t' + " ");
		// // arrivalMatrixfloat [a][b]= (arrivalMatrix[a][b]).parseIn;
		// //
		//
		// }
		// System.out.println(" ");
		//
		// }
//		System.out.println('\n');
		for (int a = 0; a < rows; a++) {
			for (int b = 0; b < cols; b++) {

				arrivalMatrixfloat[a][b] = Float
						.parseFloat(arrivalMatrix[a][b]);

//				System.out.print(" " + arrivalMatrixfloat[a][b] + '\t' + " ");

			}

//			System.out.println(" ");

		}
		// pos1 = Float.parseFloat(arrivalMatrix[0][0]);
		// float pos2 = arrivalMatrixfloat[0][0] + arrivalMatrixfloat[0][1];
		// System.out.println(" String: " + pos1 + " float? " + pos2);

		return arrivalMatrixfloat;
	}

	// Transfer Functions

	public double linearTransfer(double u) {
		zLinear = u;

		return zLinear;
	}

	public static void initSaticVar() {
		setHour(0);
		setDay(0);
		setWeek(0);
		setDayTotal(0);
	}

	public double logisticTransfer(double a, double c, double z) {
		zLogistic = 1 / (1 + Math.exp((-a * (z - c))));

		return zLogistic;
	}

	public double bipolarTransfer(double u) {
		zBipolar = ((2 / (1 + Math.exp(-u))) - 1);

		return zBipolar;
	}

	public double hyperTan(double u) {
		zHyperTan = Math.tanh(u);

		return zHyperTan;
	}

	// Getters and setters
	public final void setId(String id) {
		this.id = id;
	}

	public final void setLoc(GridPoint loc) {
		this.loc = loc;
	}

	public final void setX(int x) {
		this.x = x;
	}

	public final void setY(int y) {
		this.y = y;
	}

	// @ScheduledMethod(start = 0, interval = 1, duration = 50)
	// public void Step() {
	//
	// }

	public static final int getHour() {
		return hour;
	}

	public static final void setHour(int hour) {
		General.hour = hour;
	}

	public static final int getDay() {
		return day;
	}

	public static final void setDay(int day) {
		General.day = day;
	}

	public static final int getWeek() {
		return week;
	}

	public static final void setWeek(int week) {
		General.week = week;
	}

	public static final int getDayTotal() {
		return dayTotal;
	}

	public static final void setDayTotal(int dayTotal) {
		General.dayTotal = dayTotal;
	}

	public static float[][] getMatrixArrivalWalkIn() {
		return matrixArrivalWalkIn;
	}

	public static void setMatrixArrivalWalkIn(float[][] matrixArrivalIn) {
		General.matrixArrivalWalkIn = matrixArrivalIn;
	}

	public static float[][] getMatrixArrivalAmbulance() {
		return matrixArrivalAmbulance;
	}

	public static void setMatrixArrivalAmbulance(
			float[][] matrixArrivalAmbulance) {
		General.matrixArrivalAmbulance = matrixArrivalAmbulance;
	}

	public final double getzLinear() {
		return zLinear;
	}

	public final void setzLinear(double zLinear) {
		this.zLinear = zLinear;
	}

	public final double getzLogistic() {
		return zLogistic;
	}

	public final void setzLogistic(double zLogistic) {
		this.zLogistic = zLogistic;
	}

	public final double getzBipolar() {
		return zBipolar;
	}

	public final void setzBipolar(double zBipolar) {
		this.zBipolar = zBipolar;
	}

	public final double getzHyperbolicT() {
		return zHyperTan;
	}

	public final void setzHyperbolicT(double zHyperbolicT) {
		this.zHyperTan = zHyperbolicT;
	}

	public final double getvPhysis() {
		return vPhysis;
	}

	public final void setvPhysis(double vPhysis) {
		this.vPhysis = vPhysis;
	}

	public final double getvEmotion() {
		return vEmotion;
	}

	public final void setvEmotion(double vEmotion) {
		this.vEmotion = vEmotion;
	}

	public final double getvCognition() {
		return vCognition;
	}

	public final void setvCognition(double vCognition) {
		this.vCognition = vCognition;
	}

	public final double getvSocial() {
		return vSocial;
	}

	public final void setvSocial(double vSocial) {
		this.vSocial = vSocial;
	}

	public static float[][] getMatrixNurse() {
		return matrixNurse;
	}

	public static void setMatrixNurse(float[][] matrixStaff) {
		General.matrixNurse = matrixStaff;
	}

	public static float[][] getMatrixSHO() {
		return matrixSHO;
	}

	public static void setMatrixSHO(float[][] matrixSHO) {
		General.matrixSHO = matrixSHO;
	}

	public static float[][] getMatrixRecepcionist() {
		return matrixRecepcionist;
	}

	public static void setMatrixRecepcionist(float[][] matrixRecepcionist) {
		General.matrixRecepcionist = matrixRecepcionist;
	}

	public double[] gettTreatment() {
		return tTreatment;
	}

	public void settTreatment(double[] tTreatment) {
		this.tTreatment = tTreatment;
	}

	public double[] gettReasesment() {
		return tReasesment;
	}

	public void settReasesment(double[] tReasesment) {
		this.tReasesment = tReasesment;
	}

	public double[] gettTriage() {
		return tTriage;
	}

	public void settTriage(double[] tTriage) {
		this.tTriage = tTriage;
	}

	public double[] gettRegistration() {
		return tRegistration;
	}

	public void settRegistration(double[] tRegistration) {
		this.tRegistration = tRegistration;
	}

	public static float[][] getMatrixTriagePropByArrival() {
		return matrixTriagePropByArrival;
	}

	public static void setMatrixTriagePropByArrival(
			float[][] matrixTriagePropByArrival) {
		General.matrixTriagePropByArrival = matrixTriagePropByArrival;
	}

	public static float[][] getMatrixPropTest() {
		return matrixPropTest;
	}

	public static void setMatrixPropTest(float[][] matrixPropTest) {
		General.matrixPropTest = matrixPropTest;
	}

	public static float[][] getMatrixSHOD1() {
		return matrixSHOD1;
	}

	public static void setMatrixSHOD1(float[][] matrixSHOD1) {
		General.matrixSHOD1 = matrixSHOD1;
	}

	public static float[][] getMatrixSHOD2() {
		return matrixSHOD2;
	}

	public static void setMatrixSHOD2(float[][] matrixSHOD2) {
		General.matrixSHOD2 = matrixSHOD2;
	}

	public static float[][] getMatrixSHOD3() {
		return matrixSHOD3;
	}

	public static void setMatrixSHOD3(float[][] matrixSHOD3) {
		General.matrixSHOD3 = matrixSHOD3;
	}

	public static float[][] getMatrixSHOD4() {
		return matrixSHOD4;
	}

	public static void setMatrixSHOD4(float[][] matrixSHOD4) {
		General.matrixSHOD4 = matrixSHOD4;
	}

	public static float[][] getMatrixSHOD5() {
		return matrixSHOD5;
	}

	public static void setMatrixSHOD5(float[][] matrixSHOD5) {
		General.matrixSHOD5 = matrixSHOD5;
	}

	public static float[][] getMatrixSHOD6() {
		return matrixSHOD6;
	}

	public static void setMatrixSHOD6(float[][] matrixSHOD6) {
		General.matrixSHOD6 = matrixSHOD6;
	}

	public static float[][] getMatrixSHOD7() {
		return matrixSHOD7;
	}

	public static void setMatrixSHOD7(float[][] matrixSHOD7) {
		General.matrixSHOD7 = matrixSHOD7;
	}

	public static float[][] getMatrixSHOD8() {
		return matrixSHOD8;
	}

	public static void setMatrixSHOD8(float[][] matrixSHOD8) {
		General.matrixSHOD8 = matrixSHOD8;
	}

	public static float[][] getMatrixSHOD9() {
		return matrixSHOD9;
	}

	public static void setMatrixSHOD9(float[][] matrixSHOD9) {
		General.matrixSHOD9 = matrixSHOD9;
	}

	public static float[][] getMatrixSHOD0() {
		return matrixSHOD0;
	}

	public static void setMatrixSHOD0(float[][] matrixSHOD0) {
		General.matrixSHOD0 = matrixSHOD0;
	}

	public double getAverageTimeAllPatients() {
		return averageTimeAllPatients;
	}

	public void setAverageTimeAllPatients(double averageTimeAllPatients) {
		this.averageTimeAllPatients = averageTimeAllPatients;
	}

	public static double getMinute() {
		double tick = (RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount());
		minute = (tick % 60);
		return minute;
	}

	public static void setMinute(double minute) {
		General.minute = minute;
	}

	public static float[][] getMatrixNurse1() {
		return matrixNurse1;
	}

	public static void setMatrixNurse1(float[][] matrixNurse1) {
		General.matrixNurse1 = matrixNurse1;
	}

	public static float[][] getMatrixNurse2() {
		return matrixNurse2;
	}

	public static void setMatrixNurse2(float[][] matrixNurse2) {
		General.matrixNurse2 = matrixNurse2;
	}

	public static float[][] getMatrixNurse3() {
		return matrixNurse3;
	}

	public static void setMatrixNurse3(float[][] matrixNurse3) {
		General.matrixNurse3 = matrixNurse3;
	}

	public static float[][] getMatrixNurse4() {
		return matrixNurse4;
	}

	public static void setMatrixNurse4(float[][] matrixNurse4) {
		General.matrixNurse4 = matrixNurse4;
	}

	public static float[][] getMatrixNurse5() {
		return matrixNurse5;
	}

	public static void setMatrixNurse5(float[][] matrixNurse5) {
		General.matrixNurse5 = matrixNurse5;
	}

	public static float[][] getMatrixClerk1() {
		return matrixClerk1;
	}

	public static void setMatrixClerk1(float[][] matrixClerk1) {
		General.matrixClerk1 = matrixClerk1;
	}

	public static float[][] getMatrixClerk2() {
		return matrixClerk2;
	}

	public static void setMatrixClerk2(float[][] matrixClerk2) {
		General.matrixClerk2 = matrixClerk2;
	}

	public static float[][] getArrayDNW() {
		return arrayDNW;
	}

	public static void setArrayDNW(float[][] fs) {
		General.arrayDNW = fs;
	}



}
