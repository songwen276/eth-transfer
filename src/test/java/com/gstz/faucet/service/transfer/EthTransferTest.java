package com.gstz.faucet.service.transfer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Description:
 * Author: songw
 * Date: 2024/5/8 16:41
 */

@Slf4j
@SpringBootTest
public class EthTransferTest {

  @Autowired
  EthTransfer ethTransfer;

  @Test
  public void testEthTransfer() {
    ethTransfer.ethTransfer();
  }

  @Test
  public void testSendRawTransaction() {
    EthTransfer.sendRawTransaction();
  }

  @Test
  public void testSendRawFuncTransactions() {
    EthTransfer.sendRawFuncTransactions();
  }

}
