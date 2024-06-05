package com.gstz.faucet.service.contract;

import com.gstz.faucet.contract.Weth9;
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
public class Weth9Contract {

  // 合约地址
  private static final String WETH_CONTRACT_ADDRESS = "0x94373a4919B3240D86eA41593D5eBa789FEF3848";
  ;
  static String workPath;
  static String separator;

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
  }

  /**
   * 批量将ETH转换为WETH
   */
  public void ethTransfer() {
    try {
      long chainId = Web3jUtils.getChainId();
      Web3j web3j = Web3jUtils.web3j;

      // 创建一个 EIP1559GasProvider 对象，设置 gas 价格为 20 GWei，gas 上限为 1,000,000
      StaticEIP1559GasProvider gasProvider = new StaticEIP1559GasProvider(chainId,
          new BigInteger(Web3jUtils.maxFeePerGas), new BigInteger(Web3jUtils.maxPriorityFeePerGas),
          new BigInteger(Web3jUtils.gaslimit));

      // 批量转换ETH为WETH
      ethTransfer.forEach((address, eth) -> {
        try {
          // 获取账户余额
          String addressStr = String.valueOf(address);
          BigDecimal etherBalance = Web3jUtils.getEtherBalance(addressStr);
          BigInteger nonce = Web3jUtils.getNonce(addressStr);
          log.info("当前要转换的钱包地址是：{}，钱包余额是：{}，nonce是：{}，要转换的ETH个数是：{}",
              addressStr, etherBalance, nonce, eth);

          // 加载WETH合约
          Weth9 weth9 = Weth9.load(WETH_CONTRACT_ADDRESS, web3j, Credentials.create(addressStr),
              gasProvider);

          // 转换
          BigDecimal ethAmount = new BigDecimal(String.valueOf(eth));
          BigInteger weiAmount = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger();
          TransactionReceipt receipt = weth9.deposit(weiAmount).sendAsync().get();

          // 获取交易哈希
          String transactionHash = receipt.getTransactionHash();

          // 获取账户转换后余额
          BigDecimal trEtherBalance = Web3jUtils.getEtherBalance(addressStr);
          BigInteger trNonce = Web3jUtils.getNonce(addressStr);
          log.info("{}转换后余额是：{}，nonce是：{}，交易哈希是：{}", addressStr, trEtherBalance,
              trNonce, transactionHash);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 获取weth供应量
   * @param weth9 以太坊连接实例
   */
  private static void getTotalSupply(Weth9 weth9) {
    try {
      BigInteger totalSupply = weth9.totalSupply().send();
      log.info("weth9总供应量：{}", totalSupply);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
