package com.gstz.faucet.service.contract;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
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
    public void testWethRestake() {
        strategyManagerContract.wethRestake("30");
    }

    @Test
    public void testAddRestake() {
        strategyManagerContract.addRestake("43");
    }

    @Test
    public void testGetBalance() throws ExecutionException, InterruptedException {
        BigDecimal balance = StrategyManagerContract.getBalance(
            "0x461488Bbd7556B7fcED3000579E97aa94Cd23a35",
            "03cd6e6bc7eb21be91c3edf9f3aa89afb5f0bf453caded6e4ed72cfd42910521");
    }
}
