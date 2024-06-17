package com.gstz.faucet.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

/**
 * Description: 
 * Author: songw
 * Date: 2024/5/11 12:02
 */

@Slf4j
public class Web3jUtils {

  private static final String INFURA_URL = "infura_url";
  private static final String PRIVATE_KEY = "private_key";
  private static final String GASLIMIT = "gaslimit";
  private static final String MAX_PRIORITY_FEE_PER_GAS = "maxPriorityFeePerGas";
  private static final String MAX_FEE_PER_GAS = "maxFeePerGas";
  private static final String GASPRICE = "gasPrice";
  private static final String WALLETPW = "metamaskpw";
  private static final String MNEMONIC = "mnemonic";
  public static Web3j web3j;
  public static Credentials credentials;
  public static String gaslimit;
  public static String maxPriorityFeePerGas;
  public static String maxFeePerGas;
  public static String gasPrice;
  public static String walletpw;
  public static List<String> mnemonics;
  public static String workPath;
  public static String separator;

  static {
    workPath = System.getProperty("user.dir");
    separator = System.getProperty("file.separator");
    Properties properties = new Properties();
    try (FileInputStream addressFis = new FileInputStream(
        workPath + separator + "web3jconfig.properties")) {
      properties.load(addressFis);
    } catch (IOException e) {
      e.printStackTrace();
    }
    // 加载Web3j实例，连接到Holesky测试网络
    web3j = Web3j.build(new HttpService(String.valueOf(properties.get(INFURA_URL))));
    // 加载凭证对象
    credentials = Credentials.create(String.valueOf(properties.get(PRIVATE_KEY)));
    // 加载gaslimit
    gaslimit = String.valueOf(properties.get(GASLIMIT));
    // 加载maxPriorityFeePerGas
    maxPriorityFeePerGas = String.valueOf(properties.get(MAX_PRIORITY_FEE_PER_GAS));
    // 加载maxFeePerGas
    maxFeePerGas = String.valueOf(properties.get(MAX_FEE_PER_GAS));
    // 加载gasPrice
    gasPrice = String.valueOf(properties.get(GASPRICE));
    // 加载钱包密码
    walletpw = String.valueOf(properties.get(WALLETPW));
    String mnemonic= String.valueOf(properties.get(MNEMONIC));
    StringTokenizer tokenizer = new StringTokenizer(mnemonic, " ");
    mnemonics = new ArrayList<>();
    while (tokenizer.hasMoreTokens()) {
      mnemonics.add(tokenizer.nextToken());
    }

  }

  public static BigInteger getNonce(String address) {
    BigInteger nonce = null;
    try {
      nonce = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send()
          .getTransactionCount();
      log.info("{}当前最新的nonce是：{}", address, nonce);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return nonce;
  }

  public static long getChainId() {
    long id;
    try {
      EthChainId ethChainId = web3j.ethChainId().send();
      BigInteger chainId = ethChainId.getChainId();
      id = Long.parseLong(String.valueOf(chainId));
      log.info("chainId是：{}", id);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return id;
  }

  public static BigDecimal getEtherBalance(String addressStr)
      throws InterruptedException, ExecutionException {
    EthGetBalance balance = web3j.ethGetBalance(addressStr, DefaultBlockParameterName.LATEST)
        .sendAsync().get();
    BigInteger weiBalance = balance.getBalance();
    return Convert.fromWei(new BigDecimal(weiBalance), Convert.Unit.ETHER);
  }

  public static BigDecimal getGasFee() {
    BigInteger gasLimit = new BigInteger(gaslimit);
    BigInteger feePerGas = new BigInteger(maxFeePerGas);
    return Convert.fromWei(String.valueOf(gasLimit.multiply(feePerGas)), Unit.ETHER);
  }

  public static void getPendTransactionCount() {
    try {
      // 获取交易笔数
      BigInteger pendCount;
      EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
          credentials.getAddress(), DefaultBlockParameterName.PENDING).send();
      if (ethGetTransactionCount == null) {
        return;
      }
      pendCount = ethGetTransactionCount.getTransactionCount();
      log.info("待打包的交易数量有：{}", pendCount);
    } catch (Exception ex) {
      // 报错应进行错误处理
      ex.printStackTrace();
    }
  }

  public static void getAllPendTransactions() {
    try {
      // 创建过滤器来获取待处理的交易
      EthFilter ethFilter = new EthFilter(DefaultBlockParameterName.PENDING,
          DefaultBlockParameterName.PENDING, credentials.getAddress());

      // 获取符合过滤器条件的待处理交易
      EthLog ethLog = web3j.ethGetLogs(ethFilter).send();

      // 打印待处理交易列表
      ethLog.getLogs().forEach(logs -> {
        log.info("Pending Transaction Hash: {}", logs.get());
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static BigInteger getBaseFee() {
    try {
      EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
      EthBlock.Block block = web3j.ethGetBlockByNumber(
          DefaultBlockParameter.valueOf(blockNumber.getBlockNumber()), true).send().getBlock();
      BigInteger baseFeePerGas = block.getBaseFeePerGas();
      BigDecimal bigDecimal = Convert.fromWei(new BigDecimal(baseFeePerGas), Convert.Unit.ETHER);
      log.info("BaseFee是：{}，换算为：{}个ETH", baseFeePerGas, bigDecimal);
      return baseFeePerGas;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static BigInteger getMaxPriorFee() {
    BigInteger fee = DefaultGasProvider.GAS_LIMIT;
    try {
      EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
      EthTransaction ethTransaction = web3j.ethGetTransactionByBlockNumberAndIndex(
          DefaultBlockParameter.valueOf(blockNumber.getBlockNumber()), BigInteger.ONE).send();
      Transaction transaction = ethTransaction.getTransaction().get();
      String maxPriorityFeePerGas = transaction.getMaxPriorityFeePerGasRaw();
      if (maxPriorityFeePerGas != null) {
        new BigInteger(maxPriorityFeePerGas.substring(2), 16);
      }
      log.info("MaxPriorityFee是:{}", maxPriorityFeePerGas);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    log.info("转换后的MaxPriorityFee是:{}", fee);
    return fee;
  }

  public static BigInteger getMaxFeePerFee() {
    BigInteger fee = DefaultGasProvider.GAS_LIMIT;
    try {
      EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
      EthTransaction ethTransaction = web3j.ethGetTransactionByBlockNumberAndIndex(
          DefaultBlockParameter.valueOf(blockNumber.getBlockNumber()), BigInteger.ONE).send();
      Transaction transaction = ethTransaction.getTransaction().get();
      String maxFeePerGas = transaction.getMaxFeePerGasRaw();
      if (maxFeePerGas != null) {
        new BigInteger(maxFeePerGas.substring(2), 16);
      }
      log.info("MaxFeePer是:{}", maxFeePerGas);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    log.info("转换后的MaxFeePer是:{}", fee);
    return fee;
  }

}
