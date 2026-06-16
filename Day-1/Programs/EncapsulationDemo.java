class BankAccount {

    private double balance;

    public void deposit(double amount) {
        balance += amount;
    }

    public double getBalance() {
        return balance;
    }
}

public class EncapsulationDemo {

    public static void main(String[] args) {

        BankAccount account = new BankAccount();

        account.deposit(5000);

        System.out.println("Balance = " + account.getBalance());
    }
}