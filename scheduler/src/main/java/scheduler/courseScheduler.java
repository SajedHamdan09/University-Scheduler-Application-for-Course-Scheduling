package scheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class courseScheduler {

    int days = 5;
    int timeslots = 6;

    LinkedList<UUID>[][] schedule = new LinkedList[days][timeslots];
    Map<UUID, course> courseMap;
    Queue<UUID> courseQueue;
    int countScheduledCourses = 0;

    Queue<UUID> AllCoursesQueue = new LinkedList<UUID>();

    String[] daysList = { "monday", "tuesday", "wednesday", "thursday", "friday" };

    private final List<List<Integer>> dayPairs = new ArrayList<>();

    private Set<UUID> alreadyRescheduled = new HashSet<>();

    // for courses with no place found to store them
    Set<UUID> unscheduledCourseHeap = new HashSet<>();
    // for internships and senior projects...
    Set<UUID> zeroSessionCourses = new HashSet<>();
    // for courses with errors while reading from excelz
    Set<UUID> errorCourses = new HashSet<UUID>();

    Set<UUID> coursesToBeRescheduled = new HashSet<>();

    File outputFile;
    Workbook workbook;
    Sheet sheet;

    courseScheduler() {

        courseMap = new HashMap<>();
        courseQueue = new LinkedList<UUID>();

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Course Schedule");

        // Initialize day pairs (M,W) and (T,Th)
        dayPairs.add(List.of(0, 2)); // Monday, Wednesday
        dayPairs.add(List.of(1, 3)); // Tuesday, Thursday
        dayPairs.add(List.of(2, 4)); // Wednesday, Friday

        for (int i = 0; i < days; i++) {
            for (int j = 0; j < timeslots; j++) {
                schedule[i][j] = new LinkedList<UUID>();
            }
        }

    }

    public void enqueueCourse(course course) {
        UUID courseUUID = UUID.randomUUID();
        courseMap.put(courseUUID, course);
        courseQueue.add(courseUUID);
    }

    public void ScheduleCourses(boolean isSummer) {

        String[] courseToSchedule = {
                "ENGL 101",

        };

        List<String> preinitializedCourseList = Arrays.asList(courseToSchedule);

        for (UUID courseUUID : courseQueue) {

            // if
            // (!preinitializedCourseList.contains(courseMap.get(courseUUID).getCourseID().trim()))
            // {
            // continue;
            // }

            if (courseMap.get(courseUUID).getNumberOfSessions() == 0) {
                zeroSessionCourses.add(courseUUID);
                System.out.println("0 session course");
                System.out.println(courseMap.get(courseUUID).getCourseID());
                continue;
            }

            if (courseMap.get(courseUUID).getInstructor() == null
                    || courseMap.get(courseUUID).getInstructor().getName() == null) {
                errorCourses.add(courseUUID);
                courseMap.get(courseUUID).setOtherInfo("Instructor not found in instructors sheet");
                continue;
            }

            if (isSummer) {
                courseMap.get(courseUUID).doubleSessions();
            }

            addCourseHandleLectures(courseUUID);
        }
        displaySchedule();
        moveUnscheduledCourses();

        if (coursesToBeRescheduled.isEmpty()) {
            return;
        }
        rescheduleConflicts();

    }

    public void viewCourses() {
        for (UUID courseUUID : courseQueue) {
            System.out.println(courseMap.get(courseUUID).getCourseID());
            try {
                System.out.println(courseMap.get(courseUUID).getInstructor().getName());
            } catch (Exception e) {
                System.out.println("ERROR~~~~~~~~~~~~~~~~~~");
            }

        }
    }

    public void addCourseHandleLectures(UUID courseUUID) {
        System.out.println();
        System.out.println();

        // logic for sections here
        // if more than 1 section for a course
        // add -1 -2 -3 etc... to course ID

        System.out.println("number of sections " + courseMap.get(courseUUID).getNumberOfSections());
        course mainCourse = courseMap.get(courseUUID);

        if (mainCourse.getNumberOfSections() > 1 && !mainCourse.hasCommonSessions()) {

            boolean didscheduledLectures = true;
            // this section to handle adding a course with -i lecture appended, this happens
            // when attempting to reschedule courses to fit other unscheduled courses
            for (int i = 1; i <= courseMap.get(courseUUID).getNumberOfSections(); i++) {

                // currentSection same exact attributes as course but with courseID += "-"+i
                course currentSection = new course(mainCourse.getCourseID(),
                        mainCourse.getCourseName(),
                        mainCourse.getNumberOfCredits(),
                        mainCourse.getNumberOfSections(),
                        mainCourse.getNumberOfSessions(),
                        mainCourse.getInstructor(),
                        mainCourse.getConflictingCourses(),
                        mainCourse.getCourseType(),
                        mainCourse.getNumberOfSlots(),
                        mainCourse.getDuration(),
                        i, mainCourse.getDurationMinutes());

                UUID currentSectionUUID = UUID.randomUUID();
                courseMap.put(currentSectionUUID, currentSection);
                if (!addCourse(currentSectionUUID)) {
                    didscheduledLectures = false;
                }
            }
            if (didscheduledLectures) {
                countScheduledCourses++;
            }
        } else {
            if (addCourse(courseUUID)) {
                countScheduledCourses++;
            }
        }

    }

    public boolean addCourse(UUID courseUUID) {

        if (!AllCoursesQueue.contains(courseUUID)) {
            AllCoursesQueue.add(courseUUID);
        }

        System.out.println();
        if (courseMap.get(courseUUID).getInstructor() == null) {
            System.out.println(
                    "Error: Course " + courseMap.get(courseUUID).getCourseID() + " does not have a valid instructor.");

            courseMap.get(courseUUID).setOtherInfo("stated instructor not found in Instructor's sheet");

            System.out.println("adding to heap " + courseMap.get(courseUUID).getCourseID());
            unscheduledCourseHeap.add(courseUUID);
            return true;
        }

        for (String day : daysList) {
            List<Integer> InstructorSlots = courseMap.get(courseUUID).getInstructor().getAvailablityMap().get(day);
            if (InstructorSlots.size() > 3) {
                Collections.reverse(InstructorSlots);
            }
            courseMap.get(courseUUID).getInstructor().setAvailability(day, InstructorSlots);
        }

        if (AttemptDayPairSchedule(courseUUID)) {
            System.out.println("day pair Course successfully scheduled");
            return true;
        } else {
            System.out.println("day pair failed");
        }
        if (AttemptEqualSpreadSchedule(courseUUID)) {
            System.out.println("equal spread Course successfully scheduled");
            return true;
        } else {
            System.out.println("equal spread failed");
        }
        if (AttemptAnySchedule(courseUUID)) {
            System.out.println("any schedule Course successfully scheduled");
            return true;
        } else {
            System.out.println("any schedule failed");
        }

        courseMap.get(courseUUID).setOtherInfo("course unscheduled due to no available timeslots found");
        System.out.println("adding to heap " + courseMap.get(courseUUID).getCourseID());
        unscheduleCourseFromAll(courseUUID);
        unscheduledCourseHeap.add(courseUUID);
        return false;
    }

    // ~~~~~~~~~~~ 3 patterns of attempt schedule methods ~~~~~~~~~~~

    private boolean AttemptDayPairSchedule(UUID courseUUID) {
        System.out.println("Attempting day pair schedule for course: " + courseMap.get(courseUUID).getCourseID());
        course courseToBeScheduled = courseMap.get(courseUUID);

        int sessionsPerDay = courseToBeScheduled.getNumberOfSessions() / 2;
        if (courseToBeScheduled.getNumberOfSessions() % 2 != 0) {

            return false;
        }
        System.out.println("session per day = " + sessionsPerDay);
        int dayPairIndex = 0;
        for (List<Integer> dayPair : dayPairs) {
            int firstDayIndex = dayPair.get(0);
            int secondDayIndex = dayPair.get(1);

            List<Integer> firstDayInstructorSlots = courseToBeScheduled.getInstructorSlots(daysList[firstDayIndex]);
            List<Integer> secondDayInstructorSlots = courseToBeScheduled.getInstructorSlots(daysList[secondDayIndex]);

            if (firstDayInstructorSlots == null || firstDayInstructorSlots.isEmpty() ||
                    secondDayInstructorSlots == null || secondDayInstructorSlots.isEmpty()) {
                System.out.println("No available slots for " + courseToBeScheduled.getInstructor().getName() +
                        " on " + daysList[firstDayIndex] + " and " + daysList[secondDayIndex]);
                continue;
            }

            List<Integer> commonSlots = findCommonSlotsInDays(courseUUID, dayPair);

            System.out.println("Checking available slots for " + courseToBeScheduled.getInstructor().getName() +
                    " on " + daysList[firstDayIndex] + " and " + daysList[secondDayIndex]);

            if (attemptScheduleInDays(courseUUID, dayPair, commonSlots, sessionsPerDay)) {
                System.out.println("before returning true");
                System.out.println("Sessions scheduled: " + courseToBeScheduled.getSessionsScheduled());
                System.out.println("Total sessions to be: " + courseToBeScheduled.getNumberOfSessions());
                moveDayPairToLast(dayPairIndex);
                courseToBeScheduled.setPerfectlyScheduled(true);
                return true;
            } else {
                unscheduleCourseFromAll(courseUUID);
                if ((scheduleCourseInDay(courseUUID, firstDayIndex, firstDayInstructorSlots, sessionsPerDay) &&
                        scheduleCourseInDay(courseUUID, secondDayIndex, secondDayInstructorSlots, sessionsPerDay))) {

                    System.out.println("Sessions scheduled: " + courseToBeScheduled.getSessionsScheduled());
                    System.out.println("Total sessions to be: " + courseToBeScheduled.getNumberOfSessions());

                    if (courseToBeScheduled.getNumberOfSessions() == courseToBeScheduled.getSessionsScheduled()) {
                        moveDayPairToLast(dayPairIndex);
                        return true;
                    } else {
                        // If the second day of the pair doesn't have enough slots, we should revert the
                        // scheduling of the first day
                        unscheduleCourseInDay(courseUUID, firstDayIndex);
                        unscheduleCourseInDay(courseUUID, secondDayIndex);
                        courseToBeScheduled.setSessionsScheduled(0);
                    }
                }
            }
            dayPairIndex++;
        }
        unscheduleCourseFromAll(courseUUID);
        return false;
    }

    private boolean AttemptEqualSpreadSchedule(UUID courseUUID) {
        System.out.println("Attempting equal spread schedule for course: " + courseMap.get(courseUUID).getCourseID());

        course courseToBeScheduled = courseMap.get(courseUUID);
        int totalSessions = courseToBeScheduled.getNumberOfSessions();

        // Count the number of days with available slots
        int availableDaysCount = 0;
        List<Integer> AvailableDays = new ArrayList<Integer>();
        for (int dayIndex = 0; dayIndex < 5; dayIndex++) {
            List<Integer> instructorSlots = courseToBeScheduled.getInstructorSlots(daysList[dayIndex]);
            if (instructorSlots != null && !instructorSlots.isEmpty()) {
                availableDaysCount++;
                AvailableDays.add(dayIndex);
            }
        }

        if (availableDaysCount == 0) {
            System.out.println("No available slots for " + courseToBeScheduled.getInstructor().getName());
            return false;
        }

        // Calculate the number of sessions to be scheduled on each day
        int sessionsPerDay = totalSessions / availableDaysCount;
        int extraSessions = totalSessions % availableDaysCount; // Extra sessions to be distributed

        List<Integer> commonSlots = findCommonSlotsInDays(courseUUID, AvailableDays);

        // First, schedule the non-extra sessions equally on all available days
        if (attemptScheduleInDays(courseUUID, AvailableDays, commonSlots, sessionsPerDay, 0)) {
            System.out.println("attempt and success common slots schedule");
            // Then, attempt to schedule the extra sessions wherever possible
            if (extraSessions > 0) {
                if (attemptScheduleInDays(courseUUID, AvailableDays, commonSlots, 1, extraSessions)) {
                    System.out.println("Extra sessions scheduled successfully!");
                }
            }
            courseToBeScheduled.setPerfectlyScheduled(true);
            System.out.println("Sessions scheduled: " + courseToBeScheduled.getSessionsScheduled());
            System.out.println("Total sessions to be: " + courseToBeScheduled.getNumberOfSessions());
            return true;
        } else {
            unscheduleCourseFromAll(courseUUID);
            System.out.println("attempt non common slots schedule");
            for (int dayIndex = 0; dayIndex < 5; dayIndex++) {
                List<Integer> instructorSlots = courseToBeScheduled.getInstructorSlots(daysList[dayIndex]);
                if (instructorSlots != null && !instructorSlots.isEmpty()) {
                    int sessionsOnCurrentDay = sessionsPerDay;
                    if (extraSessions > 0) {
                        sessionsOnCurrentDay++;
                        extraSessions--;
                    }

                    if (scheduleCourseInDay(courseUUID, dayIndex, instructorSlots, sessionsOnCurrentDay)) {
                        System.out.println("succesfully added to day");
                    }

                    System.out.println("Sessions scheduled: " + courseToBeScheduled.getSessionsScheduled());
                    System.out.println("Total sessions to be: " + courseToBeScheduled.getNumberOfSessions());

                    if (courseToBeScheduled.getSessionsScheduled() >= courseToBeScheduled.getNumberOfSessions()) {
                        return true;
                    }
                }
            }
        }

        System.out.println("equal spread scheduling returning false");
        return false;
    }

    private boolean AttemptAnySchedule(UUID courseUUID) {
        System.out.println("Attempting any schedule for course: " + courseMap.get(courseUUID).getCourseID());
        course courseToBeScheduled = courseMap.get(courseUUID);

        for (int dayIndex = 0; dayIndex < 5; dayIndex++) {

            List<Integer> instructorSlots = courseToBeScheduled.getInstructorSlots(daysList[dayIndex]);
            if (instructorSlots == null || instructorSlots.isEmpty()) {
                System.out.println("No available slots for " + courseToBeScheduled.getInstructor().getName()
                        + " on " + daysList[dayIndex]);
                continue;
            }

            System.out.println("Checking available slots for " + courseToBeScheduled.getInstructor().getName()
                    + " on " + daysList[dayIndex]);

            if (scheduleCourseInDay(courseUUID, dayIndex, instructorSlots, courseToBeScheduled.getNumberOfSessions())) {
                return true;

            }
        }

        return false;
    }

    // ~~~~~~~~~~~ Schedule course in day ~~~~~~~~~~~

    private boolean scheduleCourseInDay(UUID courseUUID, int dayIndex, List<Integer> instructorSlots,
            int sessionsToSchedule) {
        System.out.println(
                "Scheduling course " + courseMap.get(courseUUID).getCourseID() + " on day " + daysList[dayIndex]);
        course courseToBeScheduled = courseMap.get(courseUUID);
        boolean canCrossLunchBreak = courseToBeScheduled.getDuration().equalsIgnoreCase("5 hours");

        for (int currentIndex = 0; currentIndex < instructorSlots.size(); currentIndex++) {
            System.out.println("Sessions scheduled: " + courseToBeScheduled.getSessionsScheduled());
            System.out.println("Total sessions to be: " + courseToBeScheduled.getNumberOfSessions());

            if (courseToBeScheduled.getSessionsScheduled() == courseToBeScheduled.getNumberOfSessions()) {
                return true;
            }

            int timeSlotIndex = instructorSlots.get(currentIndex);

            int timeSlotEnd = timeSlotIndex + courseToBeScheduled.getNumberOfSessions();
            int timeSlotStart = timeSlotIndex;
            if (courseToBeScheduled.getNumberOfSlots() > 1
                    && timeSlotIndex + courseToBeScheduled.getNumberOfSlots() > 3 && timeSlotIndex < 3) {
                if (!canCrossLunchBreak) {
                    // Skip this slot since it cannot cross the lunch break
                    System.out.println(
                            "Skipping slot at index " + timeSlotIndex + " as it cannot cross the lunch break.");
                    continue;
                } else {
                    int nextAvailableSlot = 3 - courseToBeScheduled.getNumberOfSlots();
                    timeSlotIndex = instructorSlots.get(nextAvailableSlot);
                    System.out.println("Adjusting timeSlotIndex to " + timeSlotIndex + " to cross the lunch break.");
                }
            }

            System.out.println(
                    "Attempting to schedule course in slots from possible indicies " + timeSlotIndex + " to "
                            + timeSlotEnd);

            if (courseToBeScheduled.getNumberOfSlots() > 1 && areSlotsAvailable(courseUUID, dayIndex, timeSlotIndex,
                    timeSlotEnd)) {

                System.out.println("Scheduling multiple slots on day " + daysList[dayIndex]
                        + ", starting at timeslot index: " + timeSlotIndex);
                System.out.println("timeSlotStart: " + timeSlotStart + ", timeSlotEnd: " + timeSlotEnd);

                scheduleCourseInSlots(courseUUID, dayIndex, timeSlotIndex,
                        timeSlotIndex + courseToBeScheduled.getNumberOfSlots());

                courseMap.get(courseUUID).incrementSessionsScheduled();
                currentIndex += courseToBeScheduled.getNumberOfSlots() - 1;

            } else if (courseToBeScheduled.getNumberOfSlots() == 1) {
                if (isSlotAvailable(courseUUID, dayIndex, timeSlotIndex)) {
                    System.out.println(
                            "Single slot available on " + daysList[dayIndex] + ", timeslot index: " + timeSlotIndex);
                    scheduleCourseInSlot(courseUUID, dayIndex, timeSlotIndex);
                    courseToBeScheduled.incrementSessionsScheduled();
                } else {
                    continue;
                }
            } else {
                continue;
            }

            sessionsToSchedule--;
            if (sessionsToSchedule == 0) {
                return true;
            }
        }
        System.out.println("Schedule in day returning false");
        return false;
    }

    // ~~~~~~~~~~~ Attempt days schedule for common slots ~~~~~~~~~~~

    private boolean attemptScheduleInDays(UUID courseUUID, List<Integer> dayIndices, List<Integer> commonSlots,
            int sessionsPerDay) {
        return attemptScheduleInDays(courseUUID, dayIndices, commonSlots, sessionsPerDay, 0);
    }

    private boolean attemptScheduleInDays(UUID courseUUID, List<Integer> dayIndices, List<Integer> commonSlots,
            int sessionsPerDay, int extraSessions) {
        System.out.println("attempt schedule in common slots");
        course courseToBeScheduled = courseMap.get(courseUUID);

        for (int dayIndex : dayIndices) {
            int sessionsOnCurrentDay = sessionsPerDay;
            if (extraSessions > 0) {
                sessionsOnCurrentDay++;
                extraSessions--;
            }

            System.out.println("sessions for " + daysList[dayIndex] + " = " + sessionsOnCurrentDay);
            System.out.println(commonSlots.toString());
            if (!scheduleCourseInDay(courseUUID, dayIndex, commonSlots, sessionsOnCurrentDay)) {
                // If scheduling fails for any day, revert all the previously scheduled sessions
                unscheduleCourseFromAll(courseUUID);
                return false;
            }

        }

        System.out.println("Sessions scheduled: " + courseToBeScheduled.getSessionsScheduled());
        System.out.println("Total sessions to be: " + courseToBeScheduled.getNumberOfSessions());

        return courseToBeScheduled.getNumberOfSessions() == courseToBeScheduled.getSessionsScheduled();
    }

    // ~~~~~~~~~~~ Unschedule course methods ~~~~~~~~~~~

    public void unscheduleCourseFromAll(UUID courseUUID) {
        courseMap.get(courseUUID).setSessionsScheduled(0);
        courseMap.get(courseUUID).setOtherInfo("");

        for (int dayIndex = 0; dayIndex < 5; dayIndex++) {
            for (int timeSlotIndex = 0; timeSlotIndex < 6; timeSlotIndex++) {
                if (courseMap.get(courseUUID).isScheduled(dayIndex, timeSlotIndex)) {
                    unscheduleCourseInSlot(courseUUID, dayIndex, timeSlotIndex);
                }
            }
        }
    }

    private void unscheduleCourseInDay(UUID courseUUID, int dayIndex) {
        course courseToBeScheduled = courseMap.get(courseUUID);
        for (int timeSlotIndex = 0; timeSlotIndex < timeslots; timeSlotIndex++) {
            if (courseToBeScheduled.isScheduled(dayIndex, timeSlotIndex)) {
                unscheduleCourseInSlot(courseUUID, dayIndex, timeSlotIndex);
                courseToBeScheduled.setScheduled(dayIndex, timeSlotIndex, false);
            }
        }
    }

    private void unscheduleCourseInSlot(UUID courseUUID, int dayIndex, int timeSlotIndex) {
        schedule[dayIndex][timeSlotIndex].remove(courseUUID);
        courseMap.get(courseUUID).setScheduled(dayIndex, timeSlotIndex, false);
    }

    // ~~~~~~~~~~~ Slot availability methods ~~~~~~~~~~~

    private boolean isSlotAvailable(UUID courseUUID, int dayIndex, int timeSlotIndex) {
        System.out.println("Checking availability for course " + courseMap.get(courseUUID).getCourseID() + " in slot "
                + timeSlotIndex);

        // Check if the dayIndex is valid
        if (dayIndex < 0 || dayIndex >= daysList.length) {
            System.out.println("Day index out of bounds");
            return false;
        }

        // Check if the timeSlotIndex is valid
        if (timeSlotIndex < 0 || timeSlotIndex >= schedule[dayIndex].length) {
            System.out.println("Time slot index out of bounds");
            return false;
        }

        if (!(courseMap.get(courseUUID).getInstructorSlots(daysList[dayIndex]).contains(timeSlotIndex))) {
            System.out.println("Slot not available");
            return false;
        }

        for (UUID scheduledCourseUUID : schedule[dayIndex][timeSlotIndex]) {

            if (!courseMap.get(courseUUID).doesConflict(courseMap.get(scheduledCourseUUID))) {
                System.out.println("Slot not available");

                return false;
            }
        }
        System.out.println("Slot available");

        return true;
    }

    private boolean areSlotsAvailable(UUID courseUUID, int dayIndex, int timeSlotIndexStart, int timeSlotIndexEnd) {

        System.out.println("checking slots between " + timeSlotIndexStart + " and " + timeSlotIndexEnd);

        if (courseMap.get(courseUUID).getNumberOfSlots() + timeSlotIndexStart > 6) {
            System.out.println("!!slots returning false");
            return false;
        }

        for (int currentIndex = timeSlotIndexStart; currentIndex <= timeSlotIndexEnd; currentIndex++) {
            if (!isSlotAvailable(courseUUID, dayIndex, currentIndex)) {
                System.out.println("slots returning false");
                return false;
            }
        }
        System.out.println("slots returning true");
        return true;
    }

    // ~~~~~~~~~~~ Schedule course methods ~~~~~~~~~~~

    private void scheduleCourseInSlot(UUID courseUUID, int dayIndex, int timeSlotIndex) {
        course mainCourse = courseMap.get(courseUUID);
        schedule[dayIndex][timeSlotIndex].add(courseUUID);
        if (courseMap.get(courseUUID).hasCommonSessions()) {
            System.out.println("enetered common sessions if statements~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            for (int LectureIndex = 1; LectureIndex < courseMap.get(courseUUID).getNumberOfSections(); LectureIndex++) {
                course otherSection = new course(mainCourse.getCourseID(),
                        mainCourse.getCourseName(),
                        mainCourse.getNumberOfCredits(),
                        mainCourse.getNumberOfSections(),
                        mainCourse.getNumberOfSessions(),
                        mainCourse.getInstructor(),
                        mainCourse.getConflictingCourses(),
                        mainCourse.getCourseType(),
                        mainCourse.getNumberOfSlots(),
                        mainCourse.getDuration(),
                        LectureIndex + 1, mainCourse.getDurationMinutes());
                UUID otherSectionUUID = UUID.randomUUID();
                courseMap.put(otherSectionUUID, otherSection);

                otherSection.setScheduled(dayIndex, timeSlotIndex, true);
                schedule[dayIndex][timeSlotIndex].add(otherSectionUUID);

            }
        }

        courseMap.get(courseUUID).setScheduled(dayIndex, timeSlotIndex, true);
    }

    private void scheduleCourseInSlots(UUID courseUUID, int dayIndex, int timeSlotIndexStart, int timeSlotIndexEnd) {
        System.out
                .println("scheduling course " + courseMap.get(courseUUID).getCourseID() + " (inclusive) between slots "
                        + timeSlotIndexStart + " and " + (timeSlotIndexEnd - 1));
        for (int currentIndex = timeSlotIndexStart; currentIndex < timeSlotIndexEnd; currentIndex++) {
            scheduleCourseInSlot(courseUUID, dayIndex, currentIndex);

        }

    }

    // Returns a list of the common timeslots in all days provided
    private List<Integer> findCommonSlotsInDays(UUID courseUUID, List<Integer> days) {
        if (days.isEmpty()) {
            return new ArrayList<>();
        }

        String firstDay = daysList[days.get(0)];
        List<Integer> commonSlots = new ArrayList<>(courseMap.get(courseUUID).getInstructorSlots(firstDay));

        for (int i = 1; i < days.size(); i++) {
            String currentDay = daysList[days.get(i)];
            List<Integer> currentDaySlots = courseMap.get(courseUUID).getInstructorSlots(currentDay);

            commonSlots.retainAll(currentDaySlots);
        }

        return commonSlots;
    }

    public Set<UUID> getUnscheduledCourseHeap() {
        return unscheduledCourseHeap;
    }

    public void moveUnscheduledCourses() {
        for (UUID courseUUID : unscheduledCourseHeap) {
            if (courseMap.get(courseUUID).getNumberOfSessions() == 0) {
                continue;
            }
            coursesToBeRescheduled.add(courseUUID);
        }
    }

    public void rescheduleConflicts() {
        System.out.println("enetered rescheduleConflicts");
        if (!coursesToBeRescheduled.isEmpty()) {
            Iterator<UUID> iterator = coursesToBeRescheduled.iterator();
            while (iterator.hasNext()) {
                UUID unscheduledCourseUUID = iterator.next();
                System.out.println(courseMap.get("attempting " + courseMap.get(unscheduledCourseUUID)));
                if (!alreadyRescheduled.contains(unscheduledCourseUUID)) {
                    System.out.println("attempting reschedule for course");
                    if (rescheduleConflictingCourse(unscheduledCourseUUID)) {
                        alreadyRescheduled.add(unscheduledCourseUUID);
                    }
                }

                if (courseMap.get(unscheduledCourseUUID).getNumberOfSessions() == courseMap.get(unscheduledCourseUUID)
                        .getSessionsScheduled() && courseMap.get(unscheduledCourseUUID).getNumberOfSessions() != 0) {
                    System.out.println("iterator removing " + courseMap.get(unscheduledCourseUUID).getCourseID());
                    iterator.remove(); // Safely remove the current element from coursesToBeRescheduled
                    unscheduledCourseHeap.remove(unscheduledCourseUUID);
                }

            }

        }
    }

    private boolean rescheduleConflictingCourse(UUID unscheduledCourseUUID) {
        return rescheduleConflictingCourse(unscheduledCourseUUID, 1);
    }

    private boolean rescheduleConflictingCourse(UUID unscheduledCourseUUID, int depth) {
        if (depth == 0) {
            // Limit reached, stop rescheduling
            return false;
        }

        course courseToBeScheduled = courseMap.get(unscheduledCourseUUID);
        unscheduleCourseFromAll(unscheduledCourseUUID);
        List<UUID> conflictingCourses = new ArrayList<>();

        if (courseToBeScheduled.getInstructor() == null) {
            return true;
        }

        for (int dayIndex = 0; dayIndex < 5; dayIndex++) {
            List<Integer> instructorSlots = new ArrayList<>(courseToBeScheduled.getInstructorSlots(daysList[dayIndex]));

            for (int timeSlotIndex : instructorSlots) {
                if (timeSlotIndex > 5) {
                    continue;
                }
                List<UUID> scheduledCourses = new ArrayList<>(schedule[dayIndex][timeSlotIndex]);

                for (UUID scheduledCourseUUID : scheduledCourses) {
                    course courseScheduled = courseMap.get(scheduledCourseUUID);

                    if (courseToBeScheduled.getCourseID().equals(courseScheduled.getCourseID())) {
                        continue;
                    }

                    boolean doesConflict = courseToBeScheduled.doesConflict(courseScheduled);

                    if (!doesConflict) {
                        conflictingCourses.add(scheduledCourseUUID);
                    }
                }
            }
        }

        // Perform rescheduling
        for (UUID scheduledCourseUUID : conflictingCourses) {
            boolean wasPerfectlyScheduled = courseMap.get(scheduledCourseUUID).isPerfectlyScheduled();

            if (courseMap.get(scheduledCourseUUID).hasCommonSessions()) {
                continue;
            }

            unscheduleCourseFromAll(scheduledCourseUUID);
            addCourse(unscheduledCourseUUID);

            if (!addCourse(scheduledCourseUUID)) {
                if (!(courseMap.get(scheduledCourseUUID).isPerfectlyScheduled() && wasPerfectlyScheduled)) {
                    if (!rescheduleConflictingCourse(scheduledCourseUUID, depth - 1)) {
                        unscheduleCourseFromAll(unscheduledCourseUUID);
                        unscheduleCourseFromAll(scheduledCourseUUID);
                        if (addCourse(scheduledCourseUUID)) {
                            break;
                        }

                    }
                }
            }

        }

        if (courseMap.get(unscheduledCourseUUID).getSessionsScheduled() == courseMap
                .get(unscheduledCourseUUID).getNumberOfSessions()) {
            return true;
        }

        return false;
    }

    private void moveDayPairToLast(int indexToMove) {
        if (indexToMove >= 0 && indexToMove < dayPairs.size()) {
            List<Integer> selectedDayPair = dayPairs.remove(indexToMove);
            dayPairs.add(selectedDayPair);
            System.out.println("Day pair at index " + indexToMove + " moved to the end.");
        } else {
            System.out.println("Invalid index. No day pair moved.");
        }
    }

    public void outputExcel() {

        String[] timeSlots = { "08:00", "09:30", "11:00", "13:00", "14:30", "16:00" };
        String[] daysOfWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };

        // Create the header row with days of the week
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < daysOfWeek.length; i++) {
            Cell cell = headerRow.createCell(i + 1);
            cell.setCellValue(daysOfWeek[i]);
        }

        // Iterate over time slots and populate the sheet with course IDs
        for (int i = 0; i < timeSlots.length; i++) {
            Row row = sheet.createRow(i + 1);
            Cell timeSlotCell = row.createCell(0);
            timeSlotCell.setCellValue(timeSlots[i]);

            for (int j = 0; j < daysOfWeek.length; j++) {
                Cell cell = row.createCell(j + 1);

                // Get the course ID for the specific time slot and day (replace this with your
                // actual data retrieval logic)
                String courseId = "";
                String cellValue = "";
                try {
                    for (UUID courseUUID : schedule[j][i]) {

                        courseId = courseMap.get(courseUUID).getCourseID() + "-"
                                + courseMap.get(courseUUID).getLectureNumber() + " by "
                                + courseMap.get(courseUUID).getInstructor().getName();
                        cellValue += courseId + "\n";

                    }
                } catch (Exception e) {
                    courseId = "";
                }

                // Set the course ID in the cell
                if (courseId != null) {
                    cell.setCellValue(cellValue);
                }
            }
        }
        try (FileOutputStream outputStream = new FileOutputStream(
                "scheduler/data/CourseSchedule.xlsx")) {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the workbook
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Excel file generated successfully!");
    }

    public void displaySchedule() {
        int unscheduledCoursesCount = 0;
        for (int i = 0; i < days; i++) {
            for (int j = 0; j < timeslots; j++) {
                System.out.print("[");
                for (UUID courseUUID : schedule[i][j]) {
                    System.out.print(courseMap.get(courseUUID).getCourseID() + "-"
                            + courseMap.get(courseUUID).getLectureNumber() + " ");
                }
                System.out.print("]\n");
            }
            System.out.println("\n");
        }



        System.out.println();
        System.out.println("Unscheduled courses");
        for (UUID courseUUID : unscheduledCourseHeap) {
            String instructor_name = "";
            if (courseMap.get(courseUUID).getInstructor() == null) {
                instructor_name = "instructor not found";
            } else {
                instructor_name = courseMap.get(courseUUID).getInstructor().getName();
            }
            String info = courseMap.get(courseUUID).getCourseID() + "-"
                    + courseMap.get(courseUUID).getLectureNumber() + " by "
                    + instructor_name + " "
                    + courseMap.get(courseUUID).getOtherInfo();

            System.out.println(info);

        }

        System.out.println();
        System.out.println("Zero session courses");
        for (UUID ZeroSessionsCourseUUID : zeroSessionCourses) {
            String instructor_name = "";
            if (courseMap.get(ZeroSessionsCourseUUID).getInstructor() == null) {
                instructor_name = "instructor not found";
            } else {
                instructor_name = courseMap.get(ZeroSessionsCourseUUID).getInstructor().getName();
            }

            String info = courseMap.get(ZeroSessionsCourseUUID).getCourseID() + "-"
                    + courseMap.get(ZeroSessionsCourseUUID).getLectureNumber() + " by "
                    + instructor_name + " "
                    + courseMap.get(ZeroSessionsCourseUUID).getOtherInfo();

            System.out.println(info);
        }

        System.out.println();
        System.out.println("error courses");
        for (UUID courseUUID : errorCourses) {
            String instructor_name = "";
            if (courseMap.get(courseUUID).getInstructor() == null) {
                instructor_name = "instructor not found";
            } else {
                instructor_name = courseMap.get(courseUUID).getInstructor().getName();
            }
            String info = courseMap.get(courseUUID).getCourseID() + "-"
                    + courseMap.get(courseUUID).getLectureNumber() + " by "
                    + instructor_name + " "
                    + courseMap.get(courseUUID).getOtherInfo();

            System.out.println(info);

        }

        System.out.println("number of unscheduled Courses " + unscheduledCourseHeap.size());
    }

    public List<UUID>[][] getSchedule() {
        return schedule;
    }

    public course getCourse(UUID courseUUID) {
        if (courseMap.containsKey(courseUUID)) {
            return courseMap.get(courseUUID);
        }
        return null;
    }

}
