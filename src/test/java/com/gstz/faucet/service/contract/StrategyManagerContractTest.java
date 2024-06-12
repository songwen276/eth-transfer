package com.gstz.faucet.service.contract;

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
public class StrategyManagerContractTest {

    @Autowired
    StrategyManagerContract strategyManagerContract;

    @Test
    public void testEthTransfer() {
        strategyManagerContract.wethRestake("0.01");
    }
}
