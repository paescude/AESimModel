package AESim;

import java.io.IOException;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class AEContextBuilder implements ContextBuilder<Object> {


	private static int currentRun;

	@Override
	public Context build(Context<Object> context) {
		
//		XXX
		

		System.out.println("context");
		context.setId("AESim");
		currentRun++;
		
		// currentRun = RunEnvironment.getInstance().getParameters().
		System.out.println("current run is: " + currentRun);
		// context.removeAll(context);
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);

		//
		// ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
		// "space", context, new RandomCartesianAdder<Object>(),
		// new repast.simphony.space.continuous.WrapAroundBorders(), 50,
		// 50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 20, 20));

		Parameters params = RunEnvironment.getInstance().getParameters();
		General.initSaticVar();
		Clerk.initSaticVars();
		Doctor.initSaticVars();
		Nurse.initSaticVars();
		Resource.initSaticVars();
		Patient.initSaticVars();
		
		int triageCubicles = 5;
		int trolleys = 10;
//		int minorCubicles = 5; // this is the real numb =5
		int minorCubicles =5;
		int majorCubicles = 5;
		int resusCubicles = 3;
		int xRayRoom = 3;
		int testRoom = 5;
		int sHO = 9;
		int consultant = 1;
		int NurseMultitask = 4;
		int patientInitial = 2;
		int nurseInitial = 5 ;
		int clerkInitial = 2;
		// int clerkInitial = (Integer) params.getValue("clerk_count");
		int multiTSho=4;
		int multiTConsultant=6;
		
		for (int i = 1; i <= clerkInitial; i++) {
			Clerk clerk = new Clerk(grid, i, i, 4);
			context.add(clerk);
			grid.moveTo(clerk, 17, 4+i);
			System.out.println(" xxc");

		}

		for (int i = 1; i <= triageCubicles; i++) {
			Resource cubicleTriage = new Resource("triage cubicle ",
					"triage cublicle " + i, grid);
			context.add(cubicleTriage);
			grid.moveTo(cubicleTriage, i + 1, 3);
		}

		for (int i = 1; i <= trolleys; i++) {
			Resource trolley = new Resource("trolley ", "trolley " + i, grid);
			context.add(trolley);
			grid.moveTo(trolley, 16, i + 1);
		}

		for (int i = 1; i <= minorCubicles; i++) {
			Resource minorCubicle = new Resource("minor cubicle ",
					"minor cublicle " + i, grid);
			context.add(minorCubicle);
			grid.moveTo(minorCubicle, 5, 9 + i);
			minorCubicle.setTypeResource(1);
		}

		for (int i = 1; i <= majorCubicles; i++) {
			Resource majorCubicle = new Resource("major cubicle ",
					"major cublicle " + i, grid);
			context.add(majorCubicle);
			grid.moveTo(majorCubicle, 3, 9 + i);
			majorCubicle.setTypeResource(1);

		}

		for (int i = 1; i <= resusCubicles; i++) {
			Resource resusCubicle = new Resource("resus cubicle ",
					"resus cublicle " + i, grid);
			context.add(resusCubicle);
			grid.moveTo(resusCubicle, 1, i + 9);
			resusCubicle.setTypeResource(1);
		}

		for (int i = 1; i <= xRayRoom; i++) {
			Resource xRayRooms = new Resource("xRayRoom ", "xRayRoom " + i,
					grid);
			context.add(xRayRooms);
			grid.moveTo(xRayRooms, i + 10, 15);
		}

		for (int i = 1; i <= testRoom; i++) {
			Resource testRooms = new Resource("testRoom ", "testRoom " + i,
					grid);
			context.add(testRooms);
			grid.moveTo(testRooms, i + 10, 11);
		}
		// Doctors
	
		for (int i = 1; i <= sHO; i++) {
					
			
			Doctor doctor = new Doctor(grid, 6 + i, 4, "SHO ", i,multiTSho);
			context.add(doctor);
			grid.moveTo(doctor, 19, 4 + i);
		}

		for (int i = 0; i <= consultant - 1; i++) {
			
			Doctor doctor = new Doctor(grid, 6 + i, 0, "Consultant ", i,multiTConsultant);
			context.add(doctor);
			grid.moveTo(doctor, i + 6, 0);
		
		}
		Exit exit;
		try {
			exit = new Exit(grid, currentRun);
			context.add(exit);
			grid.moveTo(exit, 14, 5);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		// int nurseInitial = (Integer) params.getValue("nurse_count");
		for (int i = 1; i <= nurseInitial; i++) {
			Nurse nurse = new Nurse(grid, i + 7, 2, i, NurseMultitask);
			context.add(nurse);
			grid.moveTo(nurse, 18, 4+i);
			// grid.moveTo(nurse, 2, 3);

		}

		WalkInDoor walkIn = new WalkInDoor("Door ", "WalkIn Door", grid);
		context.add(walkIn);
		grid.moveTo(walkIn, 0, 0);

		AmbulanceIn ambulanceIn = new AmbulanceIn("Door ", "Ambulance Door",
				grid);
		context.add(ambulanceIn);
		grid.moveTo(ambulanceIn, 0, 6);

		// int patientInitial = (Integer) params.getValue("patient_count");
		for (int i = 1; i <= patientInitial; i++) {
			Patient patient = new Patient(grid, "Context", 0);
			context.add(patient);
			// int x= (int) (50* Math.random());
			// int y= (int) (50* Math.random());
			// grid.moveTo(patient, x,y);
			grid.moveTo(patient, 1, 0);

		}

		General general = new General();
		context.add(general);
		Queue queueBReassess = new Queue("queueBReassess ", grid);
		context.add(queueBReassess);
		grid.moveTo(queueBReassess, 7, 9);
		Queue queueR = new Queue("queueR ", grid);
		context.add(queueR);
		grid.moveTo(queueR, 1, 1);
		// grid.moveTo(queueR, 15, 0);
		Queue queueTriage = new Queue("queueTriage ", grid);
		context.add(queueTriage);
		grid.moveTo(queueTriage, 2, 1);
		// grid.moveTo(queueTriage, 16, 0);
		Queue qBlue = new Queue("qBlue ", grid);
		context.add(qBlue);
		grid.moveTo(qBlue, 5, 7);
		Queue qGreen = new Queue("qGreen ", grid);
		context.add(qGreen);
		grid.moveTo(qGreen, 4, 7);
		Queue qYellow = new Queue("qYellow ", grid);
		context.add(qYellow);
		grid.moveTo(qYellow, 3, 7);
		Queue qOrange = new Queue("qOrange ", grid);
		context.add(qOrange);
		grid.moveTo(qOrange, 2, 7);
		Queue qRed = new Queue("qRed ", grid);
		context.add(qRed);
		grid.moveTo(qRed, 1, 7);
		Queue qTest = new Queue("qTest ", grid);
		context.add(qTest);
		grid.moveTo(qTest, 11, 9);
		Queue qXRay = new Queue("qXRay ", grid);
		context.add(qXRay);
		grid.moveTo(qXRay, 11, 13);
		Queue qTrolley = new Queue("qTrolley ", grid);
		context.add(qTrolley);
		grid.moveTo(qTrolley, 15, 7);

		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(524160);// End the simulation
														// after
														// 52 weeks
														// RunEnvironment.getInstance().endAt(10080);
			double simTime = RunEnvironment.getInstance().getSparklineLength();

			System.out.println("Simulation will end at: " + simTime);
		}

		
		return context;
	}

}
