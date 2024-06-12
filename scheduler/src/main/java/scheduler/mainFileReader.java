package scheduler;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.SwingUtilities;

public class mainFileReader {
    public static void main(String[] args) throws IOException {

        FileReader fileReader = new FileReader("scheduler/data/Spring-courses-2.xlsx");

        courseScheduler scheduler = new courseScheduler();

        Map<UUID, course> coursesMap = fileReader.readCoursesFromSheet();


        for(course course: fileReader.courseQueue){
            scheduler.enqueueCourse(course);
        }

        //scheduler.viewCourses();

        scheduler.ScheduleCourses(false);
        scheduler.displaySchedule();
        scheduler.outputExcel();

        SwingUtilities.invokeLater(() -> {
            CourseSchedulerGUI gui = new CourseSchedulerGUI(scheduler);
            gui.setVisible(true);
        });

    }
}
