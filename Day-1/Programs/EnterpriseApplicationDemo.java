class UserRepository {

    public void saveUser() {
        System.out.println("Saving User");
    }
}

class UserService {

    private UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void registerUser() {
        repository.saveUser();
        System.out.println("User Registered");
    }
}

public class EnterpriseApplicationDemo {

    public static void main(String[] args) {

        UserRepository repository = new UserRepository();

        UserService service =
                new UserService(repository);

        service.registerUser();
    }
}