package speakeridentification.view;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;

import lombok.extern.slf4j.Slf4j;
import speakeridentification.persistence.domain.SpeakerType;
import speakeridentification.model.exceptions.InvalidInputException;
import speakeridentification.model.exceptions.ModelStateException;
import speakeridentification.model.service.TrainingService;
import speakeridentification.model.data.ProfileData;
import speakeridentification.persistence.domain.Settings;
import speakeridentification.persistence.exceptions.PersistenceException;

@Slf4j
public class TrainingPanel extends JPanel {

    public static final int DEFAULT_SPINNER_MINIMUM = 2;
    public static final String NO_MODEL = "[ no model ]";
    public static final String MODEL_READY = "Model ready for use";
    public static final String MODEL_NOT_TRAINED = "Model needs training";
    public static final String PROCESSING = "Processing data...";

    private TrainingService service;
    private MainWindow parent;
    private List<String> modelNames;
    private List<ProfileData> profiles = new ArrayList<>();
    private List<ProfileData> profilesToUse = new ArrayList<>();
    private String modelToUse;
    private JSpinner profileNumberChooser;
    private JList<ProfileData> profileChooser;
    private JSpinner modelChooser;
    private JList<ProfileData> chosenProfilesList;
    private DefaultListModel<ProfileData> listModelTo;
    private DefaultListModel<ProfileData> listModelFrom;
    private SpinnerNumberModel profileSpinnerModel;
    private ButtonGroup filterButtons;
    private SpeakerType activeFilter;
    private JRadioButton noFilterButton;
    private JSpinner audioNumberChooser;
    private JLabel statusLabel;
    private JButton trainButton;
    private JButton predictButton;
    private JButton resetButton;

    public TrainingPanel(TrainingService service, MainWindow parent) {
        this.service = service;
        this.parent = parent;
        this.setBorder(new EmptyBorder(10, 30, 30, 30));
        initData();
        createGUI();
        loadLastSaveIfExists();
    }

    private void initData() {
        modelNames = new ArrayList<>();
        modelNames.add(NO_MODEL);
        modelNames.addAll(service.listModels());
        profiles = service.findAllProfiles();
    }

    private void createGUI() {
        setLayout(new BorderLayout());
        JPanel settingsPanel = new JPanel();
        JPanel buttonsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS));
        settingsPanel.setBorder(new TitledBorder("Training settings"));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        add(settingsPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        JPanel line1 = new JPanel();
        JPanel line2 = new JPanel();
        JPanel line3 = new JPanel();
        JPanel line4 = new JPanel();
        JPanel line5 = new JPanel();
        line1.setLayout(new BoxLayout(line1, BoxLayout.LINE_AXIS));
        line2.setLayout(new BoxLayout(line2, BoxLayout.LINE_AXIS));
        line3.setLayout(new BoxLayout(line3, BoxLayout.LINE_AXIS));
        line4.setLayout(new BoxLayout(line4, BoxLayout.LINE_AXIS));
        line5.setLayout(new BoxLayout(line5, BoxLayout.LINE_AXIS));
        settingsPanel.add(line1);
        settingsPanel.add(line2);
        settingsPanel.add(line3);
        settingsPanel.add(line4);
        settingsPanel.add(line5);

        line1.setMaximumSize(new Dimension(900, 40));
        line2.setMaximumSize(new Dimension(900, 40));
        line3.setMaximumSize(new Dimension(900, 40));
        line4.setMaximumSize(new Dimension(900, 40));
        line5.setMaximumSize(new Dimension(900, 200));

        JLabel modelChoserLabel = new JLabel("Pretrained model");
        modelChooser = new JSpinner();
        modelChooser.setModel(new SpinnerCircularListModel(modelNames));
        modelChooser.setMaximumSize(new Dimension(200, 30));
        line1.add(modelChoserLabel);
        line1.add(Box.createRigidArea(new Dimension(20, 40)));
        line1.add(modelChooser);

        JLabel profileNumberLabel = new JLabel("Number of profiles");
        profileSpinnerModel = new SpinnerNumberModel(3, DEFAULT_SPINNER_MINIMUM, 5, 1);
        profileNumberChooser = new JSpinner(profileSpinnerModel);
        profileNumberChooser.setMaximumSize(new Dimension(50, 30));
        JLabel audioNumberLabel = new JLabel("Number of audio samples / profile");
        SpinnerNumberModel audioSpinnerModel = new SpinnerNumberModel(6, 6, 12, 1);
        audioNumberChooser = new JSpinner(audioSpinnerModel);
        audioNumberChooser.setMaximumSize(new Dimension(50, 30));
        line2.add(profileNumberLabel);
        line2.add(Box.createRigidArea(new Dimension(12, 40)));
        line2.add(profileNumberChooser);
        line2.add(Box.createRigidArea(new Dimension(50, 40)));
        line2.add(audioNumberLabel);
        line2.add(Box.createRigidArea(new Dimension(12, 40)));
        line2.add(audioNumberChooser);

        JLabel profileChooserLabel = new JLabel("Profiles to use");
        line3.add(profileChooserLabel);

        JLabel filtersLabel = new JLabel("Filter by:");
        noFilterButton = new JRadioButton();
        noFilterButton.setSelected(true);
        JRadioButton maleFilterButton = new JRadioButton();
        JRadioButton femaleFilterButton = new JRadioButton();
        JRadioButton childFilterButton = new JRadioButton();
        noFilterButton.addActionListener((ActionEvent e) -> setFilter());
        maleFilterButton.addActionListener((ActionEvent e) -> setFilter());
        femaleFilterButton.addActionListener((ActionEvent e) -> setFilter());
        childFilterButton.addActionListener((ActionEvent e) -> setFilter());
        maleFilterButton.setActionCommand("male");
        femaleFilterButton.setActionCommand("female");
        childFilterButton.setActionCommand("child");
        filterButtons = new ButtonGroup();
        filterButtons.add(noFilterButton);
        filterButtons.add(maleFilterButton);
        filterButtons.add(femaleFilterButton);
        filterButtons.add(childFilterButton);
        line4.add(filtersLabel);
        line4.add(Box.createRigidArea(new Dimension(25, 40)));
        line4.add(new JLabel("no filter"));
        line4.add(noFilterButton);
        line4.add(Box.createRigidArea(new Dimension(15, 40)));
        line4.add(new JLabel("male"));
        line4.add(maleFilterButton);
        line4.add(Box.createRigidArea(new Dimension(15, 40)));
        line4.add(new JLabel("female"));
        line4.add(femaleFilterButton);
        line4.add(Box.createRigidArea(new Dimension(15, 40)));
        line4.add(new JLabel("child"));
        line4.add(childFilterButton);

        JPanel leftColumn = new JPanel();
        JPanel rightColumn = new JPanel();
        leftColumn.setMaximumSize(new Dimension(400, 200));
        rightColumn.setMaximumSize(new Dimension(400, 200));
        line5.add(Box.createRigidArea(new Dimension(70,50)));
        line5.add(leftColumn);
        line5.add(Box.createRigidArea(new Dimension(30,50)));
        line5.add(rightColumn);

        leftColumn.setLayout(new BorderLayout());
        listModelFrom = new DefaultListModel<>();
        listModelFrom.addAll(profiles);
        profileChooser = new JList<>();
        profileChooser.setModel(listModelFrom);
        profileChooser.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        profileChooser.setBorder(new TitledBorder("Select profiles to use (click to add items to selection):"));
        profileChooser.addListSelectionListener(this::addProfileToSelected);
        JScrollPane scroll = new JScrollPane(profileChooser);
        leftColumn.add(scroll);

        rightColumn.setLayout(new BorderLayout());
        listModelTo = new DefaultListModel<>();
        chosenProfilesList = new JList<>(listModelTo);
        profileChooser.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        chosenProfilesList.setBorder(new TitledBorder("Selected (click to remove items):"));
        chosenProfilesList.addListSelectionListener(this::removeProfileFromSelected);
        rightColumn.add(chosenProfilesList);

        trainButton = new JButton("Train");
        predictButton = new JButton("Predict");
        resetButton = new JButton("Reset model");
        trainButton.addActionListener((ActionEvent e) -> onTrainClicked());
        predictButton.addActionListener((ActionEvent e) -> onPredictClicked());
        resetButton.addActionListener((ActionEvent e) -> onResetClicked());
        statusLabel = new JLabel("");
        buttonsPanel.add(trainButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(20, 40)));
        buttonsPanel.add(predictButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(20, 40)));
        buttonsPanel.add(resetButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(20, 40)));
        buttonsPanel.add(statusLabel);

    }

    private void loadLastSaveIfExists() {
        try {
            if (service.lastSaveExists()) {
                Settings settings = service.loadLastSettings();
                modelChooser.setValue(settings.getModelToUse());
                profileNumberChooser.setValue(settings.getProfilesMap().size());
                audioNumberChooser.setValue(settings.getNumAudio());
                listModelTo.addAll(profiles.stream()
                    .filter(p -> settings.getProfilesMap().containsValue(p.getName())).collect(Collectors.toList()));
                enableInputForTrainedState();
            } else {
                enableInputForUntrainedState();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            showErrorMessage("Failed to load saved settings");
            service.reset();
            enableInputForUntrainedState();
        }
    }

    private void enableInputForTrainedState() {
        modelChooser.setEnabled(false);
        profileNumberChooser.setEnabled(false);
        audioNumberChooser.setEnabled(false);
        profileChooser.setEnabled(false);
        chosenProfilesList.setEnabled(false);
        trainButton.setEnabled(false);
        predictButton.setEnabled(true);
        resetButton.setEnabled(true);
        statusLabel.setText(MODEL_READY);
    }

    private void enableInputForUntrainedState() {
        modelChooser.setEnabled(true);
        profileNumberChooser.setEnabled(true);
        audioNumberChooser.setEnabled(true);
        profileChooser.setEnabled(true);
        chosenProfilesList.setEnabled(true);
        trainButton.setEnabled(true);
        predictButton.setEnabled(false);
        resetButton.setEnabled(false);
        statusLabel.setText(MODEL_NOT_TRAINED);
    }

    private void setFilter() {
        String command = filterButtons.getSelection().getActionCommand();
        if (command != null) {
            switch (command) {
                case "male": activeFilter=SpeakerType.MALE; break;
                case "female": activeFilter=SpeakerType.FEMALE; break;
                case "child": activeFilter=SpeakerType.CHILD; break;
            }
        } else {
            activeFilter = null;
        }
        filterProfiles();
    }

    private void filterProfiles() {
        listModelFrom.removeAllElements();
        if (activeFilter==null) {
            listModelFrom.addAll(profiles);
        } else {
            listModelFrom.addAll(profiles.stream()
                .filter(p -> p.getType().equals(activeFilter)).collect(Collectors.toList()));
        }
    }

    private void addProfileToSelected(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            int selectedIndex = profileChooser.getSelectedIndex();
            if (selectedIndex > -1) {
                ProfileData selected = profileChooser.getModel().getElementAt(selectedIndex);
                boolean canBeAdded = listModelTo.getSize() < (Integer) profileNumberChooser.getValue() && !listModelTo.contains(selected);
                if (canBeAdded) {
                    listModelTo.addElement(selected);
                    checkSpinnerMinimum();
                }
            }
        }
    }

    private void removeProfileFromSelected(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            int selectedIndex = chosenProfilesList.getSelectedIndex();
            if (selectedIndex!=-1) {
                listModelTo.removeElementAt(selectedIndex);
                checkSpinnerMinimum();
            }
        }
    }

    private void checkSpinnerMinimum() {
        profileSpinnerModel.setMinimum(Math.max(listModelTo.size(), DEFAULT_SPINNER_MINIMUM));
    }

    private void onResetClicked() {
        service.reset();
        resetGui();
        enableInputForUntrainedState();
    }

    private void resetGui() {
        modelChooser.setValue(NO_MODEL);
        profileNumberChooser.setValue(3);
        audioNumberChooser.setValue(6);
        noFilterButton.setSelected(true);
        listModelTo.removeAllElements();
        listModelFrom.removeAllElements();
        listModelFrom.addAll(profiles);
        statusLabel.setText(MODEL_NOT_TRAINED);
        profilesToUse = new ArrayList<>();
        modelToUse = null;
        checkSpinnerMinimum();
    }

    private void onPredictClicked() {
        service.setupModelForPrediction();
        PredictModal modal = new PredictModal(service);
        modal.setLocationRelativeTo(this);
        modal.setVisible(true);
    }

    private void onTrainClicked() {
        disableAll();
        modelToUse = (String) modelChooser.getValue();
        for (Object item : listModelTo.toArray()) {
            profilesToUse.add((ProfileData) item);
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override protected Void doInBackground() {
                service.train(modelToUse, profilesToUse, (Integer) profileNumberChooser.getValue(), (Integer) audioNumberChooser.getValue());
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    enableInputForTrainedState();
                } catch (InterruptedException ex) {
                    //ignore
                } catch (ExecutionException ex) {
                    log.error(ex.getMessage(), ex);
                    Throwable cause = ex.getCause();
                    if (cause instanceof InvalidInputException || cause instanceof PersistenceException || cause instanceof ModelStateException) {
                        showErrorMessage("Training failed - " + cause.getMessage());
                    }
                    else showErrorMessage("Training failed");
                    enableInputForUntrainedState();
                } catch (Exception ex) {
                    showErrorMessage("Training failed");
                    enableInputForUntrainedState();
                } finally {
                    parent.setMenuEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void disableAll() {
        modelChooser.setEnabled(false);
        profileNumberChooser.setEnabled(false);
        audioNumberChooser.setEnabled(false);
        profileChooser.setEnabled(false);
        chosenProfilesList.setEnabled(false);
        trainButton.setEnabled(false);
        predictButton.setEnabled(false);
        resetButton.setEnabled(false);
        parent.setMenuEnabled(false);
        statusLabel.setText(PROCESSING);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
