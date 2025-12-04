package com.gstz.faucet.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class RpcUrlUtils {
    private static final Logger log = Logger.getLogger(RpcUrlUtils.class.getName());
    private static final java.util.Random random = new java.util.Random(System.currentTimeMillis());

    private static final List<String> BSC_URL_ARRAY = List.of(
        // "http://192.168.100.101:8545",
        // "http://192.168.100.108:8545",
        // "http://192.168.100.1:8545"
        // "https://bsc-rpc.publicnode.com"
        //     "http://127.0.0.1:8545"
            "http://178.63.243.234:8545"
    );

    private static final List<String> ETH_URL_ARRAY = List.of(
        "https://ethereum-rpc.publicnode.com"
        // "https://eth-mainnet.public.blastapi.io",
        // "https://gateway.tenderly.co/public/mainnet",
    );

    // 用于存储不同类型的 URL 队列
    private static final ConcurrentMap<String, ConcurrentLinkedQueue<String>> urlQueues = new ConcurrentHashMap<>();

    // refillQueue 用于随机打乱并填充队列
    private static void refillQueue(String urlType, List<String> urlList) {
        // 随机打乱 URL 顺序
        List<String> urls = new ArrayList<>(urlList);
        Collections.shuffle(urls, random);

        // 获取或创建队列
        ConcurrentLinkedQueue<String> queue = urlQueues.computeIfAbsent(urlType, k -> new ConcurrentLinkedQueue<>());

        // 清空并重新填充队列
        queue.clear();
        queue.addAll(urls);
        log.info(() -> String.format("Refilled queue for %s with %d URLs", urlType, Integer.valueOf(urls.size())));
    }

    // GetRpcURL 获取一个 URL，如果队列为空则重新填充
    public static String getRpcURL(String urlType) {
        // 获取队列
        ConcurrentLinkedQueue<String> queue = urlQueues.computeIfAbsent(urlType, k -> {
            log.info(() -> String.format("Created new queue for %s", urlType));
            return new ConcurrentLinkedQueue<>();
        });

        // 如果队列为空，则重新填充
        if (queue.isEmpty()) {
            log.info(() -> String.format("Queue for %s is empty, refilling...", urlType));
            switch (urlType) {
                case "BSC":
                    refillQueue(urlType, BSC_URL_ARRAY);
                    break;
                case "ETH":
                    refillQueue(urlType, ETH_URL_ARRAY);
                    break;
                default:
                    log.warning(() -> String.format("Unknown URL type: %s", urlType));
                    return "";
            }
        }

        // 获取并返回队列中的第一个 URL
        String url = queue.poll();
        log.info(() -> String.format("Retrieved URL %s from queue for %s", url, urlType));
        return url;
    }
}    