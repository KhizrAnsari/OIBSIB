import java.util.Random;
import java.util.Scanner;

/**
 * Console based Number Guessing Game
 * Features: 3 difficulty levels, limited attempts, high/low + "close" hints,
 *           score system, best score tracking, play again option.
 */
public class NumberguessingGame {

    private static final Scanner sc = new Scanner(System.in);
    private static final Random random = new Random();

    private static int bestScore = 0;   // highest score in this session
    private static int gamesPlayed = 0;
    private static int gamesWon = 0;

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("      WELCOME TO NUMBER GUESSING GAME     ");
        System.out.println("==========================================");

        System.out.print("Apna naam batayein: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) name = "Player";
        System.out.println("Hello, " + name + "! Chalo shuru karte hain.");

        boolean playAgain = true;
        while (playAgain) {
            playRound(name);
            playAgain = askPlayAgain();
        }

        printSummary(name);
    }

    // ---------- One complete round ----------
    private static void playRound(String name) {
        int[] level = chooseDifficulty();   // [maxNumber, maxAttempts]
        int maxNumber = level[0];
        int maxAttempts = level[1];

        int secret = random.nextInt(maxNumber) + 1;
        int attemptsUsed = 0;
        boolean guessed = false;

        System.out.println("\nMaine 1 se " + maxNumber + " ke beech ek number socha hai.");
        System.out.println("Aapke paas " + maxAttempts + " attempts hain. Best of luck!\n");

        gamesPlayed++;

        while (attemptsUsed < maxAttempts) {
            int remaining = maxAttempts - attemptsUsed;
            int guess = readInt("Attempt " + (attemptsUsed + 1) + "/" + maxAttempts
                    + " - Aapka guess (1-" + maxNumber + "): ");

            if (guess < 1 || guess > maxNumber) {
                System.out.println("Number 1 se " + maxNumber + " ke beech hona chahiye! (Attempt waste nahi hua)");
                continue;
            }

            attemptsUsed++;

            if (guess == secret) {
                guessed = true;
                break;
            }

            giveHint(guess, secret, maxNumber);

            if (remaining - 1 > 0) {
                System.out.println("Bache hue attempts: " + (remaining - 1) + "\n");
            }
        }

        if (guessed) {
            int score = calculateScore(maxAttempts, attemptsUsed, maxNumber);
            gamesWon++;
            System.out.println("\n*** CONGRATULATIONS " + name + "! ***");
            System.out.println("Aapne number " + secret + " sirf " + attemptsUsed + " attempt(s) me guess kar liya!");
            System.out.println("Is round ka score: " + score);

            if (score > bestScore) {
                bestScore = score;
                System.out.println("NAYA BEST SCORE! \u2605");
            }
        } else {
            System.out.println("\nGame Over! Aapke attempts khatam ho gaye.");
            System.out.println("Sahi number tha: " + secret);
        }
    }

    // ---------- Difficulty selection ----------
    private static int[] chooseDifficulty() {
        System.out.println("\n------ DIFFICULTY LEVEL ------");
        System.out.println("1. Easy   (1-50,  10 attempts)");
        System.out.println("2. Medium (1-100, 7 attempts)");
        System.out.println("3. Hard   (1-500, 9 attempts)");
        System.out.println("------------------------------");

        while (true) {
            int choice = readInt("Level choose karein (1-3): ");
            switch (choice) {
                case 1: return new int[]{50, 10};
                case 2: return new int[]{100, 7};
                case 3: return new int[]{500, 9};
                default: System.out.println("Invalid choice! 1, 2 ya 3 enter karein.");
            }
        }
    }

    // ---------- Hints ----------
    private static void giveHint(int guess, int secret, int maxNumber) {
        int diff = Math.abs(guess - secret);

        if (guess > secret) {
            System.out.println("Bahut zyada! Isse chhota number try karein.");
        } else {
            System.out.println("Bahut kam! Isse bada number try karein.");
        }

        // "Hot / Cold" hint based on closeness (percentage of range)
        double percent = (diff * 100.0) / maxNumber;
        if (percent <= 5) {
            System.out.println("Hint: Bahut kareeb ho! (HOT)");
        } else if (percent <= 15) {
            System.out.println("Hint: Thoda kareeb ho. (WARM)");
        } else {
            System.out.println("Hint: Abhi door ho. (COLD)");
        }
    }

    // ---------- Score calculation ----------
    private static int calculateScore(int maxAttempts, int attemptsUsed, int maxNumber) {
        int base = maxNumber;                          // harder range = more points
        int bonus = (maxAttempts - attemptsUsed + 1) * 10;
        return base + bonus;
    }

    // ---------- Play again ----------
    private static boolean askPlayAgain() {
        while (true) {
            System.out.print("\nKya aap dobara khelna chahte hain? (y/n): ");
            String ans = sc.nextLine().trim();
            if (ans.equalsIgnoreCase("y")) return true;
            if (ans.equalsIgnoreCase("n")) return false;
            System.out.println("Sirf 'y' ya 'n' enter karein.");
        }
    }

    // ---------- Final summary ----------
    private static void printSummary(String name) {
        System.out.println("\n==========================================");
        System.out.println("              GAME SUMMARY                ");
        System.out.println("==========================================");
        System.out.println("Player        : " + name);
        System.out.println("Games Played  : " + gamesPlayed);
        System.out.println("Games Won     : " + gamesWon);
        System.out.println("Games Lost    : " + (gamesPlayed - gamesWon));
        System.out.println("Best Score    : " + bestScore);
        System.out.println("==========================================");
        System.out.println("Khelne ke liye shukriya, " + name + "! Phir milenge.");
    }

    // ---------- Safe integer input ----------
    private static int readInt(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please sirf number enter karein.");
            }
        }
    }
}