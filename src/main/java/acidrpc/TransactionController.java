package acidrpc;

import java.io.IOException;
import java.util.List;

import com.couchbase.client.core.deps.com.fasterxml.jackson.core.JsonFactory;
import com.couchbase.client.core.deps.com.fasterxml.jackson.core.JsonParser;
import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.JsonNode;
import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import transactor.Transactor;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
class TransactionController {

    private final Transactor transactor;
    private TransactorConfiguration transactorConfiguration;


    private String connectionString;
    private String username;
    private String password;

    @Autowired
    TransactionController(@Value("${AWS_NODES}") String connectionString,
                          @Value("${USERNAME}") String username,
                          @Value("${PASSWORD}") String password) {
        transactorConfiguration = new TransactorConfiguration(connectionString, username, password);
        transactorConfiguration.printVars();
        this.transactor = new Transactor(connectionString,
                                         username,
                                         password);
    }

    @RequestMapping(value="/submitorder", method=POST, consumes= APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    String submitOrder(HttpEntity<String> httpEntity) {
        //JsonArray requests = heroToSave.getArray("requests");
        String orderJson = httpEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory(); // since 2.1 use mapper.getFactory() instead
        JsonParser jp = null;
        JsonNode actualObj = null;
        try {
            jp = factory.createParser(orderJson);
            actualObj = mapper.readTree(jp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (actualObj != null) {
            try {
                transactor.processOrder(actualObj);
                return String.format("{\"OrderStatus\":\"Complete\"}");
            } catch (InsufficientStockException ex) {
                System.err.println("We failed twice");
                throw new InsufficientStockException(ex.getMessage());
            }

        }
        throw new InsufficientStockException("Error processing received order.");

    }
}