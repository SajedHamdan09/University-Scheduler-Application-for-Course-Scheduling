package scheduler;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CourseSchedulerGUI extends JFrame {

    private courseScheduler scheduler;
    private JButton[][] scheduleButtons;
    private JPanel schedulePanel;
    private JPanel[] rows;
    private JPanel courseListPanel;
    // boreders
    Border border1 = BorderFactory.createLineBorder(Color.gray, 1);
    Border border2 = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1);

    public CourseSchedulerGUI(courseScheduler scheduler) {
        this.scheduler = scheduler;
        this.setTitle("Course Scheduler");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        schedulePanel = new JPanel(new GridLayout(9, 5));
        courseListPanel = new JPanel(new GridLayout(0, 1));

        // logo image
        ImageIcon logo = new ImageIcon("scheduler/data/logo1.png");

        // logo label
        JLabel logo_label = new JLabel();
        logo_label.setIcon(logo);
        logo_label.setFont(new Font("Comic Sans", Font.BOLD, 20));
        logo_label.setBackground(Color.gray);
        logo_label.setOpaque(true);
        logo_label.setHorizontalAlignment(JLabel.CENTER);
        logo_label.setVerticalTextPosition(JLabel.CENTER);
        logo_label.setBorder(border1);

        scheduleButtons = new JButton[scheduler.days][scheduler.timeslots];

        for (int i = 0; i < scheduler.timeslots; i++) {

            for (int j = 0; j < scheduler.days; j++) {
                scheduleButtons[j][i] = new JButton("");
                scheduleButtons[j][i].setFocusable(false);
                scheduleButtons[j][i].setFont(new Font("Comic Sans", Font.PLAIN, 10));
                scheduleButtons[j][i].setBackground(Color.LIGHT_GRAY);
                scheduleButtons[j][i].setBorder(border1);
                scheduleButtons[j][i].addActionListener(new ScheduleButtonListener(i, j));
            }
        }

        // array
        String[] labels_string = { "By ASF", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                "08:00", "09:30", "11:00", "13:00", "14:00", "16:00" };

        // days and hours(labels)
        JLabel[] labels = new JLabel[12];
        JLabel[] days = new JLabel[5];
        JLabel[] time_slots = new JLabel[6];
        for (int i = 0; i < 12; i++) {
            JLabel label = new JLabel();
            label.setFont(new Font("Comic Sans", Font.BOLD, 20));
            label.setOpaque(true);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalTextPosition(JLabel.CENTER);
            label.setOpaque(true);

            // if days
            if (i == 0) {
                label.setBackground(Color.gray);
                label.setBorder(border2);
            } else if (i < 6) {
                label.setBackground(new Color(0x991D02));
                label.setBorder(border1);
                days[i - 1] = label;
            } else {
                label.setBackground(Color.gray);
                label.setBorder(border2);
                time_slots[i - 6] = label;
            }
            label.setText(labels_string[i]);
            labels[i] = label;
        }

        // last row
        JButton all_courses = new JButton("All Courses");
        all_courses.setFocusable(false);
        all_courses.setFont(new Font("Comic Sans", Font.BOLD, 20));
        all_courses.setBackground(Color.gray);
        all_courses.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                courseListPanel.removeAll();
                for (UUID courseUUID : scheduler.AllCoursesQueue) {
                    JPanel coursePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    JButton course_button = new JButton();
                    course_button.setText(scheduler.courseMap.get(courseUUID).getCourseID() + "-"
                            + scheduler.courseMap.get(courseUUID).getLectureNumber()
                            + " by " + scheduler.courseMap.get(courseUUID).getInstructor().getName());
                    course_button.setFocusable(false);
                    course_button.setFont(new Font("Comic Sans", Font.PLAIN, 10));
                    course_button.setBackground(Color.gray);
                    course_button.setForeground(Color.BLACK);
                    course_button.addActionListener(new CourseButtonListener(courseUUID));
                    coursePanel.add(course_button);
                    courseListPanel.add(coursePanel);
                }
                for (int i = 0; i < scheduler.timeslots; i++) {
                    for (int j = 0; j < scheduler.days; j++) {
                        scheduleButtons[j][i].setFocusable(false);
                        scheduleButtons[j][i].setFont(new Font("Comic Sans", Font.PLAIN, 10));
                        scheduleButtons[j][i].setBackground(Color.LIGHT_GRAY);
                        scheduleButtons[j][i].setBorder(border1);

                    }
                }

                courseListPanel.revalidate();
                courseListPanel.repaint();

            }
        });

        JButton not_scheduled = new JButton("Unscheduled Courses");
        not_scheduled.setFocusable(false);
        not_scheduled.setFont(new Font("Comic Sans", Font.BOLD, 20));
        not_scheduled.setBackground(Color.gray);
        not_scheduled.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                courseListPanel.removeAll();
                JLabel no_place = new JLabel("NO PLACE FOUND:");
                no_place.setFont(new Font("Comic Sans", Font.BOLD, 20));
                no_place.setBackground(Color.darkGray);
                JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                panel1.add(no_place);
                courseListPanel.add(panel1);
                for (UUID uuid : scheduler.unscheduledCourseHeap) {

                    JLabel label = new JLabel(scheduler.courseMap.get(uuid).getCourseID() + "-"
                            + scheduler.courseMap.get(uuid).getLectureNumber() + " "
                            + scheduler.courseMap.get(uuid).getOtherInfo());
                    label.setFont(new Font("Comic Sans", Font.BOLD, 20));
                    label.setBackground(Color.gray);
                    JPanel coursePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    coursePanel.add(label);
                    courseListPanel.add(coursePanel);
                }
                JLabel border = new JLabel();
                border.setBackground(Color.gray);
                courseListPanel.add(border);
                JLabel no_sessions = new JLabel("NO SESSIONS NEEDED:");
                no_sessions.setFont(new Font("Comic Sans", Font.BOLD, 20));
                no_sessions.setBackground(Color.darkGray);
                JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                panel2.add(no_sessions);
                courseListPanel.add(panel2);
                for (UUID uuid : scheduler.zeroSessionCourses) {
                    JLabel label = new JLabel(scheduler.courseMap.get(uuid).getCourseID());
                    label.setFont(new Font("Comic Sans", Font.BOLD, 20));
                    label.setBackground(Color.gray);
                    JPanel coursePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    coursePanel.add(label);
                    courseListPanel.add(coursePanel);
                }
                JLabel border_two = new JLabel();
                border_two.setBackground(Color.gray);
                courseListPanel.add(border_two);
                JLabel error_course = new JLabel("ERROR FROM INFO:");
                error_course.setFont(new Font("Comic Sans", Font.BOLD, 20));
                error_course.setBackground(Color.darkGray);
                JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                panel3.add(error_course);
                courseListPanel.add(panel3);
                for (UUID uuid : scheduler.errorCourses) {
                    JLabel label = new JLabel(scheduler.courseMap.get(uuid).getCourseID() + "-"
                            + scheduler.courseMap.get(uuid).getLectureNumber() + " "
                            + scheduler.courseMap.get(uuid).getOtherInfo());
                    label.setFont(new Font("Comic Sans", Font.BOLD, 20));
                    label.setBackground(Color.gray);
                    JPanel coursePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    coursePanel.add(label);
                    courseListPanel.add(coursePanel);
                }

                courseListPanel.revalidate();
                courseListPanel.repaint();

            }

        }

        );

        rows = new JPanel[9];

        for (int i = 0; i < 9; i++) {
            JPanel panel = new JPanel();
            panel.setBackground(Color.LIGHT_GRAY);
            if (i == 0) {
                panel.setBackground(Color.gray);
                panel.setBorder(border2);
                panel.add(logo_label);
            } else if (i == 1) {
                panel.setLayout(new GridLayout(1, 6 + scheduler.days));
                panel.add(labels[0]);
                for (JLabel l : days) {
                    panel.add(l);
                }
            } else if (i == 8) {
                panel.setLayout(new GridLayout(1, 2));
                panel.add(all_courses);
                panel.add(not_scheduled);

            } else {
                panel.setLayout(new GridLayout(1, 6 + scheduler.days));
                panel.add(time_slots[i - 2]);
                for (int j = 0; j < 5; j++) {
                    panel.add(scheduleButtons[j][i - 2]);
                }
            }
            rows[i] = panel;
        }

        for (JPanel p : rows) {
            schedulePanel.add(p);
        }

        updateScheduleGrid();

        JScrollPane scheduleScrollPane = new JScrollPane(schedulePanel);
        scheduleScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scheduleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollPane listJScrollPane = new JScrollPane(courseListPanel);
        listJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.setSize(750, 750);

        this.add(scheduleScrollPane, BorderLayout.CENTER);
        this.add(listJScrollPane, BorderLayout.EAST);
        // this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    private void updateScheduleGrid() {
        List<UUID>[][] schedule = scheduler.getSchedule();

        for (int i = 0; i < scheduler.timeslots; i++) {
            for (int j = 0; j < scheduler.days; j++) {
                List<UUID> coursesInSlot = schedule[j][i];
                if (coursesInSlot.isEmpty()) {
                    scheduleButtons[j][i].setText("No courses");
                } else {
                    StringBuilder buttonText = new StringBuilder("<html>");

                    for (UUID courseUUID : coursesInSlot) {
                        buttonText.append(scheduler.getCourse(courseUUID).toString()).append("<br>");
                    }

                    buttonText.append("</html>");
                    scheduleButtons[j][i].setText(buttonText.toString());
                }
            }
        }
    }

    private class ScheduleButtonListener implements ActionListener {
        private int timeSlotIndex;
        private int dayIndex;

        public ScheduleButtonListener(int timeSlotIndex, int dayIndex) {
            this.timeSlotIndex = timeSlotIndex;
            this.dayIndex = dayIndex;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<UUID> coursesInSlot = scheduler.getSchedule()[dayIndex][timeSlotIndex];
            courseListPanel.removeAll();
            for (UUID courseUUID : coursesInSlot) {
                course course = scheduler.getCourse(courseUUID);
                JButton courseButton = new JButton(
                        course.getCourseID() + "-" + course.getLectureNumber() + " " + course.getOtherInfo());
                courseButton.setFocusable(false);
                courseButton.setFont(new Font("Comic Sans", Font.PLAIN, 10));
                courseButton.setBackground(Color.gray);
                courseButton.setForeground(Color.BLACK);
                courseButton.setBorder(border2);
                courseButton.addActionListener(new CourseButtonListener(courseUUID));
                courseListPanel.add(courseButton);
            }
            for (int i = 0; i < scheduler.timeslots; i++) {
                for (int j = 0; j < scheduler.days; j++) {
                    scheduleButtons[j][i].setFocusable(false);
                    scheduleButtons[j][i].setFont(new Font("Comic Sans", Font.PLAIN, 10));
                    scheduleButtons[j][i].setBackground(Color.LIGHT_GRAY);
                    scheduleButtons[j][i].setBorder(border1);

                }
            }

            courseListPanel.revalidate();
            courseListPanel.repaint();
        }
    }

    private class CourseButtonListener implements ActionListener {
        private UUID courseUUID;

        public CourseButtonListener(UUID courseUUID) {
            this.courseUUID = courseUUID;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Highlight the time slots for this course
            course course = scheduler.getCourse(courseUUID);
            boolean[][] isScheduled = course.getIsScheduledArray();

            for (int i = 0; i < scheduler.timeslots; i++) {
                for (int j = 0; j < scheduler.days; j++) {
                    if (isScheduled[j][i]) {
                        scheduleButtons[j][i].setBackground(Color.GREEN);
                    } else {
                        scheduleButtons[j][i].setFocusable(false);
                        scheduleButtons[j][i].setFont(new Font("Comic Sans", Font.PLAIN, 10));
                        scheduleButtons[j][i].setBackground(Color.LIGHT_GRAY);
                        scheduleButtons[j][i].setBorder(border1);
                    }
                }
            }
        }
    }

}
