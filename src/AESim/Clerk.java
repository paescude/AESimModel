package AESim;


import org.apache.commons.math3.distribution.TriangularDistribution;

import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import cern.jet.random.Exponential;

public class Clerk extends General {

	private static int count;
	private Boolean available;
	// private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int patientWaiting;
	private boolean inShiftClerk;
	private int idNum;
	private float[][] myShiftMatrix;
	private float[] durationOfShift = new float[7];
	private double nextEndingTime;
	private int initPosX;
	private int initPosY;

	private int prueba;
	private double[] registrationTimeParams = { 3, 5, 8 };
	private int numAvailable;

	public Clerk(Grid<Object> grid, int idNum, int x1, int y1) {
		// this.space = space;
		this.setId("Clerk " + idNum);
		this.grid = grid;
		this.available = true;
		this.numAvailable = 1;
		this.settTriage(registrationTimeParams);
		this.inShiftClerk=false;
		this.idNum= idNum;
		this.initPosX = x1;
		this.initPosY = y1;
		this.getLoc(grid);
		
	}
	
	@ScheduledMethod(start = 0, priority = 99, shuffle = false, pick = 1)
	public void initNumClerks() {
		printTime();
		System.out.println("When simulation starts, the clerk conditions are "
				+ this.getId());
		GridPoint currentLoc = grid.getLocation(this);
		int currentX = currentLoc.getX();
		int currentY = currentLoc.getY();

		if (currentX == 17) {
			this.setAvailable(false);
			this.setInShiftClerk(false);
			System.out.println(this.getId()
					+ " is not in shift and is not available, time: "
					+ getTime());

		} else if (currentY == 4) {
			this.setAvailable(true);
			this.setInShiftClerk(true);
			System.out.println(this.getId()
					+ " is in shift and is available, time: " + getTime());
		}

		
		int id = this.idNum;
		float sum = 0;
		switch (id) {

		case 1:
			this.setMyShiftMatrix(getMatrixClerk1());
			// this doctor is a consultant, minimum experience is 8 years
			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixClerk1()[j][i];
				}
				this.durationOfShift[i] = sum;
			}
			break;
		case 2:
			this.setMyShiftMatrix(getMatrixClerk2());
			// this doctor is a consultant, minimum experience is 8 years
			for (int i = 0; i < 7; i++) {
				sum = 0;
				for (int j = 0; j < 23; j++) {
					sum = sum + getMatrixClerk2()[j][i];
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
	
	@ScheduledMethod(start=0, interval=50,priority=98, shuffle=false)
	public void scheduleWorkClerk(){
		int hour = getHour();
		int day = getDay();
		System.out.println("\n \n " +this.getId() + " is doing method : SCHEDULE WORK ");
//		TODO if (this.get)
		int requiredAtWork = (int) this.getMyShiftMatrix()[hour][day];
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
			if (this.isInShiftClerk() == false) {
				System.out.println(this.getId() + " will move to doctors area"
						+ " method: schedule work"
						+ " this method is being called by " + this.getId());
				grid.moveTo(this, this.initPosX, this.initPosY);
				this.setInShiftClerk(true);
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
		private Clerk clerk;

		public Endshift(Clerk clerk) {
			this.clerk = clerk;
		}

		@Override
		public void execute() {
			clerk.endShift();

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

		this.setInShiftClerk(false);
		this.setAvailable(false);
		int i = this.idNum;
		int x = 17;
		int y = i + 4;
		
		
		grid.moveTo(this, x, y);
		System.out.println(this.getId()
				+ "  has finished his shift and has moved out to "
				+ this.getLoc(grid).toString());
		
	}
	
	private static class EndRegistration implements IAction {
		private Clerk clerk;
		private Patient patient;

		private EndRegistration(Clerk clerk, Patient patient) {
			this.clerk = clerk;
			this.patient = patient;

		}

		@Override
		public void execute() {
			clerk.endRegistration(this.patient);

		}

	}
	
	public void endRegistration(Patient patient) {
		System.out
				.println("                                                                              tick: "
						+ getTime()
						+ " (week: "
						+ getWeek()
						+ " day: "
						+ getDay() + " hour: " + getHour() + ")");

		GridPoint locQueue = this.getQueueLocation("queueTriage ", grid);
		Queue queue = (Queue) grid
				.getObjectAt(locQueue.getX(), locQueue.getY());
		// patient.addToQ("queueTriage ");
		grid.moveTo(patient, locQueue.getX(), locQueue.getY());
		int totalProcess = patient.getTotalProcesses();
		patient.setTotalProcesses(totalProcess+1);;
		System.out.println("End Registration " + patient.getId() + " is at "
				+ patient.getCurrentQueue().getId());
		System.out.println(patient.getId() + " is added to Q triage");
		patient.addToQ("queueTriage ");
		
		
		this.setAvailable(true);

		startRegistration();

	}

	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQr", triggerCondition = "$watcher.getNumAvailable()>0", scheduleTriggerPriority=2,whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void startRegistration() {
		System.out
				.println("                                                                              tick: "
						+ getTime()
						+ " (week: "
						+ getWeek()
						+ " day: "
						+ getDay() + " hour: " + getHour() + ")");
		double time = getTime();
		if (this.available) {
			Patient fstpatient = null;
			Object o = grid.getObjectAt(1, 2);
			if (o != null) {
				if (o instanceof Patient) {
					fstpatient = (Patient) o;

					int xClerk= grid.getLocation(this).getX(); 
					int yClerk= grid.getLocation(this).getY(); 
					
					grid.moveTo(fstpatient,xClerk, yClerk);

					GridPoint locQueue = this.getQueueLocation("queueR ", grid);
					Queue queue = ((Queue) grid.getObjectAt(locQueue.getX(),
							locQueue.getY()));
					Patient patientToRemove = queue.removeFromQueue(time);
					queue.elementsInQueue();
					patientToRemove.setIsFirstInQueueR(false);
					// queue.getSize();

					setAvailable(false);
					double serviceTime = distTriangular(
							registrationTimeParams[0],
							registrationTimeParams[1],
							registrationTimeParams[2]);

					ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
							.getInstance().getCurrentSchedule();

					// double timeEndService = schedule.getTickCount()
					// + serviceTime;

					double timeEndService = schedule.getTickCount()
							+ serviceTime;

					ScheduleParameters scheduleParams = ScheduleParameters
							.createOneTime(timeEndService);
					EndRegistration action2 = new EndRegistration(this,
							fstpatient);
fstpatient.setTimeEndCurrentService(timeEndService);
					schedule.schedule(scheduleParams, action2);

					System.out.println("Start registration "
							+ fstpatient.getId() + " expected to end at "
							+ timeEndService);

					Patient newfst = null;
					Object o2 = grid.getObjectAt(locQueue.getX(),
							locQueue.getY());
					if (o2 instanceof Queue) {
						Queue newQueue = (Queue) o2;
						if (newQueue.firstInQueue() != null) {
							newfst = newQueue.firstInQueue();
							newfst.setIsFirstInQueueR(true);
							grid.moveTo(newfst, locQueue.getX(),
									(locQueue.getY() + 1));
							// System.out.println(newfst.getId() +
							// " is first in "
							// + newfst.getCurrentQueue().getId());
						}
					}

				}
			}

		}

		else {

		}

	}

//	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "wasFirstInQr", triggerCondition = "$watcher.getPrueba()<1", scheduleTriggerPriority=2,whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
//	public void startService() {
//		System.out
//				.println("                                                                              tick: "
//						+ getTime()
//						+ " (week: "
//						+ getWeek()
//						+ " day: "
//						+ getDay() + " hour: " + getHour() + ")");
//		double time = getTime();
//		if (this.available) {
//			Patient fstpatient = null;
//			Object o = grid.getObjectAt(1, 2);
//			if (o != null) {
//				if (o instanceof Patient) {
//					fstpatient = (Patient) o;
//
//					grid.moveTo(fstpatient, this.getX(), this.getY());
//
//					GridPoint locQueue = this.getQueueLocation("queueR ", grid);
//					Queue queue = ((Queue) grid.getObjectAt(locQueue.getX(),
//							locQueue.getY()));
//					Patient patientToRemove = queue.removeFromQueue(time);
//					queue.elementsInQueue();
//					patientToRemove.setIsFirstInQueueR(false);
//					// queue.getSize();
//
//					setAvailable(false);
//					this.setPrueba(1);
//					Exponential serviceTimeDist = RandomHelper
//							.createExponential(5);
//					double serviceTime = serviceTimeDist.nextDouble();
////TODO CAMBIAR ESTA DISTRIBUCION POR LA DE LOS DATOS DE EXCEL
//					ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
//							.getInstance().getCurrentSchedule();
//
//					// double timeEndService = schedule.getTickCount()
//					// + serviceTime;
//
//					double timeEndService = schedule.getTickCount() + 5;
//
//					ScheduleParameters scheduleParams = ScheduleParameters
//							.createOneTime(timeEndService);
//					EndRegistration action2 = new EndRegistration(this,
//							fstpatient);
//
//					schedule.schedule(scheduleParams, action2);
//
//					System.out.println("Start registration "
//							+ fstpatient.getId() + " expected to end at "
//							+ timeEndService);
//
//					Patient newfst = null;
//					Object o2 = grid.getObjectAt(locQueue.getX(),
//							locQueue.getY());
//					if (o2 instanceof Queue) {
//						Queue newQueue = (Queue) o2;
//						if (newQueue.firstInQueue() != null) {
//							newfst = newQueue.firstInQueue();
//							newfst.setIsFirstInQueueR(true);
//							grid.moveTo(newfst, locQueue.getX(),
//									(locQueue.getY() + 1));
//							// System.out.println(newfst.getId() +
//							// " is first in "
//							// + newfst.getCurrentQueue().getId());
//						}
//					}
//
//				}
//
//			}
//
//			else {
//
//			}
//
//		}
//	}

	public static void initSaticVars() {
		setCount(0);

	}

	public Boolean getAvailable() {
		return available;
	}

	public int getPrueba() {
		return prueba;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
		if (available){
			this.setNumAvailable(1);
		}
		else{
			this.setNumAvailable(0);
		}
	}

	public void setPrueba(int prueba) {
		this.prueba = prueba;
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		Clerk.count = count;
	}

	public boolean isInShiftClerk() {
		return inShiftClerk;
	}

	public void setInShiftClerk(boolean inShiftClerk) {
		this.inShiftClerk = inShiftClerk;
	}

	public float[][] getMyShiftMatrix() {
		return myShiftMatrix;
	}

	public void setMyShiftMatrix(float[][] myShiftMatrix) {
		this.myShiftMatrix = myShiftMatrix;
	}

	public int getNumAvailable() {
		return numAvailable;
	}

	public void setNumAvailable(int numAvailable) {
		this.numAvailable = numAvailable;
	}
}
