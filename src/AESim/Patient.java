package AESim;

import java.util.ArrayList;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Patient extends General {

	private int testCountX;
	private int testCountT;
	
	private int totalProcesses;
	private static int count;
	private int weekOutSystem;
	private int dayOutSystem;
	private int hourOutSystem;
	

	private int weekInSystem;
	private int dayInSystem;
	private int hourInSystem;
	private int totalNumTest;
 

	
	private Grid<Object> grid;
	private boolean hasReachedtarget;
	private boolean isFirstInQueueR;
	private boolean isFirstInQueueTriage;
	private boolean isFirstInQBlue;
	private boolean isFirstInQGreen;
	private boolean isFirstInQYellow;
	private boolean isFirstInQOrange;
	private boolean isFirstInQRed;
	private boolean isFirstInQueueTest;
	private boolean isFirstInQueueXRay;
	private boolean isInSystem;
	private boolean isEnteredSystem;
	private boolean wasInTest;
	private boolean wasInXray;
	private boolean waitInCublicle;

	private boolean backInBed;

	private String triage;
	private int triageNum;
	private int numPatient;
	private double queuingTimeQr;
	private double queuingTimeQt;
	private double queuingTimeQa;
	private double timeOfArrival;
	private double timeInSystem;
	private Queue currentQueue;
	private double timeEnteredCurrentQ;
	private double timeOutCurrentQueue;
	private int nextProc;
	
	private double timeEndCurrentService;
	// 1 if goes to test and 2 if goes back to bed

	private String typeArrival;
	private boolean wasFirstInQr;
	private double testRatio;

	private boolean wasFirstInQueueTriage;
	private boolean wasFirstInQueueXRay;
	private boolean wasFirstInQueueTest;
	private boolean wasFirstforAsses;
	
	private int isWaitingBedReassessment;

	private Resource myResource;
	private Resource myBedReassessment;
	private Doctor myDoctor;
	private int numWasFstForAssess;


	public Patient(Grid<Object> grid, String typeArrival, double time) {
		count++;
		this.setId("Patient " + count);
		this.setNumPatient(count);
		this.grid = grid;
		this.queuingTimeQr = 0;
		this.triage = " has not been triaged ";
		this.triageNum = 0;
		this.isFirstInQueueR = false;
		this.wasFirstforAsses = false;
		this.wasFirstInQr = false;
		this.setHasReachedtarget(false);
		this.isFirstInQueueTriage = false;
		this.isFirstInQueueXRay = false;
		this.wasFirstInQueueXRay = false;
		this.isFirstInQueueTest = false;
		this.wasFirstInQueueTest = false;
		this.typeArrival = typeArrival;
		this.timeOfArrival = time;
		this.timeInSystem = 0;
		this.queuingTimeQr = 0;
		this.queuingTimeQt = 0;
		this.queuingTimeQa = 0;
		this.currentQueue = null;
		this.timeEnteredCurrentQ = 0;
		this.timeOutCurrentQueue = 0;
		this.myResource = null;
		this.isInSystem = true;
		this.myBedReassessment = null;
		this.myDoctor = null;
		this.nextProc = 0;
		this.backInBed = false;
		this.isEnteredSystem = true;
		this.wasInTest= false;
		this.wasInXray= false;
		this.numWasFstForAssess=0;
		this.testCountX=0;
		this.testCountT=0;
		this.setTotalProcesses(0);
		this.timeEndCurrentService=0;
		
		this.waitInCublicle = true;
		
		this.isWaitingBedReassessment = 0;
	}
	


	@ScheduledMethod(start = 5, interval = 5, pick = 1)
	public void trackPatient() {
		Context<Object> context = this.getContext();
		for (Object agent : context.getObjects(Patient.class)) {
			if (agent != null) {
				Patient patient = (Patient) agent;
				if (patient.getId().equals("Patient 3")) {
					System.out.println("******* tracking:  ");
					System.out.println(" \t " + patient.getId()
							+ "\t \tcurrent Tick: " + patient.getTime()
							+ " (week: " + getWeek() + " day: " + getDay()
							+ " hour: " + getHour() + ")"
							+ "\n \t arrivalTime is "
							+ patient.getArrivalTime() + "\n \t time in Qr: "
							+ patient.getQueuingTimeQr()
							+ "\n \t time in System: "
							+ patient.getTimeInSystem()
							+ "\n \t current Queue: "
							+ patient.getCurrentQueue().getId()
							+ "\n \t Is in system? " + patient.getIsInSystem()
							+ "\n \t Type of arrival is: "
							+ patient.getTypeArrival() + "\n");
				}
			}
		}
	}

	public void addToQ(String name) {

		Queue queue = null;
		GridPoint locQueue = this.getQueueLocation(name, grid);
		queue = (Queue) grid.getObjectAt(locQueue.getX(), locQueue.getY());
		queue.addPatientToQueue(this);
		this.setCurrentQueue(queue);
		this.setTimeEnteredCurrentQ(getTime());
		grid.moveTo(this, locQueue.getX(), locQueue.getY());
		// queue.elementsInQueue();
		System.out.println(" *****************  " + this.getId()
				+ " has joined " + queue.getId() + " current loc "
				+ this.getLoc(grid).toString() + " time: " + getTime());
		System.out.println(queue.getId()+" : ");
		queue.elementsInQueue();
		// patient will seat in the patch in front of the queue if he is the
		// head of the queue
		Patient patientTocheck = queue.firstInQueue();
		if (patientTocheck == this) {
			this.moveToHeadOfQ(queue);
		}
	}

	public void moveToHeadOfQ(Queue queue) {

		int newX = queue.getLoc(grid).getX();
		int newY = queue.getLoc(grid).getY() + 1;

		System.out.println("when " + this.getId()+ " has reached the head of the queue, the objects in " + queue.getId());
		queue.elementsInQueue();
		

		Object whoInFirst = grid.getObjectAt(newX, newY);
		
		if (whoInFirst instanceof Patient) {
			Patient patientAtHead = (Patient) whoInFirst;
			System.out.println("alreade there is at the head of "+ queue.getId() +": " +  patientAtHead.getId());
		} else {
			grid.moveTo(this, newX, newY);
			System.out.println(this.getId() + " has moved to the head of: " + queue.getId() + " loc: "+ this.getLoc(grid).toString() + " time: " + getTime());

			if (this.getCurrentQueue().getId() == "queueR ") {
				this.setIsFirstInQueueR(true);
				this.setWasFirstInQr(true);
			}
			if (this.getCurrentQueue().getId() == "queueTriage ") {
				this.setIsFirstInQueueTriage(true);
				this.setWasFirstInQueueTriage(true);
			}
			if (this.getCurrentQueue().getId() == "qBlue ") {
				this.setIsFirstInQBlue(true);
				this.setWasFirstforAsses(true);
			}
			if (this.getCurrentQueue().getId() == "qGreen ") {
				this.setIsFirstInQGreen(true);
				this.setWasFirstforAsses(true);
			}
			if (this.getCurrentQueue().getId() == "qYellow ") {
				this.setIsFirstInQYellow(true);
				this.setWasFirstforAsses(true);
			}
			if (this.getCurrentQueue().getId() == "qOrange ") {
				this.setIsFirstInQOrange(true);
				this.setWasFirstforAsses(true);
			}
			if (this.getCurrentQueue().getId() == "qRed ") {
				this.setIsFirstInQRed(true);
				this.setWasFirstforAsses(true);
			}
			if (this.getCurrentQueue().getId() == "qTest ") {
				
				this.setIsFirstInQueueTest(true);
				this.setWasFirstInQueueTest(true);
			}
			if (this.getCurrentQueue().getId() == "qXRay ") {
				this.setIsFirstInQueueXRay(true);
				this.setWasFirstInQueueXRay(true);
			}

			if (this.wasFirstforAsses==true){
				this.numWasFstForAssess=1;
			}
		}
		//
	}
	
	@Watch(watcheeClassName = "AESim.Resource", watcheeFieldNames = "freeCount", triggerCondition = "$watcher.getIsWaitingBedReassessment()>0", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE, shuffle=false,scheduleTriggerPriority =120, pick = 1)
	public void catchBed(Resource watchedAgent){
		System.out.println(this.getId()+ " is waiting bed reassessment?: " + this.getIsWaitingBedReassessment()+ ".\t"+watchedAgent.getId() + " is available?: " + watchedAgent.getAvailable() + ". Simulation time: "+ getTime() );
	this.setIsWaitingBedReassessment(0);
		
	}
	
	public void moveBackToBed(Resource bed) {

		this.getTimeInSystem();
		// this.setIsInSystem(false);
		this.setIsInSystem(true);
		
		Doctor doctor = this.getMyDoctor();
		if (doctor == null) {
			System.err.println("\n ERROR: there is no doctor with patient");
		} else {
			System.out.println(" at end of test " + this.getId()
					+ " has in mind the doctor " + doctor.getId());
			this.moveTo(grid, bed.getLoc(grid));
			System.out.println(this.getId() + " has moved to bed reassessment "
					+ bed.getId());

			doctor.setMyPatientCalling(this);
			doctor.myPatientsInTestRemove(this);
			doctor.myPatientsInBedAdd(this);
			doctor.getMyPatientsBackInBed().add(this);

			this.setMyResource(bed);
			doctor.setMyNumPatientsInBed(doctor.getMyPatientsInBedTime().size());

			System.out.println(" AFTER TESTS (any) " + this.getId()
					+ " has in mind the doctor " + doctor.getId());

			System.out.println(doctor.getId()
					+ " has removed from his patients in test " + this.getId());
			System.out.println(doctor.getId()
					+ " has added to his patients in bed " + this.getId());
			printElementsQueue(doctor.getMyPatientsInBedTime(),
					" my patients in bed (time and triage)");

			String nameD = doctor.getId();

			printElementsArray(doctor.getMyPatientsInTests(), nameD
					+ " my patients in test ");

			System.out.println(" patients waiting in bed for " + doctor.getId()
					+ ": " + doctor.getMyPatientsInBedTriage() + " list size: "
					+ doctor.getMyPatientsInBedTriage().size());

			this.setBackInBed(true);

		}
	}
	
	public void decideWhereToGo() {
		
		if (this.waitInCublicle == false) {
			Doctor myDoctor = this.getMyDoctor();
			Resource bed = myDoctor.findBed(this.getTriageNum());
			if (bed != null) {
				this.isWaitingBedReassessment = 0;
				this.setMyBedReassessment(bed);
				if (this.getMyBedReassessment()!=null){
					System.out.println(this.getMyBedReassessment().getId()+ " is available " + this.getMyBedReassessment().getAvailable() );
					this.getMyBedReassessment().setAvailable(false);
				} else {
					System.err.println("CualquiercosaYA ya ya!");
				}
				
				this.moveBackToBed(bed);
			}

			else {
				this.isWaitingBedReassessment = 1;
				//may need an array here to add pati wait for b reass
				this.addToQ("queueBReassess ");
				System.out.println(this.getId() + " has joined qBReassess.");
				patientsWaitingForCubicle.add(this);
			}
		}

		else {
			Resource myBed = this.getMyBedReassessment();
			this.moveBackToBed(myBed);			
			System.out.println(this.getId() + " is back to his bed reassessment "+ myBed.getId());
		}
	}
	

	/*
	 * This method is read when an new patient arrives
	 */

	public void addToQR() {
		
		Queue queueR = null;
		GridPoint locQueueR = this.getQueueLocation("queueR ", grid);
		queueR = (Queue) grid.getObjectAt(locQueueR.getX(), locQueueR.getY());

		GridPoint currentLoc = this.getLoc(grid);

		int x = currentLoc.getX();
		int y = currentLoc.getY();

		if (x == 1 && y == 0) {

			queueR.addPatientToQueue(this);
			this.setCurrentQueue(queueR);
			this.setTimeEnteredCurrentQ(getTime());
			grid.moveTo(this, locQueueR.getX(), locQueueR.getY());

		
		}
		
		queueR.elementsInQueue();

		if (queueR.firstInQueue() == this) {
			Object whoInFirst = grid.getObjectAt(1, 2);
			if (whoInFirst instanceof Patient) {

			} else {
				grid.moveTo(this, 1, 2);
				this.setIsFirstInQueueR(true);
				this.setWasFirstInQr(true);
			}
			

		}

	}

	/*
	 * This method is read when the simulation starts
	 */
	@ScheduledMethod(start = 0, shuffle = false, priority = 1)
	public void checkFstInQR() {
		System.out
				.println("                                                                              tick: "
						+ getTime()
						+ " (week: "
						+ getWeek()
						+ " day: "
						+ getDay() + " hour: " + getHour() + ")");
		Queue queueR = null;
		GridPoint locQueueR = this.getQueueLocation("queueR ", grid);
		queueR = (Queue) grid.getObjectAt(locQueueR.getX(), locQueueR.getY());
		queueR.elementsInQueue();
		if (queueR.firstInQueue() == this) {

			Object whoInFirst = grid.getObjectAt(1, 2);
			if (whoInFirst instanceof Patient) {

			} else {

				grid.moveTo(this, 1, 2);
				this.setIsFirstInQueueR(true);
				this.setWasFirstInQr(true);

			}
			
		}

	}

	/*
	 * This method is read when the simulation starts
	 */

	@ScheduledMethod(start = 0, shuffle = false, priority = 10)
	public void addToQRInit() {
		System.out
				.println("                                                                              tick: "
						+ getTime()
						+ " (week: "
						+ getWeek()
						+ " day: "
						+ getDay() + " hour: " + getHour() + ")");
		Queue queueR = null;
		GridPoint locQueueR = this.getQueueLocation("queueR ", grid);
		queueR = (Queue) grid.getObjectAt(locQueueR.getX(), locQueueR.getY());

		GridPoint currentLoc = this.getLoc(grid);

		int x = currentLoc.getX();
		int y = currentLoc.getY();

		if (x == 1 && y == 0) {
			queueR.addPatientToQueue(this);
			this.setCurrentQueue(queueR);
			this.setTimeEnteredCurrentQ(getTime());
			grid.moveTo(this, locQueueR.getX(), locQueueR.getY());
		}
		System.out.println(this.getId() + " has been added to "
				+ this.getCurrentQueue().getId() + " by " + this.typeArrival);
	}

	public void goBackReassesment(Resource bed) {
	}

	public static void initSaticVars() {
		setCount(0);

	}

	public final Boolean getIsFirstInQueueR() {
		return isFirstInQueueR;
	}

	public final Boolean getIsFirstInQueueTriage() {
		return isFirstInQueueTriage;
	}

	public int getTimeEnterQueue() {

		return 0;
	}

	public int getTimeLeaveQueue() {

		return 0;
	}

	public String getTriage() {
		return this.triage;
	}

	public final void setIsFirstInQueueR(Boolean isFirstInQueueR) {
		this.isFirstInQueueR = isFirstInQueueR;
	}

	public final void setIsFirstInQueueTriage(Boolean isFirstInQueueTriage) {
		this.isFirstInQueueTriage = isFirstInQueueTriage;
	}

	public void setTimeLeaveQueue(double time) {

	}

	public void setTriage(String triage) {
		this.triage = triage;
	}

	public final void setTriageNum(int triageNum) {
		this.triageNum = triageNum;
	}

	public final int getTriageNum() {
		return triageNum;
	}

	public String getTypeArrival() {
		return typeArrival;
	}

	public void setTypeArrival(String typeArrival) {
		this.typeArrival = typeArrival;
	}

	public final Boolean getIsFirstInQueueTest() {
		return isFirstInQueueTest;
	}

	public final void setIsFirstInQueueTest(Boolean isFirstInQueueTest) {
		this.isFirstInQueueTest = isFirstInQueueTest;
	}

	public final Boolean getWasFirstInQr() {
		return wasFirstInQr;
	}

	public final void setWasFirstInQr(Boolean wasFirstInQr) {
		this.wasFirstInQr = wasFirstInQr;
	}

	public final double getQueuingTimeQr() {
		if (this.currentQueue.getId().equals("queueR ")) {
			queuingTimeQr = this.getTime() - this.getTimeEnteredCurrentQ();
		}
		return queuingTimeQr;
	}

	public final void setQueuingTimeQr(double queuingTimeQr) {
		this.queuingTimeQr = queuingTimeQr;
	}

	public final double getQueuingTimeQt() {
		return queuingTimeQt;
	}

	public final void setQueuingTimeQt(double queuingTimeQt) {
		this.queuingTimeQt = queuingTimeQt;
	}

	public final double getQueuingTimeQa() {
		return queuingTimeQa;
	}

	public final void setQueuingTimeQa(double queuingTimeQa) {
		this.queuingTimeQa = queuingTimeQa;
	}

	public final double getArrivalTime() {
		return timeOfArrival;
	}

	public final void setArrivalTime(double arrivalTime) {
		this.timeOfArrival = arrivalTime;
	}

	public final double getTimeInSystem() {
		if (this.isInSystem){
			this.timeInSystem = getTime() - getArrivalTime();
			if (this.timeInSystem> 240){
				this.hasReachedtarget=true;
//				System.out.println(this.getId() + " has reached the target ");
//				printTime();
				if(this.timeInSystem>600)
				System.err.println("WARNING: "+ this.getId() + " has stayed more than 10 hours, and is at: " +this.getLoc(grid).toString()+ " time in system: "+ this.timeInSystem);
			}
		}
		return this.timeInSystem;
	}

	public final void setTimeInSystem(double timeInSystem) {
		this.timeInSystem = timeInSystem;
	}

	public final double getTimeOfArrival() {
		return timeOfArrival;
	}

	public final void setTimeOfArrival(double timeOfArrival) {
		this.timeOfArrival = timeOfArrival;
	}

	public Queue getCurrentQueue() {
		return currentQueue;
	}

	public void setCurrentQueue(Queue currentQueue) {
		this.currentQueue = currentQueue;
	}

	public double getTimeEnteredCurrentQ() {
		return timeEnteredCurrentQ;
	}

	public void setTimeEnteredCurrentQ(double timeInCurrentQueue) {
		this.timeEnteredCurrentQ = timeInCurrentQueue;
	}

	public double getTimeOutCurrentQueue() {
		return timeOutCurrentQueue;
	}

	public void setTimeOutCurrentQueue(double timeOutCurrentQueue) {
		this.timeOutCurrentQueue = timeOutCurrentQueue;
	}

	public final Boolean getWasFirstInQueueTriage() {
		return wasFirstInQueueTriage;
	}

	public final void setWasFirstInQueueTriage(Boolean wasFirstInQueueTriage) {
		this.wasFirstInQueueTriage = wasFirstInQueueTriage;
	}

	public final Resource getMyResource() {
		return myResource;
	}

	public final void setMyResource(Resource myResource) {
		this.myResource = myResource;
		// System.out.println(this.getId() + " is at "+myResource.getId()+
		// " is available? "+ myResource.getAvailable()+ " at "+ getTime());
	}

	public Boolean getWasFirstforAsses() {
		return wasFirstforAsses;
	}

	public void setWasFirstforAsses(Boolean wasFirstforAsses) {
		this.wasFirstforAsses = wasFirstforAsses;
	}

	public final Boolean getIsFirstInQBlue() {
		return isFirstInQBlue;
	}

	public final void setIsFirstInQBlue(Boolean isFirstInQBlue) {
		this.isFirstInQBlue = isFirstInQBlue;
	}

	public final Boolean getIsFirstInQGreen() {
		return isFirstInQGreen;
	}

	public final void setIsFirstInQGreen(Boolean isFirstInQGreen) {
		this.isFirstInQGreen = isFirstInQGreen;
	}

	public final Boolean getIsFirstInQYellow() {
		return isFirstInQYellow;
	}

	public final void setIsFirstInQYellow(Boolean isFirstInQYellow) {
		this.isFirstInQYellow = isFirstInQYellow;
	}

	public final Boolean getIsFirstInQOrange() {
		return isFirstInQOrange;
	}

	public final void setIsFirstInQOrange(Boolean isFirstInQOrange) {
		this.isFirstInQOrange = isFirstInQOrange;
	}

	public final Boolean getIsFirstInQRed() {
		return isFirstInQRed;
	}

	public final void setIsFirstInQRed(Boolean isFirstInQRed) {
		this.isFirstInQRed = isFirstInQRed;
	}

	public final Boolean getIsFirstInQueueXRay() {
		return isFirstInQueueXRay;
	}

	public final void setIsFirstInQueueXRay(Boolean isFirstInQueueXRay) {
		this.isFirstInQueueXRay = isFirstInQueueXRay;
	}

	public final Boolean getWasFirstInQueueXRay() {
		return wasFirstInQueueXRay;
	}

	public final void setWasFirstInQueueXRay(Boolean wasFirstInQueueXRay) {
		System.out
				.println("************************************************************************************"
						+ this.getId()
						+ "\n"
						+ " has the status of WAS FIRST in QUEUE "
						+ this.wasFirstInQueueXRay
						+ " it is going to be changed now to the oposite value. \n IT HAS TO TRIGGER NOW START XRAY, CHECK IF THERE IS AT LEAST ONE XRAY ROOM AVAILABLE");
		this.wasFirstInQueueXRay = wasFirstInQueueXRay;
		System.out
				.println("************************************************************************************"
						+ this.getId()
						+ "\n  has changed the status of WAS FIRST IN QUEUE to "
						+ this.wasFirstInQueueXRay
						+ "\n  the method START XRAY SHOULD HAVE FINISHED JUST NOW, TRUE??, IF NOT CHECK WHAT DID THIS PATIENT DO ");

	}

	public final Boolean getWasFirstInQueueTest() {

		return wasFirstInQueueTest;

	}

	public final void setWasFirstInQueueTest(Boolean wasFirstInQueueTest) {
		this.wasFirstInQueueTest = wasFirstInQueueTest;
	}

	public Boolean getIsInSystem() {
		return this.isInSystem;
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		Patient.count = count;
	}

	public void setIsInSystem(Boolean isInSystem) {
		this.isInSystem = isInSystem;
		if (this.isInSystem == false) {
			this.setHourOutSystem(getHour());
			this.setWeekOutSystem(getWeek());
			this.setDayOutSystem(getDay());
			}
	}

	public Boolean getIsEnteredSystem() {
		return isEnteredSystem;
	}

	public void setIsEnteredSystem(Boolean isEnteredSystem) {
		this.isEnteredSystem = isEnteredSystem;
		if (this.isEnteredSystem) {
			this.setHourInSystem(getHour());
			this.setWeekInSystem(getWeek());
			this.setDayInSystem(getDay());
			System.out.println(this.getId() + " has entered the system at: ");
			printTime();
		}

	}

	public Resource getMyBedReassessment() {
		return myBedReassessment;
	}

	public void setMyBedReassessment(Resource resourceReassesment) {
		this.myBedReassessment = resourceReassesment;
	}

	public int getNextProc() {
		return nextProc;
	}

	public void setNextProc(int nextProc) {
		this.nextProc = nextProc;
	}

	public int getNumPatient() {
		return numPatient;
	}

	public void setNumPatient(int numPatient) {
		this.numPatient = numPatient;
	}

	public Boolean getBackInBed() {
		return backInBed;
	}

	public void setBackInBed(Boolean backInBed) {
		this.backInBed = backInBed;
		if (this.backInBed==true){
			Doctor doctor= this.getMyDoctor();
			if (doctor.getMyPatientsBackInBed().contains(this)){
				
			}
			else {
				doctor.getMyPatientsBackInBed().add(this);
			}
			
		} 
	}

	public Doctor getMyDoctor() {
		return myDoctor;
	}

	public void setMyDoctor(Doctor myDoctor) {
		this.myDoctor = myDoctor;
	
	}

	public int getWeekInSystem() {
		return weekInSystem;
	}

	public void setWeekInSystem(int weekInSystem) {
		this.weekInSystem = weekInSystem;
	}

	public int getDayInSystem() {
		return dayInSystem;
	}

	public void setDayInSystem(int dayInSystem) {
		this.dayInSystem = dayInSystem;
	}

	public int getHourInSystem() {
		return hourInSystem;
	}

	public void setHourInSystem(int hourInSystem) {
		this.hourInSystem = hourInSystem;
	}

	public int getWeekOutSystem() {
		return weekOutSystem;
	}

	public void setWeekOutSystem(int weekOutSystem) {
		this.weekOutSystem = weekOutSystem;
	}

	public int getDayOutSystem() {
		return dayOutSystem;
	}

	public void setDayOutSystem(int dayOutSystem) {
		this.dayOutSystem = dayOutSystem;
	}

	public int getHourOutSystem() {
		return hourOutSystem;
	}

	public void setHourOutSystem(int hourOutSystem) {
		this.hourOutSystem = hourOutSystem;
	}

	public int getTotalNumTest() {
		return totalNumTest;
	}

	public void setTotalNumTest(int totalNumTest) {
		this.totalNumTest = totalNumTest;
	}

	public double getTestRatio() {
		return testRatio;
	}

	public void setTestRatio(double testRatio) {
		this.testRatio = testRatio;
	}

	public int getTestCounterX() {
		return testCountX;
	}

	public void increaseTestCounterXray() {
		this.testCountX++;
	}
	public int getTestCounterT() {
		return testCountT;
	}

	public void increaseTestCounterTest() {
		this.testCountT++;
	}

	public int getTotalProcesses() {
		return totalProcesses;
	}

	public void setTotalProcesses(int totalProcesses) {
		this.totalProcesses = totalProcesses;
	}

	public Boolean getHasReachedtarget() {
		return hasReachedtarget;
	}

	public void setHasReachedtarget(Boolean hasReachedtarget) {
		this.hasReachedtarget = hasReachedtarget;
	}
	
	public Boolean getWasInTest() {
		return wasInTest;
	}

	public void setWasInTest(Boolean wasInTest) {
		this.wasInTest = wasInTest;
	}

	public Boolean getWasInXray() {
		return wasInXray;
	}

	public void setWasInXray(Boolean wasInXray) {
		this.wasInXray = wasInXray;
	}

	public double getTimeEndCurrentService() {
		return timeEndCurrentService;
	}

	public void setTimeEndCurrentService(double timeEndCurrentService) {
		this.timeEndCurrentService = timeEndCurrentService;
	}



	public int getIsWaitingBedReassessment() {
		return isWaitingBedReassessment;
	}



	public void setIsWaitingBedReassessment(int isWaitingBedReassessment) {
		this.isWaitingBedReassessment = isWaitingBedReassessment;
	}



	public boolean isWaitInCublicle() {
		return waitInCublicle;
	}



	public int getNumWasFstForAssess() {
		return numWasFstForAssess;
	}



	public void setNumWasFstForAssess(int numWasFstForAssess) {
		this.numWasFstForAssess = numWasFstForAssess;
	}



	public void setWaitInCublicle(boolean waitInCublicle) {
		this.waitInCublicle = waitInCublicle;
	}


}