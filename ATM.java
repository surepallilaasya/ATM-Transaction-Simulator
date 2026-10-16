
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.function.*;
import java.util.stream.*;

interface ATMOperations {
    void deposit(double amount);
    void withdraw(double amount);
}

abstract class TransactionAction {
    abstract String getType();
}

class Transaction extends TransactionAction
        implements Comparable<Transaction> {

    private String type;
    private double amount;
    private double balance;

    Transaction(String type, double amount, double balance) {
        this.type = type;
        this.amount = amount;
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    String getTypeForAction() {
        return type;
    }

    @Override
    String getType() {
        return type;
    }

    @Override
    public int compareTo(Transaction t) {
        return Double.compare(amount, t.amount);
    }

    @Override
    public String toString() {
        return type + " | Amount: Rs. " + amount
                + " | Balance: Rs. " + balance;
    }
}

class BankAccount implements ATMOperations {

    private double balance;
    private ArrayList<Transaction> history = new ArrayList<>();
    private HashMap<String, Integer> counts = new HashMap<>();
    private HashSet<String> types = new HashSet<>();
    private TreeMap<String, Double> summary = new TreeMap<>();
    private TreeSet<String> orderedTypes = new TreeSet<>();

    static final String FILE = "transactions.txt";

    BankAccount(double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public void deposit(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        balance += amount;
        record("Deposit", amount);
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        if (amount > balance)
            throw new IllegalArgumentException("Insufficient balance");

        balance -= amount;
        record("Withdrawal", amount);
    }

    void record(String type, double amount) {
        Transaction t = new Transaction(type, amount, balance);
        history.add(t);

        counts.put(type, counts.getOrDefault(type, 0) + 1);
        types.add(type);
        orderedTypes.add(type);
        summary.put(type, summary.getOrDefault(type, 0.0) + amount);

        save(t);
    }

    void save(Transaction t) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Path.of(FILE),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            writer.write(t.getType() + "|" + t.getAmount()
                    + "|" + t.getBalance());
            writer.newLine();

        } catch (IOException | SecurityException e) {
            System.out.println("Could not save transaction.");
        }
    }

    void showHistory() {
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }

        history.forEach(System.out::println);
    }

    void search(String keyword) {
        String key = keyword.trim();

        Predicate<Transaction> condition =
                t -> t.getType().equalsIgnoreCase(key);

        history.stream()
                .filter(condition)
                .forEach(System.out::println);
    }

    void sortHistory() {
        ArrayList<Transaction> sorted = new ArrayList<>(history);

        Comparator<Transaction> byAmount =
                Comparator.comparingDouble(Transaction::getAmount);

        sorted.sort(byAmount);
        sorted.forEach(System.out::println);
    }

    void showReport() {
        System.out.println("Transaction counts: " + counts);
        System.out.println("Transaction types: " + types);
        System.out.println("Ordered transaction types: " + orderedTypes);
        System.out.println("Amount summary: " + summary);

        double total = history.stream()
                .map(Transaction::getAmount)
                .reduce(0.0, Double::sum);

        System.out.println("Total amount transacted: Rs. " + total);

        long deposits = history.stream()
                .filter(t -> t.getType().equals("Deposit"))
                .count();

        System.out.println("Number of deposits: " + deposits);

        Map<String, Long> grouped = history.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getType,
                        TreeMap::new,
                        Collectors.counting()));

        System.out.println("Grouped report: " + grouped);

        String report = history.stream()
                .map(Transaction::getType)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));

        System.out.println("Transaction categories: " + report);
    }

    void searchAmount(double amount) {
        double[] values = history.stream()
                .mapToDouble(Transaction::getAmount)
                .sorted()
                .toArray();

        int index = Arrays.binarySearch(values, amount);

        System.out.println(index >= 0
                ? "Amount found."
                : "Amount not found.");
    }

    void readSavedTransactions() {
        Path path = Path.of(FILE);

        if (!Files.exists(path)) {
            System.out.println("No saved transactions found.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");

                if (parts.length == 3) {
                    try {
                        System.out.println(
                                parts[0].trim()
                                + " | Amount: Rs. "
                                + Double.parseDouble(parts[1].trim())
                                + " | Balance: Rs. "
                                + Double.parseDouble(parts[2].trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid record: " + line);
                    }
                } else {
                    System.out.println("Malformed record: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }

        try {
            List<String> lines = Files.readAllLines(path);
            System.out.println("Saved record count: " + lines.size());
        } catch (IOException e) {
            System.out.println("Could not read complete file.");
        }

        try (Stream<String> lines = Files.lines(path)) {
            System.out.println("Records using stream: " + lines.count());
        } catch (IOException e) {
            System.out.println("Stream reading failed.");
        }
    }

    double recursiveTotal(int index) {
        if (index >= history.size())
            return 0;

        return history.get(index).getAmount()
                + recursiveTotal(index + 1);
    }

    void matrixReport() {
        // Rows: deposits and withdrawals
        // Columns: transaction count and total amount
        double[][] data = new double[2][2];

        for (Transaction t : history) {
            int row = t.getType().equals("Deposit") ? 0 : 1;
            data[row][0]++;
            data[row][1] += t.getAmount();
        }

        System.out.println("Type       Count      Total Amount");
        System.out.println("Deposits   " + data[0][0] + "         Rs. " + data[0][1]);
        System.out.println("Withdrawals " + data[1][0] + "        Rs. " + data[1][1]);
    }

    void demonstrateStrings() {
        String message = "  ATM Transaction Simulator  ";
        String clean = message.trim();

        System.out.println(clean);
        System.out.println("Length: " + clean.length());
        System.out.println("Substring: " + clean.substring(0, 3));
        System.out.println("Uppercase: " + clean.toUpperCase());
        System.out.println("Contains ATM: " + clean.contains("ATM"));
        System.out.println("Split: " + Arrays.toString(clean.split(" ")));
        System.out.println("Joined: " + String.join("-", clean.split(" ")));

        String a = new String("ATM");
        String b = new String("ATM");

        System.out.println("equals(): " + a.equals(b));
        System.out.println("== comparison: " + (a == b));

        StringBuilder builder = new StringBuilder("ATM");
        builder.append(" Simulator");
        System.out.println(builder);

        StringBuffer buffer = new StringBuffer("Transaction");
        buffer.append(" History");
        System.out.println(buffer);
    }

    void demonstrateAlgorithms() {
        int[] values = {30, 10, 20};
        Arrays.sort(values);
        System.out.println("Sorted array: " + Arrays.toString(values));
        System.out.println("Binary search: "
                + Arrays.binarySearch(values, 20));

        Integer[] numbers = {5, 2, 8};
        Arrays.sort(numbers, Comparator.reverseOrder());
        System.out.println("Reverse order: " + Arrays.toString(numbers));
    }

    void demonstrateFunctionalInterfaces() {
        Predicate<Double> valid = amount -> amount > 0;
        Function<Double, Double> fee = amount -> amount * 0.01;
        Consumer<String> display = System.out::println;
        Supplier<String> message = () -> "ATM ready";

        display.accept(message.get());
        System.out.println("Valid amount: " + valid.test(100.0));
        System.out.println("Example fee: Rs. " + fee.apply(1000.0));
    }

    // Method overloading
    void receipt(String message) {
        System.out.println(message);
    }

    void receipt(Transaction t) {
        System.out.println(t);
    }
}

public class ATM {

    static final String TITLE = "ATM TRANSACTION SIMULATOR";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BankAccount account = new BankAccount(5000.0);
        int choice = -1;

        System.out.println(TITLE);
        System.out.println("Starting balance: Rs. 5000");

        do {
            System.out.println("\n1. Check balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transaction history");
            System.out.println("5. Search transaction type");
            System.out.println("6. Sort transactions");
            System.out.println("7. Transaction report");
            System.out.println("8. Read saved transactions");
            System.out.println("9. Search amount");
            System.out.println("10. Recursive total");
            System.out.println("11. Matrix report");
            System.out.println("12. String demonstrations");
            System.out.println("13. Array algorithms");
            System.out.println("14. Functional interfaces");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            if (!sc.hasNextInt()) {
                System.out.println("Enter a whole number.");
                sc.nextLine();
                continue;
            }

            choice = sc.nextInt();

            try {
                switch (choice) {
                    case 1:
                        System.out.printf(
                                "Balance: Rs. %.2f%n",
                                account.getBalance());
                        break;

                    case 2:
                        System.out.print("Deposit amount: ");
                        double deposit = sc.nextDouble();
                        account.deposit(deposit);
                        account.receipt("Deposit successful!");
                        break;

                    case 3:
                        System.out.print("Withdrawal amount: ");
                        double withdrawal = sc.nextDouble();
                        account.withdraw(withdrawal);
                        account.receipt("Withdrawal successful!");
                        break;

                    case 4:
                        account.showHistory();
                        break;

                    case 5:
                        System.out.print("Enter Deposit or Withdrawal: ");
                        account.search(sc.next());
                        break;

                    case 6:
                        account.sortHistory();
                        break;

                    case 7:
                        account.showReport();
                        break;

                    case 8:
                        account.readSavedTransactions();
                        break;

                    case 9:
                        System.out.print("Enter amount to search: ");
                        account.searchAmount(sc.nextDouble());
                        break;

                    case 10:
                        System.out.println("Total: Rs. "
                                + account.recursiveTotal(0));
                        break;

                    case 11:
                        account.matrixReport();
                        break;

                    case 12:
                        account.demonstrateStrings();
                        break;

                    case 13:
                        account.demonstrateAlgorithms();
                        break;

                    case 14:
                        account.demonstrateFunctionalInterfaces();
                        break;

                    case 0:
                        System.out.println("Thank you for using ATM!");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Enter a valid number.");
                sc.nextLine();
            } catch (IllegalArgumentException e) {
                System.out.println("Transaction failed: " + e.getMessage());
            } finally {
                // Executes after each menu operation
            }

        } while (choice != 0);

        sc.close();
    }
}
