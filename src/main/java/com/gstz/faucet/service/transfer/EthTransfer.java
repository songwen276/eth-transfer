package com.gstz.faucet.service.transfer;

import com.gstz.faucet.utils.Web3jUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

/**
 * Description:
 * Author: songw
 * Date: 2024/5/7 11:13
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class EthTransfer {

  // 合约地址
  private static final String WETH_CONTRACT_ADDRESS = "0x94373a4919B3240D86eA41593D5eBa789FEF3848";

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
   * 批量发送特定eth到合约
   */
  public void ethTransfer(String eth) {
    // 批量转换ETH为WETH
    ethTransfer.forEach((address, privateKey) -> {
      try {
        // 获取账户余额和nonce
        String addressStr = String.valueOf(address);
        String privateKeyStr = String.valueOf(privateKey);
        BigDecimal etherBalance = Web3jUtils.getEtherBalance(addressStr);
        BigInteger nonce = Web3jUtils.getNonce(addressStr);
        log.info("当前要转换的钱包地址是：{}，钱包余额是：{}，nonce是：{}，要转换的ETH个数是：{}",
            addressStr, etherBalance, nonce, eth);

        TransactionReceipt transactionReceipt = Transfer.sendFundsEIP1559(Web3jUtils.web3j,
            Credentials.create(privateKeyStr), WETH_CONTRACT_ADDRESS,
            new BigDecimal(String.valueOf(eth)), Convert.Unit.ETHER,
            new BigInteger(Web3jUtils.gaslimit), new BigInteger(Web3jUtils.maxPriorityFeePerGas),
            new BigInteger(Web3jUtils.maxFeePerGas)).sendAsync().get();
        String transactionHash = transactionReceipt.getTransactionHash();
        log.info("交易已发送，交易哈希：{}", transactionHash);

        // 获取账户转换后余额与nonce
        BigDecimal trEtherBalance = Web3jUtils.getEtherBalance(addressStr);
        BigInteger trNonce = Web3jUtils.getNonce(addressStr);
        log.info("{}转换后余额是：{}，nonce是：{}", addressStr, trEtherBalance, trNonce);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

  }

  /**
   * 主账号发送原始交易到合约-function
   */
  public static void sendRawFuncTransactions() {
    try {
      // 手续费
      BigInteger gasPrice = new BigInteger(Web3jUtils.gasPrice);
      BigInteger gasLimit = new BigInteger(Web3jUtils.gaslimit);

      // 构造交易内容
      BigInteger bigInteger = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger();
      Function function = new Function("transfer",
          Arrays.asList(new Address(Web3jUtils.credentials.getAddress()), new Uint256(bigInteger)),
          Collections.singletonList(new TypeReference<Type>() {
          }));
      String encodedFunction = FunctionEncoder.encode(function);

      // 创建交易对象
      RawTransaction rawTransaction = RawTransaction.createTransaction(new BigInteger("21"),
          gasPrice, gasLimit, WETH_CONTRACT_ADDRESS, encodedFunction);

      // 进行签名操作
      byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, Web3jUtils.getChainId(),
          Web3jUtils.credentials);
      String hexValue = Numeric.toHexString(signMessage);

      // 发起交易
      EthSendTransaction ethSendTransaction = Web3jUtils.web3j.ethSendRawTransaction(hexValue)
          .sendAsync().get();
      String hash = ethSendTransaction.getTransactionHash();
      if (hash != null) {
        log.info("执行成功：{}", hash);
      }
    } catch (Exception ex) {
      // 报错应进行错误处理
      ex.printStackTrace();
    }

  }

  /**
   * 主账号发送原始交易到合约
   */
  public static void sendRawTransaction() {
    long chainId = Web3jUtils.getChainId();
    try {
      // 创建原始交易
      RawTransaction rawTransaction = RawTransaction.createEtherTransaction(chainId,
          new BigInteger(String.valueOf(1)), new BigInteger(Web3jUtils.gaslimit),
          WETH_CONTRACT_ADDRESS, new BigInteger("1"),
          new BigInteger(Web3jUtils.maxPriorityFeePerGas), new BigInteger(Web3jUtils.maxFeePerGas));

      // 对交易进行签名
      byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId,
          Web3jUtils.credentials);
      String hexValue = Numeric.toHexString(signedMessage);

      // 发送签名后的交易
      EthSendTransaction ethSendTransaction = Web3jUtils.web3j.ethSendRawTransaction(hexValue)
          .sendAsync().get();
      String hash = ethSendTransaction.getTransactionHash();
      if (hash != null) {
        log.info("执行成功：{}", hash);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
