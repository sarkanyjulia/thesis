package speakeridentification.persistence;

import static java.sql.DriverManager.getConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import speakeridentification.domain.Audio;
import speakeridentification.domain.Profile;
import speakeridentification.domain.SpeakerType;
import speakeridentification.persistence.exceptions.PersistenceException;

@AllArgsConstructor
public class DefaultProfileDAO implements ProfileDAO {

    private static final Logger log = LoggerFactory.getLogger(DefaultProfileDAO.class);

    private static final String CREATE_PROFILE ="insert into profile (name, type) values (?, ?)";
    private static final String CREATE_AUDIO ="insert into audio (profile_id, training_data, content) values (?, ?, ?)";
    private static final String FIND_ALL_PROFILES = "select * from profile";
    private static final String DELETE_PROFILES = "delete from profile where id in (";
    private static final String FIND_ALL_AUDIO = "select * from audio where profile_id in (";

    private final String connectionString;

    @Override public int createProfile(Profile profileToSave) {
        int generatedKey = 0;
        try (Connection conn = getConnection(connectionString)) {
            try (PreparedStatement statement = conn.prepareStatement(CREATE_PROFILE, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, profileToSave.getName());
                statement.setInt(2, profileToSave.getType().getValue());
                statement.executeUpdate();
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    generatedKey = rs.getInt(1);
                    addAudio(generatedKey, profileToSave.getAudios(), conn);
                }
            }
        } catch (DerbySQLIntegrityConstraintViolationException e) {
            throw new PersistenceException("Failed to save profile: name already exists", e);
        } catch (SQLException e) {
            throw new PersistenceException("Failed to save profile", e);
        }
        return generatedKey;
    }

    private void addAudio(int generatedKey, List<Audio> audios, Connection conn) throws SQLException {
        for (Audio audio : audios) {
            try (PreparedStatement statement = conn.prepareStatement(CREATE_AUDIO)) {
                statement.setInt(1, generatedKey);
                statement.setBoolean(2, audio.isTrainingData());
                statement.setBytes(3, audio.getContent());
                statement.executeUpdate();
            }
        }
    }

    @Override public List<Profile> findAllProfiles() {
        List<Profile> profiles = new ArrayList<>();
        try (Connection conn = getConnection(connectionString)) {
            try (Statement statement = conn.createStatement()) {
                ResultSet resultSet = statement.executeQuery(FIND_ALL_PROFILES);
                while (resultSet.next()) {
                    profiles.add(Profile.builder()
                        .id(resultSet.getInt("id"))
                        .name(resultSet.getString("name"))
                        .type(SpeakerType.valueOf(resultSet.getInt("type")))
                        .build());
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to load profiles from database", e);
        }
        return profiles;
    }

    @Override public void deleteProfiles(List<Integer> ids) {
        String sql = buildQueryWithInClause(ids, DELETE_PROFILES);
        try (Connection conn = getConnection(connectionString)) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to delete the selected profile(s)", e);
        }
    }

    private String buildQueryWithInClause(Collection<Integer> ids, String sql) {
        StringBuilder sb = new StringBuilder(sql);
        boolean notFirst = false;
        for (Integer id : ids) {
            if (notFirst) {
                sb.append(", ");
            }
            sb.append(id);
            notFirst = true;
        }
        sb.append(")");
        return sb.toString();
    }

    @Override public List<Audio> findAllAudioByProfileIds(Collection<Integer> ids) {
        List<Audio> audioList = new ArrayList<>();
        String sql = buildQueryWithInClause(ids, FIND_ALL_AUDIO);
        try (Connection conn = getConnection(connectionString)) {
            try (Statement statement = conn.createStatement()) {
                ResultSet resultSet =  statement.executeQuery(sql);
                while (resultSet.next()) {
                    audioList.add(Audio.builder()
                        .id(resultSet.getInt("id"))
                        .profileId(resultSet.getInt("profile_id"))
                        .isTrainingData(resultSet.getBoolean("training_data"))
                        .content(resultSet.getBytes("content"))
                        .build());
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to load spectrogram samples from database", e);
        }
        return audioList;
    }
}
