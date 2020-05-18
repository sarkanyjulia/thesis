package speakeridentification.view;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

public class ProbabilityInputField extends JTextField implements FocusListener {

    private String lastText = "";

    public ProbabilityInputField() {
        super();
        addFocusListener(this);
        setText("0.0");
    }

    @Override
    public void processKeyEvent(KeyEvent ev) {
        if (Character.isDigit(ev.getKeyChar())
            || ev.getKeyChar()=='.'
            || ev.getKeyCode()== KeyEvent.VK_DELETE
            || ev.getKeyCode()== KeyEvent.VK_BACK_SPACE
            || ev.getKeyCode()== KeyEvent.VK_LEFT
            || ev.getKeyCode()== KeyEvent.VK_RIGHT
            || ev.getKeyCode()== KeyEvent.VK_TAB
        ) {
            lastText = getText();
            super.processKeyEvent(ev);
            if (!canContinue(getText())) {
                setText(lastText);
            }
        }
        ev.consume();

    }

    private boolean canContinue(String text) {
        return  text.equals("") || text.equals("100") || text.matches("\\d{1,2}") || text.matches("\\d{1,2}\\.\\d{0,2}") ;
    }

    public Double getValue() {
        Double result = null;
        String text = getText();
        try {
            if (!text.isBlank()) {
                result = Double.parseDouble(text);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    @Override public void focusGained(FocusEvent e) {}

    @Override public void focusLost(FocusEvent e) {
        String text = getText();
        if (text.isBlank()) setText("0.0");
        if (text.matches("\\d{1,3}")) setText(text + ".0");
        if (text.matches("\\d{1,2}\\.")) setText(text + "0");
    }
}
