package scheduler;

import java.util.LinkedList;
import java.util.List;

public class course {

    private String courseID;
    private String courseName;
    private int numberOfCredits;
    private int numberOfSessions;
    private int numberOfSections;
    private int Lecturenumber;
    private boolean hasCommonSessions;

    private instructor Instructor;

    private LinkedList<String> conflictingCourses;

    private String courseType;
    private int numberOfSlots; // nb of slots each session takes
                               // based on duration from excel file
    private int sessionsScheduled;
    private String duration;
    private int durationMinutes;

    private boolean[][] isScheduled;
    private boolean isPerfectlyScheduled;

    private String otherInfo;

    public course(String ID, String name, int creds, int sections, int sessions, instructor inst,
            LinkedList<String> conflicts, String Type, int Slots, String Duration, int lecturenb, int durationmins) {
        courseID = ID;
        courseName = name;
        numberOfCredits = creds;
        numberOfSections = sections;
        numberOfSessions = sessions;
        Instructor = inst;
        conflictingCourses = conflicts;
        courseType = Type;
        numberOfSlots = Slots;
        Lecturenumber = lecturenb;
        sessionsScheduled = 0;
        isScheduled = new boolean[5][6];
        duration = Duration;
        isPerfectlyScheduled = false;
        durationMinutes = durationmins;
        otherInfo = "";
        hasCommonSessions = false;
    }

    public void setCommonSessions(boolean state) {
        hasCommonSessions = state;
    }

    public boolean hasCommonSessions() {
        return hasCommonSessions;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String info) {
        otherInfo = info;
    }

    public void doubleSessions() {
        numberOfSessions *= 2;
    }

    public boolean[][] getIsScheduledArray() {
        boolean[][] isScheduledArray = new boolean[5][6];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 6; j++) {
                isScheduledArray[i][j] = isScheduled(i, j);
            }
        }
        return isScheduledArray;
    }

    public void setDurationMinutes(int durationmins) {
        this.durationMinutes = durationmins;
    }

    public int getDurationMinutes() {
        return this.durationMinutes;
    }

    public List<Integer> getInstructorSlots(String Day) {
        return this.Instructor.getAvailablityMap().get(Day);
    }

    public boolean doesConflict(course OtherCourse) {
        if (this.getConflictingCourses().contains(OtherCourse.getCourseID())) { // condition 1: other course is part of
                                                                                // this.conflicting courses
            return false;
        }

        if (OtherCourse.getConflictingCourses().contains(this.getCourseID())) { // condition 1: other course is part of
            // this.conflicting courses
            return false;
        }

        if (this.getInstructor().getName().equalsIgnoreCase(OtherCourse.getInstructor().getName())) { // condition 2:
                                                                                                      // courses have
                                                                                                      // same instructor
            return false;
        }
        if (this.getInstructor().getName().contains(OtherCourse.getInstructor().getName())) {
            return false;
        }
        if (OtherCourse.getInstructor().getName().contains(this.getInstructor().getName())) {
            return false;
        }

        return true;
    }

    public void setPerfectlyScheduled(boolean state) {
        isPerfectlyScheduled = state;
    }

    public boolean isPerfectlyScheduled() {
        return isPerfectlyScheduled;
    }

    public void incrementSessionsScheduled() {
        this.sessionsScheduled++;
    }

    public void addToSessionsScheduled(int sessionsToAdd) {
        this.sessionsScheduled += sessionsToAdd;
    }

    public void setScheduled(int dayIndex, int timeSlotIndex, boolean state) {
        isScheduled[dayIndex][timeSlotIndex] = state;
    }

    public boolean isScheduled(int dayIndex, int timeSlotIndex) {
        return isScheduled[dayIndex][timeSlotIndex];
    }

    public void setCourseID(String ID) {
        courseID = ID;
    }

    public void setCourseName(String name) {
        courseName = name;
    }

    public void setNumberOfCredits(int creds) {
        numberOfCredits = creds;
    }

    public void setNumberOfSessions(int sessions) {
        numberOfSessions = sessions;
    }

    public void setNumberOfSections(int sections) {
        numberOfSections = sections;
    }

    public void setInstructor(instructor inst) {
        Instructor = inst;
    }

    public void setConflictingCourses(LinkedList<String> conflicts) {
        conflictingCourses = conflicts;
    }

    public void setCourseType(String Type) {
        courseType = Type;
    }

    public void setNumberOfSlots(int Slots) {
        numberOfSlots = Slots;
    }

    public void setSessionsScheduled(int sessions) {
        sessionsScheduled = sessions;
    }

    public void setDuration(String Duration) {
        duration = Duration;
    }

    public String getCourseID() {
        return courseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getNumberOfCredits() {
        return numberOfCredits;
    }

    public int getNumberOfSessions() {
        return numberOfSessions;
    }

    public int getNumberOfSections() {
        return numberOfSections;
    }

    public instructor getInstructor() {
        return Instructor;
    }

    public LinkedList<String> getConflictingCourses() {
        return conflictingCourses;
    }

    public String getCourseType() {
        return courseType;
    }

    public int getNumberOfSlots() {
        return numberOfSlots;
    }

    public int getSessionsScheduled() {
        return sessionsScheduled;
    }

    public String getDuration() {
        return duration;
    }

    public int getLectureNumber() {
        return Lecturenumber;
    }

    // toString method

    @Override
    public String toString() {

        String courseCode = "error";
        String lectureNB = "error";
        String InstructorName = "error";

        try {
            courseCode = courseID;
        } catch (Exception e) {

        }
        try {
            lectureNB = "" + Lecturenumber;
        } catch (Exception e) {

        }
        try {
            InstructorName = Instructor.getName();
        } catch (Exception e) {

        }

        return "Course ID: " + courseCode + "-" + lectureNB + " by " + InstructorName;
    }
}