import java.util.*;

public class WildcardMatcher {
    // Match a string against patterns in the container
    public boolean matchString(String transactionString, ListContainer patterns) {
        return patterns.matches(transactionString);
    }

    public static void main(String[] args) {
        WildcardMatcher matcher = new WildcardMatcher();

        // Sample patterns
        List<String> patternStrings = Arrays.asList(
            "abc*", "abc*def*ghi", "*xyz", "t?st", "test*data", "test*", "123?", "no*match"
        );
        ListContainer patterns = new ListContainer(patternStrings);

        // Test transactions
        String[] transactions = {"testxyz", "abcdef", "1234", "tast"};
        for (String transaction : transactions) {
            boolean matches = matcher.matchString(transaction, patterns);
            System.out.println("'" + transaction + "' matches: " + matches);
        }
    }
}
