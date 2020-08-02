package net.fabricmc.example;

public class Timer {
	public static short convert(float perSecond) {

		return (short) (1000 / perSecond);

	}



	public static long getCurrentTime() {

		return System.nanoTime() / 1000000;

	}



	private long previousTime;



	public Timer() {

		previousTime = -1L;

	}



	public long get() {

		return previousTime;

	}



	public boolean check(float milliseconds) {

		return Timer.getCurrentTime() - previousTime >= milliseconds;

	}



	public void reset() {

		previousTime = Timer.getCurrentTime();

	}
	
	
	public void addTime(float milliseconds) {

		previousTime = (long)(previousTime - milliseconds);

	}
}
