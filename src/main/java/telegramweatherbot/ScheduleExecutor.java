package telegramweatherbot;

public class ScheduleExecutor extends Thread {
    private final long initDelay;
    private final long period;
    private final Runnable task;

    public ScheduleExecutor(Runnable task, String s, long initDelay, long period) {
        super(task, s);
        this.task = task;
        this.initDelay = initDelay;
        this.period = period;
    }

    @Override
    public void run() {
        try {
            sleep(initDelay);
            while (true) {
                task.run();
                sleep(period);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
