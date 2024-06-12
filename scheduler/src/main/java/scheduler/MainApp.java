package scheduler;

import javax.swing.SwingUtilities;

public class MainApp { // Run this to start main program
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileSelectorWindow fileSelectorWindow = new FileSelectorWindow();
            fileSelectorWindow.setVisible(true);
        });
    }
}
