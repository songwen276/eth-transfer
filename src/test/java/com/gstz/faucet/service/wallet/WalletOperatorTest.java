package com.gstz.faucet.service.wallet;

import com.gstz.faucet.service.transfer.EthTransfer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Description: 
 * Author: songw
 * Date: 2024/6/14 15:20
 */

@Slf4j
@SpringBootTest
public class WalletOperatorTest {

  @Autowired
  WalletOperator walletOperator;

  @Test
  public void testImportAccountToMetamask() {
    walletOperator.importAccountToMetamask();
  }

}
