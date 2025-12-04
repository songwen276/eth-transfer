package com.gstz.faucet.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Description: 
 * Author: songw
 * Date: 2024/5/31 12:04
 */
public class FileUtils {

  public static void saveToFile(List<String> data, String fileName) {
    try (BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(Files.newOutputStream(Paths.get(fileName)),
            StandardCharsets.UTF_8))) {
      for (String line : data) {
        writer.write(line + System.lineSeparator());
      }
      System.out.println("输出到文件成功：路径为：" + fileName);
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("输出到文件异常：" + fileName);
    }
  }

}
