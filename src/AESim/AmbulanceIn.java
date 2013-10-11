package AESim;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import cern.jet.random.Exponential;
import cern.jet.random.Uniform;

/**
 * @author pescuder
 * 
 */
public class AmbulanceIn extends General {

	private float lambdaMax;
	private float[][] lambdaHoursArray;
	private float lambdaHour;
	private int numberAmbulanceIn;
	private String typeResouce;
	private String rName;

	public AmbulanceIn(String typeResouce, String rName, Grid<Object> grid) {
		this.setId("Ambulance Entrance Door ");
		this.setTypeResouce(typeResouce);
		this.setrName(rName);

	}

	@ScheduledMethod(start = 0, priority = 97)
	public void scheduleNextPatient() {
		// System.out.println(getMatrixArrivalAmbulance());
		lambdaMax = findMax(getMatrixArrivalAmbulance());
		// System.out.println("The maximum number is: "+lambdaMax);
		Exponential iATDist = RandomHelper.createExponential(lambdaMax / 60);
		double iAT = iATDist.nextDouble();

		ISchedule schedule = repast.simphony.engine.environment.RunEnvironment
				.getInstance().getCurrentSchedule();

		double timeNextArrival = schedule.getTickCount() + iAT;

		ScheduleParameters scheduleParams = ScheduleParameters
				.createOneTime(timeNextArrival);
		NextArrival action2 = new NextArrival(this);

		schedule.schedule(scheduleParams, action2);
		// System.out.println(" next ambulance arrival scheduled for: " +
		// timeNextArrival);
	}

	@ScheduledMethod(start = 0, interval = 60, priority = 65)
	public void reInitNumberIn() {
		setNumberAmbulanceIn(0);
	}

	public void arriveByAmbulance() {
		lambdaHoursArray = getMatrixArrivalAmbulance();
		int i = getHour();
		int j = getDay();
		lambdaHour = lambdaHoursArray[i][j];

		// XXX
		System.out.println("number of patients in time " + getTime()
				+ " (hour: " + getHour() + ", day:" + getDay() + ")" + " is: "
				+ lambdaHour);

		// Thinning Algorithm

		double U1 = RandomHelper.nextDoubleFromTo(0, 1);
		if (U1 <= lambdaHour / lambdaMax) {
			Grid<Object> grid = getGrid();
			System.out
					.println("                                                                              tick: "
							+ getTime()
							+ " (week: "
							+ getWeek()
							+ " day: "
							+ getDay() + " hour: " + getHour() + ")");
			Context<Object> context = getContext();
			Patient patient = new Patient(grid, "Ambulance", getTime());
			context.add(patient);
			System.out.println(patient.getId() + " arrived by ambulance");
			grid.moveTo(patient, 1, 0);
			this.triageAmbulance(patient);
			setNumberAmbulanceIn(getNumberAmbulanceIn() + 1);
		}
		scheduleNextPatient();
	}

	public void triageAmbulance(Patient patient) {
		Uniform unif = RandomHelper.createUniform();
		double rnd = unif.nextDouble();
		float[][] probsTriage = getMatrixTriagePropByArrival();
		if (rnd <= probsTriage[0][1]) {
			patient.setTriage("Blue ");
			patient.setTriageNum(1);
			patient.addToQ("qBlue ");
		} else if (probsTriage[0][1] < rnd && rnd <= probsTriage[1][1]) {
			patient.setTriage("Green ");
			patient.setTriageNum(2);
			patient.addToQ("qGreen ");
		} else if (probsTriage[1][1] < rnd && rnd <= probsTriage[2][1]) {
			patient.setTriage("Yellow ");
			patient.setTriageNum(3);
			patient.addToQ("qYellow ");
		} else if (probsTriage[2][1] < rnd && rnd <= probsTriage[3][1]) {
			patient.setTriage("Orange ");
			patient.setTriageNum(4);
			patient.addToQ("qOrange ");
		} else if (probsTriage[3][1] < rnd && rnd <= probsTriage[4][1]) {
			patient.setTriage("Red ");
			patient.setTriageNum(5);
			patient.addToQ("qRed ");
		}

	}

	public int getNumberAmbulanceIn() {
		return numberAmbulanceIn;
	}

	public void setNumberAmbulanceIn(int numberAmbulanceIn) {
		this.numberAmbulanceIn = numberAmbulanceIn;
	}

	public String getTypeResouce() {
		return typeResouce;
	}

	public void setTypeResouce(String typeResouce) {
		this.typeResouce = typeResouce;
	}

	public String getrName() {
		return rName;
	}

	public void setrName(String rName) {
		this.rName = rName;
	}

	private static class NextArrival implements IAction {
		private AmbulanceIn ambulanceIn;

		private NextArrival(AmbulanceIn ambulanceIn) {
			this.ambulanceIn = ambulanceIn;
		}

		@Override
		public void execute() {
			ambulanceIn.arriveByAmbulance();

		}

	}

}
