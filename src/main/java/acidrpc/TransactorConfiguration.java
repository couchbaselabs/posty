package acidrpc;

public class TransactorConfiguration {

    private String connectionString;
    private String username;
    private String password;

    public TransactorConfiguration(String connectionString, String username, String password) {
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    public void printVars() {
        System.out.println("Connection String in config: " + this.getConnectionString());
        System.out.println("Password String in config: " + this.getPassword());
        System.out.println("Username in config: " + this.getUsername());
    }
}
