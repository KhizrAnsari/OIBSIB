import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console based ATM Machine Simulation
 * Features: Login with PIN (3 attempts), Balance Enquiry, Deposit, Withdraw,
 *           Fund Transfer, Mini Statement, Change PIN, Daily Withdrawal Limit
 */

// ---------------------- Transaction ----------------------
class Transaction {
    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final String dateTime;

    public Transaction(String type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    @Override
    public String toString() {
        return String.format("%-20s | %-22s | Rs. %10.2f | Bal: Rs. %10.2f",
                dateTime, type, amount, balanceAfter);
    }
}

// ---------------------- Account ----------------------
class Account {
    private final String cardNumber;
    private final String holderName;
    private int pin;
    private double balance;
    private double withdrawnToday;
    private final List<Transaction> history = new ArrayList<>();

    public static final double DAILY_LIMIT = 20000.0;
    public static final double MIN_BALANCE = 0.0;

    public Account(String cardNumber, String holderName, int pin, double openingBalance) {
        this.cardNumber = cardNumber;
        this.holderName = holderName;
        this.pin = pin;
        this.balance = openingBalance;
    }

    public String getCardNumber() { return cardNumber; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }

    public boolean validatePin(int enteredPin) {
        return this.pin == enteredPin;
    }

    public boolean changePin(int oldPin, int newPin) {
        if (this.pin != oldPin) return false;
        this.pin = newPin;
        return true;
    }

    public void deposit(double amount) {
        balance += amount;
        history.add(new Transaction("DEPOSIT", amount, balance));
    }

    /** Returns a message: "OK" if success, otherwise the reason for failure. */
    public String withdraw(double amount) {
        if (amount % 100 != 0) return "Amount 100 ke multiple me hona chahiye.";
        if (amount > balance - MIN_BALANCE) return "Insufficient balance.";
        if (withdrawnToday + amount > DAILY_LIMIT)
            return "Daily withdrawal limit (Rs. " + DAILY_LIMIT + ") exceed ho jayegi. "
                    + "Aaj aur nikal sakte ho: Rs. " + (DAILY_LIMIT - withdrawnToday);
        balance -= amount;
        withdrawnToday += amount;
        history.add(new Transaction("WITHDRAWAL", amount, balance));
        return "OK";
    }

    public String transferOut(double amount, String toCard) {
        if (amount > balance - MIN_BALANCE) return "Insufficient balance.";
        balance -= amount;
        history.add(new Transaction("TRANSFER TO " + maskCard(toCard), amount, balance));
        return "OK";
    }

    public void transferIn(double amount, String fromCard) {
        balance += amount;
        history.add(new Transaction("TRANSFER FROM " + maskCard(fromCard), amount, balance));
    }

    public void printMiniStatement(int count) {
        System.out.println("\n------------- MINI STATEMENT -------------");
        System.out.println("Account Holder : " + holderName);
        System.out.println("Card Number    : " + maskCard(cardNumber));
        System.out.println("-------------------------------------------------------------------------------");
        if (history.isEmpty()) {
            System.out.println("Koi transaction nahi mila.");
        } else {
            int start = Math.max(0, history.size() - count);
            for (int i = history.size() - 1; i >= start; i--) {
                System.out.println(history.get(i));
            }
        }
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("Available Balance: Rs. %.2f%n", balance);
    }

    public static String maskCard(String card) {
        if (card.length() <= 4) return card;
        return "XXXX-XXXX-" + card.substring(card.length() - 4);
    }
}

// ---------------------- Bank (acts as database) ----------------------
class Bank {
    private final Map<String, Account> accounts = new HashMap<>();

    private final java.util.Random random = new java.util.Random();

    /** Generates a unique 16-digit card number. */
    public String generateCardNumber() {
        String card;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(random.nextInt(10));
            }
            card = sb.toString();
        } while (accounts.containsKey(card));
        return card;
    }

    public void addAccount(Account acc) {
        accounts.put(acc.getCardNumber(), acc);
    }

    public Account getAccount(String cardNumber) {
        return accounts.get(cardNumber);
    }
}

// ---------------------- ATM (main program) ----------------------
public class ATM {
    private static final Scanner sc = new Scanner(System.in);
    private static final Bank bank = new Bank();

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("        WELCOME TO JAVA ATM MACHINE       ");
        System.out.println("==========================================");

        while (true) {
            System.out.println("\n========== MAIN MENU ==========");
            System.out.println("1. Naya Account Banayein");
            System.out.println("2. Login (ATM use karein)");
            System.out.println("3. Exit");
            System.out.println("===============================");

            int choice = readInt("Option choose karein (1-3): ");

            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    Account acc = login();
                    if (acc != null) {
                        atmMenu(acc);
                    }
                    break;
                case 3:
                    System.out.println("Thank you! ATM use karne ke liye dhanyavaad.");
                    return;
                default:
                    System.out.println("Invalid option! 1 se 3 ke beech choose karein.");
            }
        }
    }

    // ---------- Create new account (user input) ----------
    private static void createAccount() {
        System.out.println("\n------- NAYA ACCOUNT -------");

        String name;
        while (true) {
            System.out.print("Account holder ka naam: ");
            name = sc.nextLine().trim();
            if (!name.isEmpty()) break;
            System.out.println("Naam khali nahi ho sakta.");
        }

        int pin;
        while (true) {
            pin = readInt("4-digit PIN set karein: ");
            if (pin >= 1000 && pin <= 9999) break;
            System.out.println("PIN exactly 4 digits ka hona chahiye (1000-9999).");
        }

        int confirmPin = readInt("PIN dobara enter karein: ");
        if (pin != confirmPin) {
            System.out.println("PIN match nahi hua. Account nahi bana.");
            return;
        }

        double openingBalance;
        while (true) {
            openingBalance = readDouble("Opening deposit amount: Rs. ");
            if (openingBalance >= 0) break;
            System.out.println("Amount negative nahi ho sakta.");
        }

        String cardNumber = bank.generateCardNumber();
        bank.addAccount(new Account(cardNumber, name, pin, openingBalance));

        System.out.println("\nAccount successfully ban gaya!");
        System.out.println("Naam        : " + name);
        System.out.println("Card Number : " + cardNumber);
        System.out.printf("Balance     : Rs. %.2f%n", openingBalance);
        System.out.println("Apna Card Number yaad rakhein, login ke time chahiye hoga.");
    }

    // ---------- Login with 3 attempts ----------
    private static Account login() {
        System.out.print("\nApna 16-digit Card Number enter karein: ");
        String card = sc.nextLine().trim();
        Account acc = bank.getAccount(card);

        if (acc == null) {
            System.out.println("Invalid card number!");
            return null;
        }

        int attempts = 3;
        while (attempts > 0) {
            int pin = readInt("PIN enter karein: ");
            if (acc.validatePin(pin)) {
                System.out.println("\nLogin successful! Welcome, " + acc.getHolderName());
                return acc;
            }
            attempts--;
            if (attempts > 0) {
                System.out.println("Galat PIN! Bache hue attempts: " + attempts);
            }
        }
        System.out.println("3 galat attempts. Card block kar diya gaya (session end).");
        return null;
    }

    // ---------- Main ATM menu ----------
    private static void atmMenu(Account acc) {
        while (true) {
            System.out.println("\n=========== ATM MENU ===========");
            System.out.println("1. Balance Enquiry");
            System.out.println("2. Cash Withdrawal");
            System.out.println("3. Cash Deposit");
            System.out.println("4. Fund Transfer");
            System.out.println("5. Mini Statement");
            System.out.println("6. Change PIN");
            System.out.println("7. Exit");
            System.out.println("================================");

            int choice = readInt("Apna option choose karein (1-7): ");

            switch (choice) {
                case 1:
                    System.out.printf("Aapka available balance: Rs. %.2f%n", acc.getBalance());
                    break;
                case 2:
                    withdraw(acc);
                    break;
                case 3:
                    deposit(acc);
                    break;
                case 4:
                    transfer(acc);
                    break;
                case 5:
                    acc.printMiniStatement(5);
                    break;
                case 6:
                    changePin(acc);
                    break;
                case 7:
                    System.out.println("Card nikal lein. Thank you, " + acc.getHolderName() + "!");
                    return;
                default:
                    System.out.println("Invalid option! 1 se 7 ke beech choose karein.");
            }
        }
    }

    private static void withdraw(Account acc) {
        double amount = readDouble("Withdraw amount enter karein: Rs. ");
        if (amount <= 0) {
            System.out.println("Amount 0 se zyada hona chahiye.");
            return;
        }
        String result = acc.withdraw(amount);
        if (result.equals("OK")) {
            System.out.printf("Rs. %.2f withdraw ho gaye. Cash collect karein.%n", amount);
            System.out.printf("Remaining balance: Rs. %.2f%n", acc.getBalance());
        } else {
            System.out.println("Transaction failed: " + result);
        }
    }

    private static void deposit(Account acc) {
        double amount = readDouble("Deposit amount enter karein: Rs. ");
        if (amount <= 0) {
            System.out.println("Amount 0 se zyada hona chahiye.");
            return;
        }
        acc.deposit(amount);
        System.out.printf("Rs. %.2f successfully deposit ho gaye.%n", amount);
        System.out.printf("Updated balance: Rs. %.2f%n", acc.getBalance());
    }

    private static void transfer(Account acc) {
        System.out.print("Receiver ka 16-digit Card Number enter karein: ");
        String toCard = sc.nextLine().trim();

        if (toCard.equals(acc.getCardNumber())) {
            System.out.println("Aap khud ko transfer nahi kar sakte.");
            return;
        }
        Account receiver = bank.getAccount(toCard);
        if (receiver == null) {
            System.out.println("Receiver account nahi mila.");
            return;
        }
        double amount = readDouble("Transfer amount enter karein: Rs. ");
        if (amount <= 0) {
            System.out.println("Amount 0 se zyada hona chahiye.");
            return;
        }
        String result = acc.transferOut(amount, toCard);
        if (result.equals("OK")) {
            receiver.transferIn(amount, acc.getCardNumber());
            System.out.printf("Rs. %.2f successfully %s ko transfer ho gaye.%n",
                    amount, receiver.getHolderName());
            System.out.printf("Remaining balance: Rs. %.2f%n", acc.getBalance());
        } else {
            System.out.println("Transfer failed: " + result);
        }
    }

    private static void changePin(Account acc) {
        int oldPin = readInt("Purana PIN enter karein: ");
        int newPin = readInt("Naya 4-digit PIN enter karein: ");
        if (newPin < 1000 || newPin > 9999) {
            System.out.println("PIN exactly 4 digits ka hona chahiye.");
            return;
        }
        int confirm = readInt("Naya PIN dobara enter karein: ");
        if (newPin != confirm) {
            System.out.println("PIN match nahi hua.");
            return;
        }
        if (acc.changePin(oldPin, newPin)) {
            System.out.println("PIN successfully change ho gaya!");
        } else {
            System.out.println("Purana PIN galat hai.");
        }
    }

    // ---------- Safe input helpers ----------
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

    private static double readDouble(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please valid amount enter karein.");
            }
        }
    }
}