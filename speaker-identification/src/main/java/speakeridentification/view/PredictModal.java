package speakeridentification.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import lombok.extern.slf4j.Slf4j;
import speakeridentification.model.deeplearning.NeuralNetworkHolder;
import speakeridentification.model.service.TrainingService;

@Slf4j
public class PredictModal extends JDialog {

    private TrainingService service;
    private Map<String, String> profileNames;
    private JTextField predictedText;
    private JTextField actualText;
    private ProbabilityInputField tresholdField;
    private JLabel allNum;
    private JLabel correctNum;
    private JLabel incorrectNum;
    private JLabel uncertainNum;
    private List<JButton> activeProfileButtons;

    public PredictModal(TrainingService service) {
        super((Frame) null, "Predictions", true);
        this.service = service;
        setMinimumSize(new Dimension(600, 300));
        URL iconURL = Thread.currentThread().getContextClassLoader().getResource("logo.png");
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());
        activeProfileButtons = new ArrayList<>();
        initData();
        createGUI();
    }

    private void initData() {
        profileNames = service.getProfileNamesFromModel();
    }

    private void createGUI() {
        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setBorder(new EmptyBorder(10, 20, 20, 20));
        contentPane.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.setBorder(new EmptyBorder(5, 5, 20, 5));
        contentPane.add(topPanel, BorderLayout.NORTH);
        JLabel topLabel = new JLabel("Load a new spectrogram file for prediction by clicking on one of the profile name buttons!");
        topPanel.add(topLabel);

        JPanel tresholdPanel = new JPanel();
        tresholdPanel.setLayout(new BoxLayout(tresholdPanel, BoxLayout.LINE_AXIS));
        tresholdField = new ProbabilityInputField();
        tresholdField.setMaximumSize(new Dimension(60,30));
        tresholdField.setMinimumSize(new Dimension(60,30));
        tresholdPanel.add(new JLabel("Acceptance treshold:"));
        tresholdPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        tresholdPanel.add(tresholdField);
        tresholdPanel.add(Box.createRigidArea(new Dimension(5, 10)));
        tresholdPanel.add(new JLabel("%"));
        tresholdPanel.add(Box.createRigidArea(new Dimension(10,20)));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));
        contentPane.add(mainPanel);

        JPanel profileButtonPanel = new JPanel();
        profileButtonPanel.setLayout(new BoxLayout(profileButtonPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(profileButtonPanel);

        for (var entry : profileNames.entrySet()) {
            JButton button = new JButton(entry.getValue());
            button.setActionCommand(entry.getKey());
            button.addActionListener(this::profileButtonClicked);
            profileButtonPanel.add(button);
            profileButtonPanel.add(Box.createRigidArea(new Dimension(10, 10)));
            activeProfileButtons.add(button);
        }
        profileButtonPanel.add(Box.createVerticalGlue());

        JPanel predictionsPanel = new JPanel();
        predictionsPanel.setLayout(new BoxLayout(predictionsPanel, BoxLayout.PAGE_AXIS));
        predictionsPanel.setBorder(new EmptyBorder(0,40,0,20));
        mainPanel.add(predictionsPanel);

        JPanel line1 = new JPanel();
        JPanel line2 = new JPanel();
        JPanel line3 = new JPanel();
        JPanel line4 = new JPanel();
        line1.setLayout(new BoxLayout(line1, BoxLayout.LINE_AXIS));
        line2.setLayout(new BoxLayout(line2, BoxLayout.LINE_AXIS));
        line3.setLayout(new BoxLayout(line3, BoxLayout.LINE_AXIS));
        line4.setLayout(new BoxLayout(line4, BoxLayout.LINE_AXIS));
        predictionsPanel.add(tresholdPanel);
        predictionsPanel.add(Box.createRigidArea(new Dimension(20,20)));
        predictionsPanel.add(line1);
        predictionsPanel.add(Box.createRigidArea(new Dimension(20,5)));
        predictionsPanel.add(line2);
        predictionsPanel.add(Box.createRigidArea(new Dimension(20,20)));
        predictionsPanel.add(line3);
        predictionsPanel.add(Box.createRigidArea(new Dimension(20,5)));
        predictionsPanel.add(line4);
        predictionsPanel.add(Box.createVerticalGlue());

        JLabel predictedLabel = new JLabel("Predicted class:");
        predictedText = new JTextField();
        predictedText.setMaximumSize(new Dimension(200, 40));
        predictedText.setMinimumSize(new Dimension(200, 40));
        predictedText.setEditable(false);

        JLabel actualLabel = new JLabel("Actual class:");
        actualText = new JTextField();
        actualText.setMaximumSize(new Dimension(200, 40));
        actualText.setMinimumSize(new Dimension(200, 40));
        actualText.setEditable(false);

        line1.add(predictedLabel);
        line1.add(Box.createHorizontalGlue());
        line2.add(predictedText);
        line3.add(actualLabel);
        line3.add(Box.createHorizontalGlue());
        line4.add(actualText);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.PAGE_AXIS));
        Border border = BorderFactory.createCompoundBorder(
            new TitledBorder(new LineBorder(Color.lightGray),"Statistics"),
            new EmptyBorder(10, 10, 10, 10));
        statsPanel.setBorder(border);
        statsPanel.setBackground(Color.white);
        mainPanel.add(statsPanel);
        JPanel line11 = new JPanel();
        JPanel line22 = new JPanel();
        JPanel line33 = new JPanel();
        JPanel line44 = new JPanel();
        line11.setBackground(Color.white);
        line22.setBackground(Color.white);
        line33.setBackground(Color.white);
        line44.setBackground(Color.white);
        line11.setLayout(new BoxLayout(line11, BoxLayout.LINE_AXIS));
        line22.setLayout(new BoxLayout(line22, BoxLayout.LINE_AXIS));
        line33.setLayout(new BoxLayout(line33, BoxLayout.LINE_AXIS));
        line44.setLayout(new BoxLayout(line44, BoxLayout.LINE_AXIS));

        JLabel allLabel = new JLabel("All predictions:");
        JLabel correctLabel = new JLabel("Correct predictions:");
        JLabel incorrectLabel = new JLabel("Incorrect predictions:");
        JLabel uncertainLabel = new JLabel("Uncertain predictions:");
        allNum = new JLabel("0");
        correctNum = new JLabel("0");
        incorrectNum = new JLabel("0");
        uncertainNum = new JLabel("0");

        line11.add(allLabel);
        line11.add(Box.createRigidArea(new Dimension(61,20)));
        line11.add(allNum);
        line11.add(Box.createHorizontalGlue());
        line22.add(correctLabel);
        line22.add(Box.createRigidArea(new Dimension(32,20)));
        line22.add(correctNum);
        line22.add(Box.createHorizontalGlue());
        line33.add(incorrectLabel);
        line33.add(Box.createRigidArea(new Dimension(23,20)));
        line33.add(incorrectNum);
        line33.add(Box.createHorizontalGlue());
        line44.add(uncertainLabel);
        line44.add(Box.createRigidArea(new Dimension(20,20)));
        line44.add(uncertainNum);
        line44.add(Box.createHorizontalGlue());

        statsPanel.add(line11);
        statsPanel.add(line22);
        statsPanel.add(line33);
        statsPanel.add(line44);
    }

    private void profileButtonClicked(ActionEvent e) {
        setEnabledAllInput(false);
        String profileName = profileNames.get(e.getActionCommand());
        try {
            String nextPrediction = service.getNextPrediction(e.getActionCommand(), tresholdField.getValue() / 100);
            predictedText.setText(nextPrediction);
            actualText.setText(profileName);
            incrementCounter(allNum);
            if (nextPrediction.equals(profileName)) {
                incrementCounter(correctNum);
            } else if (nextPrediction.equals(NeuralNetworkHolder.UNCERTAIN)) {
                incrementCounter(uncertainNum);
            } else {
                incrementCounter(incorrectNum);
            }
        } catch (NoSuchElementException ex) {
            showInfoMessage("No more samples for " + profileName);
            JButton button = (JButton)e.getSource();
            activeProfileButtons.remove(button);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            showErrorMessage("Prediction failed");
        } finally {
            setEnabledAllInput(true);
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void incrementCounter(JLabel label) {
        int value = Integer.parseInt(label.getText());
        label.setText(String.valueOf(value + 1));
    }

    private void setEnabledAllInput(boolean enabled) {
        for (JButton b : activeProfileButtons) {
            b.setEnabled(enabled);
        }
        tresholdField.setEnabled(enabled);
    }
}
