package speakeridentification.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class AudioSource {

    private String sourcePath;
    private SourceType type;
}
