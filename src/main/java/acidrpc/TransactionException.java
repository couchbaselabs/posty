package acidrpc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class TransactionException extends RuntimeException {
    public TransactionException(String cause) {
        super("Could not complete order: " + cause);
    }
}