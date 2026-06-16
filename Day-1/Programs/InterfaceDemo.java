interface Payment {

    void pay();
}

class UPIPayment implements Payment {

    @Override
    public void pay() {
        System.out.println("Payment through UPI");
    }
}

class CardPayment implements Payment {

    @Override
    public void pay() {
        System.out.println("Payment through Card");
    }
}

public class InterfaceDemo {

    public static void main(String[] args) {

        Payment payment1 = new UPIPayment();
        payment1.pay();

        Payment payment2 = new CardPayment();
        payment2.pay();
    }
}