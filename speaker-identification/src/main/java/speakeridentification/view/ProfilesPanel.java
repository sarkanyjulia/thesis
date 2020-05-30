package speakeridentification.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import lombok.extern.slf4j.Slf4j;
import speakeridentification.model.exceptions.SoxException;
import speakeridentification.persistence.domain.SpeakerType;
import speakeridentification.model.data.AudioSource;
import speakeridentification.model.data.ProfileData;
import speakeridentification.model.data.SourceType;
import speakeridentification.model.exceptions.InvalidInputException;
import speakeridentification.model.service.ProfileService;
import speakeridentification.persistence.exceptions.PersistenceException;

@Slf4j
public class ProfilesPanel extends JPanel {

    private MainWindow parent;
    private ProfileService service;
    private JTable profilesTable;
    private List<ProfileData> profiles = new ArrayList<>();
    private ProfilesTableModel tableModel;
    private JTextField nameField;
    private JLabel sourcePath;
    private JSpinner typeField;
    private SourceType selectedSourceType;
    private JButton saveButton;
    private JButton deleteButton;
    private JLabel statusLabel;
    private JButton addAudioFileButton;

    public ProfilesPanel(ProfileService service, MainWindow parent) {
        this.service = service;
        this.parent = parent;
        this.setBorder(new EmptyBorder(10, 30, 30, 30));
        initData();
        createGUI();
    }

    private void initData() {
        profiles = service.findAllProfiles();
        tableModel = new ProfilesTableModel(profiles);
    }

    private void createGUI() {
        JPanel leftSide = new JPanel();
        JPanel rightSide = new JPanel();
        JSplitPane splitPane = new JSplitPane(SwingConstants.VERTICAL, leftSide, rightSide);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        JLabel titleLabel = new JLabel("Profiles");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(titleLabel, BorderLayout.PAGE_START);

        // left side
        profilesTable = new JTable(tableModel);
        profilesTable.setRowHeight(20);
        JScrollPane scrollPane = new JScrollPane(profilesTable);
        profilesTable.setFillsViewportHeight(true);
        profilesTable.setColumnSelectionAllowed(false);
        profilesTable.setRowSelectionAllowed(true);

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener((ActionEvent e) -> {
            onDeleteClicked();
        });
        deleteButton.setMargin(new Insets(0, 10, 0, 10));
        JPanel deleteButtonPane = new JPanel();
        deleteButtonPane.setLayout(new BoxLayout(deleteButtonPane, BoxLayout.LINE_AXIS));
        deleteButtonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        deleteButtonPane.add(Box.createRigidArea(new Dimension(300, 0)));
        deleteButtonPane.add(deleteButton);

        leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.PAGE_AXIS));
        leftSide.add(scrollPane);
        leftSide.add(deleteButtonPane);

        // right side
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.PAGE_AXIS));
        rightSide.setBorder(BorderFactory.createTitledBorder("Add new profile"));

        JPanel line1 = new JPanel();
        line1.setLayout(new BoxLayout(line1, BoxLayout.LINE_AXIS));
        line1.setMaximumSize(new Dimension(400, 40));
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setSize(100, 30);
        nameField = new JTextField();
        line1.add(nameLabel);
        line1.add(Box.createRigidArea(new Dimension(20, 30)));
        line1.add(nameField);

        JPanel line2 = new JPanel();
        line2.setLayout(new BoxLayout(line2, BoxLayout.LINE_AXIS));
        line2.setMaximumSize(new Dimension(400, 40));
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setSize(100, 40);

        typeField = new JSpinner();
        typeField.setModel(new SpinnerCircularListModel(SpeakerType.values()));
        typeField.setMaximumSize(new Dimension(380, 30));

        line2.add(typeLabel);
        line2.add(Box.createRigidArea(new Dimension(26, 40)));
        line2.add(typeField);

        JPanel line3 = new JPanel();
        line3.setLayout(new BoxLayout(line3, BoxLayout.LINE_AXIS));
        line3.setMaximumSize(new Dimension(400, 40));
        JLabel sourceLabel = new JLabel("Source:");
        sourceLabel.setSize(100, 40);
        sourcePath = new JLabel("");
        line3.add(sourceLabel);
        line3.add(Box.createRigidArea(new Dimension(14, 40)));
        line3.add(sourcePath);

        JPanel line4 = new JPanel();
        line4.setLayout(new BoxLayout(line4, BoxLayout.LINE_AXIS));
        line4.setMaximumSize(new Dimension(400, 40));
        addAudioFileButton = new JButton("Add audio file");
        addAudioFileButton.addActionListener((ActionEvent e) -> {
            onAddAudioFileClicked();
        });
        JButton addSpectrograms = new JButton("Add spectrograms");
        addSpectrograms.addActionListener((ActionEvent e) -> {
            onAddSpectrogramsClicked();
        });
        line4.add(Box.createRigidArea(new Dimension(120, 40)));
        line4.add(addAudioFileButton);
        line4.add(Box.createRigidArea(new Dimension(10, 40)));
        line4.add(addSpectrograms);
        line4.add(Box.createRigidArea(new Dimension(10, 40)));

        JPanel line5 = new JPanel();
        line5.setLayout(new BoxLayout(line5, BoxLayout.LINE_AXIS));
        line5.setMaximumSize(new Dimension(400, 40));
        saveButton = new JButton("Save");
        saveButton.addActionListener((ActionEvent e) -> {
            saveProfile();
        });
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener((ActionEvent e) -> {
            clearInput();
        });
        line5.add(saveButton);
        line5.add(Box.createRigidArea(new Dimension(10, 40)));
        line5.add(clearButton);

        statusLabel = new JLabel();

        rightSide.add(line1);
        rightSide.add(Box.createRigidArea(new Dimension(400, 10)));
        rightSide.add(line2);
        rightSide.add(Box.createRigidArea(new Dimension(400, 10)));
        rightSide.add(line3);
        rightSide.add(line4);
        rightSide.add(Box.createRigidArea(new Dimension(400, 50)));
        rightSide.add(line5);
        rightSide.add(Box.createRigidArea(new Dimension(400, 100)));
        rightSide.add(statusLabel);
        rightSide.add(Box.createVerticalGlue());


    }

    private void saveProfile() {
        enableButtons(false);
        statusLabel.setText("Saving data...");
        ProfileData toSave = ProfileData.builder()
            .name(nameField.getText())
            .type((SpeakerType) typeField.getValue())
            .source(AudioSource.builder()
                .sourcePath(sourcePath.getText())
                .type(selectedSourceType)
                .build())
            .build();

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override protected Integer doInBackground() {
                return service.createProfile(toSave);
            }

            @Override protected void done() {
                try {
                    toSave.setId(get());
                    toSave.setSource(null);
                    profiles.add(toSave);
                    tableModel = new ProfilesTableModel(profiles);
                    profilesTable.setModel(tableModel);
                } catch (InterruptedException ex) {
                    //ignore
                }
                catch (ExecutionException ex) {
                    log.error(ex.getMessage(), ex);
                    Throwable cause = ex.getCause();
                    if (cause instanceof InvalidInputException || cause instanceof PersistenceException || cause instanceof SoxException) {
                        showErrorMessage("Failed to save profile - " + cause.getMessage());
                    }
                    else showErrorMessage("Failed to save profile - unexpected error");
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    showErrorMessage("Failed to save profile - unexpected error");
                } finally {
                    enableButtons(true);
                    statusLabel.setText("");
                }
            }
        };
        worker.execute();
    }

    private void enableButtons(boolean enabled) {
        saveButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        parent.setMenuEnabled(enabled);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void clearInput() {
        nameField.setText("");
        typeField.setValue(SpeakerType.UNCATEGORIZED);
        sourcePath.setText("");
        selectedSourceType = null;
    }

    private void onAddAudioFileClicked() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(".wav", "wav"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        int option = fileChooser.showOpenDialog(this);
        if(option == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            sourcePath.setText(file.getAbsolutePath());
            selectedSourceType = SourceType.WAV;
        }
    }

    private void onAddSpectrogramsClicked() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if(option == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            sourcePath.setText(file.getAbsolutePath());
            selectedSourceType = SourceType.SPECT;
        }
    }

    private void onDeleteClicked() {
        if (profilesTable.getSelectedRows().length > 0) {
            int n = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the selected profile(s)?",
                "Confirmation",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null
            );
            if (n == JOptionPane.OK_OPTION) {
                executeDelete();
            }
        }
    }

    private void executeDelete() {
        int[] selectedRows = profilesTable.getSelectedRows();
        List<Integer> idsToDelete = new ArrayList<>();
        for (int i=0; i<selectedRows.length; ++i) {
            idsToDelete.add((Integer) profilesTable.getValueAt(selectedRows[i], 0));
        }
        try {
            service.deleteProfiles(idsToDelete);
            profiles = profiles.stream().filter(p -> !idsToDelete.contains(p.getId())).collect(Collectors.toList());
            tableModel = new ProfilesTableModel(profiles);
            profilesTable.setModel(tableModel);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            showErrorMessage("Failed to delete profile(s).");
        }
    }

    public void enableSoxRelatedControls(boolean soxPresent) {
        addAudioFileButton.setEnabled(soxPresent);
    }
}
