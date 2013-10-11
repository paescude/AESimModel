package AESim;

import java.util.Iterator;
import java.util.LinkedList;

import repast.simphony.space.grid.Grid;

public class Queue extends General {
	private Grid<Object> grid;
	private int maxInQueue;
	private double maxWaitTime;

	// number in queue
	private double meanInQueue;

	// Simulation variables
	// time in queue
	private double meanWaitTime;
	String name;
	// agents in queue
	private Patient patientInQueue;

	private Patient patientOutQueue;
	// private GridPoint loc;
	private LinkedList<Patient> queue;

	private int totalInQueue;
	private double totalWaitTime;

	public Queue(String queueName, Grid<Object> grid) {
		queue = new LinkedList<Patient>();
		this.setName(queueName);
		this.setId(queueName);
		this.grid = grid;
		maxWaitTime = 0;
		maxInQueue = 0;
		meanInQueue = 0;
		meanWaitTime = 0;
		totalWaitTime = 0;
		totalInQueue = 0;
		setLoc(grid.getLocation(this));
	}

	public Boolean addPatientToQueue(Patient patient) {
		boolean b = queue.add(patient);
		patientInQueue = patient;

		totalInQueue = this.queue.size();

		return b;
	}

	public Patient removeFromQueue(double time) {
		patientOutQueue = queue.poll();
		// patientOutQueue.setCurrentQueue(null);
		String qName = patientOutQueue.getCurrentQueue().getName();
		double qTime = getTime() - patientOutQueue.getTimeEnteredCurrentQ();
		if (qName.equals("queueR ")) {
			patientOutQueue.setQueuingTimeQr(qTime);
			System.out.println("****------******---- "
					+ patientOutQueue.getId() + " time in " + qName + ": "
					+ patientOutQueue.getQueuingTimeQr());
		}
		if (qName.equals("queueTriage ")) {
			patientOutQueue.setQueuingTimeQt(qTime);
			System.out.println("****------******---- "
					+ patientOutQueue.getId() + " time in " + qName + ": "
					+ patientOutQueue.getQueuingTimeQt());
		}
		if (qName.equals("queueInitA ")) {
			patientOutQueue.setQueuingTimeQa(qTime);
			System.out.println("****------******---- "
					+ patientOutQueue.getId() + " time in " + qName + ": "
					+ patientOutQueue.getQueuingTimeQa());
		}
		if (qName.equals("qTest ")) {

		}
		totalInQueue = this.queue.size();
		return patientOutQueue;
	}

	public void elementsInQueue() {

		Patient patientQueuing = null;
		Iterator<Patient> iter = this.iterator();

		String a = "[";
		while (iter.hasNext()) {
			Patient elementInQueue = iter.next();

			if (elementInQueue instanceof Patient) {
				patientQueuing = elementInQueue;
				a = a + patientQueuing.getId() + ", ";
			}

		}
		if (a.length() > 2)
			System.out.println("" + this.getId() + ": "
					+ a.substring(0, a.length() - 2) + "]");
		// Object [] array = this.queue.toArray();
		//
		// for (int j = 0; j < array.length; j++) {
		// Patient o= (Patient)array[j];
		//
		// System.out.println(o.getId());
		// }
	}

	public Patient firstInQueue() {
		return queue.peek();
	}

	public int getMaxInQueue() {
		return maxInQueue;
	}

	public double getMaxWaitTime() {
		return maxWaitTime;
	}

	public double getMeanInQueue() {
		return meanInQueue;
	}

	public double getMeanWaitTime() {
		return meanWaitTime;
	}

	public String getName() {
		return name;
	}

	public int getSize() {

		return totalInQueue;
	}

	public int getTotalInQueue() {
		return totalInQueue;
	}

	public double getTotalWaitTime() {
		return totalWaitTime;
	}

	public void setMaxInQueue(int maxInQueue) {
		this.maxInQueue = maxInQueue;
	}

	public void setMaxWaitTime(double maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	public void setMeanInQueue(double meanInQueue) {
		this.meanInQueue = meanInQueue;
	}

	public void setMeanWaitTime(double meanWaitTime) {
		this.meanWaitTime = meanWaitTime;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTotalInQueue(int totalInQueue) {
		this.totalInQueue = totalInQueue;
	}

	public void setTotalWaitTime(double totalWaitTime) {
		this.totalWaitTime = totalWaitTime;
	}

	public Iterator<Patient> iterator() {
		return queue.iterator();

	}

	// public GridPoint getLoc() {
	// return loc;
	// }
	//
	//
	// public void setLoc(GridPoint loc) {
	// this.loc = loc;
	// }

}
