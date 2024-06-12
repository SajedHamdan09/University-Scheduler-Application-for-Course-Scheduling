package scheduler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;
import java.util.UUID;

public class FileSelectorWindow extends JFrame {
    private JFileChooser fileChooser;
    private JCheckBox isSummerCheckBox;

    public FileSelectorWindow() {
        setTitle("File Selector");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        isSummerCheckBox = new JCheckBox("Summer?");

        JButton scheduleButton = new JButton("Schedule");
        scheduleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null && selectedFile.exists()) {
                    boolean isSummer = isSummerCheckBox.isSelected(); // Get the checkbox value

                    FileReader fileReader = new FileReader(selectedFile.getAbsolutePath());
                    courseScheduler scheduler = new courseScheduler();

                    Map<UUID, course> coursesMap = fileReader.readCoursesFromSheet();

                    for (course course : fileReader.courseQueue) {
                        scheduler.enqueueCourse(course);
                    }

                    // scheduler.viewCourses();

                    scheduler.ScheduleCourses(isSummer);
                    scheduler.displaySchedule();
                    scheduler.outputExcel();
                    System.out.println("read courses to schedule = " + fileReader.countCoursesToScheduled);
                    System.out.println(
                            "scheduled courses count(not coutning diff lectures) = " + scheduler.countScheduledCourses);

                    SwingUtilities.invokeLater(() -> {
                        CourseSchedulerGUI gui = new CourseSchedulerGUI(scheduler);
                        gui.setVisible(true);
                    });

                    // Close the file selector window
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(FileSelectorWindow.this,
                            "Please select a valid file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JPanel mainPanel = new JPanel();
        mainPanel.add(fileChooser);
        mainPanel.add(isSummerCheckBox); // Add the checkbox to the mainPanel
        mainPanel.add(scheduleButton);
        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileSelectorWindow fileSelectorWindow = new FileSelectorWindow();
            fileSelectorWindow.setVisible(true);
        });
    }
}
