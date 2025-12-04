package com.gstz.faucet.service.contract;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

/**
 * Description:
 * Author: songw
 * Date: 2024/5/8 16:41
 */

@Slf4j
@SpringBootTest
public class Weth9ContractTest {
    // @Autowired
    // Weth9Contract weth9Contract;
    //
    // @Test
    // public void testEthDeposit() {
    //     weth9Contract.ethDeposit("370");
    // }
    //
    // @Test
    // public void testEthTransfer() {
    //     weth9Contract.ethTransfer("1");
    // }
    //
    // @Test
    // public void testWithdraw() {
    //     weth9Contract.withdraw("6");
    // }
    //
    // @Test
    // public void testAddWeth() {
    //     weth9Contract.addWeth("10");
    // }

    @Test
    public void testBalanceOf() throws ExecutionException, InterruptedException {
        BigDecimal balance = Weth9Contract.getBalance("0xcE94ABa9086F408b3F2af4E8b5AC1aBc42F4da9a");
        log.info("余额是：{}", balance);
    }
}
