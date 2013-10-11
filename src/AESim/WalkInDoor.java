package AESim;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import cern.jet.random.Exponential;

public class WalkInDoor extends General {

	private Grid grid;
	private String id;
	private String rName;
	private String typeResouce;
	private float lambdaMax;
	private float[][] lambdaHoursArray;
	private float lambdaHour;
	private int numberWalkedIn = 0;

	public WalkInDoor(String typeResouce, String rName, Grid<Object> grid) {
		this.id = "WalkIn Door ";
		this.typeResouce = typeResouce;
		this.rName = rName;
		this.grid = grid;
	}

	public void scheduleNextWalkinPatient() {
		lambdaMax = findMax(getMatrixArrivalWalkIn());
		// System.out.println("The maximum number is: " + lambdaMax);

		Exponential IATLambdaMaxDist = RandomHelper
				.createExponential(lambdaMax / 60);
		double IATWalkIn = IATLambdaMaxDist.nextDouble();

		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();

		double timeNextArrival = schedule.getTickCount() + IATWalkIn;

		// System.out.println("new arrival is being scheduled for: "
		// + timeNextArrival);

		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeNextArrival);
		NextArrivalWalkin actionArrive = new NextArrivalWalkin(this);

		schedule.schedule(scheduleParams, actionArrive);
		System.out.println(" next arrival scheduled for: " + timeNextArrival);
	}

	private static class NextArrivalWalkin implements IAction {
		private WalkInDoor walkInDoor;

		public NextArrivalWalkin(WalkInDoor walkInDoor) {
			this.walkInDoor = walkInDoor;
		}

		@Override
		public void execute() {
			walkInDoor.arriveByWalkIn();

		}

	}

//	@Watch(watcheeClassName = "AESim.Patient", watcheeFieldNames = "hasReachedtarget", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE, pick = 1)
//	public void checkWhoReachedTarget(Patient watchedPatient ){
//		String whosName = this.getId();
//		System.out.println(watchedPatient.getId() + " has reached the 4 hours time target. Time in system: " + watchedPatient.getTimeInSystem());
//	}
	
	public void arriveByWalkIn() {
		lambdaHoursArray = getMatrixArrivalWalkIn();
		int i = getHour();
		int j = getDay();

		lambdaHour = lambdaHoursArray[i][j];

		// XXX
		System.out.println("Number of patients in time " + getTime()
				+ " (week: " + getWeek() + " day: " + j + " hour: " + i + ")"
				+ " is: " + lambdaHour);

		/* Thinning Algorithm */
		double U1 = RandomHelper.nextDoubleFromTo(0, 1);
		if (U1 <= lambdaHour / lambdaMax) {
			Grid<Object> grid = getGrid();
			Context<Object> context = getContext();
			System.out
					.println("                                                                              tick: "
							+ getTime()
							+ " (week: "
							+ getWeek()
							+ " day: "
							+ getDay() + " hour: " + getHour() + ")");
			Patient patient = new Patient(grid, "walkIn", getTime());
			context.add(patient);
			grid.moveTo(patient, 1, 0);
			System.out.println(patient.getId() + " arrived by walk-in");
			patient.addToQR();
			setNumberWalkedIn(getNumberWalkedIn() + 1);
			System.out.println("Number walked in: " + getNumberWalkedIn());

		}
		scheduleNextWalkinPatient();
	}

	@ScheduledMethod(start = 0, priority = 90)
	public void initializePatientArrival() {
		Grid<Object> grid = getGrid();
		Context<Object> context = getContext();
		System.out
				.println("                                                                              tick: "
						+ getTime()
						+ " (week: "
						+ getWeek()
						+ " day: "
						+ getDay() + " hour: " + getHour() + ")");
		Patient patient = new Patient(grid, "walkIn", getTime());
		context.add(patient);
		grid.moveTo(patient, 1, 0);
		System.out.println(patient.getId() + " arrived by walk-in");
		patient.addToQR();
		scheduleNextWalkinPatient();
		System.out.println(" week: " + getWeek() + " day: " + getDay()
				+ " hour: " + getHour());

	}

	/*
	 * Reinicia el contador cada hora
	 */
	@ScheduledMethod(start = 0, interval = 60)
	public void reInitNumberIn() {
		setNumberWalkedIn(0);
	}

	// @ScheduledMethod(start=1440)
	// public void arrayToFile() {
	// // String fileName =
	// "C:\\RepastSimphony-2.0\\workspace\\AESim\\src\\AESim\\OutPuts\\arrivalTimes.txt";
	// // DataOutputStream dos = new DataOutputStream(new
	// BufferedOutputStream(new FileOutputStream(fileName)));
	// int size= this.Arrivals.size();
	// System.out.println("+++++++ -------- +++++ ------- ****** + "
	// +this.Arrivals);
	// for(int i=1;i<=size;i++){
	// //dos.writeDouble(this.Arrivals.get(i));
	// }
	//
	// }
	//

	public float getLambdaHour() {
		return lambdaHour;

	}

	public void setLambdaHour(float lambdaHour) {
		this.lambdaHour = lambdaHour;
	}

	public float[][] getLambdaHoursArray() {
		return lambdaHoursArray;
	}

	public void setLambdaHoursArray(float[][] lambdaHoursArray) {
		this.lambdaHoursArray = lambdaHoursArray;
	}

	public int getNumberWalkedIn() {
		return numberWalkedIn;
	}

	public void setNumberWalkedIn(int numberWalkedIn) {
		this.numberWalkedIn = numberWalkedIn;
	}

}
