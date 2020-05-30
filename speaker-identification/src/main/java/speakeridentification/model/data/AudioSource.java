package speakeridentification.model.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AudioSource {

    private String sourcePath;
    private SourceType type;
}
