import java.util.*;

public class ListContainer {
    static class PatternInfo {
        String pattern;
        List<String> literals;
        int complexityScore;
        int matchCount;
        int evalCount;

        PatternInfo(String pattern, List<String> literals) {
            this.pattern = pattern;
            this.literals = literals;
            this.complexityScore = computeComplexity(pattern);
            this.matchCount = 0;
            this.evalCount = 0;
        }

        double getMatchFrequency() {
            return evalCount == 0 ? 0.0 : (double) matchCount / evalCount;
        }

        private static int computeComplexity(String pattern) {
            if (!pattern.contains("*") && !pattern.contains("?")) return 0;
            if (!pattern.startsWith("*") && !pattern.contains("?")) return 1;
            if (!pattern.startsWith("*")) return 2;
            if (pattern.contains("*") && pattern.contains("?")) return 4;
            return 3;
        }
    }

    private List<PatternInfo> patterns;
    private int totalTransactions;

    public ListContainer(List<String> patternStrings) {
        this.patterns = new ArrayList<>();
        this.totalTransactions = 0;
        initializePatterns(patternStrings);
    }

    // Initialize and remove redundant patterns
    private void initializePatterns(List<String> patternStrings) {
        List<PatternInfo> tempPatterns = new ArrayList<>();
        for (String pattern : patternStrings) {
            List<String> literals = splitPattern(pattern);
            tempPatterns.add(new PatternInfo(pattern, literals));
        }
        this.patterns = removeRedundantPatterns(tempPatterns);
    }

    // Split pattern into literal parts
    private List<String> splitPattern(String pattern) {
        List<String> literals = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (char c : pattern.toCharArray()) {
            if (c == '*' || c == '?') {
                if (current.length() > 0) {
                    literals.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            literals.add(current.toString());
        }
        return literals;
    }

    // Remove redundant patterns
    private List<PatternInfo> removeRedundantPatterns(List<PatternInfo> patterns) {
        List<PatternInfo> filtered = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        patterns.sort(Comparator.comparingInt(p -> p.complexityScore));

        for (PatternInfo p1 : patterns) {
            boolean isRedundant = false;
            for (String seenPattern : seen) {
                if (isIncluded(seenPattern, p1.pattern)) {
                    isRedundant = true;
                    break;
                }
            }
            if (!isRedundant) {
                filtered.add(p1);
                seen.add(p1.pattern);
            }
        }
        return filtered;
    }

    // Check if p1 includes p2
    private boolean isIncluded(String p1, String p2) {
        if (p1.equals(p2)) return false;
        if (p1.endsWith("*") && p2.startsWith(p1.substring(0, p1.length() - 1))) {
            return true;
        }
        List<String> literals1 = splitPattern(p1);
        List<String> literals2 = splitPattern(p2);
        if (literals1.size() > literals2.size()) return false;
        for (int i = 0; i < literals1.size(); i++) {
            if (i >= literals2.size() || !literals1.get(i).equals(literals2.get(i))) {
                return false;
            }
        }
        String remainder = p2.substring(p1.length());
        return p1.endsWith("*") || remainder.startsWith("*") || remainder.startsWith("?");
    }

    // Sort patterns dynamically
    private void sortPatterns() {
        patterns.sort((a, b) -> {
            double freqA = a.getMatchFrequency();
            double freqB = b.getMatchFrequency();
            int freqCompare = Double.compare(freqB, freqA);
            if (freqCompare != 0) return freqCompare;
            return Integer.compare(a.complexityScore, b.complexityScore);
        });
    }

    // Match a string against patterns
    public boolean matches(String transactionString) {
        Set<String> stringSubstrings = getSubstrings(transactionString);
        totalTransactions++;

        sortPatterns();

        for (PatternInfo patternInfo : patterns) {
            patternInfo.evalCount++;

            boolean canMatch = true;
            for (String literal : patternInfo.literals) {
                if (!stringSubstrings.contains(literal)) {
                    canMatch = false;
                    break;
                }
            }

            if (canMatch && matchesWildcard(patternInfo.pattern, transactionString)) {
                patternInfo.matchCount++;
                return true; // Early exit on first match
            }
        }
        return false; // No match found
    }

    // Extract substrings
    private Set<String> getSubstrings(String str) {
        Set<String> substrings = new HashSet<>();
        for (int i = 0; i < str.length(); i++) {
            for (int j = i + 1; j <= str.length(); j++) {
                if (j - i >= 2) {
                    substrings.add(str.substring(i, j));
                }
            }
        }
        return substrings;
    }

    // Fast wildcard matching
    private boolean matchesWildcard(String pattern, String str) {
        int strPos = 0;
        int patternPos = 0;
        while (patternPos < pattern.length()) {
            char pChar = pattern.charAt(patternPos);
            if (pChar == '*') {
                patternPos++;
                if (patternPos == pattern.length()) return true;
                String nextLiteral = getNextLiteral(pattern, patternPos);
                if (nextLiteral.isEmpty()) return true;
                int nextPos = str.indexOf(nextLiteral, strPos);
                if (nextPos == -1) return false;
                strPos = nextPos + nextLiteral.length();
                patternPos += nextLiteral.length();
            } else if (pChar == '?') {
                if (strPos >= str.length()) return false;
                strPos++;
                patternPos++;
            } else {
                if (strPos >= str.length() || str.charAt(strPos) != pChar) return false;
                strPos++;
                patternPos++;
            }
        }
        return strPos == str.length() || pattern.endsWith("*");
    }

    private String getNextLiteral(String pattern, int start) {
        StringBuilder literal = new StringBuilder();
        for (int i = start; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*' || c == '?') break;
            literal.append(c);
        }
        return literal.toString();
    }
}
