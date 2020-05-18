/* www.java2s.com */

package speakeridentification.view;

import java.util.List;
import javax.swing.SpinnerListModel;

public class SpinnerCircularListModel extends SpinnerListModel {

    public SpinnerCircularListModel(Object[] items) {
        super(items);
    }

    public SpinnerCircularListModel(List<?> items) {
        super(items);
    }

    public Object getNextValue() {
        List list = getList();
        int index = list.indexOf(getValue());

        index = (index >= list.size() - 1) ? 0 : index + 1;
        return list.get(index);
    }

    public Object getPreviousValue() {
        List list = getList();
        int index = list.indexOf(getValue());

        index = (index <= 0) ? list.size() - 1 : index - 1;
        return list.get(index);
    }
}
