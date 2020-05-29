package speakeridentification.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class HomePanel extends JPanel {

    public HomePanel() {

        setLayout(new BorderLayout());


        JPanel titlePanel = new JPanel();
        add(titlePanel, BorderLayout.NORTH);
        titlePanel.setBorder(new EmptyBorder(20,20,20,20));
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.PAGE_AXIS));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        centerPanel.setBorder(new EmptyBorder(0,20,20,20));
        add(centerPanel, BorderLayout.CENTER);

        JPanel line1 = new JPanel();
        JPanel line2 = new JPanel();
        line1.setLayout(new BoxLayout(line1, BoxLayout.LINE_AXIS));
        line2.setLayout(new BoxLayout(line2, BoxLayout.LINE_AXIS));
        titlePanel.add(line1);
        titlePanel.add(Box.createRigidArea(new Dimension(10,10)));
        titlePanel.add(line2);

        JLabel titleLabel = new JLabel("Welcome to Speaker Identification App!", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Monaco", Font.BOLD, 28));
        line1.add(titleLabel);
        line1.add(Box.createHorizontalGlue());

        JLabel textLabel = new JLabel(
            "<html>Add audio samples from different speakers to the database, and use a neural network to recognize the speakers.</html>");
        titleLabel.setFont(new Font("Monaco", Font.PLAIN, 24));
        line2.add(textLabel);
        line2.add(Box.createHorizontalGlue());

        JTextArea info = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(info);
        centerPanel.add(scrollPane);
        info.setEditable(false);

    }
}
