package speakeridentification.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import speakeridentification.model.service.TrainingService;
import speakeridentification.model.service.ProfileService;
import speakeridentification.persistence.DbHandler;

public class MainWindow extends JFrame {

    public static final String MAIN_WINDOW_TITLE = "Speaker Identification";
    public static final String CONFIRM_EXIT_MESSAGE = "Are you sure you want to exit the program?";
    public static final String CONFIRMATION_TITLE = "Confirmation";

    private ProfileService profileService;
    private TrainingService trainingService;
    private boolean soxPresent;

    JButton homeButton;
    JButton profilesButton;
    JButton trainingButton;
    List<JButton> menuButtons;
    JPanel mainPanel;

    public MainWindow(ProfileService profileService, TrainingService trainingService, boolean soxPresent) {
        this.profileService = profileService;
        this.trainingService = trainingService;
        this.soxPresent = soxPresent;
        initFrame();
        createGUI();
        setActionListeners();
        if (!soxPresent) {
            JOptionPane.showMessageDialog(this, "<html>SoX not found on your device.<br>Download and install it from http://sox.sourceforge.net/ to enable input from audio files!</html>", "Warning", JOptionPane.WARNING_MESSAGE);
        }
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
        URL iconURL = Thread.currentThread().getContextClassLoader().getResource("logo.png");
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());
    }

    private void showExitConfirmation() {
        int n = JOptionPane.showConfirmDialog(
            this,
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
        DbHandler.shutdown();
        this.dispose();
        System.exit(0);
    }

    private void createGUI() {
        JToolBar mainToolBar = new JToolBar();
        mainPanel = new HomePanel();

        homeButton = new JButton("Home");
        profilesButton = new JButton("Profiles");
        trainingButton = new JButton("Train & Predict");
        menuButtons = new ArrayList<>();

        menuButtons.add(homeButton);
        menuButtons.add(profilesButton);
        menuButtons.add(trainingButton);

        mainToolBar.add(homeButton);
        mainToolBar.add(profilesButton);
        mainToolBar.add(trainingButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainToolBar, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    private void setActionListeners() {
        homeButton.addActionListener((ActionEvent e) -> {
            homeButtonClicked();
        });
        profilesButton.addActionListener((ActionEvent e) -> {
            profilesButtonClicked();
        });
        trainingButton.addActionListener((ActionEvent e) -> {
            trainingButtonClicked();
        });
    }

    private void homeButtonClicked() {
        mainPanel.removeAll();
        mainPanel = new HomePanel();
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void profilesButtonClicked() {
        mainPanel.removeAll();
        mainPanel = new ProfilesPanel(profileService, this);
        ((ProfilesPanel)mainPanel).enableSoxRelatedControls(soxPresent);
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
