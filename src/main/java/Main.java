
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import static queuehandling.QueueHandler.handleQueue;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, SQLException {
        handleQueue();
    }
}
