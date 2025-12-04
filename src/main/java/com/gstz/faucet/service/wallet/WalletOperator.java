package com.gstz.faucet.service.wallet;

import com.gstz.faucet.utils.PropertiesUtils;
import com.gstz.faucet.utils.Web3jUtils;
import java.io.File;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.springframework.stereotype.Component;

/**
 * Description: 
 * Author: songw
 * Date: 2024/6/14 15:00
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletOperator {

  // 要转换ETH的地址和金额
  static LinkedHashMap<String, String> ethTransfer;

  static {
    ethTransfer = PropertiesUtils.getKeyValueLinkMapFromProperFile("transferaddress.properties");
  }

  /**
   * 导入钱包到metamask
   */
  public void importAccountToMetamask() {
    // 创建EdgeDriver实例
    System.setProperty("webdriver.edge.driver",
        "D:\\Development\\Software\\Driver\\msedgedriver.exe");
    EdgeOptions edgeOptions = new EdgeOptions();
    edgeOptions.addArguments("--start-maximized");
    // edgeOptions.addArguments("--headless");
    // 设置自定义用户数据目录
    // String userDataDir = workPath+separator+"src\\main\\resources\\edgeDataDir";
    // String userDataDir = "C:\\Users\\songw\\AppData\\Local\\Microsoft\\Edge\\User Data";
    // edgeOptions.addArguments("user-data-dir=" + userDataDir);
    // 添加扩展的路径
    File file = new File(
        "C:\\Users\\songw\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\Extensions\\ejbalbakoplchlghecdalmeeeajnimhm\\11.12.2_0.crx");
    edgeOptions.addExtensions(file);
    WebDriver driver = new EdgeDriver(edgeOptions);

    try {
      // 打开路由器管理页面
      driver.get("extension://ejbalbakoplchlghecdalmeeeajnimhm/home.html");
      Thread.sleep(3000);

      // 初始化
      WebElement agreeProf = driver.findElement(By.id("onboarding__terms-checkbox"));
      agreeProf.click();
      Thread.sleep(500);
      WebElement importCurrButton = driver.findElement(
          By.cssSelector("input[data-testid='onboarding-import-wallet']"));
      importCurrButton.click();
      Thread.sleep(500);
      WebElement noThanks = driver.findElement(
          By.cssSelector("button[data-testid='metametrics-no-thanks']"));
      noThanks.click();
      Thread.sleep(500);
      for (int i = 0; i < 12; i++) {
        WebElement mnemonic = driver.findElement(
            By.cssSelector("input[data-testid='import-srp__srp-word-" + i + "']"));
        mnemonic.sendKeys(Web3jUtils.mnemonics.get(i));
      }
      Thread.sleep(500);
      WebElement initImport = driver.findElement(
          By.cssSelector("button[data-testid='import-srp-confirm']"));
      initImport.click();

      // 输入用户名和密码并提交（根据实际页面元素ID或名称进行修改）
      WebElement passwordField = driver.findElement(By.id("password"));
      WebElement loginButton = driver.findElement(
          By.cssSelector("button[data-testid='unlock-submit']"));

      passwordField.sendKeys(Web3jUtils.walletpw);
      loginButton.click();
      Thread.sleep(2500);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // 关闭浏览器
      driver.quit();
    }
  }

}
