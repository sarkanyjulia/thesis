package speakeridentification.view;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class HomePanel extends JPanel {

    private static final String INFO_TEXT = "<h1>Welcome to the Speaker Identification App!</h1>"
        + "With this app, you can add  audio samples from different speakers to a database, then use a neural network to recognize speakers.<hr>"
        + "<h2>How to add profiles</h2>"
        + "Go to <i>Profiles</i> and use the form on the right side. Add a name up to 40 characters, choose type from the available labels "
        + "and attach audio source. You can add a wav file from your file system, or, if you have converted spectrograms ready, add the folder "
        + "containing them. Save your choices with <i>Save</i> or reset all fields with <i>Clear</i>."
        + "<h2>How to delete profiles</h2>"
        + "Go to <i>Profiles</i> and use the table on the left side. Select one or more lines then hit <i>Delete</i>."
        + "<h2>How to train the network</h2>"
        + "Go to <i>Train & Predict</i>. Choose a pretrained model from those available, then set the number of profiles you want to use. "
        + "This can be 2 at the minimum and 5 at the maximum. Set the number of training samples you want to load from the database for each profile. "
        + "This can vary between 6 and 12, which means spectrograms generated from 30-60s of audio. Finally, select some profiles to train the network with. "
        + "Available profiles are on the left side, chosen profiles on the right. You can add and remove profiles to and from your selection by clicking "
        + "on them. If you've finished setup, click <i>Train</i> and wait until your network is ready."
        + "<h2>How to use the network</h2>"
        + "Go to <i>Train & Predict</i> and train your network if needed. Then click <i>Predict</i>. A new window will open, with buttons for all the profiles "
        + "you used for training. By clicking one of these, the program loads a new sample of the given profile and makes the network predict a profile name "
        + "for it. You can set a treshold value for the predictions between 0 and 100%. It means that if the network is less certain than the set treshold "
        + "about its prediction, the program will ignore it and returns \"uncertain\" instead."
        + "<h2>How delete previous training</h2>"
        + "Go to <i>Train & Predict</i> and hit <i>Reset</i>.";

    public HomePanel() {

        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        centerPanel.setBorder(new EmptyBorder(20,20,20,20));
        add(centerPanel, BorderLayout.CENTER);

        JTextPane info = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(info);
        centerPanel.add(scrollPane);
        info.setEditable(false);
        info.setMargin(new Insets(20,20,20,20));
        info.setContentType("text/html");
        info.setText(INFO_TEXT);
        info.setCaretPosition(0);
    }
}
