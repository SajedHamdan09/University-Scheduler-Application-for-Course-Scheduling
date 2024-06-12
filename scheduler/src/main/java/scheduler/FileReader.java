package scheduler;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FileReader {

    private String file;
    int countCoursesToScheduled = 0;

    HashMap<UUID, course> Courses = new HashMap<UUID, course>();
    Queue<course> courseQueue = new LinkedList<course>();;
    HashMap<String, instructor> Instructors = new HashMap<String, instructor>();

    String[] daysList = { "monday", "tuesday", "wednesday", "thursday", "friday" };

    public FileReader(String f) {
        this.file = f;
    }

    public Map<UUID, course> readCoursesFromSheet() {
        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {

            instructor nullInstructor = new instructor("null"); // null instructor for courses with invalid instructors

            // Get the specific sheet by name
            Sheet course_sheet = workbook.getSheetAt(0);
            Sheet instructor_sheet = workbook.getSheetAt(1);

            if (!isCourseSheet(course_sheet)) {
                Sheet temp_sheet = course_sheet;
                course_sheet = instructor_sheet;
                instructor_sheet = temp_sheet;
            }

            Iterator<Row> instructor_Iterator = instructor_sheet.iterator();
            // Skip the header row
            if (instructor_Iterator.hasNext()) {
                instructor_Iterator.next();
            }

            // Process each row
            while (instructor_Iterator.hasNext()) {
                Row row = instructor_Iterator.next();
                String instructor_name = (row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                        .getStringCellValue());

                instructor instructor = new instructor(instructor_name.trim());

                System.out.println("instrutor name from insts " + instructor_name.trim());

                for (int i = 1; i < 6; i++) {
                    String time = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                            .getStringCellValue();

                    LinkedList<String> hours = Split_String(time, "-");

                    System.out.println(hours.getFirst());
                    System.out.println(hours.getLast());

                    try {
                        instructor.setAvailability(daysList[i - 1], parseToLocalTime(hours.getFirst().trim()),
                                parseToLocalTime(hours.getLast().trim()));
                    } catch (Exception e) {
                        instructor.setAvailability(daysList[i - 1], null, null);
                    }

                    Instructors.put(instructor_name.trim(), instructor);

                }
            }

            Iterator<Row> courseIterator = course_sheet.iterator();

            // Skip the header row
            if (courseIterator.hasNext()) {
                courseIterator.next();
            }

            // Process each row
            while (courseIterator.hasNext()) {
                Row row = courseIterator.next();
                // Iterate over cells in the row
                String course_code = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                        .getStringCellValue();
                if (course_code.equals("")) {
                    continue;
                }
                course_code.replace(" ", "");

                // if(!course_code.equalsIgnoreCase("CMPS210")){
                // continue;
                // }

                System.out.println("reading course " + course_code.trim());

                String course_name = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                        .getStringCellValue();
                String type = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                        .getStringCellValue();

                int num_credits;
                try {
                    num_credits = (int) row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                            .getNumericCellValue();
                } catch (Exception e) {
                    num_credits = 0;
                }

                // get number of sessions per section
                // AND
                // if these lecture sessions are to be common
                int num_sessions = 0;
                boolean hasCommonSessions = false;
                try {
                    String sessionCellInfo = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                            .getStringCellValue();
                    if (sessionCellInfo.toLowerCase().contains("common")) {
                        System.out.println("file reader has common sessions~~~~~~~~~~~~~~~~~");
                        hasCommonSessions = true;
                        String[] parts = sessionCellInfo.split("\\s+");
                        for (String part : parts) {
                            if (part.matches("\\d+")) {
                                num_sessions = Integer.parseInt(part);
                                break;
                            }
                        }
                    } else {
                        System.out.println("file reader has no common sessions~~~~~~~~~~~~~~~~~");

                        num_sessions = Integer.parseInt(sessionCellInfo);
                    }
                } catch (Exception e) {
                    num_sessions = (int) row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                            .getNumericCellValue();
                }

                // get number of sections
                int num_sections;
                try {
                    num_sections = (int) row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                            .getNumericCellValue();
                } catch (Exception e) {
                    num_sections = 0;
                }

                // get duration string and minutes
                String duration;
                Cell durationCell = row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                if (durationCell.getCellType() == CellType.NUMERIC) {
                    // If the cell contains a numeric value, convert it to a string
                    duration = String.valueOf((int) durationCell.getNumericCellValue());
                } else {
                    // Otherwise, read it as a string
                    duration = durationCell.getStringCellValue();
                }
                int totalMinutes = calculateDurationMinutes(duration);

                String instructor_name = row.getCell(7,
                        Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                        .getStringCellValue();
                String restrictions_slot = row.getCell(8, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                        .getStringCellValue();

                LinkedList<String> conflicting_courses = Split_String(restrictions_slot, "/");

                String CombinedInstructorName = "";

                System.out.println("Instructor name for this course " + instructor_name);

                if (instructor_name.contains(";")) {
                    String[] instructorsArray = instructor_name.split(";");
                    List<String> instructorsList = new ArrayList<>();

                    for (String instructorName : instructorsArray) {
                        instructorName = instructorName.trim();
                        instructorsList.add(instructorName);
                    }

                    instructor combinedInstructor = combineInstructors(instructorsList, totalMinutes);
                    Instructors.put(CombinedInstructorName, combinedInstructor);
                    System.out.println(combinedInstructor.getName());
                    instructor_name = combinedInstructor.getName();
                } else if (!Instructors.containsKey(instructor_name.trim())) {
                    instructor_name = "null";
                } else {
                    parseInstructorHoursToIntegers(instructor_name, totalMinutes);
                }

                course course = new course(course_code.trim(), course_name.trim(), num_credits,
                        num_sections, num_sessions, Instructors.get(instructor_name.trim()),
                        conflicting_courses, type.trim(), calculateSlots(totalMinutes),
                        duration.toLowerCase(), 1, totalMinutes);

                if(totalMinutes > 0){
                    countCoursesToScheduled++;
                }

                UUID courseUUID = UUID.randomUUID();
                course.setCommonSessions(hasCommonSessions);
                Courses.put(courseUUID, course);
                courseQueue.add(course);

            }

            fis.close();
            workbook.close();

            return Courses;
        } catch (IOException e) {
            System.err.println("Error reading the Excel file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric values from the Excel file: " + e.getMessage());
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date/time values from the Excel file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input in the Excel file: " + e.getMessage());
        }

        return Collections.emptyMap(); // Return an empty map in case of any exceptions
    }

    public int calculateDurationMinutes(String duration) {
        String[] parts = duration.split("\\s+");

        int hours = 0;
        int minutes = 0;

        // Arrays for all possible permutations of the words "hour" and "minute"
        String[] hourPermutations = { "hour", "hours", "hr", "hrs", "h" };
        String[] minutePermutations = { "min", "mins", "minutes", "m" };

        for (int i = 0; i < parts.length; i++) {
            // Check against the hour permutations
            for (String hourPermutation : hourPermutations) {
                if (parts[i].equalsIgnoreCase(hourPermutation)) {
                    hours = Integer.parseInt(parts[i - 1]);
                    break;
                }
            }

            // Check against the minute permutations
            for (String minutePermutation : minutePermutations) {
                if (parts[i].equalsIgnoreCase(minutePermutation)) {
                    minutes = Integer.parseInt(parts[i - 1]);
                    break;
                }
            }

            if (parts[i].equalsIgnoreCase("internship") || parts[i].equalsIgnoreCase("no")) {
                return 0;
            }
        }

        int totalMinutes = (hours * 60) + minutes;
        return totalMinutes;
    }

    public int calculateSlots(int totalMintues) {

        int slots = (int) Math.ceil((double) totalMintues / 75);

        return slots;
    }

    private boolean isCourseSheet(Sheet sheet) {
        // Get the first row in the sheet
        Row firstRow = sheet.getRow(0);
        if (firstRow != null) {
            // Iterate over cells in the first row
            for (Cell cell : firstRow) {
                // Check the header value of a specific column
                if (cell.getColumnIndex() == 0 && cell.getStringCellValue().equalsIgnoreCase("coursecode")) {
                    return true;
                } else {
                    break;
                }
            }
        }
        return false;
    }

    private LinkedList<String> Split_String(String LongString, String Splitter) { // splits long string according to
        LinkedList<String> slots = new LinkedList<String>();
        String[] hours = LongString.split(Splitter);

        for (String hour : hours) {
            slots.add(hour.trim());
        }

        return slots;
    }

    private LocalTime convertToMilitaryTime(String inputTime) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalTime time;
        try {
            time = LocalTime.parse(inputTime, inputFormatter);
        } catch (Exception e) {
            try {
                // If the parsing fails, try parsing in "H:mm" format
                inputFormatter = DateTimeFormatter.ofPattern("H:mm");
                time = LocalTime.parse(inputTime, inputFormatter);
            } catch (Exception ex) {
                System.err.println("Invalid time format. Please use HH:mm or H:mm format.");
                return null;
            }
        }

        // Add 12 hours to the time if hours are between 1 and 6
        int hours = time.getHour();
        if (hours >= 1 && hours <= 6) {
            time = time.plusHours(12);
        }

        return time;
    }

    public List<Integer> getSlotsIndicies(LocalTime startTime, LocalTime endTime) {
        return getSlotsIndicies(startTime, endTime, 75);
    }

    public List<Integer> getSlotsIndicies(LocalTime startTime, LocalTime endTime, int totalMinutes) {

        if (startTime == null || endTime == null) {
            return List.of();
        }

        LocalTime[] slots = {
                LocalTime.parse("08:00"), // Slot 0
                LocalTime.parse("09:30"), // Slot 1
                LocalTime.parse("11:00"), // Slot 2
                LocalTime.parse("13:00"), // Slot 3
                LocalTime.parse("14:30"), // Slot 4
                LocalTime.parse("16:00"), // Slot 5
                LocalTime.parse("17:15") // Final hour
        };

        int slotDuration = 75; // The default duration of each time slot in minutes
        if (totalMinutes > 0) {
            slotDuration = totalMinutes;
        }

        int startSlot = -1;
        int endSlot = -1;

        // Find the first slot
        if ((startTime.isAfter(LocalTime.parse("12:15")) && startTime.isBefore(LocalTime.parse("13:00"))) ||
                startTime.equals(LocalTime.parse("12:15"))) {
            startSlot = 3;
        } else {
            boolean slotFound = false;
            for (int i = 0; i < slots.length; i++) {
                if (startTime.isBefore(slots[i]) || startTime.equals(slots[i])) {
                    startSlot = i;
                    slotFound = true;
                    break;
                }
            }
            if (!slotFound) {
                startSlot = slots.length - 2;
            }
        }

        // Find the last slot
        if ((endTime.isAfter(LocalTime.parse("12:15")) && endTime.isBefore(LocalTime.parse("13:00"))) ||
                endTime.equals(LocalTime.parse("12:15"))) {
            endSlot = 3;
        } else {
            boolean slotFound = false;
            for (int i = startSlot; i < slots.length; i++) {
                if (endTime.isBefore(slots[i]) || endTime.equals(slots[i])) {
                    endSlot = i - 1;
                    slotFound = true;
                    break;
                }
            }
            if (!slotFound) {
                endSlot = slots.length - 1;
            }
        }

        // Calculate the total available duration between start and end times
        int totalAvailableDuration = 0;
        for (int i = startSlot; i <= endSlot; i++) {
            if (i == startSlot && startSlot != endSlot) {
                totalAvailableDuration += calculateDuration(startTime, slots[i + 1]);
            } else if (i == endSlot) {
                totalAvailableDuration += calculateDuration(slots[i], endTime);
            } else {
                totalAvailableDuration += calculateDuration(slots[i], slots[i + 1]);
            }
        }

        // Check if the session can fit within the available slots based on its duration
        if (totalAvailableDuration < totalMinutes) {
            return new LinkedList<>(); // Return an empty list as it can't be scheduled within the available slots
        }

        // Return the viable slots as a list of indices
        List<Integer> timeSlotIndices = new LinkedList<>();
        for (int i = startSlot; i <= endSlot; i++) {
            timeSlotIndices.add(i);
        }
        return timeSlotIndices;
    }

    // Helper method to calculate the duration between two LocalTime objects
    private int calculateDuration(LocalTime startTime, LocalTime endTime) {
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    public static List<Integer> combineListsNoDuplicates(List<Integer> list1, List<Integer> list2) {
        Set<Integer> uniqueElements = new HashSet<>();

        uniqueElements.addAll(list1);

        uniqueElements.addAll(list2);

        List<Integer> combinedList = new ArrayList<>(uniqueElements);

        return combinedList;
    }

    public static LocalTime parseToLocalTime(String hourString) {
        // Define the format patterns for parsing the input string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        DateTimeFormatter formatterWithLeadingZero = DateTimeFormatter.ofPattern("HH:mm");

        try {
            // Try parsing the input string with the first format pattern (without leading
            // zero)
            LocalTime time = LocalTime.parse(hourString, formatter);

            // Check if the hour is between 1 and 6 (inclusive)
            int hour = time.getHour();
            if (hour >= 1 && hour <= 6) {
                // Convert to military time by adding 12 hours
                time = time.plusHours(12);
            }

            return time;
        } catch (DateTimeParseException e) {
            try {
                // If parsing fails, try again with the second format pattern (with leading
                // zero)
                LocalTime time = LocalTime.parse(hourString, formatterWithLeadingZero);

                // Check if the hour is between 1 and 6 (inclusive)
                int hour = time.getHour();
                if (hour >= 1 && hour <= 6) {
                    // Convert to military time by adding 12 hours
                    time = time.plusHours(12);
                }

                return time;
            } catch (DateTimeParseException ex) {
                // If both parsing attempts fail, return a default time (e.g., 08:00) or handle
                // the error accordingly
                System.err.println("Invalid hour format: " + hourString + ". Using default time 08:00.");
                return LocalTime.parse("08:00");
            }
        }
    }

    private void parseInstructorHoursToIntegers(String instructor_name, int courseTotalMinutes) {

        for (int i = 1; i < 6; i++) {

            instructor instructor = Instructors.get(instructor_name.trim());

            System.out.println(instructor_name);
            System.out.println(instructor.getName());

            List<Integer> time_slots;
            try {
                time_slots = getSlotsIndicies(instructor.getAvailabilityStart().get(daysList[i - 1]),
                        instructor.getAvailabilityEnd().get(daysList[i - 1]), courseTotalMinutes);
            } catch (Exception e) {
                time_slots = List.of();
            }
            // System.out.println(hours.toString());
            // System.out.println(time_slots.toString());
            instructor.setAvailability(daysList[i - 1], time_slots);
            Instructors.put(instructor_name.trim(), instructor);

        }
    }

    public instructor combineInstructors(List<String> instructorNames, int totalMinutes) {
        List<instructor> instructorsList = new ArrayList<>();
        StringBuilder combinedInstructorName = new StringBuilder();
    
        for (String instructorName : instructorNames) {
            instructorName = instructorName.trim();
            if (Instructors.containsKey(instructorName)) {
                instructorsList.add(Instructors.get(instructorName));
                combinedInstructorName.append(instructorName).append(";");
            } else {
                instructorsList.add(new instructor(instructorName));
                combinedInstructorName.append("[instructor " + instructorName + " not found];");
            }
        }
    
        instructor combInstructor = new instructor(combinedInstructorName.toString());
    
        for (String day : daysList) {
            List<Integer> combinedSlots = new ArrayList<>();
            for (instructor instructor : instructorsList) {
                List<Integer> instructorSlots = getSlotsIndicies(instructor.getAvailabilityStart().get(day),
                        instructor.getAvailabilityEnd().get(day), totalMinutes);
                combinedSlots.addAll(instructorSlots);
            }
            // Remove duplicates by converting the list to a set and back to a list
            List<Integer> uniqueSlots = new ArrayList<>(new HashSet<>(combinedSlots));
            Collections.sort(uniqueSlots); // Sort the combined slots
            combInstructor.setAvailability(day, uniqueSlots);
        }
    
        // Add the combined instructor to the Instructors map
        Instructors.put(combInstructor.getName(), combInstructor);
    
        // Return the combined instructor's name
        return combInstructor;
    }
    

}