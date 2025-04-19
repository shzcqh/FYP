package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class BaiduSentimentService {

    // 1. 对齐 application.yml 中的配置路径
    //    application.yml 示例：
    //    baidu:
    //      nlp:
    //        api-key: YOUR_API_KEY
    //        secret-key: YOUR_SECRET_KEY

    @Value("${baidu.nlp.api-key}")
    private String apiKey;

    @Value("${baidu.nlp.secret-key}")
    private String secretKey;

    private final RestTemplate rest;

    // 缓存 token 和过期时间
    private String accessToken;
    private long expiresAt;

    public BaiduSentimentService(RestTemplate rest) {
        this.rest = rest;
    }

    /**
     * 2. 获取或刷新 access_token，打印原始返回并做简单校验
     */
    private String getAccessToken() {
        // 如果缓存还没过期，直接返回
        if (accessToken != null && System.currentTimeMillis() < expiresAt) {
            return accessToken;
        }

        String url = "https://aip.baidubce.com/oauth/2.0/token"
                + "?grant_type=client_credentials"
                + "&client_id="     + apiKey
                + "&client_secret=" + secretKey;

        // 打印即将调用的 token 接口
        System.out.println(">>> [Baidu.getAccessToken] 调用 URL = " + url);

        // 调用并打印原始返回
        Map<?,?> resp = rest.getForObject(url, Map.class);
        System.out.println(">>> [Baidu.getAccessToken] 原始返回 = " + resp);

        // 简单错误判定
        if (resp == null || resp.get("access_token") == null) {
            throw new RuntimeException("无法获取百度 AccessToken，请检查 API Key/Secret Key；返回值 = " + resp);
        }

        // 缓存起来，并提前 60s 刷新
        accessToken = (String) resp.get("access_token");
        int expiresIn = ((Number) resp.get("expires_in")).intValue();
        expiresAt = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
        return accessToken;
    }

    /**
     * 3. 调用情感分析接口，并打印 URL/Body/Raw Response
     */
    public SentimentResult analyze(String text) {
        System.out.println(">>> [Baidu.analyze] 待分析文本 = " + text);

        // 3.1 取 token
        String token = getAccessToken();

        // 3.2 拼 URL
        String url = "https://aip.baidubce.com/rpc/2.0/nlp/v1/sentiment_classify"
                + "?charset=UTF-8&access_token=" + token;
        System.out.println(">>> [Baidu.analyze] 调用 URL  = " + url);

        // 3.3 构造请求体
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String,String> body = Map.of("text", text);
        HttpEntity<Map<String,String>> req = new HttpEntity<>(body, headers);
        System.out.println(">>> [Baidu.analyze] 请求 Body = " + body);

        // 3.4 发送并打印原始返回
        Map<?,?> resp = rest.postForObject(url, req, Map.class);
        System.out.println(">>> [Baidu.analyze] 原始返回 = " + resp);

        // 3.5 解析结果
        List<?> items = (List<?>) resp.get("items");
        Map<?,?> item = (Map<?,?>) items.get(0);
        int polarity   = ((Number) item.get("sentiment")).intValue();
        double posProb = ((Number) item.get("positive_prob")).doubleValue();
        double negProb = ((Number) item.get("negative_prob")).doubleValue();

        return new SentimentResult(polarity, posProb, negProb);
    }
}
