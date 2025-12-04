package com.gstz.faucet.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

@Slf4j
@SpringBootTest
public class Web3jUtilsTest {

    @Test
    public void testGetChainId() {
        long chainId = Web3jUtils.getChainId();
        log.info("chainId:{}", Long.valueOf(chainId));
    }

    @Test
    public void testGetEtherBalance() throws ExecutionException, InterruptedException {
        BigDecimal etherBalance = Web3jUtils.getEtherBalance("0x3aF23717C7637595b7C95D9E03B2A08d0470290d");
        log.info("etherBalance:{}", etherBalance);
    }

}
