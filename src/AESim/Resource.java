package AESim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Resource extends General {

	private static int count = 0;
	private static int countTypeXRay = 1;

	private static int countTypeTest = 1;
	private static int countTypeTriage = 1;
	private static int countTypeTrolley = 1;
	private static int countTypeMinor = 1;
	private static int countTypeMajor = 1;
	private static int countTypeResus = 1;
	private General whoBlockedMe;
	
	private int freeCount;

	private Boolean available;
	private String resourceType;
	private int numAvailableTest;
	private int numAvailableXray;
	private ArrayList<Double> testTsample = new ArrayList<>();
	private ArrayList<Double> xRayTsample = new ArrayList<>();

	private Grid<Object> grid;
	private double processTime[] = new double[3];

	public  double[] getProcessTime() {
		return processTime;
	}

	public void setProcessTime(double[] processTime) {
		this.processTime = processTime;
	}

	// solo va a ser 1 cuando sean camas
	private int typeResource;

	public Resource(String resourceType, String rName, Grid<Object> grid) {
		// this.setId(resourceType + count++);
		this.available = true;
this.freeCount=0;
		this.resourceType = resourceType;
		if (resourceType.equals("triage cubicle ")) {
			this.setId(resourceType + countTypeTriage++);
		}

		if (resourceType.equals("trolley ")) {
			this.setId(resourceType + countTypeTrolley++);

		}

		if (resourceType.equals("minor cubicle ")) {
			this.setId(resourceType + countTypeMinor++);
			this.setAvailable(true);
		}

		if (resourceType.equals("major cubicle ")) {
			this.setId(resourceType + countTypeMajor++);

		}
		if (resourceType.equals("resus cubicle ")) {
			this.setId(resourceType + countTypeResus++);

		}

		if (resourceType.equals("xRayRoom ")) {
			this.setId(resourceType + countTypeXRay++);
			this.setNumAvailableXray(1);
			processTime[0] = 20;
			processTime[1] = 40;
			processTime[2] = 32;
		} else if (resourceType.equals("testRoom ")) {
			this.setId(resourceType + countTypeTest++);
			this.setNumAvailableTest(1);
			processTime[0] = 10;
			processTime[1] = 25;
			processTime[2] = 20;
		} else {
			processTime[0] = 0;
			processTime[1] = 0;
			processTime[2] = 0;
		}
		this.typeResource = 0;
		this.grid = grid;
	}

	// @Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames =
	// "wasFirstInQueueXRay", triggerCondition =
	// "$watcher.getNumAvailableXray()>0", whenToTrigger =
	// WatcherTriggerSchedule.IMMEDIATE, pick = 1)
	// public void checkWatched(Patient watchedAgent) {
	// String name = watchedAgent.getId();
	// System.out.println("watched patient is: " + name);
	//
	// }

	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQueueXRay", triggerCondition = "$watcher.getNumAvailableXray()>0", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE, pick = 1)
	public void startXRay() {
		System.out
				.println("                                                                              tick: "
						+ getTime()
						+ " (week: "
						+ getWeek()
						+ " day: "
						+ getDay() + " hour: " + getHour() + ")");
		// watchedAgent.increaseTestCounterXray();
		System.out.println(" XRay has started ");
		double time = getTime();
		Resource rAvailable = findResourceAvailable("xRayRoom ");
		// System.out.println(" Start XRay has began, resource available is: " +
		// rAvailable.getId()+ "num available: " + rAvailable.getNumAvailable()
		// + " who is this? (excecutin the method?)" + this.getId());
		if (rAvailable != null) {
			GridPoint loc = rAvailable.getLoc(grid);
			GridPoint locQueue = this.getQueueLocation("qXRay ", grid);
			int locQX = locQueue.getX();
			int locQY = locQueue.getY();
			int locX = loc.getX();
			int locY = loc.getY();
			Patient fstpatient = null;
			// The head of the queue is at (x,y-1)
			// XXX aqui yo cambio (locX, locY-1) por (11, locY-1) porque
			// hay dos salas de test y una cola.
			Object o = grid.getObjectAt(locQX, locQY + 1);
			System.out
					.println(" the object that is ahead of QXray is "
							+ o
							+ "\n this means that there is a patient to be seen in Xray ");
			if (o != null) {
				if (o instanceof Patient) {
					fstpatient = (Patient) o;
					// Queue queue = fstpatient.getCurrentQueue();
					// Patient checkPatient= queue.removeFromQueue(time);
					// if (checkPatient != fstpatient){
					// System.err.println(" \n ERROR:  xRAY may not be choosing the first of the queue Xray");
					// }
					//
					fstpatient.increaseTestCounterXray();
					// grid.moveTo(this, locX, locY);
					grid.moveTo(fstpatient, locX, locY);
					grid.moveTo(fstpatient.getMyNurse(), locX, locY);

					System.out
							.println(fstpatient.getId()
									+ " hast moved to "
									+ fstpatient.getLoc(grid).toString()
									+ "\n that is the same position that the resource he will be using has: "
									+ rAvailable.getLoc(grid).toString());
					rAvailable.setAvailable(false);

					fstpatient.setMyResource(rAvailable);

					Doctor doctor = fstpatient.getMyDoctor();
					System.out.println(" at start Xray " + fstpatient.getId()
							+ " has in mind the doctor " + doctor.getId());

					Queue queue = ((Queue) grid.getObjectAt(locQueue.getX(),
							locQueue.getY()));
					Patient patientToRemove = queue.removeFromQueue(time);
					queue.elementsInQueue();
					patientToRemove.setIsFirstInQueueXRay(false);
					// queue.getSize();

					rAvailable.setNumAvailableXray(0);

					// scheduleEndTriage(fstpatient);

					scheduleEndXRay(fstpatient, rAvailable);

					Patient newfst = null;
					Object o2 = grid.getObjectAt(locQueue.getX(),
							locQueue.getY());
					if (o2 instanceof Queue) {
						Queue newQueue = (Queue) o2;
						if (newQueue.firstInQueue() != null) {
							newfst = newQueue.firstInQueue();
							newfst.setIsFirstInQueueXRay(true);
							grid.moveTo(newfst, locQueue.getX(),
									(locQueue.getY() + 1));
							// System.out.println(newfst.getId()
							// + " is first in "
							// + newfst.getCurrentQueue().getId()
							// + " tick: " + getTime());
						} else
							System.out.println(" there is not patient in "
									+ newQueue.getId());
					}

				}

			}

			else {

			}

		}

	}

	private void scheduleEndXRay(Patient fstpatient, Resource resource) {

		double min = fstpatient.getMyResource().getProcessTime()[0];
		double mean = fstpatient.getMyResource().getProcessTime()[1];
		double max = fstpatient.getMyResource().getProcessTime()[2];

		double serviceTime = distLognormal(min, mean, max);
		this.xRayTsample.add(serviceTime);
		System.out.println(" Xray times: " + this.xRayTsample);

		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();

		// double timeEndService = schedule.getTickCount()
		// + serviceTime;

		double timeEndService = schedule.getTickCount() + serviceTime;

		// double serviceTime= 16;
		// ISchedule schedule =
		// repast.simphony.engine.environment.RunEnvironment
		// .getInstance().getCurrentSchedule();
		//
		//
		//
		// double timeEndService = schedule.getTickCount() + serviceTime;

		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		EndXRay action2 = new EndXRay(resource, fstpatient);
		fstpatient.setTimeEndCurrentService(timeEndService);
		schedule.schedule(scheduleParams, action2);

		System.out.println("Start Xray " + fstpatient.getId()
				+ " expected to end at " + timeEndService);

	}

	private static class EndXRay implements IAction {
		;
		private Patient patient;
		private Resource resource;

		private EndXRay(Resource resource, Patient patient) {
			this.resource = resource;
			this.patient = patient;

		}

		@Override
		public void execute() {
			resource.endXRay(this.resource, this.patient);

		}

	}

	public void endXRay(Resource resource, Patient patient) {
		// grid.moveTo(nurse, this.initPosX, this.initPosY);
		printTime();
		System.out.println("End Xray " + patient.getId() + " is at "
				+ patient.getLoc(grid).toString());
		resource.setAvailable(true);
		patient.setMyResource(null);
		int totalProcess = patient.getTotalProcesses();
		patient.setTotalProcesses(totalProcess + 1);
		resource.setNumAvailableXray(1);
		patient.getTimeInSystem();
		patient.setIsInSystem(true);
		patient.setWasInXray(true);
		if (patient.getNextProc() == 1) {
			System.out.println(patient.getId() + " is joining q test ");
			patient.addToQ("qTest ");

		} else if (patient.getNextProc() == 2) {
			patient.decideWhereToGo();
		}

		System.out.println(" start Xray about to start");
		startXRay();

	}

	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQueueTest", triggerCondition = "$watcher.getNumAvailableTest()>0", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void startTest() {
		System.out
				.println("                                                                              tick: "
						+ getTime()
						+ " (week: "
						+ getWeek()
						+ " day: "
						+ getDay() + " hour: " + getHour() + ")");
		double time = getTime();
		Resource rAvailable = findResourceAvailable("testRoom ");
		System.out.println(" who is this in Start test " + this.getId());
		if (rAvailable != null) {
			GridPoint loc = rAvailable.getLoc(grid);
			GridPoint locQueue = this.getQueueLocation("qTest ", grid);
			int locQX = locQueue.getX();
			int locQY = locQueue.getY();
			int locX = loc.getX();
			int locY = loc.getY();
			if (this.available) {
				Patient fstpatient = null;
				// The head of the queue is at (x,y-1)
				// aqui yo cambio (locX, locY-1) por (11, locY-1) porque hay dos
				// salas de test y una cola.
				Object o = grid.getObjectAt(locQX, locQY + 1);
				if (o != null) {
					if (o instanceof Patient) {
						fstpatient = (Patient) o;
						fstpatient.increaseTestCounterTest();
						// grid.moveTo(this, locX, locY);
						grid.moveTo(fstpatient, locX, locY);
						grid.moveTo(fstpatient.getMyNurse(), locX, locY);

						fstpatient.setMyResource(rAvailable);

						Doctor doctor = fstpatient.getMyDoctor();
						System.out.println(" at start of test "
								+ fstpatient.getId()
								+ " has in mind the doctor " + doctor.getId());
						System.out
								.println("checking if doctor has this patient in mind in test");
						if (doctor.getMyPatientsInTests().contains(fstpatient)) {
							System.out.println(doctor.getId() + " has "
									+ fstpatient.getId()
									+ " in ' my patients in test' ");
						} else {
							System.err
									.println(" ERROR: Patient is in test and doctor has no memory of him in test. Adding "
											+ fstpatient.getId()
											+ " to "
											+ doctor.getId());
							doctor.getMyPatientsInTests().add(fstpatient);
						}

						Queue queue = ((Queue) grid.getObjectAt(
								locQueue.getX(), locQueue.getY()));
						Patient patientToRemove = queue.removeFromQueue(time);
						queue.elementsInQueue();
						patientToRemove.setIsFirstInQueueTest(false);
						// queue.getSize();

						rAvailable.setAvailable(false);

						rAvailable.setNumAvailableTest(0);

						// scheduleEndTriage(fstpatient);

						scheduleEndTest(fstpatient, rAvailable);

						Patient newfst = null;
						Object o2 = grid.getObjectAt(locQueue.getX(),
								locQueue.getY());
						if (o2 instanceof Queue) {
							Queue newQueue = (Queue) o2;
							if (newQueue.firstInQueue() != null) {
								newfst = newQueue.firstInQueue();
								newfst.setIsFirstInQueueTest(true);
								grid.moveTo(newfst, locQueue.getX(),
										(locQueue.getY() + 1));
								// System.out.println(newfst.getId()
								// + " is first in "
								// + newfst.getCurrentQueue().getId()
								// + " tick: " + getTime());
							}
						}

					}

				}
			}

			else {

			}

		}

	}

	private void scheduleEndTest(Patient fstpatient, Resource resource) {
		double min = fstpatient.getMyResource().getProcessTime()[0];
		double mean = fstpatient.getMyResource().getProcessTime()[1];
		double max = fstpatient.getMyResource().getProcessTime()[2];

		double serviceTime = distLognormal(min, mean, max);

		this.testTsample.add(serviceTime);
		System.out.println(" test times: " + this.testTsample);

		// // double serviceTime = triangularObs(fstpatient.getMyResource()
		// // .getProcessTime()[0], fstpatient.getMyResource()
		// // .getProcessTime()[1], fstpatient.getMyResource()
		// // .getProcessTime()[2]);
		//
		// double serviceTime = 10;

		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();

		// double timeEndService = schedule.getTickCount()
		// + serviceTime;

		double timeEndService = schedule.getTickCount() + serviceTime;

		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		EndTest action2 = new EndTest(resource, fstpatient);
		fstpatient.setTimeEndCurrentService(timeEndService);
		schedule.schedule(scheduleParams, action2);

		System.out.println("Start test " + fstpatient.getId()
				+ " expected to end at " + timeEndService);

	}

	private static class EndTest implements IAction {

		private Patient patient;
		private Resource resource;

		private EndTest(Resource resource, Patient patient) {
			this.resource = resource;
			this.patient = patient;

		}

		@Override
		public void execute() {
			resource.endTest(this.resource, this.patient);

		}

	}

	public void endTest(Resource resource, Patient patient) {
		printTime();
		System.out.println("End test " + patient.getId() + " is at "
				+ patient.getCurrentQueue().getId());
		resource.setAvailable(true);
		patient.setWasInTest(true);
		int totalProcess = patient.getTotalProcesses();
		patient.setTotalProcesses(totalProcess + 1);
		patient.setMyResource(null);
		resource.setNumAvailableTest(1);
		patient.decideWhereToGo();
//		printTime();
//		System.out.println("End test " + patient.getId() + " is at "
//				+ patient.getCurrentQueue().getId());
//		resource.setAvailable(true);
//		int totalProcess = patient.getTotalProcesses();
//		patient.setTotalProcesses(totalProcess + 1);
//		patient.setMyResource(null);
//		resource.setNumAvailableTest(1);
//		patient.moveTo(grid, patient.getMyBedReassessment().getLoc(grid));
//
//		patient.setMyResource(patient.getMyBedReassessment());
//
//		patient.getTimeInSystem();
//		// patient.setIsInSystem(false);
//		patient.setIsInSystem(true);
//		patient.getTimeInSystem();
//		patient.setWasInTest(true);
//		Doctor doctor = patient.getMyDoctor();
//		System.out.println(" at end of test " + patient.getId()
//				+ " has in mind the doctor " + doctor.getId());
//
//		doctor.setMyPatientCalling(patient);
//
//		ArrayList<Patient> pInTest = doctor.getMyPatientsInTests();
//		System.out.println(" method end test, time: " + getTime());
//		doctor.myPatientsInTestRemove(patient);
//		System.out
//				.println(" method end test "
//						+ patient.getId()
//						+ " has finished test, test room is available, patient has not resource attached,  "
//						+ doctor.getId()
//						+ " has removed from his patients in test "
//						+ patient.getId());
//		printElementsArray(doctor.getMyPatientsInTests(),
//				" my patients in test ");
//
//		System.out.println(" method end test" + doctor.getId()
//				+ " is adding to his patients in bed " + patient.getId());
//		doctor.myPatientsInBedAdd(patient);
//
//		printElementsQueue(doctor.getMyPatientsInBedTime(),
//				" my patients in bed (time and triage) when Test ended ");
//
//		System.out
//				.println(" ************************************** AT END TEST "
//						+ doctor.getId() + " patients in bed : "
//						+ doctor.getMyNumPatientsInBed() + " my patient "
//						+ doctor.getMyPatientCalling());
//
//		System.out.println(" &&&&&&&&&&&&&&&&&&&&&&& " + doctor.getId()
//				+ " patients waiting, size:  "
//				+ doctor.getMyPatientsInBedTriage().size());
//		System.out.println(" patients waiting in bed for " + doctor.getId()
//				+ ": " + doctor.getMyPatientsInBedTriage() + " list size: "
//				+ doctor.getMyPatientsInBedTriage().size());
//
//		// LinkedList<Patient> myPatientsBackInBed =
//		// doctor.getMyPatientsBackInBed();
//		// myPatientsBackInBed.add(patient);
//		// doctor.setMyPatientsBackInBed(myPatientsBackInBed);
//		doctor.getMyPatientsBackInBed().add(patient);
//		System.out.println(doctor.getId() + " has added " + patient.getId()
//				+ " to his patients BACK IN BED ");
//		System.out.println(" at the end of test " + patient.getId()
//				+ " is changing 'setBackInBed' to true. Time: " + getTime());
//		patient.setBackInBed(true);
		System.out.println(" new test is about to start ");
		startTest();

	}

	public Boolean getAvailable() {
		return this.available;
	}

	public static void initSaticVars() {
		setCountTypeMajor(1);
		setCountTypeMinor(1);
		setCountTypeResus(1);
		setCountTypeTest(1);
		setCountTypeTriage(1);
		setCountTypeTrolley(1);
		setCountTypeXRay(1);
		setCount(0);

	}

	public void setAvailable(Boolean available) {
		this.available = available;
		if (available){
			if(this.getResourceType().equals("minor cubicle ")||this.getResourceType().equals("major cubicle "))
				this.freeCount+=1;
		}
		System.out.println(this.getId() + " has been set available? " + available + " time " + getTime());
		if (RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount()>1){
			this.checkWhoInResource();
		}

	}

	
	public General checkWhoInResource(){
		General general= null;
		System.out.println("the objects at  " + this.getId() + " : ");
		grid= getGrid();
	int x= grid.getLocation(this).getX();
	int y= grid.getLocation(this).getY();
		for (Object o : grid.getObjectsAt(x,y)) {
			if (o instanceof General){
				 general = (General) o;
				System.out.print("\t" +general.getId() + ", ");
			}
		}
		if (this.getWhoBlockedMe()!=null)
		System.out.println(this.getWhoBlockedMe().getId() + " has  " + this.getId() + " blocked ");
		System.out.println("\n");
		return general;
	}
	public  String getResourceType() {
		return this.resourceType;
	}

	public  int getTypeResource() {
		return typeResource;
	}

	public void setTypeResource(int typeResource) {
		this.typeResource = typeResource;
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		Resource.count = count;
	}

	public int getNumAvailableTest() {
		return numAvailableTest;
	}

	public void setNumAvailableTest(int numAvailableTest) {
		this.numAvailableTest = numAvailableTest;
	}

	public int getNumAvailableXray() {
		return numAvailableXray;
	}

	public void setNumAvailableXray(int numAvailableXray) {
		this.numAvailableXray = numAvailableXray;
	}

	public static void setCountTypeXRay(int countTypeXRay) {
		Resource.countTypeXRay = countTypeXRay;
	}

	public static void setCountTypeTest(int countTypeTest) {
		Resource.countTypeTest = countTypeTest;
	}

	public static void setCountTypeTriage(int countTypeTriage) {
		Resource.countTypeTriage = countTypeTriage;
	}

	public static void setCountTypeTrolley(int countTypeTrolley) {
		Resource.countTypeTrolley = countTypeTrolley;
	}

	public static void setCountTypeMinor(int countTypeMinor) {
		Resource.countTypeMinor = countTypeMinor;
	}

	public static void setCountTypeMajor(int countTypeMajor) {
		Resource.countTypeMajor = countTypeMajor;
	}

	public static void setCountTypeResus(int countTypeResus) {
		Resource.countTypeResus = countTypeResus;
	}

	public int getFreeCount() {
		return freeCount;
	}

	public void setFreeCount(int freeCount) {
		this.freeCount = freeCount;
	}

	public General getWhoBlockedMe() {
		return whoBlockedMe;
	}

	public void setWhoBlockedMe(General whoBlockedMe) {
		this.whoBlockedMe = whoBlockedMe;
	}

}
