package speakeridentification.persistence.domain;

import java.util.HashMap;
import java.util.Map;

public enum SpeakerType {

    UNCATEGORIZED(0), MALE(1), FEMALE(2), CHILD(3);

    private int value;
    private static Map<Integer, SpeakerType> map = new HashMap<>();

    SpeakerType(int value) {
        this.value = value;
    }

    static {
        for (SpeakerType speakerType : SpeakerType.values()) {
            map.put(speakerType.value, speakerType);
        }
    }

    public static SpeakerType valueOf(int speakerType) {
        return (SpeakerType) map.get(speakerType);
    }

    public int getValue() {
        return value;
    }

    /*
    @Override public String toString() {
        return this.name();
    }

     */
}
