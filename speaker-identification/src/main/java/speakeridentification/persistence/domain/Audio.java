package speakeridentification.persistence.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Audio {

    private int id;
    private int profileId;
    private boolean isTrainingData;
    private byte[] content;
}
