package top.flowerstardream.base.utils;

import cn.hutool.json.JSONObject;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.MalformedMessageException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.exception.BizException;
import top.flowerstardream.base.properties.WeChatProperties;

import java.math.BigDecimal;

import static top.flowerstardream.base.exception.ExceptionEnum.PAYMENT_ERROR;

/**
 * 微信支付工具类 (适配 wechatpay-java SDK)
 * @author 花海
 */
@Slf4j
@RequiredArgsConstructor
public class WeChatPayUtil {

    private final WeChatProperties weChatProperties;

    private final JsapiService jsapiService;

    private final JsapiServiceExtension jsapiServiceExtension;

    private final RefundService refundService;

    /**
     * 小程序支付 - 调起支付
     * 使用 JsapiServiceExtension.prepayWithRequestPayment 自动完成二次签名
     *
     * @param orderNum    商户订单号
     * @param total       金额，单位 元
     * @param description 商品描述
     * @param openid      微信用户的openid
     * @return 前端调起支付所需的参数
     */
    public JSONObject pay(String orderNum, BigDecimal total, String description, String openid) {
        try {
            // 构建预支付请求
            PrepayRequest request = prepayReq(orderNum, total, description, openid);

            // 调用扩展接口，自动完成二次签名，返回调起支付所需参数
            PrepayWithRequestPaymentResponse response = jsapiServiceExtension.prepayWithRequestPayment(request);

            // 构造返回数据
            JSONObject result = new JSONObject();
            result.set("appId", response.getAppId());
            result.set("timeStamp", response.getTimeStamp());
            result.set("nonceStr", response.getNonceStr());
            // 注意：getPackageVal() 对应 package 字段
            result.set("package", response.getPackageVal());
            result.set("signType", response.getSignType());
            result.set("paySign", response.getPaySign());

            log.info("微信支付下单成功，订单号：{}，预支付ID：{}", orderNum, response.getPackageVal());
            return result;

        } catch (HttpException e) {  // 发送 HTTP 请求失败
            log.error("微信支付HTTP请求失败，订单号：{}", orderNum, e);
            throw new BizException(PAYMENT_ERROR, "微信支付请求失败：" + e.getMessage());
        } catch (ServiceException e) {  // 微信返回业务错误
            log.error("微信支付业务错误，订单号：{}，错误码：{}，错误信息：{}",
                    orderNum, e.getErrorCode(), e.getErrorMessage(), e);
            throw new BizException(PAYMENT_ERROR, "微信支付错误：" + e.getErrorMessage());
        } catch (MalformedMessageException e) {  // 返回消息格式错误
            log.error("微信支付返回消息格式错误，订单号：{}", orderNum, e);
            throw new BizException(PAYMENT_ERROR, "微信支付响应格式错误");
        }
    }

    /**
     * JSAPI 下单（原始接口，如需直接获取 prepay_id 使用）
     *
     * @param orderNum    商户订单号
     * @param total       总金额（元）
     * @param description 商品描述
     * @param openid      微信用户的openid
     * @return prepay_id
     */
    public String jsapi(String orderNum, BigDecimal total, String description, String openid) {
        try {
            PrepayRequest request = prepayReq(orderNum, total, description, openid);
            // 调用基础接口，只返回 prepay_id
            PrepayResponse response = jsapiService.prepay(request);
            return response.getPrepayId();

        } catch (Exception e) {
            log.error("微信JSAPI下单失败，订单号：{}", orderNum, e);
            throw new RuntimeException("微信下单失败：" + e.getMessage());
        }
    }

    // 构建预支付请求
    private PrepayRequest prepayReq(String orderNum, BigDecimal total, String description, String openid) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(weChatProperties.getAppid());
        request.setMchid(weChatProperties.getMchid());
        request.setDescription(description);
        request.setOutTradeNo(orderNum);
        request.setNotifyUrl(weChatProperties.getNotifyUrl());

        Amount amount = new Amount();
        amount.setTotal(total.multiply(new BigDecimal(100)).intValue());
        request.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);
        return request;
    }

    /**
     * 申请退款
     *
     * @param outTradeNo  商户订单号
     * @param outRefundNo 商户退款单号
     * @param refund      退款金额（元）
     * @param total       原订单金额（元）
     * @return 退款结果
     */
    public Refund refund(String outTradeNo, String outRefundNo, BigDecimal refund, BigDecimal total) {
        try {
            CreateRequest request = new CreateRequest();
            request.setOutTradeNo(outTradeNo);
            request.setOutRefundNo(outRefundNo);

            // 设置金额
            AmountReq amountReq = new AmountReq();
            amountReq.setRefund((long) refund.multiply(new BigDecimal(100)).intValue());
            amountReq.setTotal((long) total.multiply(new BigDecimal(100)).intValue());
            amountReq.setCurrency("CNY");
            request.setAmount(amountReq);

            request.setNotifyUrl(weChatProperties.getRefundNotifyUrl());

            Refund result = refundService.create(request);
            log.info("微信退款申请成功，订单号：{}，退款单号：{}，状态：{}",
                    outTradeNo, outRefundNo, result.getStatus());
            return result;

        } catch (HttpException e) {
            log.error("微信退款HTTP请求失败，订单号：{}", outTradeNo, e);
            throw new BizException(PAYMENT_ERROR, "微信退款请求失败：" + e.getMessage());
        } catch (ServiceException e) {
            log.error("微信退款业务错误，订单号：{}，错误码：{}，错误信息：{}",
                    outTradeNo, e.getErrorCode(), e.getErrorMessage(), e);
            throw new BizException(PAYMENT_ERROR, "微信退款错误：" + e.getErrorMessage());
        } catch (MalformedMessageException e) {
            log.error("微信退款返回消息格式错误，订单号：{}", outTradeNo, e);
            throw new BizException(PAYMENT_ERROR, "微信退款响应格式错误");
        }
    }

    /**
     * 查询订单（新增功能示例）
     *
     * @param outTradeNo 商户订单号
     * @return 订单信息
     */
    public Transaction queryOrder(String outTradeNo) {
        try {
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(weChatProperties.getMchid());
            request.setOutTradeNo(outTradeNo);
            return jsapiService.queryOrderByOutTradeNo(request);
        } catch (Exception e) {
            log.error("查询微信订单失败，订单号：{}", outTradeNo, e);
            throw new BizException(PAYMENT_ERROR, "查询订单失败：" + e.getMessage());
        }
    }

    /**
     * 关闭订单（新增功能示例）
     *
     * @param outTradeNo 商户订单号
     */
    public void closeOrder(String outTradeNo) {
        try {
            CloseOrderRequest request = new CloseOrderRequest();
            request.setMchid(weChatProperties.getMchid());
            request.setOutTradeNo(outTradeNo);
            jsapiService.closeOrder(request);
            log.info("微信订单关闭成功，订单号：{}", outTradeNo);
        } catch (Exception e) {
            log.error("关闭微信订单失败，订单号：{}", outTradeNo, e);
            throw new BizException(PAYMENT_ERROR, "关闭订单失败：" + e.getMessage());
        }
    }
}