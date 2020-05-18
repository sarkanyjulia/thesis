package speakeridentification.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import speakeridentification.model.service.TrainingService;
import speakeridentification.model.service.ProfileService;
import speakeridentification.persistence.DbInitializer;

public class MainWindow extends JFrame {

    public static final String MAIN_WINDOW_TITLE = "Speaker identification demo";
    public static final String CONFIRM_EXIT_MESSAGE = "Are you sure you want to exit the program?";
    public static final String CONFIRMATION_TITLE = "Confirmation";

    private ProfileService profileService;
    private TrainingService trainingService;

    JButton profilesButton;
    JButton trainingButton;
    List<JButton> menuButtons;
    JPanel mainPanel;

    public MainWindow(ProfileService profileService, TrainingService trainingService) {
        this.profileService = profileService;
        this.trainingService = trainingService;
        initFrame();
        createGUI();
        setActionListeners();
    }

    private void initFrame() {
        setTitle(MAIN_WINDOW_TITLE);
        setSize(1000, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                showExitConfirmation();
            }
        });
    }

    private void showExitConfirmation() {
        int n = JOptionPane.showConfirmDialog(
            null,
            CONFIRM_EXIT_MESSAGE,
            CONFIRMATION_TITLE,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null
        );
        if (n == JOptionPane.OK_OPTION) {
            doUponExit();
        }
    }

    private void doUponExit() {
        //TODO save model state
        DbInitializer.shutdown(); // TODO this with events
        this.dispose();
        System.exit(0);
    }

    private void createGUI() {
        JToolBar mainToolBar = new JToolBar();
        //mainPanel = new ProfilesPanel(profileService);
        mainPanel = new TrainingPanel(trainingService, this);

        profilesButton = new JButton("Profiles");
        trainingButton = new JButton("Train & predict");
        menuButtons = new ArrayList<>();

        menuButtons.add(profilesButton);
        menuButtons.add(trainingButton);

        mainToolBar.add(profilesButton);
        mainToolBar.add(trainingButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainToolBar, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    private void setActionListeners() {
        profilesButton.addActionListener((ActionEvent e) -> {
            profilesButtonClicked();
        });
        trainingButton.addActionListener((ActionEvent e) -> {
            trainingButtonClicked();
        });
    }

    private void profilesButtonClicked() {
        mainPanel.removeAll();
        mainPanel = new ProfilesPanel(profileService, this);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void trainingButtonClicked() {
        mainPanel.removeAll();
        mainPanel = new TrainingPanel(trainingService, this);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void setMenuEnabled(boolean enabled) {
        for (JButton button : menuButtons) {
            button.setEnabled(enabled);
        }
    }
}
