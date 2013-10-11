package AESim;


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

public class Nurse extends General {

	private static int countNurse = 1;
	private Boolean available;
	private Grid<Object> grid;
	private int initPosX;
	private int initPosY;
	private int numAvailable;
	private Resource myResource;
	private int idNum;
	private boolean inShift;
	private float[][] myShiftMatrix;
	// TODO los tiempos reales del triage som

	// TODO private double[] nurseTriageParams = { 4, 6, 8 };
	// TODO la enfermera debe tener el tiempo de 4,6,8, pero debe acompañar al
	// medico. pensar en eso
	private double[] nurseTriageParams = { 5, 10, 12 };
	private Object isCheckinPatients;
	private float[] durationOfShift = new float[7];
	private double nextEndingTime;

	public Nurse(Grid<Object> grid, int x1, int y1, int idNum) {
		this.grid = grid;
		this.setAvailable(true);
		this.setId("nurse " + idNum);
		this.idNum=idNum;
		this.numAvailable = 0;
		this.initPosX = x1;
		this.initPosY = y1;
		this.myResource = null;
		this.settTriage(nurseTriageParams);
		this.isCheckinPatients = false;
	}
	@ScheduledMethod(start = 0, priority = 89, shuffle = false, pick = 1)
	public void initNumNurses() {
		printTime();
		System.out.println("When simulation starts, the nurse conditions are "
				+ this.getId());
		GridPoint currentLoc = grid.getLocation(this);
		int currentX = currentLoc.getX();
		int currentY = currentLoc.getY();

		if (currentX == 18) {
			this.setAvailable(false);
			this.setInShift(false);
			System.out.println(this.getId()
					+ " is not in shift and is not available, time: "
					+ getTime());

		} else if (currentY == 2) {
			this.setAvailable(true);
			this.setInShift(true);
			System.out.println(this.getId()
					+ " is in shift and is available, time: " + getTime());
		}

		
		int id = this.idNum;
		float sum = 0;
		switch (id) {

		case 1:
			this.setMyShiftMatrix(getMatrixNurse1());
			// this doctor is a consultant, minimum experience is 8 years
			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixNurse1()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			break;

		case 2:
			this.setMyShiftMatrix(getMatrixNurse2());
			// this doctor is a consultant, minimum experience is 8 years
			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixNurse2()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			break;
		case 3:
			this.setMyShiftMatrix(getMatrixNurse3());
			// this doctor is a consultant, minimum experience is 8 years
			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixNurse3()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			break;
		case 4:
			this.setMyShiftMatrix(getMatrixNurse4());
			// this doctor is a consultant, minimum experience is 8 years
			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixNurse4()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			break;
		case 5:
			this.setMyShiftMatrix(getMatrixNurse5());
			// this doctor is a consultant, minimum experience is 8 years
			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixNurse5()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			break;
		default:
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

	@ScheduledMethod(start = 0, interval = 60, priority = 61, shuffle = false, pick = 1)
	public void scheduleWork() {
		int hour = getHour();
		int day = getDay();
		System.out.println("\n \n " +this.getId() + " is doing method : SCHEDULE WORK ");
		int requiredAtWork = (int) this.getMyShiftMatrix()[hour][day];
//		if (this.doctorType=="Consultant "){ requiredAtWork =1}; 
		// doctor is not in shift
		if (requiredAtWork == 0) {
			printTime();
			if (this.getAvailable()) {
				System.out
						.println(this.getId()
								+ " has finished his shift and is moving to not working area");
				this.moveOut();

				
			} else {
				// if this is not available
				if (getTime() < this.nextEndingTime) {
					printTime();
					System.out
							.println(this.getId()
									+ " has finished his shift but needs to wait to leave because he still has work to do");
					double timeEnding = this.nextEndingTime;
					this.scheduleEndShift(timeEnding+5);
				}
			}
		}
	
	
	else if (requiredAtWork == 1) {
			// TODO here if (this.isInShift() == false) didn't have {}, i put
			// them but needs to be checked
			if (this.isInShift() == false) {
				System.out.println(this.getId() + " will move to doctors area"
						+ " method: schedule work"
						+ " this method is being called by " + this.getId());
				grid.moveTo(this, this.initPosX, this.initPosY);
				this.setInShift(true);
				this.setAvailable(true);
				System.out.println(this.getId() + " is in shift and is available at "
						+ getTime());
			}
		}

	}
	
	
	
	
	public void scheduleEndShift(double timeEnding) {
		System.out.println(" current time: " + getTime()  + " " +this.getId() + " is supposed to move out at: " + timeEnding);
		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();
		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEnding);
		Endshift actionEnd = new Endshift(this);

		schedule.schedule(scheduleParams, actionEnd);
	}

	private static class Endshift implements IAction {
		private Nurse nurse;

		public Endshift(Nurse nurse) {
			this.nurse = nurse;
		}

		@Override
		public void execute() {
			nurse.endShift();

		}

	}

	public void endShift() {
		printTime();
		System.out.println(this.getId()
				+ " has finished the shift and will move out at " +getTime());

		this.moveOut();

	}

	
	
	
	
	
	
	
	
	
	
	
	private void moveOut() {
		// TODO Auto-generated method stub

		this.setInShift(false);
		this.setAvailable(false);
		int i = this.idNum;
		int x = 18;
		int y = i + 4;
		
		
		grid.moveTo(this, x, y);
		System.out.println(this.getId()
				+ "  has finished his shift and has moved out to "
				+ this.getLoc(grid).toString());
		
	}

	private static class EndTriage implements IAction {
		private Nurse nurse;
		private Patient patient;

		private EndTriage(Nurse nurse, Patient patient) {
			this.nurse = nurse;
			this.patient = patient;

		}

		@Override
		public void execute() {
			nurse.endTriage(this.nurse, this.patient);

		}

	}

	public void endTriage(Nurse nurse, Patient patient) {
	printTime();
	System.out.println("end triage " + patient.getId());


		

		grid.moveTo(nurse, this.initPosX, this.initPosY);
		System.out.println(this.getId() + " has finished triage and has move back to nurses area" );
		// grid.moveTo(patient,locQueue.getX(), locQueue.getY() );
		
		
		this.getMyResource().setAvailable(true);
		this.setMyResource(null);
		patient.setMyResource(null);
		this.setNumAvailable(1);
		System.out.println(patient.getId() + " will get triage category");
		nurse.triage(patient);
		int totalProcess = patient.getTotalProcesses();
		patient.setTotalProcesses(totalProcess+1);
		this.setAvailable(true);
		startTriage();

	}

	// @ScheduledMethod(start=15, interval=15, priority=2)
	public void scheduleEndTriage(Patient fstpatient) {
		double serviceTime = distLognormal(nurseTriageParams[0],
				nurseTriageParams[1], nurseTriageParams[2]);
		// System.out.println("triangular OBS nurse: "+serviceTime);
		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();

		// double timeEndService = schedule.getTickCount()
		// + serviceTime;

		double timeEndService = schedule.getTickCount() + serviceTime;
		this.nextEndingTime= timeEndService;
		System.out.println(" triage " + fstpatient.getId()
				+ " expected to end at " + timeEndService);

		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeEndService);
		EndTriage action2 = new EndTriage(this, fstpatient);
		fstpatient.setTimeEndCurrentService(timeEndService);
		schedule.schedule(scheduleParams, action2);

		
	}

	public void triage(Patient patient) {
		Uniform unif = RandomHelper.createUniform();
		double rnd = unif.nextDouble();
		float[][] probsTriage = getMatrixTriagePropByArrival();
		//only patients by walk in are triaged by nurse. Ambulance patients are triaged ny ambulanceIn
		double rndDNW = RandomHelper.createUniform().nextDouble();
		float [][] matrixDNW = getArrayDNW();
		if (rndDNW <= matrixDNW[getHour()][0]){
			this.removePatientFromDepartment(patient);
		} else {
			if (rnd <= probsTriage[0][0]) {
				patient.setTriage("Blue ");
				patient.setTriageNum(1);
				patient.addToQ("qBlue ");
			} else if (probsTriage[0][0] < rnd && rnd <= probsTriage[1][0]) {
				patient.setTriage("Green ");
				patient.setTriageNum(2);
				patient.addToQ("qGreen ");
			} else if (probsTriage[1][0] < rnd && rnd <= probsTriage[2][0]) {
				patient.setTriage("Yellow ");
				patient.setTriageNum(3);
				patient.addToQ("qYellow ");
			} else if (probsTriage[2][0] < rnd && rnd <= probsTriage[3][0]) {
				patient.setTriage("Orange ");
				patient.setTriageNum(4);
				patient.addToQ("qOrange ");
			} else if (probsTriage[3][0] < rnd && rnd <= probsTriage[4][0]) {
				patient.setTriage("Red ");
				patient.setTriageNum(5);
				patient.addToQ("qRed ");
			}
			System.out.println(patient.getId() + " triage num= " + patient.getTriageNum() + " has moved to " + patient.getCurrentQueue().getId());
		}
		
	}
	
	public void removePatientFromDepartment(Patient patient){

		printTime();
		System.out.println(patient.getId() + " didn't wait and has left  the department after triage with "+ this.getId());
		System.out.println(patient.getId() + " goes to qTrolley");
		patient.addToQ("qTrolley ");
		patient.getTimeInSystem();
		patient.setIsInSystem(false);
		patient.getTimeInSystem();
		
	}

	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQueueTriage", triggerCondition = "$watcher.getNumAvailable()>0", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE, pick = 1)
	public void startTriageWatch(Patient watchedPatient) {
		System.out.println(this.getId()+ " is available? "  + this.getAvailable() + " num available "+ this.getNumAvailable()+ " entered start triage by watcher with: " + watchedPatient.getId() + " time: " + getTime());
		this.startTriage();
	}		
		
	public void startTriage() {
		printTime();
		double time = getTime();
		Resource rAvailable = findResourceAvailable("triage cubicle ");

		if (rAvailable != null) {
			GridPoint loc = rAvailable.getLoc(grid);
			int locX = loc.getX();
			int locY = loc.getY();
			if (this.available) {
				// System.out.println(" this is: " + this.getId());
				Patient fstpatient = null;
				// The head of the queue is at (x,y-1)
				Object o = grid.getObjectAt(2, 2);
				if (o != null) {
					if (o instanceof Patient) {
						fstpatient = (Patient) o;
						grid.moveTo(this, locX, locY);
						grid.moveTo(fstpatient, locX, locY);
						this.setMyResource(rAvailable);
						fstpatient.setMyResource(rAvailable);
						GridPoint locQueue = this.getQueueLocation(
								"queueTriage ", grid);
						Queue queue = ((Queue) grid.getObjectAt(
								locQueue.getX(), locQueue.getY()));
						Patient patientToRemove = queue.removeFromQueue(time);
						queue.elementsInQueue();
						patientToRemove.setIsFirstInQueueTriage(false);
						// queue.getSize();

						this.setAvailable(false);
						rAvailable.setAvailable(false);
						this.setNumAvailable(0);
						System.out.println("Start triage " + fstpatient.getId() + " with " + this.getId());

						scheduleEndTriage(fstpatient);

						Patient newfst = null;
						Object o2 = grid.getObjectAt(locQueue.getX(),
								locQueue.getY());
						if (o2 instanceof Queue) {
							Queue newQueue = (Queue) o2;
							if (newQueue.firstInQueue() != null) {
								newfst = newQueue.firstInQueue();
								newfst.setIsFirstInQueueTriage(true);
								grid.moveTo(newfst, locQueue.getX(),
										(locQueue.getY() + 1));
								// System.out.println(newfst.getId()
								// + " is first in "
								// + newfst.getCurrentQueue().getId());
							}
						}

					}

				}
			}

			else {
				// System.out.println("estoy en el watch y hay cola");
			}

		}

	}

	private void setAvailable(boolean b) {
		this.available = b;
		if (available){
			this.numAvailable=1;
		}
		else{
			this.numAvailable=0;
		}

	}

	public static void initSaticVars() {
		setCount(0);

	}

	public void setNumAvailable(int numAvailable) {
		this.numAvailable = numAvailable;
	}

	public final Boolean getAvailable() {
		return available;
	}

	public int getNumAvailable() {
		return numAvailable;
	}

	public final Resource getMyResource() {
		return myResource;
	}

	public final void setMyResource(Resource myResource) {
		this.myResource = myResource;
	}

	public final int getInitPosX() {
		return initPosX;
	}

	public final int getInitPosY() {
		return initPosY;
	}

//	public final void setAvailable(Boolean available) {
//		this.available = available;
//		
//		
//	}

	public static int getCount() {
		return countNurse;
	}

	public static void setCount(int count) {
		Nurse.countNurse = count;
	}
	public boolean isInShift() {
		return inShift;
	}
	public void setInShift(boolean inShift) {
		this.inShift = inShift;
	}
	public float[][] getMyShiftMatrix() {
		return myShiftMatrix;
	}
	public void setMyShiftMatrix(float[][] myShiftMatrix) {
		this.myShiftMatrix = myShiftMatrix;
	}

}
