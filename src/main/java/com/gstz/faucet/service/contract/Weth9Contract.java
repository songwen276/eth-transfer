package com.gstz.faucet.service.contract;

import com.gstz.faucet.contract.Weth9;
import com.gstz.faucet.utils.PropertiesUtils;
import com.gstz.faucet.utils.Web3jUtils;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
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
//@Component
public class Weth9Contract {

  // 合约地址
  // public static final String WETH_CONTRACT_ADDRESS = "0x94373a4919B3240D86eA41593D5eBa789FEF3848";
  public static final String WETH_CONTRACT_ADDRESS = "0xFf7d6A96ae471BbCD7713aF9CB1fEeB16cf56B41";

  // 要转换ETH的地址和金额
  static LinkedHashMap<String, String> ethTransfer;

  static StaticEIP1559GasProvider gasProvider;

  static {
    ethTransfer = PropertiesUtils.getKeyValueLinkMapFromProperFile("transferaddress.properties");
    long chainId = Web3jUtils.getChainId();
    // 创建一个 EIP1559GasProvider 对象，设置 gas 价格为 20 GWei，gas 上限为 1,000,000
    gasProvider = new StaticEIP1559GasProvider(chainId, new BigInteger(Web3jUtils.maxFeePerGas),
        new BigInteger(Web3jUtils.maxPriorityFeePerGas), new BigInteger(Web3jUtils.gaslimit));
  }

  /**
   * 批量将ETH转换为WETH
   */
  public void ethDeposit(String eth) {
    try {
      Web3j web3j = Web3jUtils.web3j;
      // 批量转换ETH为WETH
      ethTransfer.forEach((address, privateKey) -> {
        try {
          // 获取账户余额
          String addressStr = String.valueOf(address);
          String privateKeyStr = String.valueOf(privateKey);
          BigDecimal etherBalance = Web3jUtils.getEtherBalance(addressStr);
          BigInteger nonce = Web3jUtils.getNonce(addressStr);
          log.info("当前要转换的钱包地址是：{}，钱包余额是：{}，nonce是：{}，要转换的ETH个数是：{}",
              addressStr, etherBalance, nonce, eth);

          if (etherBalance.compareTo(new BigDecimal("22")) > 0) {
            // 加载WETH合约
            Weth9 weth9 = Weth9.load(WETH_CONTRACT_ADDRESS, web3j,
                Credentials.create(privateKeyStr),
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
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 批量将转发WETH
   */
  public void ethTransfer(String eth) {
    try {
      // 加载WETH合约
      Web3j web3j = Web3jUtils.web3j;
      Weth9 weth9 = Weth9.load(WETH_CONTRACT_ADDRESS, web3j, Web3jUtils.credentials, gasProvider);
      String mainAddress = Web3jUtils.credentials.getAddress();
      // 批量将转发WETH
      ethTransfer.forEach((address, privateKey) -> {
        try {
          // 获取账户余额
          BigDecimal balance = getBalance(mainAddress);
          BigInteger nonce = Web3jUtils.getNonce(mainAddress);
          log.info("当前主钱包地址是：{}，钱包余额是：{}，nonce是：{}，要转换的ETH个数是：{}", mainAddress,
              balance, nonce, eth);

          // 转换
          String subAddress = String.valueOf(address);
          BigDecimal subBanlance = getBalance(subAddress);
          if (subBanlance.compareTo(new BigDecimal("10")) < 0) {
            BigDecimal ethAmount = new BigDecimal(String.valueOf(eth));
            BigInteger weiAmount = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger();
            TransactionReceipt receipt = weth9.transfer(subAddress, weiAmount).sendAsync().get();

            // 获取交易哈希
            String transactionHash = receipt.getTransactionHash();

            // 获取账户转换后余额
            BigDecimal trEtherBalance = getBalance(subAddress);
            log.info("子账户{}接收后余额是：{}，交易哈希是：{}", subAddress, trEtherBalance,
                transactionHash);
          } else {
            log.info("子账户{}余额大于等于1，直接跳过", subAddress);
          }

        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 补充WETH
   */
  public void addWeth(String eth) {
    try {
      // 加载WETH合约
      Web3j web3j = Web3jUtils.web3j;
      Weth9 weth9 = Weth9.load(WETH_CONTRACT_ADDRESS, web3j, Web3jUtils.credentials, gasProvider);
      String mainAddress = Web3jUtils.credentials.getAddress();
      // 批量将转发WETH
      ethTransfer.forEach((address, privateKey) -> {
        try {
          // 获取账户余额
          BigDecimal balance = getBalance(mainAddress);
          BigInteger nonce = Web3jUtils.getNonce(mainAddress);
          log.info("当前主钱包地址是：{}，钱包余额是：{}，nonce是：{}，要补充到的ETH上限是：{}",
              mainAddress,
              balance, nonce, eth);

          // 转换
          String subAddress = String.valueOf(address);
          BigDecimal subBanlance = getBalance(subAddress);
          BigDecimal amountPerRecipient = new BigDecimal(eth);
          if (subBanlance.compareTo(amountPerRecipient) < 0) {
            BigDecimal add = amountPerRecipient.subtract(subBanlance);
            log.info("当前子账户余额小于{}，开始补充{}个eth", amountPerRecipient, add);
            BigInteger weiAmount = Convert.toWei(add, Convert.Unit.ETHER).toBigInteger();
            TransactionReceipt receipt = weth9.transfer(subAddress, weiAmount).sendAsync().get();

            // 获取交易哈希
            String transactionHash = receipt.getTransactionHash();

            // 获取账户转换后余额
            BigDecimal trEtherBalance = getBalance(subAddress);
            log.info("子账户{}接收后余额是：{}，交易哈希是：{}", subAddress, trEtherBalance,
                transactionHash);
          } else {
            log.info("子账户{}余额大于等于{}，直接跳过", eth, subAddress);
          }

        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 提现
   */
  public void withdraw(String eth) {
    try {
      // 加载WETH合约
      Web3j web3j = Web3jUtils.web3j;
      Weth9 weth9 = Weth9.load(WETH_CONTRACT_ADDRESS, web3j, Web3jUtils.credentials, gasProvider);
      String mainAddress = Web3jUtils.credentials.getAddress();

      // 获取账户余额
      BigDecimal balance = getBalance(mainAddress);
      BigInteger nonce = Web3jUtils.getNonce(mainAddress);
      log.info("当前主钱包地址是：{}，钱包余额是：{}，nonce是：{}，要转换的ETH个数是：{}", mainAddress,
          balance, nonce, eth);

      // 转换
      BigDecimal ethAmount = new BigDecimal(String.valueOf(eth));
      BigInteger weiAmount = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger();
      TransactionReceipt receipt = weth9.withdraw(weiAmount).sendAsync().get();

      // 获取交易哈希
      String transactionHash = receipt.getTransactionHash();

      // 获取账户转换后余额
      BigDecimal trEtherBalance = getBalance(mainAddress);
      log.info("{}提现后余额是：{}，交易哈希是：{}", mainAddress, trEtherBalance, transactionHash);
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

  public static BigDecimal getBalance(String addressStr)
      throws InterruptedException, ExecutionException {
    Weth9 weth9 = Weth9.load(WETH_CONTRACT_ADDRESS, Web3jUtils.web3j,
        Credentials.create(addressStr),
        gasProvider);
    BigInteger weiBalance = weth9.balanceOf(addressStr).sendAsync().get();
    return Convert.fromWei(new BigDecimal(weiBalance), Convert.Unit.ETHER);
  }

}
