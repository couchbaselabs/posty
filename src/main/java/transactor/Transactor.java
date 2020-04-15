package transactor;

import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.JsonNode;
import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.node.TextNode;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.transactions.TransactionGetResult;
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.config.TransactionConfigBuilder;
import com.couchbase.transactions.error.TransactionFailed;
import acidrpc.InsufficientStockException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


class ProductNotFound extends RuntimeException {}

public class Transactor {

    private Cluster cluster;
    private Bucket bucket;
    private Collection collection;
    private Transactions transactions;

    private ArrayList<JsonObject> inserts;
    private ArrayList<JsonObject> upserts;

    public Transactor() {
        // Initialize the Couchbase cluster
        cluster = Cluster.connect("192.168.33.10", "Administrator", "password");
        bucket = cluster.bucket("couchmart");
        collection = bucket.defaultCollection();

        // Create the single Transactions object
        transactions = Transactions.create(cluster, TransactionConfigBuilder.create()
                // The configuration can be altered here, but in most cases the defaults are fine.
                .build());

        inserts = new ArrayList<JsonObject>();
        upserts = new ArrayList<JsonObject>();
    }

    public void queueInsert(JsonObject insertRequest) {
        inserts.add(insertRequest);
    }

    public void queueUpdate(JsonObject updateRequest) {
        upserts.add(updateRequest);
    }

    public void clearRequests() {
        inserts.clear();
        upserts.clear();
    }

    public void commitQueuedTransactions() {
        try {
            transactions.run((ctx) -> {
                for(JsonObject insertObject: this.inserts) {
                    ctx.insert(collection, "thing", insertObject);
                }
            });
        } catch (TransactionFailed transactionFailed) {
            transactionFailed.printStackTrace();
        }
    }

    public boolean processOrder(JsonNode orderJson) {
        try {
            transactions.run((ctx) -> {
                // 'ctx' is an AttemptContext, which permits getting, inserting,
                // removing and replacing documents, along with committing and
                // rolling back the transaction.

                //TODO: test that this will raise an exception if it doesn't contain an order value
                JsonNode order = orderJson.findValue("order");

                if (order.isArray()) {
                    for (JsonNode productOrdered : order) {
                        String productOrderedId = ((TextNode) productOrdered).asText();
                        TransactionGetResult productDocument = ctx.get(collection, productOrderedId);

                        //TransactionGetResult productDocument = ctx.get(collection, productOrderedId);
                        JsonObject productContent = productDocument.contentAsObject();
                        int stockLevel = productContent.getInt("stock");
                        if(stockLevel < 1) {
                            throw new InsufficientStockException("There is not enough stock of: " + productOrderedId);
                        }
                        //decrement the stock levels
                        --stockLevel;
                        productContent.put("stock", stockLevel);
                        ctx.replace(productDocument, productContent);
                    }
                    //TransactionGetResult doc = ctx.get(collection, productsOrdered.textValue());
                }


                // TODO: 01/04/2020  update this to be correct once you know the object layout of the order sent from the python API
                String id = null;
                for (Iterator<Map.Entry<String, JsonNode>> it = orderJson.fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    String keyName = entry.getKey();
                    if(keyName.contains("Order::")) {
                        id = keyName;
                        break;
                    }
                }

                // populate orderObject with order contents
                //ObjectNode orderObject = (ObjectNode) orderJson.get(id);
                JsonObject orderObject = JsonObject.fromJson(orderJson.get(id).toString());
//                orderObject.put("type", "order");
//                orderObject.put("name", orderObject.get("name"));
//                orderObject.put("ts", orderJson.get("ts"));
                if (id != null)
                    ctx.insert(collection, id, orderObject);
                //String docId = "Aaron0";

                // use getOptional if the document may or may not exist
                //Optional<TransactionGetResult> docOpt = ctx.getOptional(collection, docId);
                // use get if the document should exist and the transaction will fail if not
                //TransactionGetResult doc = ctx.get(collection, docId);

                //System.out.println(doc.contentAs(JsonObject.class));

                // Replacing a doc:
                //TransactionGetResult anotherDoc = ctx.get(collection, "Aliaksey0");
                // TransactionGetResult is immutable, so get its content as a mutable JsonObject
                /*JsonObject content = anotherDoc.contentAs(JsonObject.class);
                content.put("transactions", "are awesome");
                ctx.replace(anotherDoc, content);*/

                // This call is optional - if you leave it off, the transaction
                // will be committed anyway.
                ctx.commit();
                });
        } catch (TransactionFailed err) {

            if (err.getCause() instanceof InsufficientStockException) {
                throw (InsufficientStockException) err.getCause();
            }
            // ctx.getOrError can raise a DocumentNotFoundException
            if (err.getCause() instanceof ProductNotFound) {
                System.err.println("Transaction " + err.result().transactionId() + " failed: ");
                err.result().log().logs().forEach(System.err::println);
                throw (RuntimeException) err.getCause(); // propagate up
            }
            else {
                // Unexpected error - log for human review
                // This per-txn log allows the app to only log failures
                System.err.println("Transaction " + err.result().transactionId() + " failed:");

                err.result().log().logs().forEach(System.err::println);
            }
        }

        return true;

    }



    public static void main(String[] args) throws Exception {
        Transactor transactor = new Transactor();
        //transactor.transact();
    }


}
