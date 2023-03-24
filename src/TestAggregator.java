// Copyright (c) 2023 Matyas Urban. Licensed under the MIT license.

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main TestAggregator class that builds the layout and controls user interaction
 */
public class TestAggregator {
    private JFrame frame;
    private JTextArea inputArea;

    /**
     * Class constructor
     */
    public TestAggregator() {
        createAndShowGUI();
    }

    /**
     * Builds the layout of the main frame
     */
    private void createAndShowGUI() {
        // frame setup
        frame = new JFrame("Test Aggregator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(400, 200));
        frame.setLayout(new GridBagLayout());

        // grid setup
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        // first button: to aggregate test results from a file
        constraints.weighty = 0;
        JButton fileButton = new JButton("Aggregate test results from file");
        fileButton.addActionListener(new FileSelectActionListener());
        frame.add(fileButton, constraints);

        // subtle visual divider
        constraints.gridy = 1;
        JSeparator separator = new JSeparator();
        frame.add(separator, constraints);

        // input field: to enter/paste test results, scrollable in case of large input
        constraints.gridy = 2;
        inputArea = new JTextArea(5, 20);
        inputArea.setLineWrap(false); // Disable line wrapping
        JScrollPane scrollPane = new JScrollPane(inputArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Enable horizontal scroll
        constraints.fill = GridBagConstraints.BOTH; // Fill the available space in both directions
        constraints.weighty = 1.0; // Set the weighty to 1 for the JScrollPane
        frame.add(scrollPane, constraints);

        // second button: to aggregate test results from the input field
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 0;
        JButton inputButton = new JButton("Aggregate test results from input");
        inputButton.addActionListener(new InputAggregateActionListener());
        frame.add(inputButton, constraints);

        // third button: to fill the input field with random data (to test out the functionality of this application)
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 0; // Reset the weighty to 0 for the third button
        JButton fillInputButton = new JButton("Fill input with random tests");
        fillInputButton.addActionListener(new FillInputActionListener()); // Add the ActionListener to the button
        frame.add(fillInputButton, constraints);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Action listener for the button first button (Select a file and aggregate entailed test data)
     */
    class FileSelectActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // let the user select a file using native a GUI file explorer
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    String fileContent = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
                    String mimeType = Files.probeContentType(selectedFile.toPath());

                    // proceed if it is a text file
                    if (mimeType != null && mimeType.startsWith("text/")) {
                        displayResults(fileContent);
                    } else {
                    JOptionPane.showMessageDialog(frame, "Only text files are allowed.", "Invalid File Type", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "An error occurred while reading the file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Action listener for the second button (Aggregate data from the input field)
     */
    class InputAggregateActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // take the text from input field and proceed if it is not empty
            String inputContent = inputArea.getText();
            if (!inputContent.trim().isEmpty()) {
                displayResults(inputContent);
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter some text in the input field.", "Empty Input", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Action listener for the third button (Fill in the input field with random test data)
     */
    class FillInputActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String randomTestsString = TestResults.getRandomTestsString();
            inputArea.setText(randomTestsString);
        }
    }

    /**
     * Function that displays pane with test results
     * @param testData Test data (from file or input area)
     */
    public void displayResults(String testData){
        // run statistics on the data
        TestResults testResults = new TestResults(testData);

        // create a visual representation for successful vs failed tests and add it to the panel
        ColoredBarPanel coloredBar = new ColoredBarPanel(testResults.passRate);
        coloredBar.setPreferredSize(new Dimension(200, 20));
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(coloredBar, BorderLayout.NORTH);

        // add statistics to the panel
        JTextArea textArea = new JTextArea(testResults.aggregatedString);
        textArea.setEditable(false);
        panel.add(textArea, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(frame, panel, "Test Results", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new TestAggregator();
    }

}
