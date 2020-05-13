# posty

Provides intermediary service between couchmart and Couchbase cluster using ACID transactions.

Service will check the stock of products in Couchmart. If stock is >=1 order will be placed, otherwise error is returned to browser detailing which product was out of stock.

Debug the service itself using:

journalctl -fu posty

Common issues are that the cluster hasn't been rebalanced leading the cluster to not meet the services durability standards. 

