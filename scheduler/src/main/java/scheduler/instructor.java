package scheduler;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class instructor {
    private String name;

    private Map<String, LocalTime> availabilityStart; // Start time for each day
    private Map<String, LocalTime> availabilityEnd;

    private Map<String, List<Integer>> availability; // each day mapped to list of slot indicies,
                                                     // for example Monday from 08:00 to 12:30 would be
                                                     // Monday, (0,1,2)

    private boolean[][] isScheduled;

    public instructor(String name) {
        this.name = name;
        this.availability = new HashMap<>();
        availabilityStart = new HashMap<>();
        availabilityEnd = new HashMap<>();

        initializeAvailability();
        isScheduled = new boolean[5][6];
    }

    private void initializeAvailability() {
        availability.put("monday", List.of());
        availability.put("tuesday", List.of());
        availability.put("wednesday", List.of());
        availability.put("thursday", List.of());
        availability.put("friday", List.of());

        availabilityStart.put("monday", null);
        availabilityStart.put("tuesday", null);
        availabilityStart.put("wednesday", null);
        availabilityStart.put("thursday", null);
        availabilityStart.put("friday", null);

        availabilityEnd.put("monday", null);
        availabilityEnd.put("tuesday", null);
        availabilityEnd.put("wednesday", null);
        availabilityEnd.put("thursday", null);
        availabilityEnd.put("friday", null);

    }

    public Map<String, List<Integer>> getAvailablityMap() {
        return availability;
    }

    public Map<String, LocalTime> getAvailabilityStart() {
        return availabilityStart;
    }

    public Map<String, LocalTime> getAvailabilityEnd() {
        return availabilityEnd;
    }

    public void setScheduleInSlot(int DayIndex, int TimeSlotIndex) {
        isScheduled[DayIndex][TimeSlotIndex] = true;
    }

    public void setAvailability(String day, List<Integer> slots) {
        day = day.toLowerCase();
        if (availability.containsKey(day)) {
            availability.put(day, slots);
        }
    }

    public void setAvailability(String day, LocalTime startTime, LocalTime endTime) {
        day = day.toLowerCase();
        if (availabilityStart.containsKey(day) && availabilityEnd.containsKey(day)) {
            availabilityStart.put(day, startTime);
            availabilityEnd.put(day, endTime);
        }
    }

    public String getName() {
        return name;
    }
}