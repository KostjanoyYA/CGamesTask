package kostyanoy.game;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum HeadOrTailMoves {
    HEAD,
    TAIL;

    public static List<String> getAllMoves() {
        return Arrays.stream(HeadOrTailMoves.values())
                .map(s->s.toString())
                .collect(Collectors.toList());
    }
}
