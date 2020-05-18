package speakeridentification.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import speakeridentification.model.data.ProfileData;

public class ProfilesTableModel extends AbstractTableModel {

    private final String[] columnNames = {
        "Id", "Name", "Type"
    };
    private final Object[][] data;
    private List<ProfileData> profiles = new ArrayList<>();

    public ProfilesTableModel(List<ProfileData> profiles) {
        this.profiles = profiles;
        ProfileData temp;
        data = new Object[profiles.size()][columnNames.length];
        for (int i=0; i<profiles.size(); ++i) {
            temp = profiles.get(i);
            data[i][0] = temp.getId();
            data[i][1] = temp.getName();
            data[i][2] = temp.getType();
        }
    }

    @Override public int getRowCount() {
        return profiles.size();
    }

    @Override public int getColumnCount() {
        return columnNames.length;
    }

    @Override public Object getValueAt(int i, int j) {
        return data[i][j];
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
}
