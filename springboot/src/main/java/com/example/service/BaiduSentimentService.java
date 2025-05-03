package com.example.service;

import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class BaiduSentimentService {

    private static final Logger log = LoggerFactory.getLogger(BaiduSentimentService.class);

    /* ---------- Baidu NLP 情感 ---------- */
    @Value("${baidu.nlp.api-key}")
    private String apiKey;
    @Value("${baidu.nlp.secret-key}")
    private String secretKey;

    /* ---------- Baidu 翻译（可与上面用同一账户） ---------- */
    @Value("${baidu.translate.app-id}")
    private String transAppId;               // ★★ 新增
    @Value("${baidu.translate.secret}")
    private String transSecret;              // ★★ 新增

    /* ---------- token 缓存 ---------- */
    private String accessToken;
    private long   expiresAt;

    /* ---------- 结果对象 ---------- */
    public static class SentimentResult {
        public boolean success;
        public int sentiment;        // 0 neg / 1 neu / 2 pos
        public double posProb;
        public double negProb;


        public int getSentiment() {
            return 0;
        }
        public double getPositiveProb() {
            return posProb;
        }

        public double getNegativeProb() {
            return negProb;
        }
    }

    /* =========================================================
       主入口 —— 建议新增 analyseSmart 兼容英文
       ========================================================= */
    public SentimentResult analyseSmart(String rawHtml) {
        // 1. 先做基础清洗
        String plain = HtmlUtil.cleanHtmlTag(rawHtml)
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (plain.length() > 1024) plain = plain.substring(0, 1024);

        // 2. 简单检测是否含英文
        if (containsEnglish(plain)) {                         // ★★ 新增
            try {
                plain = translateToZh(plain);                // ★★ 先翻译
            } catch (Exception e) {
                log.warn("翻译失败，直接用原文本做情感分析: {}", e.toString());
            }
        }
        // 3. 调原有情感分析流程
        return doSentiment(plain);
    }

    /* =========================================================
       私有方法：真正调用百度情感接口
       ========================================================= */
    private SentimentResult doSentiment(String textZH) {
        SentimentResult res = new SentimentResult();
        try {
            String url = "https://aip.baidubce.com/rpc/2.0/nlp/v1/sentiment_classify"
                    + "?charset=UTF-8&access_token=" + getAccessToken();

            String jsonReq = JSONUtil.createObj().set("text", textZH).toString();
            String raw     = HttpUtil.post(url, jsonReq, 5000);

            JSONObject jo = JSONUtil.parseObj(raw);
            if (!jo.containsKey("items") || jo.getJSONArray("items").isEmpty()) {
                log.warn("Baidu NLP 返回异常: {}", raw);
                return res;
            }

            JSONObject first = jo.getJSONArray("items").getJSONObject(0);
            res.sentiment = first.getInt("sentiment");
            res.posProb   = first.getDouble("positive_prob");
            res.negProb   = first.getDouble("negative_prob");
            res.success   = true;
        } catch (Exception e) {
            log.warn("Baidu NLP 调用失败: {}", e.toString());
        }
        return res;
    }

    /* =========================================================
       判断是否包含英文字符
       ========================================================= */
    private boolean containsEnglish(String s){               // ★★ 新增
        return s.matches(".*[A-Za-z].*");
    }

    /* =========================================================
       调用百度翻译 API，把英文翻译成中文
       ========================================================= */
    private String translateToZh(String text) throws Exception { // ★★ 新增
        // ---- 构造签名（百度翻译标准流程）----
        String salt = String.valueOf(System.currentTimeMillis());
        String signRaw = transAppId + text + salt + transSecret;
        String sign = cn.hutool.crypto.digest.DigestUtil.md5Hex(signRaw);

        String url = "https://fanyi-api.baidu.com/api/trans/vip/translate"
                + "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8.name())
                + "&from=auto&to=zh"
                + "&appid=" + transAppId
                + "&salt=" + salt
                + "&sign=" + sign;

        String raw = HttpUtil.get(url, 5000);
        JSONObject jo = JSONUtil.parseObj(raw);
        if (!jo.containsKey("trans_result")) {
            throw new RuntimeException("百度翻译失败: " + raw);
        }
        return jo.getJSONArray("trans_result")
                .getJSONObject(0)
                .getStr("dst");
    }

    /* =========================================================
       获取 / 缓存 NLP access_token（与你原来的逻辑一致）
       ========================================================= */
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
        accessToken = jo.getStr("access_token");
        int ttl     = jo.getInt("expires_in", 0);
        expiresAt   = System.currentTimeMillis() + (ttl - 60) * 1000L;
        return accessToken;
    }
}
