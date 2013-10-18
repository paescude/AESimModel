package AESim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import cern.jet.random.Uniform;

public class Doctor extends General {
	private static int count;
	// private ArrayList<Double> doctorMemory []= new ArrayList[2];
	// private PriorityQueue<Patient> memoryByTriage;
	// private PriorityQueue<Patient> memoryByTime;
	private PriorityQueue<Patient> myPatientsInBedTriage;
	private PriorityQueue<Patient> myPatientsInBedTime;
	private LinkedList<Patient> myPatientsBackInBed;

	private ArrayList<Patient> myPatientsInTests;
	private ArrayList<Patient> allMyPatients;
	private ArrayList<Patient> patientsInMultiTask;
	private int requiredAtWork;

	private static ArrayList<GridPoint> locFstAssessmentArray = new ArrayList<>();
	private Boolean available;
	private Grid<Object> grid;
	private boolean isInShift;
	private boolean canHelpAnotherDoctor;

	private double nextEndingTime;
	private int idNum;
	private int initPosX;
	private int initPosY;
	private int numAvailable;
	private int myNumPatientsInBed;

	private int multiTaskingFactor;
	private Doctor doctorToHandOver;

	private double[] durationOfShift = new double[7];
	private double k;

	private float[][] myShiftMatrix;
	private String doctorType;
	private Resource myResource;
	private Patient myPatientCalling;

	private double timeInitShift;
	private double timeEnterSimulation;

	// PECS information and variables
	// INPUT variables
	private double x1MyNumPatientsSeen;
	private double x2MyTimeWorkedInShift;
	private double x3TriageMaxAmongMyPatients;
	private double x4MyPatientsAverageTimeInSys;
	private double x5RatioTestMaxTestMyPatients;
	private double x6MyTotalTimeWorkedInDpmnt; // TotalTimeWorkedInDpmnt is
												// measured in weeks
	private double x7MyPatientsMaxTimeInSys;

	// PARAMETERS
	private double c1MyMaxPatientHour;
	private double c2MyDurationShift; // this is set when doctor moves to
										// doctors' area
	private double c3LogisticCalmC;
	private double c4LogisticKnowledgeC;// TODO this depends on other variables.
										// I'll calculate it later. c4= - c5 z4+
										// 2c5
	private double c5LogisticExperienceC;
	private double c6LogisticReputationC;
	private double alpha3Experience;
	private double alpha1Calmness;
	private double alpha2Knowledge;

	// STATE (PECS) Variables
	private double z1Energy;
	private double z2Calmness;
	private double z3Knowledge;
	private double[] z3KnowledgeMatrixPatient; // this is a matrix 1 row i cols
												// (number of "allMyPatients")
	// if I know the position i in the vector z3, i know the position i in the
	// array allmypatients
	// position [i]= knowledge about the patient i.

	private double z4Experience;
	private double z5Reputation;

	// Intensity state variables

	private double alphaZ1W1;
	private double alphaZ2W2;
	private double alphaZ3W3;
	private double alphaZ5W4;

	private double cZ1W1;
	private double cZ2W2;
	private double cZ3W3;
	// I don't use the intensity of the experience to make decisions.
	private double cZ5W4;

	private double w1Fatigue;
	private double w2Stress;
	private double w3WillOfKnowledge;
	private double[] w3WillOfKnowledgeMatrix;
	private boolean startShift;

	private double w3WillOfKnowledge1;
	private double w3WillOfKnowledge2;
	private double w3WillOfKnowledge3;
	private double w3WillOfKnowledge4;
	private double w3WillOfKnowledge5;
	private double w3WillOfKnowledge6;

	private double w4SocialDesire;
	private double averageKnowledge;
	private boolean isBusy;
	public boolean isAtDoctorArea;

	public Doctor(Grid<Object> grid, int initPosX, int initPosY,
			String doctorType, int idNum, int multiTasking) {
		this.setIdNum(idNum);
		this.grid = grid;
		this.numAvailable = 0;
		this.initPosX = initPosX;
		this.initPosY = initPosY;
		this.doctorType = doctorType;
		this.setId("doctor " + idNum);

		this.myPatientCalling = null;
		this.myNumPatientsInBed = 0;
		this.available = false;
		this.setNextEndingTime(0);
		this.timeEnterSimulation = getTime();

		this.myPatientsInBedTime = new PriorityQueue<Patient>(5,
				new PriorityQueueComparatorTime());
		this.myPatientsInBedTriage = new PriorityQueue<Patient>(5,
				new PriorityQueueComparatorTime());
		this.myPatientsInTests = new ArrayList<Patient>();
		this.myPatientsBackInBed = new LinkedList<Patient>();
		this.canHelpAnotherDoctor = false;
		this.doctorToHandOver = null;
		this.multiTaskingFactor = multiTasking;
		// this.numAvailable=multiTasking;
		this.patientsInMultiTask = new ArrayList<>();
		this.isAtDoctorArea = false;
		// this.requiredAtWork= (int) this.getMyShiftMatrix()[0][0];

	}

	@ScheduledMethod(start = 0, priority = 1)
	public void assignLocQueueArray() {
		int locX;
		int locY = 8;
		GridPoint locQueueFstAssessment;
		for (int i = 1; i <= 5; i++) {
			locX = i;
			locQueueFstAssessment = new GridPoint(locX, locY);
			locFstAssessmentArray.add(locQueueFstAssessment);

		}
	}

	public void myPatientsInTestAdd(Patient patient) {
		System.out.println("\n" + this.getId()
				+ " will add to patients in test " + patient.getId()
				+ " time: " + getTime());

		if (this.myPatientsInTests.contains(patient)) {
			System.err.println("\n ERROR: the list already contains "
					+ patient.getId() + " the method is being executed by "
					+ this.getId());
		} else {
			this.myPatientsInTests.add(patient);

			printTime();
			printElementsArray(this.myPatientsInTests, "in tests");
			System.out.println("\n ");
		}
	}

	public void myPatientsInTestRemove(Patient patient) {
		System.out.println("\n" + this.getId()
				+ " will remove to patients in test " + patient.getId());

		if (this.myPatientsInTests.remove(patient)) {
			System.out.println(this.getId() + " has removed from PinTest "
					+ patient.getId() + " new array of patients in test:");
			printTime();
			printElementsArray(this.myPatientsInTests, "in tests");
			System.out.println("\n ");
		} else {
			System.err
					.println("\n ERROR: (I don't know what method is calling myPatientsInTestRemove )"
							+ this.getId()
							+ " it was not possible to remove from my patients test: "
							+ patient.getId());
		}
	}

	public void myPatientsInBedAdd(Patient patient) {
		System.out.println("\n" + this.getId()
				+ " will add to patients in bed " + patient.getId());
		this.myPatientsInBedTriage.add(patient);
		this.myPatientsInBedTime.add(patient);
		patient.setMyDoctor(this);

	}

	public void myPatientsInBedRemove(Patient patient) {
		System.out.println("\n" + this.getId()
				+ " is in method remove from bed, is removing: "
				+ patient.getId() + " time: " + getTime());

		if ((this.myPatientsInBedTriage.remove(patient))
				&& (this.myPatientsInBedTime.remove(patient))) {
			printElementsQueue(myPatientsInBedTime,
					" my patients in bed (time and triage ");
		} else {
			System.err
					.println(" \n ERROR: "
							+ this.getId()
							+ "  it was not possible to remove from my patients bed (by triage and time): "
							+ patient.getId());

		}

	}

	public ArrayList<Patient> getAllMyPatients() {

		PriorityQueue<Patient> myPatientsInBed = this.getMyPatientsInBedTime();
		ArrayList<Patient> myPatientsInTest = this.getMyPatientsInTests();

		this.allMyPatients = new ArrayList<Patient>();
		this.allMyPatients.addAll(myPatientsInTest);
		this.allMyPatients.addAll(myPatientsInBed);

		/*
		 * if (this.isInShift) { System.out.println("\n" + this.getId() +
		 * " all my patients: " + " time: " + getTime()); if
		 * (this.allMyPatients.size() > 0) {
		 * printElementsQueue(this.myPatientsInBedTime, "in bed by Time");
		 * 
		 * printElementsQueue(this.myPatientsInBedTriage, "in bed by triage");
		 * 
		 * printElementsArray(this.myPatientsInTests, "in tests");
		 * 
		 * printElementsArray(this.allMyPatients, "all my patients");
		 * 
		 * } else System.out.println(" has no patients");
		 * 
		 * }
		 */

		return this.allMyPatients;
	}

	@ScheduledMethod(start = 5, interval = 10, priority = 60, shuffle = false, pick = 1)
	public void calcPECSvariables() {

		// state variables calculation . I start with the Z's calculation
		// because they depend on the previous values of X's and Z's
		this.setZ1Energy(calcFCOverCplusX(this.getC1MyMaxPatientHour(),
				this.getC2MyDurationShift(), this.getX1MyNumPatientsSeen(),
				this.getX2MyTimeWorkedInShift()));
		double alpha1 = this.getX3TriageMaxAmongMyPatients()
				* (1 - this.getZ1Energy());
		this.setZ2Calmness(calcFLogisticPositive(
				this.getX4MyPatientsAverageTimeInSys(), alpha1,
				this.getC3LogisticCalmC()));
		double alpha2 = this.getX5RatioTestMaxTestMyPatients()
				+ this.getZ4Experience();

		this.setKnowledgeEachPatient();

		this.setZ3Knowledge(averageKnowledge);

		// this.setZ3Knowledge(calcFLogisticNegative(
		// this.getX4MyPatientsAverageTimeInSys(), alpha2,
		// this.getC4LogisticKnowledgeC()));

		this.setZ4Experience(calcFLogisticNegative(
				this.getX6MyTotalTimeWorkedInDpmnt(),
				this.getAlpha3Experience(), this.getC5LogisticExperienceC()));
		double alpha4 = 1 + this.getZ4Experience();
		this.setZ5Reputation(calcFLogisticPositive(
				this.getX7MyPatientsMaxTimeInSys(), alpha4,
				this.getC6LogisticReputationC()));
		double timeWorkedInWeeks = (getTime() - this.getTimeEnterSimulation())
				/ (60 * 24 * 7);

		this.setW1Fatigue(calcFLogisticPositive(this.getZ1Energy(),
				this.getAlphaZ1W1(), this.getcZ1W1()));
		this.setW2Stress(calcFLogisticPositive(this.getZ2Calmness(),
				this.getAlphaZ2W2(), this.getcZ2W2()));
		this.setW3WillOfKnowledge(calcFLogisticPositive(this.getZ3Knowledge(),
				this.getAlphaZ3W3(), this.getcZ3W3()));
		this.setW4SocialDesire(calcFLogisticPositive(this.getZ5Reputation(),
				this.getAlphaZ5W4(), this.getcZ5W4()));

		if (this.isInShift) {
			printTime();
			// this.setX1MyNumPatientsSeen(this.getAllMyPatients().size());
			this.setX2MyTimeWorkedInShift(this.calculateWorkedTimeHours());// this
																			// time
																			// is
																			// in
																			// hours
			this.setX3TriageMaxAmongMyPatients(this.getMaxTriage());
			this.setX4MyPatientsAverageTimeInSys(this
					.getAveTSysAllMyPatientsHours());
			this.setX5RatioTestMaxTestMyPatients(1); // TODO see how this can be
														// calculated
														// individually
														// to obtain a knowledge
														// of
														// each patient
			this.setX6MyTotalTimeWorkedInDpmnt(timeWorkedInWeeks);
			this.setX7MyPatientsMaxTimeInSys(this
					.getMaxTSysAllMyPatientsHours());

			System.out.println("\n	" + this.getId() + " is in shift?: "
					+ this.isInShift() + "\n		has worked in this shift "
					+ this.getX2MyTimeWorkedInShift() + " minutes "

					+ "\n		max triage among all patients "
					+ this.getMaxTriage()
					+ "\n		my patients average time in system "
					+ this.getAveTSysAllMyPatientsHours()
					+ "\n		my patients max time in system "
					+ this.getX7MyPatientsMaxTimeInSys()
					+ "\n		time worked in the department "
					+ this.getX6MyTotalTimeWorkedInDpmnt() + " weeks \n");
		}

	}

	public int calcMaxWPECS() {
		this.calcPECSvariables();
		double maxWPECS = this.getW1Fatigue();
		int decision = 1;
		if (maxWPECS < this.getW2Stress()) {
			maxWPECS = this.getW2Stress();
			decision = 2;
		} else if (maxWPECS < this.getW3WillOfKnowledge()) {
			maxWPECS = this.getW3WillOfKnowledge();
			decision = 3;
		}

		else if (maxWPECS < this.getW4SocialDesire()) {
			maxWPECS = this.getW4SocialDesire();
			decision = 4;
		}

		return decision;
	}

	// function c/(c+x)
	public double calcFCOverCplusX(double c1, double c2, double x1, double x2) {
		double z = c1 * c2 / (c1 * c2 + x1 * x2);

		return z;

	}

	// function logsistic alpha positivive
	public double calcFLogisticPositive(double x, double alpha, double c) {
		double logistic = 1 / (1 + Math.exp(alpha * (x - c)));

		return logistic;

	}

	// function logsistic alpha negative
	public double calcFLogisticNegative(double x, double alpha, double c) {
		double logistic = 1 / (1 + Math.exp(-alpha * (x - c)));

		return logistic;

	}

	// TODO this method perhaps may be useless. If doctor can access each of his
	// patients, can access each ratio

	public void setKnowledgeEachPatient() {
		double totalKnowlegde = 0;
		ArrayList<Patient> allMyPatientsV = this.getAllMyPatients();
		int myTotalPatients = allMyPatientsV.size();
		if (myTotalPatients > 0) {
			z3KnowledgeMatrixPatient = new double[myTotalPatients];
			w3WillOfKnowledgeMatrix = new double[myTotalPatients];
			for (int i = 0; i < myTotalPatients; i++) {
				Patient patient = allMyPatientsV.get(i);
				this.z3KnowledgeMatrixPatient[i] = getKnowledgePatient(patient);
				totalKnowlegde += getKnowledgePatient(patient);
				double intensityKnow = (calcFLogisticPositive(
						this.getZ3KnowledgeMatrixPatient()[i],
						this.getAlphaZ3W3(), this.getcZ3W3()));
				this.w3WillOfKnowledgeMatrix[i] = intensityKnow;
				switch (i) {
				case 0:
					this.setW3WillOfKnowledge1(w3WillOfKnowledgeMatrix[i]);
					break;
				case 1:
					this.setW3WillOfKnowledge2(w3WillOfKnowledgeMatrix[i]);
					break;
				case 2:
					this.setW3WillOfKnowledge3(w3WillOfKnowledgeMatrix[i]);
					break;
				case 3:
					this.setW3WillOfKnowledge4(w3WillOfKnowledgeMatrix[i]);
					break;
				case 4:
					this.setW3WillOfKnowledge5(w3WillOfKnowledgeMatrix[i]);
					break;

				case 5:
					this.setW3WillOfKnowledge6(w3WillOfKnowledgeMatrix[i]);
					break;
				default:
					break;
				}
			}

			this.averageKnowledge = totalKnowlegde / myTotalPatients;
		}

	}

	public double getKnowledgePatient(Patient patient) {
		double alpha2 = this.getRatioTestEachPatient(patient)
				+ this.getZ4Experience();
		double knowledge = (calcFLogisticNegative(patient.getTimeInSystem(),
				alpha2, this.getC4LogisticKnowledgeC()));

		return knowledge;
	}

	public double getRatioTestEachPatient(Patient patient) {

		int maxTest = 4;
		int totalTest = 0;
		totalTest = patient.getTotalNumTest();
		double ratio = totalTest / maxTest;
		patient.setTestRatio(ratio);

		return ratio;
	}

	public void getRatioTestAllMyPatients() {
		double ratio = 0;
		int maxTest = 4;
		int totalTest = 0;
		ArrayList<Patient> allMyPatientsV = this.getAllMyPatients();
		if (allMyPatientsV.size() > 0) {

			for (Iterator<Patient> iterator = allMyPatientsV.iterator(); iterator
					.hasNext();) {
				Patient patient = iterator.next();
				totalTest = patient.getTotalNumTest();
				ratio = totalTest / maxTest;
				patient.setTestRatio(ratio);

			}
		}

	}

	public void initializeDoctorShiftParams() {
		this.numAvailable = this.multiTaskingFactor;
		System.out.println(this.getId()
				+ " is initializing PECS at the beginning of his shift: ");
		printTime();
		this.setX1MyNumPatientsSeen(0);
		this.setX2MyTimeWorkedInShift(0);// this time
											// is in
											// hours
		this.setX3TriageMaxAmongMyPatients(1);
		this.setX4MyPatientsAverageTimeInSys(0);
		this.setX5RatioTestMaxTestMyPatients(1); // TODO see how this can be
													// calculated individually
													// to obtain a knowledge of
													// each patient
		this.setX6MyTotalTimeWorkedInDpmnt(0);
		this.setX7MyPatientsMaxTimeInSys(0);
		this.setZ1Energy(1);

		double alpha1 = this.getX3TriageMaxAmongMyPatients()
				* (1 - this.getZ1Energy());
		this.setZ2Calmness(calcFLogisticPositive(0, alpha1,
				this.getC3LogisticCalmC()));
		this.setZ3Knowledge(calcFLogisticNegative(0, 1,
				this.getC4LogisticKnowledgeC()));
		this.setZ4Experience(calcFLogisticNegative(0,
				this.getAlpha3Experience(), this.getC5LogisticExperienceC()));
		double alpha4 = 1 + this.getZ4Experience();
		this.setZ5Reputation(calcFLogisticPositive(0, alpha4,
				this.getC6LogisticReputationC()));

		this.setAlphaZ1W1(10);
		this.setcZ1W1(0.5);
		this.setAlphaZ2W2(10);
		this.setcZ2W2(0.5);
		this.setAlphaZ3W3(10);
		this.setcZ3W3(0.7);
		this.setAlphaZ5W4(5);
		this.setcZ5W4(0.5);

	}

	public int getMaxTriage() {
		int maxTriage = 0;
		ArrayList<Patient> allMyPatientsV = this.getAllMyPatients();
		if (allMyPatientsV.size() > 0) {

			for (Iterator<Patient> iterator = allMyPatientsV.iterator(); iterator
					.hasNext();) {
				Patient patient = iterator.next();
				if (patient.getTriageNum() > maxTriage) {
					maxTriage = patient.getTriageNum();
				}
			}
		}
		return maxTriage;
	}

	public double getAveTSysAllMyPatientsHours() {
		ArrayList<Patient> allMyPatientsV = this.getAllMyPatients();
		int numberOfPatients = allMyPatientsV.size();
		double totalTimeInSys = 0;
		double aveTimeInSys = 0;
		if (allMyPatientsV.size() > 0) {

			for (Iterator<Patient> iterator = allMyPatientsV.iterator(); iterator
					.hasNext();) {
				Patient patient = iterator.next();
				totalTimeInSys += (patient.getTimeInSystem()) / 60;

			}
			aveTimeInSys = totalTimeInSys / numberOfPatients;

		}
		return aveTimeInSys;
	}

	public double getMaxTSysAllMyPatientsHours() {
		ArrayList<Patient> allMyPatientsV = this.getAllMyPatients();
		int numberOfPatients = allMyPatientsV.size();
		double maxTime = 0;
		if (allMyPatientsV.size() > 0) {

			for (Iterator<Patient> iterator = allMyPatientsV.iterator(); iterator
					.hasNext();) {
				Patient patient = iterator.next();
				if (patient.getTimeInSystem() > maxTime) {
					maxTime = (patient.getTimeInSystem()) / 60;
				}

			}
		}

		return maxTime;
	}

	@ScheduledMethod(start = 0, priority = 90, shuffle = false, pick = 1)
	public void initNumDocs() {
		printTime();
		System.out.println("When simulation starts, the conditions are "
				+ this.getId());
		GridPoint currentLoc = grid.getLocation(this);
		int currentX = currentLoc.getX();
		int currentY = currentLoc.getY();

		if (currentX == 19) {
			this.setAvailable(false);
			this.setInShift(false);
			System.out.println(this.getId()
					+ " is not in shift and is not available, time: "
					+ getTime());

		} else if (currentY == 4) {
			this.setAvailable(true);
			this.setInShift(true);
			System.out.println(this.getId()
					+ " is in shift and is available, time: " + getTime());
		}

		this.setX1MyNumPatientsSeen(0);
		this.setX2MyTimeWorkedInShift(0);// this time
											// is in
											// hours
		this.setX3TriageMaxAmongMyPatients(1);
		this.setX4MyPatientsAverageTimeInSys(0);
		this.setX5RatioTestMaxTestMyPatients(1); // TODO see how this can be
													// calculated individually
													// to obtain a knowledge of
													// each patient
		this.setX6MyTotalTimeWorkedInDpmnt(0);
		this.setX7MyPatientsMaxTimeInSys(0);

		int id = this.getIdNum();
		float sum = 0;
		switch (id) {

		case 0:
			this.setMyShiftMatrix(getMatrixSHOD0());
			// this doctor is a consultant, minimum experience is 8 years
			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixSHOD0()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			break;

		case 1:
			this.setMyShiftMatrix(getMatrixSHOD1());
			// sho minim. exp is 2 years (middle grade 6)
			// doctor middle experience
			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixSHOD1()[j][i];
				}
				this.durationOfShift[i] = sum;
			}

			this.setC1MyMaxPatientHour(8);
			this.setC2MyDurationShift(this.durationOfShift[getDay()]);
			this.setC3LogisticCalmC(2);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(0);
			this.setC6LogisticReputationC(2.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0175);
			break;

		case 2:
			this.setMyShiftMatrix(getMatrixSHOD2());

			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixSHOD2()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			this.setC1MyMaxPatientHour(8);
			this.setC2MyDurationShift(this.durationOfShift[getDay()]);
			this.setC3LogisticCalmC(2);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(0);
			this.setC6LogisticReputationC(2.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0175);
			break;

		case 3:
			this.setMyShiftMatrix(getMatrixSHOD3());

			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixSHOD3()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			this.setC1MyMaxPatientHour(4);
			this.setC2MyDurationShift(this.durationOfShift[getDay()]);
			this.setC3LogisticCalmC(1);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(200);
			this.setC6LogisticReputationC(1.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.075);
			break;

		case 4:
			this.setMyShiftMatrix(getMatrixSHOD4());

			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixSHOD4()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			this.setC1MyMaxPatientHour(8);
			this.setC2MyDurationShift(this.durationOfShift[getDay()]);
			this.setC3LogisticCalmC(2);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(0);
			this.setC6LogisticReputationC(2.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0175);
			break;

		case 5:
			this.setMyShiftMatrix(getMatrixSHOD5());

			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixSHOD5()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			this.setC1MyMaxPatientHour(4);
			this.setC2MyDurationShift(this.durationOfShift[getDay()]);
			this.setC3LogisticCalmC(1);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(200);
			this.setC6LogisticReputationC(1.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.075);
			break;

		case 6:
			this.setMyShiftMatrix(getMatrixSHOD6());

			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixSHOD6()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			this.setC1MyMaxPatientHour(4);
			this.setC2MyDurationShift(this.durationOfShift[getDay()]);
			this.setC3LogisticCalmC(1);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(200);
			this.setC6LogisticReputationC(1.167);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.075);
			break;

		case 7:
			this.setMyShiftMatrix(getMatrixSHOD7());

			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixSHOD7()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			this.setC1MyMaxPatientHour(6);
			this.setC2MyDurationShift(this.durationOfShift[getDay()]);
			this.setC3LogisticCalmC(1.33);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(50);
			this.setC6LogisticReputationC(1.5);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0125);
			break;

		case 8:
			this.setMyShiftMatrix(getMatrixSHOD8());

			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixSHOD8()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			this.setC1MyMaxPatientHour(6);
			this.setC2MyDurationShift(this.durationOfShift[getDay()]);
			this.setC3LogisticCalmC(1.33);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(50);
			this.setC6LogisticReputationC(1.5);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0125);
			break;

		case 9:
			this.setMyShiftMatrix(getMatrixSHOD9());

			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixSHOD9()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			this.setC1MyMaxPatientHour(6);
			this.setC2MyDurationShift(this.durationOfShift[getDay()]);
			this.setC3LogisticCalmC(1.33);
			this.setC4LogisticKnowledgeC(0.5);
			this.setC5LogisticExperienceC(50);
			this.setC6LogisticReputationC(1.5);
			this.setAlpha1Calmness(0.0002);
			this.setAlpha2Knowledge(1);
			this.setAlpha3Experience(0.0125);
			break;
		}

		System.out.println(this.getId() + " shift's duration ["
				+ this.durationOfShift[0] + " ," + this.durationOfShift[1]
				+ "," + this.durationOfShift[2] + " ,"
				+ this.durationOfShift[3] + " ," + this.durationOfShift[4]
				+ ", " + this.durationOfShift[5] + ", "
				+ this.durationOfShift[6] + "]");
	}

	// public void setShiftDuration() {
	//
	// float sum = 0;
	// for (int i = 0; i < 7; i++) {
	// sum = 0;
	// for (int j = 0; j < 23; j++) {
	// sum = sum + getMatrixSHOD0()[j][i];
	// }
	// this.durationOfShift[i] = sum;
	// }
	// }

	public void getShiftDuration(int day) {
		float sum = 0;
		for (int j = 0; j < 23; j++) {
			sum = sum + getMatrixSHOD0()[j][day];
		}
		this.durationOfShift[day] = sum;
	}

	/*
	 * Initialize the doctor´s memory
	 */
	private void initMemory() {
		printTime();
		System.out
				.println(this.getId()
						+ " is initalizing memory and clearing all the list of patients he has");
		this.myPatientsInBedTime.clear();
		this.myPatientsInBedTriage.clear();
		this.myPatientsInTests.clear();
		this.allMyPatients.clear();
		this.setX1MyNumPatientsSeen(0);

	}

	// moves the doctor to not working Area
	public void moveOut() {
		if (this.allMyPatients.size() > 0) {
			printTime();
			System.out
					.println(this.getId()
							+ " has finished his shift and still has patients, needs to hand over his patients  ");
			this.doctorToTakeOver();

		}

		this.setZ1Energy(1);
		this.setZ2Calmness(1);
		this.setZ3Knowledge(0);
		this.setZ4Experience(this.getZ4Experience());
		this.setZ5Reputation(this.getZ5Reputation());
		this.z3KnowledgeMatrixPatient = new double[0];
		this.w3WillOfKnowledgeMatrix = new double[0];

		this.setInShift(false);
		this.setAvailable(false);
		this.initMemory();
		this.setW1Fatigue(0);
		this.setW2Stress(0);
		this.setW3WillOfKnowledge(0);
		this.setW4SocialDesire(0);

		int i = this.getIdNum();
		int x = 19;
		int y = i + 4;

		grid.moveTo(this, x, y);
		this.isAtDoctorArea = false;
		System.out.println(this.getId()
				+ "  has finished his shift and has moved out to "
				+ this.getLoc(grid).toString());
		// durationShift= getTime()- timeInitShift;
		// double hours = durationOfShift/60;
		// System.out.println(this.getId() + " has worked "+ hours + " hours ");

	}

	public void moveToDoctorsArea() {
		if (this.patientsInMultiTask.size() < this.multiTaskingFactor) {

			if (this.getIdNum() != 0) {
				boolean flag = false;
				int y = 4;
				int x;
				for (int j = 0; j < 2; j++) {
					// System.out.println(" j: " + j);
					for (int i = 1; i < 6; i++) {
						// System.out.println(" i " + i);
						Object o = grid.getObjectAt(i + 6, y + j);
						if (o == null) {
							x = i + 6;
							grid.moveTo(this, x, y + j);
							this.isAtDoctorArea = true;
							System.out.println(this.getId()
									+ " has moved to doctors area "
									+ this.getLoc(grid).toString()
									+ " at time " + getTime());
							flag = true;
							break;

						}
						if (flag) {
							break;
						}

					}
					if (flag) {
						break;
					}
				}

			} else if (this.getIdNum() == 0) {

				grid.moveTo(this, this.getInitPosX(), this.initPosY);

				System.out.println(this.getId()
						+ " has moved to consultant area "
						+ this.getLoc(grid).toString());
			}

			this.setInShift(true);
			this.setAvailable(true);
			System.out.println(this.getId()
					+ " is in shift and is available at " + getTime());
			// this.decideWhatToDoNext();
		}
	}

	// @ScheduledMethod(start=15, interval= 15, priority= 20)
	public float calculateWorkedTimeHours() {
		float timeInHours = 0;
		for (int i = 0; i < getHour(); i++) {
			timeInHours = timeInHours
					+ (int) this.getMyShiftMatrix()[i][getDay()];
		}
		if (this.isInShift) {
			double timeHours = getTime() / 60;
			double timeFloor = Math.floor(getTime() / 60);
			double minute = (timeHours - timeFloor);
			timeInHours = timeInHours + (float) minute;
		}

		return timeInHours;
	}

	public void decideWhatToDoNext() {
		if (this.patientsInMultiTask.size() < this.multiTaskingFactor) {
			printTime();
			System.out.println(this.getId() + " decides what to do next");
			this.requiredAtWork = (int) this.getMyShiftMatrix()[getHour()][getDay()];
			if (requiredAtWork == 0) {
				this.moveOut();
			} else {
				if (this.available) {
					boolean isStartReassessment = this
							.checkIfStartReassessment();
					if (isStartReassessment == false) {
						if (this.doctorType == "SHO ") {
							System.out
									.println(" checks if there is any sho available to start init assessment ");
							boolean isStartInitAssessment = this
									.checkIfStartInitAssessment();
							if (isStartInitAssessment == false) {
								if (this.isAtDoctorArea == false) {
									System.out
											.println(this.getId()
													+ " is moving to docs area because when decide what to do has nothing to do ");
									this.moveToDoctorsArea();
								}
							}
						} else {
							if (this.doctorType == "Consultant ") {
								System.out
										.println(this.getId()
												+ " is consultant and is checking any other sho is available");
								Doctor shoAvailable = checkForAnyAvailableDoctor();
								if (shoAvailable == null) {

									System.out
											.println(this.getId()
													+ " is SHO and is checking if start init assessment");
									boolean isStartInitAssessment = this
											.checkIfStartInitAssessment();
									if (isStartInitAssessment == false) {
										if (this.isAtDoctorArea == false) {
											this.moveToDoctorsArea();
										}
									}

								}

							}

						}
					}
				}
			}
		}

	}

	private Doctor checkForAnyAvailableDoctor() {
		Doctor shoAvailable = null;
		System.out
				.println(" checks if there is any sho available to start init assessment ");
		for (Object sho : getContext().getObjects(Doctor.class)) {
			Doctor shoToCheck = (Doctor) sho;
			if (shoToCheck.getAvailable()) {
				shoAvailable = shoToCheck;
				System.out.println(shoAvailable.getId() + " is available ");
				break;
			}
		}
		return shoAvailable;
	}

	private Boolean checkIfStartInitAssessment() {
<<<<<<< HEAD
		/*
		 * Prueba de branches
		 */
		boolean isStartInitAssessment = false;
		/*
		 * Prueba 3
		 */
=======
		boolean isStartInitAssessment= false;
>>>>>>> Refactor endFstAssessment method
		Patient fstpatient = null;
		Boolean flag = false;
		// The head of the queue is at (x,y-1)
		// Object o = grid.getObjectAt(locX, locY);
		int i = 1;
		while (i <= 5 && !flag) {
			// checking from left to right, which patient is at the head of
			// assessment (by each triage color) queue

			for (Object o : grid.getObjectsAt(i, 8)) {
				if (o instanceof Patient) {
					fstpatient = (Patient) o;
					flag = true;
					break;
				}

			}
			i++;
			// checks if there is anyone to start the first assessment
		}

		if (fstpatient != null) {
			System.out.println(this.getId()
					+ " decide to start init assessment with:"
					+ fstpatient.getId());
			isBusy = true;
			this.startInitAssessment(fstpatient);
			isStartInitAssessment = true;

		} else {
			isBusy = false;
			System.out.println(this.getId() + " has nothing to do?, patients:"
					+ this.getAllMyPatients());
			System.out
					.println(this.getId()
							+ " is available and...decides what to do, moving to docs area when start shift");
			if (!this.isAtDoctorArea)
				this.moveToDoctorsArea();
		}
		return isStartInitAssessment;

	}

	private boolean checkIfStartReassessment() {

		boolean isStartReassessment = false;
		if (this.patientsInMultiTask.size() < this.multiTaskingFactor) {
			int doctorPatientsNum = this.getMyPatientsBackInBed().size();
			if (doctorPatientsNum > 0) {
				// TODO esto estaba con poll, yo lo voy a cambiar a peek Patient
				// watchedAgent = this.getMyPatientsBackInBed().poll();
				Patient watchedAgent = this.getMyPatientsBackInBed().peek();
				System.out
						.println("\n start reassessment by 'decide what to do': "
								+ watchedAgent.getId() + " and " + this.getId());
				isBusy = true;
				this.startReassessment(watchedAgent);
				isStartReassessment = true;

			}
		}

		return isStartReassessment;
	}

	public void decisionPECS() {
		// see if he has patients waiting in bed and start reassessment
		int decision = this.calcMaxWPECS();// need to recorrer todos los
		// wknoledge y si hay uno que gane,
		// ese es el max
		switch (decision) {
		case 1: // Fatigue
			// this.setAvailable(false);
			// schedule end of Not Availability
			break;
		case 2:// Stress
				// choose a patient and affect the time of first assessment and
				// probability of test

			break;
		case 3:// WillOfWknowledge
				// increases number of tests

			break;
		case 4: // socialDesire
			// similar to stress, increase service time

		default:
			break;
		}

	}

	public Doctor doctorToTakeOver() {
		Doctor doctor = null;
		Context<Object> context = getContext();
		boolean enterIf = false;
		for (Object d : context.getObjects(Doctor.class)) {
			if (d != null) {
				doctor = (Doctor) d;
				int hour = getHour();
				int day = getDay();
				// Circular list for hour
				int nextHour = hour + 1;
				int nextDay = day;
				if (nextHour > 23) {
					nextHour = 0;
					nextDay = day + 1;
					if (nextDay > 6) {
						nextDay = 0;
					}
				}
				if (doctor.doctorType != "Consultant ") {
					if ((doctor.getMyShiftMatrix()[hour][day] == 1)
							&& (doctor.getMyShiftMatrix()[nextHour][nextDay] == 1)) {
						enterIf = true;
						printTime();
						System.out.println("there is a doctor to take over: "
								+ doctor.getId());
						this.handOver(doctor);
						doctor.decideWhatToDoNext();
						break;
					}

				} else
					doctor = null;
			}

		}

		if (doctor == null) {

			System.err.println("there is no doctor to take over: "
					+ this.getId() + " is leaving");

			System.out.println(this.getId()
					+ " search for somebody to stay at least when he leaves");
			for (Object d : context.getObjects(Doctor.class)) {
				if (d != null) {
					doctor = (Doctor) d;
					int hour = getHour();
					int day = getDay();
					// Circular list for hour
					int nextHour = hour + 1;
					int nextDay = day;
					if (nextHour > 23) {
						nextHour = 0;
						nextDay = day + 1;
						if (nextDay > 6) {
							nextDay = 0;
						}
					}
					if (doctor.doctorType != "Consultant ") {
						if ((doctor.getMyShiftMatrix()[hour][day] == 1)) {
							enterIf = true;
							printTime();
							System.out
									.println("there is a doctor to take over: "
											+ doctor.getId());
							this.handOver(doctor);
							doctor.decideWhatToDoNext();
							break;
						}
					}
				}

			}

		}

		if (this.getId().equals("doctor 9")) {
			System.out.println(this.getId() + " d9 handles to "
					+ doctor.getId());

		}

		return doctor;
	}

	public void handOver(Doctor doctor) {
		printTime();
		System.out.println(this.getId() + " has started method hand over "
				+ doctor.getId());
		PriorityQueue<Patient> newMyPatientsInBedTime = this
				.getMyPatientsInBedTime();
		PriorityQueue<Patient> newMyPatientsInBedTriage = this
				.getMyPatientsInBedTriage();
		ArrayList<Patient> newMyPatientsInTests = this.getMyPatientsInTests();
		LinkedList<Patient> myNewPatientsBackInBed = this.myPatientsBackInBed;

		System.out.println(doctor.getId()
				+ " before take over the patients had in all his list: \n");
		doctor.printElementsQueue(doctor.myPatientsInBedTime,
				" patients in bed \n");
		doctor.printElementsArray(doctor.myPatientsInTests,
				" patients in test \n");
		if (this.myPatientsInBedTime.size() > 0)
			doctor.myPatientsInBedTime.addAll(newMyPatientsInBedTime);
		if (this.myPatientsInBedTriage.size() > 0)
			doctor.myPatientsInBedTriage.addAll(newMyPatientsInBedTriage);
		if (this.myPatientsInTests.size() > 0)
			doctor.myPatientsInTests.addAll(newMyPatientsInTests);
		if (this.myPatientsBackInBed.size() > 0)
			doctor.myPatientsBackInBed.addAll(myNewPatientsBackInBed);

		for (int i = 0; i < this.getAllMyPatients().size(); i++) {
			Patient patient = this.getAllMyPatients().get(i);
			patient.setMyDoctor(doctor);
			printTime();
			System.out.println("/n " + patient.getId() + " has a new doctor: "
					+ patient.getMyDoctor().getId());
		}

		double x = doctor.getX1MyNumPatientsSeen();
		x = x + (double) this.getAllMyPatients().size();
		doctor.setX1MyNumPatientsSeen(x);

		this.myPatientsInBedTime.clear();
		this.myPatientsInBedTriage.clear();
		this.myPatientsInTests.clear();
		this.allMyPatients.clear();
		this.setX1MyNumPatientsSeen(0);
		this.doctorToHandOver = doctor;
		System.out.println(doctor.getId()
				+ " receiving after take over at time:  " + getTime());
		System.out.println(doctor.getId() + " has received in his list: \n");
		doctor.printElementsQueue(doctor.myPatientsInBedTime,
				" patients in bed \n");
		doctor.printElementsArray(doctor.myPatientsInTests,
				" patients in test \n");

		System.out.println(this.getId()
				+ " is leaving, after hand over at time:  " + getTime());
		System.out.println(this.getId() + " should have an empty list: \n");
		doctor.printElementsQueue(this.myPatientsInBedTime,
				" patients in bed \n");
		doctor.printElementsArray(this.myPatientsInTests,
				" patients in test \n");

		/*
		 * for (int i = 0; i < this.getMyPatientsInTests().size(); i++) {
		 * Patient patient = this.getMyPatientsInTests().get(i);
		 * patient.setMyDoctor(doctor); printTime(); System.out.println("/n " +
		 * patient.getId() + "  is in tests and has a new doctor: " +
		 * patient.getMyDoctor().getId()); }
		 * 
		 * for (Iterator<Patient> iterator =
		 * this.myPatientsInBedTime.iterator(); iterator .hasNext();) { Patient
		 * patient = (Patient) iterator.next(); patient.setMyDoctor(doctor);
		 * printTime(); System.out.println("/n " + patient.getId() +
		 * "  is in bed and has a new doctor: " +
		 * patient.getMyDoctor().getId());
		 * 
		 * }
		 */

		System.out.println(this.getId() + " handled to " + doctor.getId()
				+ " time " + getTime());
	}

	@ScheduledMethod(start = 0, interval = 60, priority = 60, shuffle = false, pick = 1)
	public void scheduleWork() {
		int hour = getHour();
		int day = getDay();
		System.out.println("\n \n " + this.getId()
				+ " is doing method : SCHEDULE WORK ");
		this.requiredAtWork = (int) this.getMyShiftMatrix()[hour][day];
		// if (this.doctorType=="Consultant "){ requiredAtWork =1};
		// doctor is not in shift
		if (requiredAtWork == 0) {
			System.out.println(this.getId()
					+ " is not required at work at time " + getTime());
			printTime();
			if (this.getAvailable()) {
				System.out.println(this.getId()
						+ " WAS AVAILABLE WHEN FINISHED SHIFT ");

				if (this.getAllMyPatients().size() > 0) {
					System.out
							.println(this.getId()
									+ " STILL HAS PATIENTS TO SEE, CHECKING IF THERE ARE DOCTOR TO TAKE OVER");
					if (this.doctorToTakeOver() != null) {
						System.out
								.println(this.getId()
										+ " has found a doctor to hand over his patients ");
						System.out.println(this.getId() + " IS MOVING OUT ");
						this.moveOut();
					}

				}

				else {
					System.out.println(this.getId()
							+ " has no patients left to see ");
					System.out.println(this.getId() + " IS MOVING OUT ");
					this.moveOut();
				}

			} else {
				System.out.println(this.getId()
						+ " WAS NOT AVAILABLE WHEN FINISHED SHIFT ");
				// if this is not available
				double maxEndingTime = 0;
				for (Iterator iterator = this.patientsInMultiTask.iterator(); iterator
						.hasNext();) {
					Patient patient = (Patient) iterator.next();
					if (patient.getTimeEndCurrentService() > maxEndingTime) {
						maxEndingTime = patient.getTimeEndCurrentService();
					}
				}
				if (getTime() < maxEndingTime) {
					printTime();
					System.out
							.println(this.getId()
									+ " has finished his shift but needs to wait to leave because he still has work to do");
					double timeEnding = maxEndingTime;
					this.scheduleEndShift(timeEnding + 5);
				}
			}
		}

		else if (requiredAtWork == 1) {
			// TODO here if (this.isInShift() == false) didn't have {}, i put
			// them but needs to be checked
			if (this.isInShift() == false) {
				this.setTimeInitShift(getTime());
				this.initializeDoctorShiftParams();
				this.doctorToHandOver = null;
				System.out.println(this.getId() + " will move to doctors area"
						+ " method: schedule work"
						+ " this method is being called by " + this.getId());
				this.startShift = true;
				this.numAvailable = this.multiTaskingFactor;
				this.setAvailable(true);
				if (this.isAtDoctorArea == false)
					System.out.println(this.getId()
							+ " is moving to docs area when schedule work");
				this.moveToDoctorsArea();
				this.decideWhatToDoNext();
			} else {
				this.startShift = false;
			}
		}

	}

	public void scheduleEndShift(double timeEnding) {
		System.out.println(" current time: " + getTime() + " " + this.getId()
				+ " is supposed to move out at: " + timeEnding);
		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();
		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEnding);
		Endshift actionEnd = new Endshift(this);

		schedule.schedule(scheduleParams, actionEnd);
	}

	private static class Endshift implements IAction {
		private Doctor doctor;

		public Endshift(Doctor doctor) {
			this.doctor = doctor;
		}

		@Override
		public void execute() {
			doctor.endShift();

		}

	}

	public void endShift() {
		printTime();
		System.out.println(this.getId()
				+ " has finished the shift and will move out at " + getTime());

		this.moveOut();

	}

	@Watch(watcheeClassName = "AESim.Doctor", watcheeFieldNames = "isAtDoctorArea", scheduleTriggerPriority = 90, whenToTrigger = WatcherTriggerSchedule.IMMEDIATE /*
																																									 * ,
																																									 * pick
																																									 * =
																																									 * 1
																																									 */)
	public void callDoctorsToWork() {
		if (this.available)
			this.checkIfStartInitAssessment();
	}

	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstforAsses", triggerCondition = "$watchee.getNumWasFstForAssess()>0", scheduleTriggerPriority = 90, whenToTrigger = WatcherTriggerSchedule.IMMEDIATE /*
																																																								 * ,
																																																								 * pick
																																																								 * =
																																																								 * 1
																																																								 */)
	public void newPatientForInitAssessment(Patient watchedPatient) {
		System.out.println("\n " + this.getId() + " watcher init, time: "
				+ getTime());
		int triage = watchedPatient.getTriageNum();
		watchedPatient.setNumWasFstForAssess(0);
		if (this.available && findBed(triage) != null) {

			// this.decideWhatToDoNext();

			this.startInitAssessment(watchedPatient);
		}
	}

	public void callFromHeadOfQueue(Patient watchedPatientFA) {

	}

	public boolean startInitAssessment(Patient watchedPatientFA) {
		boolean startInitAssessment = false;
		printTime();
		System.out.println(this.getId()
				+ " start init assessment by decide what to do next ? with:  "
				+ watchedPatientFA.getId());
		double time = getTime();

		if (this.available == false) {
			System.err
					.println(this.getId()
							+ " can´t start init assessment because doctor status available is? "
							+ this.getAvailable());
		} else {
			startInitAssessment = this.doFirstAssessment(watchedPatientFA);
		}

		return startInitAssessment;

	}

	private boolean doFirstAssessment(Patient fstpatient) {
		boolean startInitAssessment = false;
		printTime();
		System.out.println("      \n \n START FIRST ASSESSMENT  "
				+ this.getId() + " & " + fstpatient.getId() + " \n");
		double time = getTime();

		int triage = fstpatient.getTriageNum();
		// find the bed according to the patient's needs.
		Resource rAvailable = findBed(triage);
		if (rAvailable == null) {
			System.out.println(" there is no bed available");
		} else {
			// if there is a bed and it free, then
			if (rAvailable.getAvailable()) {
				GridPoint loc = rAvailable.getLoc(grid);
				int locX = loc.getX();
				int locY = loc.getY();

				Patient patientAlreadyInBed = null;
				Doctor doctorAlreadyInBed = null;
				System.out.println(this.getId()
						+ " is looking for a bed for firstAssessment "
						+ " checking: " + rAvailable.getId());
				for (Object agentObject : grid.getObjectsAt(locX, locY)) {
					if (agentObject instanceof Patient) {
						patientAlreadyInBed = (Patient) agentObject;
						System.err
								.println("ERROR:  there is already a patient in that bed: "
										+ patientAlreadyInBed.getId());
					}

					else if (agentObject instanceof Doctor) {
						doctorAlreadyInBed = (Doctor) agentObject;
						System.err
								.println("ERROR: there is already a doctor in that bed: "
										+ doctorAlreadyInBed.getId());

					}

				}
				if (doctorAlreadyInBed != null && patientAlreadyInBed == null) {
					System.err.println("\n ERROR: "
							+ doctorAlreadyInBed.getId() + " is at "
							+ rAvailable.getId()
							+ " and there is not patient in bed");
				}

				// ACTUALLY STARTS FIRST ASSESSMENT
				if (doctorAlreadyInBed == null && patientAlreadyInBed == null) {
					// fstpatient.setNumWasFstForAssess(0);
					printTime();
					System.out.println(rAvailable.getId() + " is empty ");
					grid.moveTo(this, locX, locY);
					grid.moveTo(fstpatient, locX, locY);
					System.out.println(this.getId() + " & "
							+ fstpatient.getId() + " have moved to "
							+ this.getLoc(grid).toString() + " to "
							+ rAvailable.getId());

					this.setMyResource(rAvailable);
					fstpatient.setMyResource(rAvailable);

					Queue queue = fstpatient.getCurrentQueue();
					Patient patientToRemove = queue.removeFromQueue(time);

					queue.elementsInQueue();

					// add patient to bed:
					System.out.println("method: start init assessment "
							+ this.getId()
							+ " will add to his patients in bed "
							+ fstpatient.getId());
					this.myPatientsInBedAdd(fstpatient);
					this.patientsInMultiTask.add(fstpatient);
					double x = this.getX1MyNumPatientsSeen();
					x++;
					this.setX1MyNumPatientsSeen(x);

					printElementsQueue(this.myPatientsInBedTime,
							" my patients in bed time");

					this.setMultitask(true);
					printElementsArray(this.patientsInMultiTask,
							" patients in multitasking");
					// this.setAvailable(false);
					System.out.println(this.getId() + " is setting "
							+ rAvailable.getId() + " available= false");
					rAvailable.setAvailable(false);
					if (this.available)
						this.decideWhatToDoNext();
					startInitAssessment = true;
					this.scheduleEnd1stAssessment(fstpatient);

					printTime();

					System.out
							.println(this.getId()
									+ " schedules the end of first assessment and checks if there are more patients waiting at "
									+ queue.getId());

					if (queue.getSize() > 0) {
						System.out
								.println("there are patients waiting for first assessment at:"
										+ queue.getId());

						Patient patientToMove = queue.firstInQueue();

						System.out.println(patientToMove.getId()
								+ " will move to the head of " + queue.getId()
								+ " at:" + getTime());
						patientToMove.moveToHeadOfQ(queue);
					}

				}
			}
		}
		return startInitAssessment;
	}

	private static class EndFstAssessment implements IAction {
		private Doctor doctor;
		private Patient patient;

		private EndFstAssessment(Doctor doctor, Patient patient) {
			this.doctor = doctor;
			this.patient = patient;

		}

		@Override
		public void execute() {
			doctor.endFstAssessment(this.patient);

		}

	}

	private static class EndReassessment implements IAction {
		private Doctor doctor;
		private Patient patient;

		private EndReassessment(Doctor doctor, Patient patient) {
			this.doctor = doctor;
			this.patient = patient;

		}

		@Override
		public void execute() {
			doctor.endReassessment(this.patient);

		}

	}

	public void endFstAssessment(Patient patient) {
		System.out.println("\n \t\t END FIRST ASSESSMENT");
		Doctor doctor = null;
<<<<<<< HEAD
		printTime();

=======
		printTime();		
		doctor = chooseDocFstAssess(patient, doctor);
		if (doctor != null) {
			int totalProcess = patient.getTotalProcesses();
			patient.setTotalProcesses(totalProcess+1);;
			this.patientsInMultiTask.remove(patient);
			doctor.setMultitask(false);			
			//doctor decides what to do 
			int route[] = decideTests(patient.getTriageNum());
			// Choose a route. The patient could go to test or not.
			chooseRoute(patient, doctor, route);
		} else {
			System.err
					.println(" ERROR: something is wrong here, no doctor to end fst assessment with "
							+ patient.getId());
			//this.doEndFstAssessment(this, patient);
		}
		System.out.println(doctor.getId() + " will decide what to do next");		
		System.out.println(this.getId() + " has finished fst assessment and  has removed " + patient.getId() + " from his multitasking.  " );
		System.out.println("My multitasking factor is " + this.multiTaskingFactor);
		printElementsArray(this.patientsInMultiTask, " patients in multitasking");
		System.out.println(this.getId() + "has available = " + this.getAvailable());
		//Para mover paciente de la lista de espera.
		movePatientBedReassessment(doctor);
		doctor.moveToDoctorsArea();
		doctor.decideWhatToDoNext();

	}

	private Doctor chooseDocFstAssess(Patient patient, Doctor doctor) {
>>>>>>> Refactor endFstAssessment method
		if (patient == null) {
			System.err
					.println("\n ERROR: Shouldn't be happening, patient is null at end of fst assessment");
		} else {
			if (patient.getMyDoctor() == this && (this.isInShift())) {
				System.out
						.println(" the method end fst assessment is being called by "
								+ this.getId()
								+ " that is the same doctor the patien had in mind ");
				System.out
						.println(this.getId()
								+ " is in shift, then it is possible to start end fst assessment");
				doctor = this;
			}

			else {
				if (patient.getMyDoctor() != null)
					if (patient.getMyDoctor().isInShift()) {
						System.out
								.println(patient.getMyDoctor().getId()
										+ " is not in shift but is ending the fst assessment with "
										+ patient.getId());
						doctor = patient.getMyDoctor();
					} else {
						doctor = patient.getMyDoctor();
					}
			}
		}
		return doctor;
	}

<<<<<<< HEAD
		if (doctor != null) {

			int totalProcess = patient.getTotalProcesses();
			patient.setTotalProcesses(totalProcess + 1);
			;
			this.patientsInMultiTask.remove(patient);
			doctor.setMultitask(false);

			// doctor decides what to do
			int route[] = decideTests(patient.getTriageNum());
			boolean needTest = false;
			// patient leaves the dept
			if (route[0] == 0 && route[1] == 0) {
				Resource resourceToRelease = patient.getMyResource();
				this.removePatientFromDepartment(patient);
				System.out.println("method end fst assessment " + this.getId()
						+ " is setting " + resourceToRelease.getId()
						+ " available= true");
=======
	private void chooseRoute(Patient patient, Doctor doctor, int[] route) {
		if (route[0] == 0 && route[1] == 0) {
			Resource resourceToRelease= patient.getMyResource();
			this.removePatientFromDepartment(patient);
			System.out.println("method end fst assessment "+this.getId() + " is setting " + resourceToRelease.getId() + " available= true");
			resourceToRelease.setAvailable(true);
		} else {
			if(Math.random()<0.1 && patient.getTriageNum() != 5){
				Resource resourceToRelease= patient.getMyResource();
				System.out.println("method end fst assessment "+this.getId() + " is setting " + resourceToRelease.getId() + " available= true, becasue "+ patient.getId() + " does not wait for test in bed" );
>>>>>>> Refactor endFstAssessment method
				resourceToRelease.setAvailable(true);
				patient.setWaitInCublicle(false);
				patient.setIsWaitingBedReassessment(1);
			} else {
<<<<<<< HEAD
				needTest = true;
				if (Math.random() < 0.1 && patient.getTriageNum() != 5) {
					Resource resourceToRelease = patient.getMyResource();
					System.out.println("method end fst assessment "
							+ this.getId() + " is setting "
							+ resourceToRelease.getId()
							+ " available= true, becasue " + patient.getId()
							+ " does not wait for test in bed");
					resourceToRelease.setAvailable(true);
					patient.setWaitInCublicle(false);
					patient.setIsWaitingBedReassessment(1);
				} else {
					Resource resourceToGo = patient.getMyResource();
					patient.setMyBedReassessment(resourceToGo);
					patient.getMyBedReassessment().setAvailable(false);
					resourceToGo.setWhoBlockedMe(patient);
					System.out.println(patient.getId() + " has blocked "
							+ resourceToGo.getId());
					System.out.println(patient.getId() + " reserves "
							+ patient.getMyBedReassessment().getId()
							+ " as my bed reassessment ");
				}
				patient.setMyDoctor(doctor);
				printTime();
				System.out.println(patient.getId()
						+ " keeps in mind that his assigned doctor is  "
						+ patient.getMyDoctor().getId());

				doctor.setMyResource(null);
				doctor.setMultitask(false);
				// this.setAvailable(true);
				printTime();
				System.out.println("method: endFstAssessment" + doctor.getId()
						+ " will remove from list of patients in bed "
						+ patient.getId());
				System.out.println(" method end fst assessment");

				doctor.myPatientsInBedRemove(patient);

				System.out.println(doctor.getId()
						+ " will move to doctors area"
						+ " method: endFstAssessment"
						+ " this method is being called by " + this.getId());
				System.out.println(doctor.getId()
						+ " decides for patient's test");
				System.out
						.println(this.getId() + " decides for patient's test");
				if (route[0] == 1) {
					printTime();

					System.out.println(patient.getId() + " needs Xray ");
					System.out.println(patient.getId() + " goes to qXray");
					patient.addToQ("qXRay ");
					// patient.increaseTestCounterXray();
					patient.setTotalNumTest(patient.getTotalNumTest() + 1);
					System.out.println(doctor.getId()
							+ " adds to list of patients in test "
							+ patient.getId());
					System.out.println(" method end First assessment"
							+ doctor.getId()
							+ " will add to his patients in test "
							+ patient.getId());

					doctor.myPatientsInTestAdd(patient);

					printElementsArray(doctor.getMyPatientsInTests(),
							" my patients in test ");

					if (route[1] == 1) {
						printTime();
						patient.setNextProc(1);
						System.out.println(patient.getId()
								+ " will need test after Xray");
						// patient goes to test after xray
					} else {
						printTime();
						patient.setNextProc(2);
						System.out.println(patient.getId()
								+ " will go back to bed after Xray");
						// patient goes back to bed after xray
					}
				}

				else if (route[0] == 0 && route[1] == 1) {
					printTime();

=======
				Resource resourceToGo = patient.getMyResource();					
				patient.setMyBedReassessment(resourceToGo);
				patient.getMyBedReassessment().setAvailable(false);
				resourceToGo.setWhoBlockedMe(patient);
				System.out.println(patient.getId() + " has blocked " + resourceToGo.getId());
				System.out.println(patient.getId() + " reserves "
						+ patient.getMyBedReassessment().getId()
						+ " as my bed reassessment ");
			}			
			patient.setMyDoctor(doctor);
			printTime();
			System.out.println(patient.getId()
					+ " keeps in mind that his assigned doctor is  "
					+ patient.getMyDoctor().getId());

			doctor.setMyResource(null);
			doctor.setMultitask(false);
			// this.setAvailable(true);
			printTime();
			System.out.println("method: endFstAssessment" + doctor.getId()
					+ " will remove from list of patients in bed "
					+ patient.getId());
			System.out.println(" method end fst assessment");

			doctor.myPatientsInBedRemove(patient);

			System.out.println(doctor.getId()
					+ " will move to doctors area"
					+ " method: endFstAssessment"
					+ " this method is being called by " + this.getId());
			System.out.println(doctor.getId()
					+ " decides for patient's test");
			System.out
					.println(this.getId() + " decides for patient's test");
			if (route[0] == 1) {
				printTime();

				System.out.println(patient.getId() + " needs Xray ");
				System.out.println(patient.getId() + " goes to qXray");
				patient.addToQ("qXRay ");
				// patient.increaseTestCounterXray();
				patient.setTotalNumTest(patient.getTotalNumTest() + 1);
				System.out.println(doctor.getId()
						+ " adds to list of patients in test "
						+ patient.getId());
				System.out.println(" method end First assessment"
						+ doctor.getId()
						+ " will add to his patients in test "
						+ patient.getId());

				doctor.myPatientsInTestAdd(patient);

				printElementsArray(doctor.getMyPatientsInTests(),
						" my patients in test ");

				if (route[1] == 1) {
					printTime();
					patient.setNextProc(1);
>>>>>>> Refactor endFstAssessment method
					System.out.println(patient.getId()
							+ " will need test after Xray");
					// patient goes to test after xray
				} else {
					printTime();
					patient.setNextProc(2);
<<<<<<< HEAD
					patient.setTotalNumTest(patient.getTotalNumTest() + 1);
					System.out.println(doctor.getId()
							+ " adds to list of patients in test "
							+ patient.getId());

					doctor.myPatientsInTestAdd(patient);
					System.out.println(" method end First assessment"
							+ doctor.getId()
							+ " has added to his patients in test "
							+ patient.getId());
					printElementsArray(doctor.getMyPatientsInTests(),
							" my patients in test ");

				}
			}
		} else {
			System.err
					.println(" ERROR: something is wrong here, no doctor to end fst assessment with "
							+ patient.getId());
			// this.doEndFstAssessment(this, patient);

		}
		System.out.println(doctor.getId() + " will decide what to do next");

		System.out.println(this.getId()
				+ " has finished fst assessment and  has removed "
				+ patient.getId() + " from his multitasking.  ");
		System.out.println("My multitasking factor is "
				+ this.multiTaskingFactor);
		printElementsArray(this.patientsInMultiTask,
				" patients in multitasking");
		System.out.println(this.getId() + "has available = "
				+ this.getAvailable());
		// Para mover paciente de la lista de espera.
		movePatientBedReassessment(doctor);
		System.out.println(doctor.getId()
				+ " is moving to doctors area at end first assessment");
		doctor.moveToDoctorsArea();
		doctor.decideWhatToDoNext();
=======
					System.out.println(patient.getId()
							+ " will go back to bed after Xray");
					// patient goes back to bed after xray
				}
			}

			else if (route[0] == 0 && route[1] == 1) {
				printTime();

				System.out.println(patient.getId()
						+ " needs test and didn't need xRay ");
				patient.addToQ("qTest ");
				// patient.increaseTestCounterTest();
				patient.setNextProc(2);
				patient.setTotalNumTest(patient.getTotalNumTest() + 1);
				System.out.println(doctor.getId()
						+ " adds to list of patients in test "
						+ patient.getId());
>>>>>>> Refactor endFstAssessment method

				doctor.myPatientsInTestAdd(patient);
				System.out.println(" method end First assessment"
						+ doctor.getId()
						+ " has added to his patients in test "
						+ patient.getId());
				printElementsArray(doctor.getMyPatientsInTests(),
						" my patients in test ");

			}
		}
	}

	public void movePatientBedReassessment(Doctor doctor) {
		if (patientsWaitingForCubicle.size() > 0) {
			Patient patientWaiting = patientsWaitingForCubicle.get(0);
			Resource bed = doctor.findBed(patientWaiting.getTriageNum());
			if (bed != null) {
				patientsWaitingForCubicle.remove(patientWaiting);
				patientWaiting.setMyBedReassessment(bed);
				patientWaiting.getMyBedReassessment().setAvailable(false);
				patientWaiting.moveBackToBed(bed);
			}
		}
	}

	public void removePatientFromDepartment(Patient patient) {

		printTime();
		System.out
				.println(patient.getId()
						+ " has finished first assessment and does not need anything else: IS LEAVING THE DEPATMENT");
		System.out.println("method remove patient from dep " + this.getId()
				+ " is setting " + patient.getMyResource().getId()
				+ " available= true");
		patient.getMyResource().setAvailable(true);
		patient.setMyResource(null);
		patient.setMyBedReassessment(null);

		printTime();
		System.out.println(patient.getId() + " goes to qTrolley");
		patient.addToQ("qTrolley ");
		patient.getTimeInSystem();
		System.out
				.println(patient.getId()
						+ " has finished first assessment and is leaving the depatment. His time in system is:  "
						+ patient.getTimeInSystem());
		patient.setIsInSystem(false);
		printTime();
		System.out.println(patient.getId() + " has left  the department");

		patient.getTimeInSystem();
		// TODO cambiar esto para pasarlos al trolley
		System.out.println(" resource and doctor  about to get released");
		System.out.println("method: endFstAssessment" + this.getId()
				+ " will remove from list of patients in bed "
				+ patient.getId());
		System.out.println(" method end fst assessment");

		this.setMyResource(null);
		this.myPatientsInBedRemove(patient);
		if (this.myPatientsBackInBed.contains(patient)) {
			this.myPatientsBackInBed.remove(patient);
		}

		System.out.println(this.getId() + " will move to doctors area"
				+ " method: endFstAssessment"
				+ " this method is being called by " + this.getId());

		// TODO cambiar esto para pasarlos al troley

	}

	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "backInBed",/*
																				 * triggerCondition
																				 * =
																				 * "$watcher.getNumAvailable()>0"
																				 * ,
																				 */whenToTrigger = WatcherTriggerSchedule.IMMEDIATE, scheduleTriggerPriority = 60, pick = 1)
	public void startReassessmentWatcher(Patient watchedAgent) {
		System.out.println("\n start reassessment by watcher "
				+ watchedAgent.getId() + " and " + this.getId());
		// this.startReassessment(watchedAgent);
		// if (this.myPatientsBackInBed.contains(watchedAgent)){
		// this.startInitAssessment(watchedAgent);
		// }
		// else
		if (this.getAvailable()) {
			if (this.myPatientsBackInBed.contains(watchedAgent)) {
				this.doReassessment(watchedAgent);
			}

			else
				this.decideWhatToDoNext();

		}

	}

	public void startReassessment(Patient watchedAgent) {

		if (this.myPatientsBackInBed.contains(watchedAgent)) {
			this.doReassessment(watchedAgent);
		}

		else {
			watchedAgent.getMyDoctor().doReassessment(watchedAgent);
		}

	}

	public void doReassessment(Patient patientBackInBed) {

		System.out.println(this.getId() + " is DOING reassessment to "
				+ patientBackInBed.getId());
		printTime();
		if (this.myPatientsBackInBed.contains(patientBackInBed)) {
			this.myPatientsBackInBed.remove(patientBackInBed);
		}

		System.out.println("       \n \nSTART RE-ASSESSMENT  " + this.getId()
				+ " and " + patientBackInBed.getId());
		printTime();
		System.out.println(patientBackInBed.getId() + " is at "
				+ patientBackInBed.getMyBedReassessment().getId() + " loc "
				+ patientBackInBed.getLoc(grid));

		Resource bedPatient = patientBackInBed.getMyBedReassessment();
		printTime();
		System.out.println(this.getId() + " will go to " + bedPatient.getId());
		GridPoint loc = patientBackInBed.getLoc(grid);
		this.moveTo(grid, loc);
		printTime();
		System.out.println(this.getId() + " moves to " + bedPatient.getId());
		this.setMyResource(bedPatient);
		patientBackInBed.setMyResource(bedPatient);
		System.out.println(this.getId() + " and " + patientBackInBed.getId()
				+ " have as resource:" + bedPatient.getId());
		System.out.println(this.getId() + " is setting " + bedPatient.getId()
				+ " available= false");
		bedPatient.setAvailable(false);
		this.setMultitask(true);
		// this.setAvailable(false);
		if (this.available)
			this.decideWhatToDoNext();
		System.out.println(bedPatient.getId() + " and " + this.getId()
				+ " are set not available");
		printTime();
		System.out.println(this.getId() + " will schedule end reassessment");
		this.scheduleEndReassessment(this, patientBackInBed);

	}

	public void scheduleEnd1stAssessment(Patient fstpatient) {
		// System.out
		// .println(" aquie está el metodo scheduleEnd1stAssessment(Patient fstpatient) ");
		// double parameters[] =
		// fstAssessmentParameters(fstpatient.getTriageNum());
		// double serviceTime = triangularObs(parameters[0], parameters[1],
		// parameters[2]);
		// // System.out.println("triangularOBS :   " + serviceTime);
		// ISchedule schedule =
		// repast.simphony.engine.environment.RunEnvironment
		// .getInstance().getCurrentSchedule();
		//
		// // double timeEndService = schedule.getTickCount()
		// // + serviceTime;
		//
		// double timeEndService = schedule.getTickCount() + serviceTime;
		// this.setNextEndingTime(timeEndService);
		// ScheduleParameters scheduleParams = ScheduleParameters
		// .createOneTime(timeEndService);
		// EndFstAssessment action2 = new EndFstAssessment(this, fstpatient);
		//
		// schedule.schedule(scheduleParams, action2);
		//
		// System.out.println(" first assessment of " + fstpatient.getId()
		// + " schedule to end first assessment at " + timeEndService);

		double parameters[] = fstAssessmentParameters(fstpatient.getTriageNum());
		double serviceTime = 0;
		if ((fstpatient.getTriage() == "Blue ")
				|| (fstpatient.getTriage() == "Green ")) {
			serviceTime = distTriangular(parameters[0], parameters[1],
					parameters[2]);
			// this.iniAssessmentTSampleTriang.add(serviceTime);
			// System.out.println(" init assessment sample triang: " +
			// this.iniAssessmentTSampleTriang);
		} else {
			serviceTime = distLognormal(parameters[0], parameters[1],
					parameters[2]);
			// this.iniAssessmentTSampleLogn.add(serviceTime);
			// System.out.println(" init assessment sample logn: " +
			// this.iniAssessmentTSampleLogn);
		}

		// System.out.println("triangularOBS :   " + serviceTime);
		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();

		// double timeEndService = schedule.getTickCount()
		// + serviceTime;

		double timeEndService = schedule.getTickCount() + serviceTime;
		this.setNextEndingTime(timeEndService);
		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		EndFstAssessment action2 = new EndFstAssessment(this, fstpatient);
		fstpatient.setTimeEndCurrentService(timeEndService);
		schedule.schedule(scheduleParams, action2);

		System.out.println("\n\tFirst assessment of " + fstpatient.getId()
				+ " schedule to end first assessment at " + timeEndService);

		System.out.println(this.getId()
				+ " has started first assessment and  has added "
				+ fstpatient.getId() + " to his multitasking.  ");
		System.out.println("My multitasking factor is "
				+ this.multiTaskingFactor);
		printElementsArray(this.patientsInMultiTask,
				" patients in multitasking");
		System.out.println(this.getId() + "has available = "
				+ this.getAvailable());

	}

	public void endReassessment(Patient patient) {
		printTime();
		Doctor doctor = null;
		printTime();
		if (this.isInShift == false) {
			doctor = this.doctorToTakeOver();

		}

		else {
			doctor = this;
		}

		int totalProcess = patient.getTotalProcesses();
		patient.setTotalProcesses(totalProcess + 1);

		printTime();

		System.out
				.println("*****                               \n \n END RE-ASSESSMENT "
						+ doctor.getId()
						+ " and "
						+ patient.getId()

						+ " are at "
						+ patient.getMyBedReassessment().getId()
						+ " loc "
						+ patient.getLoc(grid)
						+ "                  \n  ");

		Resource resourceToRelease = patient.getMyBedReassessment();
		System.out.println(" the bed to release is: "
				+ resourceToRelease.getId());
		// resourceToRelease.setAvailable(true);
		// patient.getMyResource().setAvailable(true);

		doctor.setMyPatientCalling(null);

		if (doctor.getMyPatientsInBedTime().size() == doctor
				.getMyPatientsInBedTriage().size()) {

			int myPatientsInBed = doctor.getMyPatientsInBedTriage().size();
			doctor.setMyNumPatientsInBed(myPatientsInBed);

		} else {
			System.err
					.println(" \n ERROR: The lists patients in bed time and triage do not have the same objects");
		}

		System.out.println(patient.getId()
				+ " has finished reassessment and will go to qTrolley ");
		patient.addToQ("qTrolley ");
		System.out.println(" method end reassessment ");

		if (doctor.myPatientsBackInBed.contains(patient)) {
			doctor.myPatientsBackInBed.remove(patient);
		}

		patient.getTimeInSystem();
		System.out
				.println(patient.getId()
						+ " has finished first Re-assessment and is leaving the depatment. His time in system is:  "
						+ patient.getTimeInSystem());

		System.out
				.println(this.getId() + " at end reassessment has assign to "
						+ patient.getId() + " a null  doctor: "
						+ patient.getMyDoctor());

		System.out.println("method: endReassessment " + doctor.getId()
				+ " has removed from patients in bed " + patient.getId());

		printTime();
		System.out.println(patient.getId() + " has left the department ");

		// TODO cambiar esto para pasarlos al trolley

		System.out.println(doctor.getId() + " is available? "
				+ doctor.getAvailable() + resourceToRelease.getId()
				+ " is available " + resourceToRelease.getAvailable());
		// grid.moveTo(doctor, doctor.initPosX, doctor.initPosY);
		System.out
				.println(" doctor will move back to doctors area, method: endReassessment"
						+ " this method is being called by " + this.getId());
		this.patientsInMultiTask.remove(patient);
		System.out.println(this.getId()
				+ " has finished re-assessment and  has removed "
				+ patient.getId() + " from his multitasking.  ");
		doctor.setMultitask(false);
		System.out.println("My multitasking factor is "
				+ this.multiTaskingFactor);
		printElementsArray(this.patientsInMultiTask,
				" patients in multitasking");
		System.out.println(this.getId() + "has available = "
				+ this.getAvailable());

		doctor.setMyResource(null);
		patient.setMyResource(null);
		patient.setIsInSystem(false);
		doctor.myPatientsInBedRemove(patient);
		patient.setMyDoctor(null);

		// doctor.setAvailable(true);

		System.out.println("method end reassessment " + doctor.getId()
				+ " is setting " + resourceToRelease.getId()
				+ " available= true");
		resourceToRelease.setAvailable(true);
		if (resourceToRelease.getWhoBlockedMe() == patient) {
			resourceToRelease.setWhoBlockedMe(null);
			System.out.println(patient.getId() + "has unblocked "
					+ resourceToRelease.getId());
		}
		movePatientBedReassessment(doctor);
		System.out.println(doctor.getId()
				+ " is moving to doctors area at end re-assessment");
		doctor.moveToDoctorsArea();
		doctor.decideWhatToDoNext();

	}

	public double getReassessmentTime(Patient patient) {
		double time = 0;
		double parameters[] = this.reassessmentParameters(patient
				.getTriageNum());
		double min = parameters[0];
		double mean = parameters[1];
		double max = parameters[2];
		if (patient.getWasInTest() || patient.getWasInXray()) {
			// time=distExponential(min, mean, max);
			time = RandomHelper.createExponential(mean).nextDouble();
			// this.reAssessmentTSampleExp.add(time);
			// System.out.println(" reassessment sample exponential: " +
			// reAssessmentTSampleExp);
		} else {
			time = distLognormal(min, mean, max);
			// this.reAssessmentTSampleLogn.add(time);
			// System.out.println(" reassessment sample lognormal: " +
			// reAssessmentTSampleLogn);
		}

		return time;
	}

	public void scheduleEndReassessment(Doctor doctor, Patient fstpatient) {

		printTime();
		System.out.println(this.getId()
				+ " is scheduling the end of reassessment between: "
				+ doctor.getId() + " and " + fstpatient.getId());
		// double parameters[] =
		// reassessmentParameters(fstpatient.getTriageNum());
		// System.out.println("params are: " + parameters[0] + " " +
		// parameters[2]
		// + " Doctor Type is " + doctor.doctorType + " triage is :  "
		// + fstpatient.getTriageNum());
		// double serviceTime = distLognormal(parameters[0], parameters[1],
		// parameters[2]);
		// System.out.println("triagular REA " + serviceTime);
		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();

		// double timeEndService = schedule.getTickCount()
		// + serviceTime;

		double timeEndService = schedule.getTickCount()
				+ getReassessmentTime(fstpatient);
		doctor.setNextEndingTime(timeEndService);
		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		fstpatient.setTimeEndCurrentService(timeEndService);
		EndReassessment action2 = new EndReassessment(doctor, fstpatient);

		schedule.schedule(scheduleParams, action2);
		this.patientsInMultiTask.add(fstpatient);
		System.out.println(this.getId()
				+ " has started re-assessment and  has added "
				+ fstpatient.getId() + " to his multitasking.  ");
		System.out.println("My multitasking factor is "
				+ this.multiTaskingFactor);
		printElementsArray(this.patientsInMultiTask,
				" patients in multitasking");
		System.out.println(this.getId() + " has available = "
				+ this.getAvailable());
		System.out.println(fstpatient.getId()
				+ " expected to end reassessment at " + timeEndService);

	}

	public Resource findBed(int Triage) {
		Resource resource = null;
		switch (Triage) {
		case 1:
			resource = findResourceAvailable("minor cubicle ");
			break;
		case 2:
			resource = findResourceAvailable("minor cubicle ");
			break;
		case 3:
			resource = findResourceAvailable("major cubicle ");
			if (resource == null) {
				resource = findResourceAvailable("minor cubicle ");
			}
			break;
		case 4:
			resource = findResourceAvailable("major cubicle ");
			if (resource == null) {
				resource = findResourceAvailable("minor cubicle ");
			}
			break;
		case 5:
			resource = findResourceAvailable("resus cubicle ");
			break;
		}
		return resource;
	}

	public double[] fstAssessmentParameters(int Triage) {
		double parameters[] = { 0, 0, 0 };
		if (this.doctorType == "SHO ") {
			switch (Triage) {
			case 1:
				parameters[0] = 5;
				parameters[1] = 20;
				parameters[2] = 55;
				break;
			case 2:
				parameters[0] = 5;
				parameters[1] = 20;
				parameters[2] = 55;
				break;
			case 3:
				parameters[0] = 1;
				parameters[1] = 38;
				parameters[2] = 30;
				break;
			case 4:
				parameters[0] = 1;
				parameters[1] = 38;
				parameters[2] = 30;
				break;
			case 5:
				parameters[0] = 1;
				parameters[1] = 27;
				parameters[2] = 15;
				break;
			}
		} else {
			switch (Triage) {
			case 1:
				parameters[0] = 5;
				parameters[1] = 15;
				parameters[2] = 45;
				break;
			case 2:
				parameters[0] = 5;
				parameters[1] = 15;
				parameters[2] = 45;
				break;
			case 3:
				parameters[0] = 1;
				parameters[1] = 30;
				parameters[2] = 24;
				break;
			case 4:
				parameters[0] = 1;
				parameters[1] = 30;
				parameters[2] = 24;
				break;
			case 5:
				parameters[0] = 1;
				parameters[1] = 27;
				parameters[2] = 15;
				break;
			}

		}
		return parameters;
	}

	public double[] reassessmentParameters(int Triage) {
		double parameters[] = { 0, 0, 0 };
		if (this.doctorType == "SHO ") {
			switch (Triage) {
			case 1:
				parameters[0] = 1;
				parameters[1] = 10;
				parameters[2] = 8;
				break;
			case 2:
				parameters[0] = 1;
				parameters[1] = 10;
				parameters[2] = 8;
				break;
			case 3:
				parameters[0] = 1;
				parameters[1] = 28;
				parameters[2] = 22;
				break;
			case 4:
				parameters[0] = 1;
				parameters[1] = 28;
				parameters[2] = 22;
				break;
			case 5:
				parameters[0] = 1;
				parameters[1] = 27;
				parameters[2] = 15;
				break;
			}
		} else {
			switch (Triage) {
			case 1:
				parameters[0] = 1;
				parameters[1] = 8;
				parameters[2] = 5;
				break;
			case 2:
				parameters[0] = 1;
				parameters[1] = 8;
				parameters[2] = 5;
				break;
			case 3:
				parameters[0] = 1;
				parameters[1] = 22;
				parameters[2] = 19;
				break;
			case 4:
				parameters[0] = 1;
				parameters[1] = 22;
				parameters[2] = 19;
				break;
			case 5:
				parameters[0] = 1;
				parameters[1] = 27;
				parameters[2] = 15;
				break;
			}

		}
		return parameters;
	}

	public int getNumAvailable() {
		return numAvailable;
	}

	private void setMyResource(Resource rAvailable) {
		this.myResource = rAvailable;
		// System.out.println(this.getId() + " is at "+myResource.getId()+
		// " is available? "+ myResource.getAvailable()+ " at "+ getTime());

	}

	public int[] decideTests(int triageNum) {
		Uniform unif = RandomHelper.createUniform();
		double rndXRay = unif.nextDouble();
		double rndTest = unif.nextDouble();
		int testRoute[] = { 0, 0 };
		if (rndXRay <= getMatrixPropTest()[triageNum - 1][0]) {
			testRoute[0] = 1;

		}
		if (rndTest <= getMatrixPropTest()[triageNum - 1][1]) {
			testRoute[1] = 1;

		}
		return testRoute;
	}

	public static void initSaticVars() {
		setCount(0);

	}

	// implements comparator

	class PriorityQueueComparatorTriage implements Comparator<Patient> {

		@Override
		public int compare(Patient p1, Patient p2) {

			if (p1.getTriageNum() > p2.getTriageNum()) {
				return -1;
			}
			if (p1.getTriageNum() < p2.getTriageNum()) {
				return 1;
			} else {
				if (p1.getTimeInSystem() > p2.getTimeInSystem()) {
					return -1;
				}
				if (p1.getTimeInSystem() < p2.getTimeInSystem()) {
					return 1;
				} else
					return 0;
			}

		}
	}

	class PriorityQueueComparatorTime implements Comparator<Patient> {

		@Override
		public int compare(Patient p1, Patient p2) {
			if (p1.getTimeInSystem() > p2.getTimeInSystem()) {
				return -1;
			}
			if (p1.getTimeInSystem() < p2.getTimeInSystem()) {
				return 1;
			}
			return 0;
		}

	}

	// GETTERS AND SETTERS

	public Resource getMyResource() {
		return myResource;
	}

	public Boolean getAvailable() {
		return this.available;
	}

	/*
	 * 
	 */
	public void setMultitask(boolean startSomethingWithAPatient) {
		// int numInMultiT = this.patientsInMultiTask.size();
		// if (this.available) {
		//
		// if (numInMultiT < this.multiTaskingFactor) {
		// this.numAvailable += 1;
		// }
		//
		// } else {
		// this.numAvailable -= 1;
		// if (this.numAvailable > 0) {
		// if (numInMultiT < this.multiTaskingFactor) {
		// this.setAvailable(true);
		// }
		// }
		// }
		if (this.patientsInMultiTask.size() > this.multiTaskingFactor) {
			System.out.println("Wait!");
		}
		if (startSomethingWithAPatient) {
			this.numAvailable -= 1;
			if (this.numAvailable == 0) {
				this.setAvailable(false);
			}
		} else {
			if (this.numAvailable < this.multiTaskingFactor) {
				this.numAvailable += 1;
				setAvailable(true);
			}
		}

	}

	public void setAvailable(boolean available) {
		this.available = available;
		// int numInMultiT = this.patientsInMultiTask.size();
		// if (numInMultiT < this.multiTaskingFactor) {
		// this.available=true;}

	}

	// public void setAvailable(Boolean available) {
	// this.available = available;
	// if (this.available){
	// if (startShift){
	// this.numAvailable = this.multiTaskingFactor;
	// int numInMultiT= this.patientsInMultiTask.size();
	// } else {
	// // if(this.numAvailable < this.multiTaskingFactor){
	// // this.numAvailable += 1;
	// // this.moveToDoctorsArea();
	// // }
	// // }
	// if (this.numAvailable == 0){
	// this.numAvailable += 1;
	// } else {
	// this.decideWhatToDoNext();
	// }
	// }
	// if (!this.isBusy & this.numAvailable < this.multiTaskingFactor){
	// this.numAvailable += 1;
	// }
	//
	// } else {
	// this.numAvailable -= 1;
	// startShift = false;
	// if(this.numAvailable > 0){
	// this.setAvailable(true);
	// }
	// }
	// // if(this.available){
	// // this.numAvailable = 1;
	// // } else {
	// // this.numAvailable = 0;
	// // }
	// //
	//
	//
	// }

	public void setNumAvailable(int numAvailable) {
		this.numAvailable = numAvailable;
	}

	public int getInitPosX() {
		return initPosX;
	}

	public int getInitPosY() {
		return initPosY;
	}

	public static final int getCount() {
		return count;
	}

	public static final void setCount(int count) {
		Doctor.count = count;
	}

	// public PriorityQueue<Patient> getMemoryByTriage() {
	//
	// return memoryByTriage;
	// }
	//
	// public void setMemoryByTriage(PriorityQueue<Patient> memoryByTriage) {
	//
	// this.memoryByTriage = memoryByTriage;
	// }

	// public PriorityQueue<Patient> getMemoryByTime() {
	// return memoryByTime;
	// }
	//
	// public void setMemoryByTime(PriorityQueue<Patient> memoryByTime) {
	//
	// this.memoryByTime = memoryByTime;
	// }

	public Patient getMyPatientCalling() {
		return myPatientCalling;
	}

	public void setMyPatientCalling(Patient myPatientCalling) {
		this.myPatientCalling = myPatientCalling;
	}

	public int getMyNumPatientsInBed() {
		return myNumPatientsInBed;
	}

	public void setMyNumPatientsInBed(int myPatientsInBed) {
		this.myNumPatientsInBed = myPatientsInBed;
	}

	public PriorityQueue<Patient> getMyPatientsInBedTime() {
		return myPatientsInBedTime;
	}

	public void setMyPatientsInBedTime(
			PriorityQueue<Patient> myPatientsInBedTime) {
		this.myPatientsInBedTime = myPatientsInBedTime;
	}

	public PriorityQueue<Patient> getMyPatientsInBedTriage() {
		return myPatientsInBedTriage;
	}

	public void setMyPatientsInBedTriage(
			PriorityQueue<Patient> myPatientsInBedTriage) {
		this.myPatientsInBedTriage = myPatientsInBedTriage;
	}

	public int getIdNum() {
		return idNum;
	}

	public void setIdNum(int idNum) {
		this.idNum = idNum;
	}

	public float[][] getMyShiftMatrix() {

		return myShiftMatrix;
	}

	public void setMyShiftMatrix(float[][] shift) {
		this.myShiftMatrix = shift;

	}

	public boolean isInShift() {
		return isInShift;
	}

	public void setInShift(boolean isInShift) {
		this.isInShift = isInShift;
	}

	public double getNextEndingTime() {
		return nextEndingTime;
	}

	public void setNextEndingTime(double nextEndingTime) {
		this.nextEndingTime = nextEndingTime;
	}

	public ArrayList<Patient> getMyPatientsInTests() {
		return myPatientsInTests;
	}

	public void setMyPatientsInTests(ArrayList<Patient> myPatientsInTests) {
		this.myPatientsInTests = myPatientsInTests;
	}

	public double getTimeInitShift() {
		return timeInitShift;
	}

	public void setTimeInitShift(double d) {
		this.timeInitShift = d;
	}

	public double getX1MyNumPatientsSeen() {
		return x1MyNumPatientsSeen;
	}

	public void setX1MyNumPatientsSeen(double x1MyNumPatientsSeen) {
		this.x1MyNumPatientsSeen = x1MyNumPatientsSeen;
	}

	public double getX2MyTimeWorkedInShift() {
		return x2MyTimeWorkedInShift;
	}

	public void setX2MyTimeWorkedInShift(double x2MyTimeWorkedInShift) {
		this.x2MyTimeWorkedInShift = x2MyTimeWorkedInShift;
	}

	public double getX3TriageMaxAmongMyPatients() {
		return x3TriageMaxAmongMyPatients;
	}

	public void setX3TriageMaxAmongMyPatients(double x3TriageMaxAmongMyPatients) {
		this.x3TriageMaxAmongMyPatients = x3TriageMaxAmongMyPatients;
	}

	public double getX4MyPatientsAverageTimeInSys() {
		return x4MyPatientsAverageTimeInSys;
	}

	public void setX4MyPatientsAverageTimeInSys(
			double x4MyPatientsAverageTimeInSys) {
		this.x4MyPatientsAverageTimeInSys = x4MyPatientsAverageTimeInSys;
	}

	public double getX5RatioTestMaxTestMyPatients() {
		return x5RatioTestMaxTestMyPatients;
	}

	public void setX5RatioTestMaxTestMyPatients(
			double x5RatioTestMaxTestMyPatients) {
		this.x5RatioTestMaxTestMyPatients = x5RatioTestMaxTestMyPatients;
	}

	public double getX6MyTotalTimeWorkedInDpmnt() {
		return x6MyTotalTimeWorkedInDpmnt;
	}

	public void setX6MyTotalTimeWorkedInDpmnt(double x6MyTotalTimeWorkedInDpmnt) {
		this.x6MyTotalTimeWorkedInDpmnt = x6MyTotalTimeWorkedInDpmnt;
	}

	public double getX7MyPatientsMaxTimeInSys() {
		return x7MyPatientsMaxTimeInSys;
	}

	public void setX7MyPatientsMaxTimeInSys(double x7MyPatientsTotalTimeInSys) {
		this.x7MyPatientsMaxTimeInSys = x7MyPatientsTotalTimeInSys;
	}

	public double getTimeEnterSimulation() {
		return timeEnterSimulation;
	}

	public void setTimeEnterSimulation(double timeEnterSimulation) {
		this.timeEnterSimulation = timeEnterSimulation;
	}

	public double getC1MyMaxPatientHour() {
		return c1MyMaxPatientHour;
	}

	public void setC1MyMaxPatientHour(double c1MyMaxPatientHour) {
		this.c1MyMaxPatientHour = c1MyMaxPatientHour;
	}

	public double getC2MyDurationShift() {
		this.c2MyDurationShift = this.durationOfShift[getDay()];
		return c2MyDurationShift;
	}

	public void setC2MyDurationShift(double c2MyDurationShift) {
		this.c2MyDurationShift = c2MyDurationShift;
	}

	public double getC3LogisticCalmC() {
		return c3LogisticCalmC;
	}

	public void setC3LogisticCalmC(double c3LogisticCalmC) {
		this.c3LogisticCalmC = c3LogisticCalmC;
	}

	public double getC4LogisticKnowledgeC() {
		return c4LogisticKnowledgeC;
	}

	public void setC4LogisticKnowledgeC(double c4LogisticKnowledgeC) {
		this.c4LogisticKnowledgeC = c4LogisticKnowledgeC;
	}

	public double getC5LogisticExperienceC() {
		return c5LogisticExperienceC;
	}

	public void setC5LogisticExperienceC(double c5LogisticExperienceC) {
		this.c5LogisticExperienceC = c5LogisticExperienceC;
	}

	public double getC6LogisticReputationC() {
		return c6LogisticReputationC;
	}

	public void setC6LogisticReputationC(double c6LogisticReputationC) {
		this.c6LogisticReputationC = c6LogisticReputationC;
	}

	public double getAlpha3Experience() {
		return alpha3Experience;
	}

	public void setAlpha3Experience(double alpha3Experience) {
		this.alpha3Experience = alpha3Experience;
	}

	public double getAlpha1Calmness() {
		return alpha1Calmness;
	}

	public void setAlpha1Calmness(double alpha1Calmness) {
		this.alpha1Calmness = alpha1Calmness;
	}

	public double getAlpha2Knowledge() {
		return alpha2Knowledge;
	}

	public void setAlpha2Knowledge(double alpha2Knowledge) {
		this.alpha2Knowledge = alpha2Knowledge;
	}

	public double getZ1Energy() {
		return z1Energy;
	}

	public void setZ1Energy(double z1Energy) {
		this.z1Energy = z1Energy;
	}

	public double getZ2Calmness() {
		return z2Calmness;
	}

	public void setZ2Calmness(double z2Calmness) {
		this.z2Calmness = z2Calmness;
	}

	public double getZ3Knowledge() {
		return z3Knowledge;
	}

	public void setZ3Knowledge(double z3Knowledge) {
		this.z3Knowledge = z3Knowledge;
	}

	public double getZ4Experience() {
		return z4Experience;
	}

	public void setZ4Experience(double z4Experience) {
		this.z4Experience = z4Experience;
	}

	public double getZ5Reputation() {
		return z5Reputation;
	}

	public void setZ5Reputation(double z5Reputation) {
		this.z5Reputation = z5Reputation;
	}

	public double getW1Fatigue() {
		return w1Fatigue;
	}

	public void setW1Fatigue(double w1Fatigue) {
		this.w1Fatigue = w1Fatigue;
	}

	public double getW2Stress() {
		return w2Stress;
	}

	public void setW2Stress(double w2Stress) {
		this.w2Stress = w2Stress;
	}

	public double getW3WillOfKnowledge() {
		return w3WillOfKnowledge;
	}

	public void setW3WillOfKnowledge(double w3WillOfKnowledge) {
		this.w3WillOfKnowledge = w3WillOfKnowledge;
	}

	public double getW4SocialDesire() {
		return w4SocialDesire;
	}

	public void setW4SocialDesire(double w4SocialDesire) {
		this.w4SocialDesire = w4SocialDesire;
	}

	public double getAlphaZ1W1() {
		return alphaZ1W1;
	}

	public void setAlphaZ1W1(double alphaZ1W1) {
		this.alphaZ1W1 = alphaZ1W1;
	}

	public double getAlphaZ2W2() {
		return alphaZ2W2;
	}

	public void setAlphaZ2W2(double alphaZ2W2) {
		this.alphaZ2W2 = alphaZ2W2;
	}

	public double getAlphaZ3W3() {
		return alphaZ3W3;
	}

	public void setAlphaZ3W3(double alphaZ3W3) {
		this.alphaZ3W3 = alphaZ3W3;
	}

	public double getAlphaZ5W4() {
		return alphaZ5W4;
	}

	public void setAlphaZ5W4(double alphaZ5W4) {
		this.alphaZ5W4 = alphaZ5W4;
	}

	public double getcZ1W1() {
		return cZ1W1;
	}

	public void setcZ1W1(double cZ1W1) {
		this.cZ1W1 = cZ1W1;
	}

	public double getcZ2W2() {
		return cZ2W2;
	}

	public void setcZ2W2(double cZ2W2) {
		this.cZ2W2 = cZ2W2;
	}

	public double getcZ3W3() {
		return cZ3W3;
	}

	public void setcZ3W3(double cZ3W3) {
		this.cZ3W3 = cZ3W3;
	}

	public double getcZ5W4() {
		return cZ5W4;
	}

	public void setcZ5W4(double cZ5W4) {
		this.cZ5W4 = cZ5W4;
	}

	public double[] getZ3KnowledgeMatrixPatient() {
		return z3KnowledgeMatrixPatient;
	}

	public void setZ3KnowledgeMatrixPatient(double[] z3KnowledgeMatrixPatient) {
		this.z3KnowledgeMatrixPatient = z3KnowledgeMatrixPatient;
	}

	public double[] getW3WillOfKnowledgeMatrix() {
		return w3WillOfKnowledgeMatrix;
	}

	public void setW3WillOfKnowledgeMatrix(double[] w3WillOfKnowledgeMatrix) {
		this.w3WillOfKnowledgeMatrix = w3WillOfKnowledgeMatrix;
	}

	public double getW3WillOfKnowledge1() {
		return w3WillOfKnowledge1;
	}

	public void setW3WillOfKnowledge1(double w3WillOfKnowledge1) {
		this.w3WillOfKnowledge1 = w3WillOfKnowledge1;
	}

	public double getW3WillOfKnowledge2() {
		return w3WillOfKnowledge2;
	}

	public void setW3WillOfKnowledge2(double w3WillOfKnowledge2) {
		this.w3WillOfKnowledge2 = w3WillOfKnowledge2;
	}

	public double getW3WillOfKnowledge3() {
		return w3WillOfKnowledge3;
	}

	public void setW3WillOfKnowledge3(double w3WillOfKnowledge3) {
		this.w3WillOfKnowledge3 = w3WillOfKnowledge3;
	}

	public double getW3WillOfKnowledge4() {
		return w3WillOfKnowledge4;
	}

	public void setW3WillOfKnowledge4(double w3WillOfKnowledge4) {
		this.w3WillOfKnowledge4 = w3WillOfKnowledge4;
	}

	public double getW3WillOfKnowledge5() {
		return w3WillOfKnowledge5;
	}

	public void setW3WillOfKnowledge5(double w3WillOfKnowledge5) {
		this.w3WillOfKnowledge5 = w3WillOfKnowledge5;
	}

	public double getW3WillOfKnowledge6() {
		return w3WillOfKnowledge6;
	}

	public void setW3WillOfKnowledge6(double w3WillOfKnowledge6) {
		this.w3WillOfKnowledge6 = w3WillOfKnowledge6;
	}

	public void setAllMyPatients(ArrayList<Patient> allMyPatients) {
		this.allMyPatients = allMyPatients;
	}

	public LinkedList<Patient> getMyPatientsBackInBed() {
		return myPatientsBackInBed;
	}

	public void setMyPatientsBackInBed(LinkedList<Patient> myPatientsBackInBed) {
		this.myPatientsBackInBed = myPatientsBackInBed;
	}

	public boolean isCanHelpAnotherDoctor() {
		return canHelpAnotherDoctor;
	}

	public void setCanHelpAnotherDoctor(boolean canHelpAnotherDoctor) {
		this.canHelpAnotherDoctor = canHelpAnotherDoctor;
	}

	public Doctor getDoctorToHandOver() {
		return doctorToHandOver;
	}

	public void setDoctorToHandOver(Doctor doctorToHandOver) {
		this.doctorToHandOver = doctorToHandOver;
	}

	public int getMultiTaskingFactor() {
		return multiTaskingFactor;
	}

	public void setMultiTaskingFactor(int multiTaskingFactor) {
		this.multiTaskingFactor = multiTaskingFactor;
	}

}
