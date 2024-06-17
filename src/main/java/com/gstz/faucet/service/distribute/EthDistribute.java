package com.gstz.faucet.service.distribute;

import com.gstz.faucet.utils.Web3jUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

/**
 * Description:
 * Author: songw
 * Date: 2024/5/7 11:13
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class EthDistribute {

  // 要转换ETH的地址和金额
  static Properties subAddress = new Properties();
  static Properties collectaAddress = new Properties();

  static {
    try (FileInputStream addressFis = new FileInputStream(
        Web3jUtils.workPath + Web3jUtils.separator + "subaddress.properties");
        FileInputStream collectaAddressFis = new FileInputStream(
            Web3jUtils.workPath + Web3jUtils.separator + "collectaddress.properties")) {
      subAddress.load(addressFis);
      collectaAddress.load(collectaAddressFis);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 将子账户代币集中到主账户
   */
  public static void collectTokens() {
    try {
      // 获取收集账户地址
      Collection<String> keys = collectaAddress.values().parallelStream().map(Object::toString)
          .toList();
      // 循环将子地址代币集中到主账户
      for (String key : keys) {
        // 获取子账户余额
        Web3j web3j = Web3jUtils.web3j;
        Credentials credentials = Credentials.create(key);
        String suAddress = credentials.getAddress();
        BigDecimal etherBalance = Web3jUtils.getEtherBalance(suAddress);

        // 判断子账户余额不足直接跳过
        if (etherBalance.compareTo(new BigDecimal(1)) < 0) {
          continue;
        }

        String address = Web3jUtils.credentials.getAddress();
        BigInteger nonce = Web3jUtils.getNonce(suAddress);
        log.info("将子账户{}钱包余额集中到主账户：{}，钱包余额为：{}，nonce是：{}", suAddress, address,
            etherBalance, nonce);

        // 计算手续费与实际发送余额
        BigDecimal gasFee = Web3jUtils.getGasFee();
        BigDecimal collectTokens = etherBalance.subtract(gasFee);

        TransactionReceipt transactionReceipt = Transfer.sendFundsEIP1559(web3j, credentials,
            address, collectTokens, Convert.Unit.ETHER,
            new BigInteger(Web3jUtils.gaslimit), new BigInteger(Web3jUtils.maxPriorityFeePerGas),
            new BigInteger(Web3jUtils.maxFeePerGas)).send();
        String transactionHash = transactionReceipt.getTransactionHash();
        log.info("将子账户{}钱包余额集中到主账户：{}成功，共发送{}个ether，交易哈希：{}", suAddress,
            address, collectTokens, transactionHash);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 将主账户代币平均分发到子账户
   */
  public static void distributeTokens() {
    try {
      // 获取主账户余额
      Web3j web3j = Web3jUtils.web3j;
      Credentials credentials = Web3jUtils.credentials;
      String addressStr = credentials.getAddress();
      BigDecimal etherBalance = Web3jUtils.getEtherBalance(addressStr);
      BigInteger nonce = Web3jUtils.getNonce(addressStr);
      log.info("当前分发的主钱包地址是：{}，钱包余额是：{}，nonce是：{}", addressStr, etherBalance,
          nonce);

      // 获取子账户地址
      Collection<String> keys = subAddress.values().parallelStream().map(Object::toString).toList();

      // 计算每个地址应分配的数量
      BigDecimal gasFee = Web3jUtils.getGasFee();
      BigDecimal size = BigDecimal.valueOf(keys.size());
      BigDecimal totalGasFee = size.multiply(gasFee);
      BigDecimal distributeTotalBalance = etherBalance.subtract(totalGasFee);
      BigDecimal amountPerRecipient = distributeTotalBalance.divide(BigDecimal.valueOf(keys.size()),
          2, RoundingMode.DOWN);

      // 逐个向子地址发送代币
      for (String key : keys) {
        String subAddress = Credentials.create(key).getAddress();
        TransactionReceipt transactionReceipt = Transfer.sendFundsEIP1559(web3j, credentials,
            subAddress, amountPerRecipient, Convert.Unit.ETHER, new BigInteger(Web3jUtils.gaslimit),
            new BigInteger(Web3jUtils.maxPriorityFeePerGas),
            new BigInteger(Web3jUtils.maxFeePerGas)).send();
        String transactionHash = transactionReceipt.getTransactionHash();
        log.info("{}子账户已分发{}个token，交易哈希：{}", subAddress, amountPerRecipient,
            transactionHash);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 根据需求补充代币
   */
  public static void addTokens(String eth) {
    try {
      // 获取主账户余额
      Web3j web3j = Web3jUtils.web3j;
      Credentials credentials = Web3jUtils.credentials;
      String addressStr = credentials.getAddress();
      BigDecimal etherBalance = Web3jUtils.getEtherBalance(addressStr);
      BigInteger nonce = Web3jUtils.getNonce(addressStr);
      log.info("当前分发的主钱包地址是：{}，钱包余额是：{}，nonce是：{}", addressStr, etherBalance,
          nonce);

      // 获取子账户地址
      Collection<String> keys = subAddress.values().parallelStream().map(Object::toString).toList();

      // 数量
      BigDecimal amountPerRecipient = new BigDecimal(eth);

      // 逐个向子地址发送代币
      for (String key : keys) {
        String subAddress = Credentials.create(key).getAddress();
        BigDecimal subEtherBalance = Web3jUtils.getEtherBalance(subAddress);
        if (subEtherBalance.compareTo(amountPerRecipient) < 0) {
          BigDecimal add = amountPerRecipient.subtract(subEtherBalance);
          log.info("当前子账户余额小于{}，开始补充{}个eth", amountPerRecipient, add);
          TransactionReceipt transactionReceipt = Transfer.sendFundsEIP1559(web3j, credentials,
              subAddress, add, Convert.Unit.ETHER,
              new BigInteger(Web3jUtils.gaslimit),
              new BigInteger(Web3jUtils.maxPriorityFeePerGas),
              new BigInteger(Web3jUtils.maxFeePerGas)).send();
          String transactionHash = transactionReceipt.getTransactionHash();
          log.info("{}子账户已分发{}个token，交易哈希：{}", subAddress, add, transactionHash);
        } else {
          log.info("当前子账户余额不小于{}，直接跳过", amountPerRecipient);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 根据需求集中代币
   */
  public static void collectTokens1(String eth) {
    try {
      // 获取主账户余额
      Web3j web3j = Web3jUtils.web3j;
      Credentials credentials = Web3jUtils.credentials;
      String addressStr = credentials.getAddress();
      BigDecimal etherBalance = Web3jUtils.getEtherBalance(addressStr);
      BigInteger nonce = Web3jUtils.getNonce(addressStr);
      log.info("当前主钱包地址是：{}，钱包余额是：{}，nonce是：{}", addressStr, etherBalance,
          nonce);

      // 获取子账户地址
      Collection<String> keys = subAddress.values().parallelStream().map(Object::toString).toList();

      // 数量
      BigDecimal amountPerRecipient = new BigDecimal(eth);

      // 逐个从子地址集中代币到主账户
      for (String key : keys) {
        Credentials subcCredentials = Credentials.create(key);
        String suAddress = subcCredentials.getAddress();
        BigDecimal subEtherBalance = Web3jUtils.getEtherBalance(suAddress);
        if (subEtherBalance.compareTo(amountPerRecipient) > 0) {
          BigDecimal collect = subEtherBalance.subtract(new BigDecimal("0.02"));
          log.info("当前子账户余额大于{}，开始集中{}", amountPerRecipient, collect);
          TransactionReceipt transactionReceipt = Transfer.sendFundsEIP1559(web3j, subcCredentials,
              addressStr, collect, Convert.Unit.ETHER,
              new BigInteger(Web3jUtils.gaslimit),
              new BigInteger(Web3jUtils.maxPriorityFeePerGas),
              new BigInteger(Web3jUtils.maxFeePerGas)).send();
          String transactionHash = transactionReceipt.getTransactionHash();
          log.info("{}子账户集中{}个token，交易哈希：{}", suAddress, collect, transactionHash);
        } else {
          log.info("当前子账户余额不小于{}，直接跳过", amountPerRecipient);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    String methodName = args[0];
    log.info("开始执行EthDistribute中的{}方法", methodName);

    // 根据传入的方法名执行相应的方法
    if ("collectTokens".equals(methodName)) {
      collectTokens();
    } else if ("distributeTokens".equals(methodName)) {
      distributeTokens();
    } else {
      System.out.println("未知方法: " + methodName);
    }
  }

}
