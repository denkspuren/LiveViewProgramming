package lvp.skills;

import java.util.Random;
import java.util.stream.Collectors;

public class IdGen {
    private static final Random RANDOM = new Random();

    public static String generateID(int n) { // random alphanumeric string of size n
        return RANDOM.ints(n, 0, 36).
                            mapToObj(i -> Integer.toString(i, 36)).
                            collect(Collectors.joining());
    }

    public static String getHashID(Object o) { return Integer.toHexString(o.hashCode()); }
}
