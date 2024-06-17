package com.gstz.faucet.service.distribute;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Description:
 * Author: songw
 * Date: 2024/5/8 16:41
 */

@Slf4j
@SpringBootTest
public class EthDistributeTest {

  @Test
  public void testCollectTokens() {
    EthDistribute.collectTokens();
  }

  @Test
  public void testDistributeTokens() {
    EthDistribute.distributeTokens();
  }

  @Test
  public void testAddTokens() {
    EthDistribute.addTokens("22.1");
  }

  @Test
  public void testCollectTokens1() {
    EthDistribute.collectTokens1("20");
  }

}
