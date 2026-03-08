package top.flowerstardream.base.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static top.flowerstardream.base.exception.ExceptionEnum.*;

/**
 * Http 工具类
 */
@Slf4j
public class HttpClientUtil {

    static final int TIMEOUT_MSEC = 5 * 1000;

    /**
     * 发送 GET 方式请求
     * @param url
     * @param paramMap
     * @return
     */
    public static String doGet(String url, Map<String, String> paramMap) {
        try (HttpResponse response = HttpRequest.get(url)
                .form(paramMap.toString())
                .timeout(TIMEOUT_MSEC)
                .execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("GET 请求失败，url: {}", url, e);
            throw GET_REQUEST_FAILED.toException();
        }
    }

    /**
     * 发送 POST 方式请求（表单格式）
     * @param url
     * @param paramMap
     * @return
     */
    public static String doPost(String url, Map<String, String> paramMap) {
        try (HttpResponse response = HttpRequest.get(url)
                .form(paramMap.toString())
                .timeout(TIMEOUT_MSEC)
                .execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("POST 请求失败，url: {}", url, e);
            throw POST_REQUEST_FAILED.toException();
        }
    }

    /**
     * 发送 POST 方式请求（JSON 格式）
     * @param url
     * @param paramMap
     * @return
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) {
         JSONObject jsonObject = new JSONObject(paramMap);
         try (HttpResponse response = HttpRequest.post(url)
                .body(jsonObject.toString())
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT_MSEC)
                .execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("POST JSON 请求失败，url: {}", url, e);
            throw POST_REQUEST_FAILED.toException();
        }
    }
}
