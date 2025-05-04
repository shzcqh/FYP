package com.example.service;

import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

import java.time.Instant;

@Service
public class BaiduSentimentService {

    private static final Logger log = LoggerFactory.getLogger(BaiduSentimentService.class);

    /* ---------- Baidu NLP Sentiment Analysis ---------- */
    @Value("${baidu.nlp.api-key}")
    private String apiKey;
    @Value("${baidu.nlp.secret-key}")
    private String secretKey;

    /* ---------- Token Cache ---------- */
    private String accessToken;
    private long   expiresAt;

    /* ---------- Result Object ---------- */
    public static class SentimentResult {
        public boolean success;
        /** 0 = negative, 1 = neutral, 2 = positive */
        public int sentiment;
        public double posProb;
        public double negProb;

        public int getSentiment()     { return sentiment; }
        public double getPositiveProb() { return posProb; }
        public double getNegativeProb() { return negProb; }
    }

    /* =========================================================
       Main entry: bilingual smart sentiment analysis
       ========================================================= */
    public SentimentResult analyseSmart(String rawHtml) {
        // 1. Clean & truncate
        String plain = HtmlUtil.cleanHtmlTag(rawHtml)
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (plain.length() > 1024) plain = plain.substring(0, 1024);

        // 2. Translate if text contains English letters
        String toAnalyze = plain;
        if (containsEnglish(plain)) {
            try {
                toAnalyze = translateToZh(plain);
            } catch (Exception e) {
                log.warn("Translation failed, fallback to original text: {}", e.getMessage());
                toAnalyze = plain;
            }
        }
        log.debug("Text to analyze for sentiment: {}", toAnalyze);

        // 3. Call sentiment API
        SentimentResult res = doSentiment(toAnalyze);
        if (!res.success) {
            // retry once with the original text
            SentimentResult retry = doSentiment(plain);
            if (retry.success) {
                return retry;
            }
            retry.success   = true;
            retry.sentiment = 1;
            retry.posProb   = 0.5;
            retry.negProb   = 0.5;
            return retry;
        }
        return res;
    }

    /* =========================================================
       Call Baidu NLP sentiment API
       ========================================================= */
    private SentimentResult doSentiment(String textZH) {
        SentimentResult res = new SentimentResult();
        try {
            String url = "https://aip.baidubce.com/rpc/2.0/nlp/v1/sentiment_classify"
                    + "?charset=UTF-8&access_token=" + getAccessToken();

            log.debug("Calling Baidu NLP API with text: {}", textZH);
            String jsonReq = JSONUtil.createObj().set("text", textZH).toString();
            String raw     = HttpUtil.post(url, jsonReq, 5000);
            log.debug("Baidu NLP API raw response: {}", raw);

            JSONObject jo = JSONUtil.parseObj(raw);
            if (!jo.containsKey("items") || jo.getJSONArray("items").isEmpty()) {
                log.warn("Baidu NLP returned no items: {}", raw);
                return res;
            }

            JSONObject first = jo.getJSONArray("items").getJSONObject(0);
            Integer label    = first.getInt("sentiment", -1);
            if (label == null || label < 0) {
                log.warn("Invalid sentiment field from Baidu NLP: {}", raw);
                return res;
            }
            res.sentiment = label;
            res.posProb   = first.getDouble("positive_prob", 0.0);
            res.negProb   = first.getDouble("negative_prob", 0.0);
            res.success   = true;
        } catch (Exception e) {
            log.warn("Baidu NLP API call failed: {}", e.toString());
        }
        return res;
    }

    /* =========================================================
       Detect English letters
       ========================================================= */
    private boolean containsEnglish(String s) {
        return s != null && s.matches(".*[A-Za-z].*");
    }

    /* =========================================================
       Call Baidu Translate API (new RPC endpoint)
       ========================================================= */
    private String translateToZh(String text) throws Exception {
        String token = getAccessToken();
        String url   = "https://aip.baidubce.com/rpc/2.0/mt/texttrans/v1?access_token=" + token;

        JSONObject bodyJson = JSONUtil.createObj()
                .set("q", text)
                .set("from", "auto")
                .set("to", "zh");

        String raw = HttpUtil.post(url, bodyJson.toString(), 5000);
        log.debug("Baidu MT raw response: {}", raw);

        JSONObject jo = JSONUtil.parseObj(raw);
        if (jo.containsKey("error_code")) {
            throw new RuntimeException("Baidu MT API error: " + raw);
        }


        JSONObject resultObj  = jo.getJSONObject("result");
        if (resultObj == null) {
            throw new RuntimeException("Baidu MT no result field: " + raw);
        }
        String dst = resultObj
                .getJSONArray("trans_result")
                .getJSONObject(0)
                .getStr("dst");


        log.debug("Translation result: {}", dst);
        return dst;
    }


    /* =========================================================
       Retrieve & cache access‑token
       ========================================================= */
    private String getAccessToken() {
        log.info("Using apiKey={}, secretKey={}", apiKey, secretKey);

        // 1. 缓存还有效就直接返回
        if (accessToken != null && System.currentTimeMillis() < expiresAt) {
            return accessToken;
        }

        // 2. The three parameters are directly spelled into the query string (Baidu gateway only recognizes this way of writing)
        String url = String.format(
                "https://aip.baidubce.com/oauth/2.0/token" +
                        "?grant_type=client_credentials" +
                        "&client_id=%s" +
                        "&client_secret=%s",
                cn.hutool.core.util.URLUtil.encodeQuery(apiKey.trim()),
                cn.hutool.core.util.URLUtil.encodeQuery(secretKey.trim())
        );

        // 3. GET 调用
        String raw = cn.hutool.http.HttpUtil.get(url, 5000);
        log.debug("Token resp raw = {}", raw);

        JSONObject jo = JSONUtil.parseObj(raw);
        if (jo.containsKey("error")) {
            throw new IllegalStateException(
                    "获取百度 access_token 失败: "
                            + jo.getStr("error_description") + " | resp=" + raw);
        }

        accessToken = jo.getStr("access_token");
        int ttl     = jo.getInt("expires_in", 0);          // 秒
        expiresAt   = System.currentTimeMillis() + (ttl - 60) * 1000L;
        return accessToken;
    }
}
