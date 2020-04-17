package com.springrest.posty;

import acidrpc.AcidTransactionApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


class AcidTransactionApplicationTests {

	@Test
	void contextLoads() {
		AcidTransactionApplication acidRpc = new acidrpc.AcidTransactionApplication();
	}

	@Test
	void successfullyLoadsConfigurationFile() {
		AcidTransactionApplication acidRpc = new acidrpc.AcidTransactionApplication();

	}

}