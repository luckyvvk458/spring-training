class Vehicle {

    void start() {
        System.out.println("Vehicle Started");
    }
}

class Car extends Vehicle {

    @Override
    void start() {
        System.out.println("Car Started");
    }
}

public class PolymorphismDemo {

    public static void main(String[] args) {

        Vehicle vehicle = new Car();

        vehicle.start();
    }
}