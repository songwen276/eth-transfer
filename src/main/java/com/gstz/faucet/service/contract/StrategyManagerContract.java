package com.gstz.faucet.service.contract;

import com.gstz.faucet.contract.StrategyManager;
import com.gstz.faucet.utils.Web3jUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.StaticEIP1559GasProvider;
import org.web3j.utils.Convert;

/**
 * Description:
 * Author: songw
 * Date: 2024/5/7 15:43
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class StrategyManagerContract {

  // 质押地址
  private static final String STRATEGY = "0x80528d6e9a2babfc766965e0e26d5ab08d9cfaf9";
  // 合约地址
  private static final String EIGEN_RESTAKE_CONTRACT_ADDRESS = "0xdfB5f6CE42aAA7830E94ECFCcAd411beF4d4D5b6";
  ;
  static String workPath;
  static String separator;
  static StaticEIP1559GasProvider gasProvider;

  // 要转换ETH的地址和金额
  static Properties ethTransfer = new Properties();

  static {
    workPath = System.getProperty("user.dir");
    separator = System.getProperty("file.separator");
    try (FileInputStream addressFis = new FileInputStream(
        workPath + separator + "transferaddress.properties")) {
      ethTransfer.load(addressFis);
    } catch (IOException e) {
      e.printStackTrace();
    }
    // 创建一个 EIP1559GasProvider 对象，设置 gas 价格为 20 GWei，gas 上限为 1,000,000
    long chainId = Web3jUtils.getChainId();
    gasProvider = new StaticEIP1559GasProvider(chainId, new BigInteger(Web3jUtils.maxFeePerGas),
        new BigInteger(Web3jUtils.maxPriorityFeePerGas), new BigInteger(Web3jUtils.gaslimit));
  }

  /**
   * 批量将WETH质押
   */
  public void wethRestake(String weth) {
    try {
      Web3j web3j = Web3jUtils.web3j;
      // 批量质押WETH
      ethTransfer.forEach((address, privateKey) -> {
        try {
          // 获取账户余额
          String addressStr = String.valueOf(address);
          String privateKeyStr = String.valueOf(privateKey);
          BigDecimal wetherBalance = Weth9Contract.getBalance(addressStr);
          BigInteger nonce = Web3jUtils.getNonce(addressStr);
          log.info("当前要质押的钱包地址是：{}，钱包余额是：{}，nonce是：{}，要质押的WETH个数是：{}",
              addressStr, wetherBalance, nonce, weth);

          // 加载EIGENLAYER_RESTAKE合约
          StrategyManager strategyManager = StrategyManager.load(
              EIGEN_RESTAKE_CONTRACT_ADDRESS, web3j, Credentials.create(privateKeyStr),
              gasProvider);

          // 质押
          BigDecimal ethAmount = new BigDecimal(String.valueOf(weth));
          BigInteger weiAmount = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger();
          TransactionReceipt receipt = strategyManager.depositIntoStrategy(STRATEGY,
              Weth9Contract.WETH_CONTRACT_ADDRESS, weiAmount).sendAsync().get();

          // 获取交易哈希
          String transactionHash = receipt.getTransactionHash();

          // 获取质押转换后余额
          BigDecimal trWetherBalance = Weth9Contract.getBalance(addressStr);
          BigInteger trNonce = Web3jUtils.getNonce(addressStr);
          log.info("{}质押后余额是：{}，nonce是：{}，交易哈希是：{}", addressStr, trWetherBalance,
              trNonce, transactionHash);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
