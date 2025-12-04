package com.gstz.faucet.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Description: 
 * Author: songw
 * Date: 2024/5/31 12:04
 */
public class PropertiesUtils {

  public static final String workPath = System.getProperty("user.dir");
  public static String separator = System.getProperty("file.separator");

  public static LinkedHashMap<String, String> getKeyValueLinkMapFromProperFile(String fileName) {
    LinkedHashMap<String, String> keyValueLinkedHashMap = new LinkedHashMap<>();
    try (BufferedReader reader = new BufferedReader(
        new FileReader(workPath + separator + fileName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        // 寻找第一个等号的位置
        int index = line.indexOf('=');
        if (index != -1) {
          // 将键值对分隔并添加到Properties对象中
          String key = line.substring(0, index).trim();
          String value = line.substring(index + 1).trim();
          keyValueLinkedHashMap.put(key, value);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return keyValueLinkedHashMap;
  }

  public static LinkedHashMap<String, String> getKeyValueLinkMapFromProperFile(String path,
      String fileName) {
    LinkedHashMap<String, String> keyValueLinkedHashMap = new LinkedHashMap<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(path + fileName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        // 寻找第一个等号的位置
        int index = line.indexOf('=');
        if (index != -1) {
          // 将键值对分隔并添加到Properties对象中
          String key = line.substring(0, index).trim();
          String value = line.substring(index + 1).trim();
          keyValueLinkedHashMap.put(key, value);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return keyValueLinkedHashMap;
  }

  public static ArrayList<String> getFileContextByReadLine(String fileName) {
    ArrayList<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(
        new FileReader(workPath + separator + fileName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
  }

  public static ArrayList<String> getFileContextByReadLine(String path, String fileName) {
    ArrayList<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(path + fileName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
  }

}
