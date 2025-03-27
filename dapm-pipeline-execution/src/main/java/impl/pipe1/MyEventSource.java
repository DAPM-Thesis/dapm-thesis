package impl.pipe1;

import communication.message.impl.event.Event;
import pipeline.processingelement.Source;

import java.util.HashSet;
import java.util.Random;

public class MyEventSource extends Source<Event> {

    private final String[] activities = {"activity1", "activity2", "activity3"};
    private final Random rand = new Random();

    @Override
    public Event process() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new Event(
                "CaseID" + rand.nextInt(0, 5),
                activities[rand.nextInt(activities.length)],
                "RandomTimeStamp",
                new HashSet<>()
        );
    }
}
