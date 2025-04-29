package com.example.service;

import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BaiduSentimentService {

    private static final Logger log = LoggerFactory.getLogger(BaiduSentimentService.class);

    @Value("${baidu.nlp.api-key}")
    private String apiKey;

    @Value("${baidu.nlp.secret-key}")
    private String secretKey;

    /* ------------ 内部缓存 ------------- */
    private String accessToken;
    private long   expiresAt;

    /* ------------ 对外结果 ------------- */
    public static class SentimentResult {
        public boolean success;
        public int sentiment;         // 0=neg,1=neu,2=pos
        public double posProb;
        public double negProb;

        public int getSentiment() {
            return sentiment;
        }

        public double getPositiveProb() {
            return posProb;
        }

        public double getNegativeProb() {
            return negProb;
        }
    }

    /* ------------ 入口 ------------- */
    public SentimentResult analyze(String rawHtml) {
        SentimentResult res = new SentimentResult();

        try {
            /* 1. 清洗 & 截断 */
            String plain = HtmlUtil.cleanHtmlTag(rawHtml)   // 去掉所有 HTML 标签
                    .replace("&nbsp;", " ")   // 去 &nbsp;
                    .replaceAll("\\s+", " ")  // 压缩多空白
                    .trim();

            if (plain.length() > 1024) {
                plain = plain.substring(0, 1024);
            }

            /* 2. 调用百度接口 */
            String url = "https://aip.baidubce.com/rpc/2.0/nlp/v1/sentiment_classify"
                    + "?charset=UTF-8&access_token=" + getAccessToken();

            String jsonReq = JSONUtil.createObj().set("text", plain).toString();
            String raw     = HttpUtil.post(url, jsonReq, 5000);    // 5s 超时

            JSONObject jo = JSONUtil.parseObj(raw);
            if (!jo.containsKey("items") || jo.getJSONArray("items").isEmpty()) {
                log.warn("Baidu NLP 无 items 字段或为空，raw={}", raw);
                return res;    // success=false
            }

            JSONObject first = jo.getJSONArray("items").getJSONObject(0);
            res.sentiment = first.getInt("sentiment");
            res.posProb   = first.getDouble("positive_prob");
            res.negProb   = first.getDouble("negative_prob");
            res.success   = true;
            return res;
        } catch (Exception e) {
            log.warn("Baidu NLP 调用失败：{}", e.toString());
            return res;     // success=false
        }
    }

    /* ------------ token 获取 ------------- */
    private String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < expiresAt) {
            return accessToken;
        }
        String url = "https://aip.baidubce.com/oauth/2.0/token"
                + "?grant_type=client_credentials"
                + "&client_id=" + apiKey
                + "&client_secret=" + secretKey;

        String raw = HttpUtil.get(url);
        JSONObject jo = JSONUtil.parseObj(raw);
        if (!jo.containsKey("access_token")) {
            throw new RuntimeException("获取百度 token 失败: " + raw);
        }
        accessToken = jo.getStr("access_token");
        int ttl     = jo.getInt("expires_in", 0);
        expiresAt   = System.currentTimeMillis() + (ttl - 60) * 1000L; // 提前 60s 刷新
        return accessToken;
    }
}
