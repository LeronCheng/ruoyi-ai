package org.ruoyi.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpOAuth2Service;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.bean.WxCpOauth2UserInfo;
import me.chanjar.weixin.cp.bean.WxCpUserDetail;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import me.chanjar.weixin.cp.util.crypto.WxCpCryptUtil;
import org.apache.commons.lang3.StringUtils;
import org.ruoyi.aes.AesException;
import org.ruoyi.aes.WXBizMsgCrypt;
import org.ruoyi.common.core.utils.JsonUtils;

import org.ruoyi.config.WxCpConfiguration;
import org.ruoyi.system.domain.SysUser;
import org.ruoyi.system.domain.bo.SysUserBo;
import org.ruoyi.system.domain.vo.SysUserVo;
import org.ruoyi.system.service.ISysUserService;
import org.ruoyi.system.service.SysLoginService;
import org.ruoyi.system.service.SysRegisterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 微信公众号登录校验
 *
 * @author ageerle
 * @date 2025-05-03
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/wx/cp")
@Slf4j
public class WxPortalController {
	
	private final ISysUserService sysUserService;
	private final SysLoginService loginService;
	private final SysRegisterService registerService;
	@Value("${wechat.cp.appConfigs[0].agentId}")
	private Integer agentId;
	@Value("${wechat.cp.corpId}")
	private String corpId;
	@Value("${wechat.cp.appConfigs[0].token}")
	private String sToken;
	@Value("${wechat.cp.appConfigs[0].aesKey}")
	private String sEncodingAESKey;
	@Value("${wechat.cp.appConfigs[0].secret}")
	private String secret;
	
	@GetMapping(produces = "text/plain;charset=utf-8")
	public String authGet(
			@RequestParam(name = "msg_signature", required = false) String signature,
			@RequestParam(name = "timestamp", required = false) String timestamp,
			@RequestParam(name = "nonce", required = false) String nonce,
			@RequestParam(name = "echostr", required = false) String echostr) {
		log.info("\n接收到来自微信服务器的认证消息：signature = [{}], timestamp = [{}], nonce = [{}], echostr = [{}]",
				signature, timestamp, nonce, echostr);
		
		if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
			throw new IllegalArgumentException("请求参数非法，请核实!");
		}
		
		final WxCpService wxCpService = WxCpConfiguration.getCpService(agentId);
		if (wxCpService == null) {
			throw new IllegalArgumentException(String.format("未找到对应agentId=[%d]的配置，请核实！", agentId));
		}
		
		if (wxCpService.checkSignature(signature, timestamp, nonce, echostr)) {
			return new WxCpCryptUtil(wxCpService.getWxCpConfigStorage()).decrypt(echostr);
		}
		
		return "非法请求";
	}
	
	@GetMapping("/feedbackFromWx")
	public String feedbackFromWxVerifyURL(String msg_signature, String timestamp, String nonce, String echostr) throws AesException {
		
		System.out.printf("-------feedbackFromWx ------ msg_signature:%s, Long timestamp:%s, String nonce:%s, String echostr:%s%n",
				msg_signature, timestamp, nonce, echostr);
//    String sToken = "pnRQhOyo5epoQHpy";
//    String sEncodingAESKey = "WcDeO4sJCx7HkP25JE6ssjydKsZ86XaqziKaTpvS9vQ";
		
		WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, corpId);
		String sMsg = wxcpt.VerifyURL(msg_signature, timestamp, nonce, echostr);
		System.out.printf("sMsg:%s%n", sMsg);
		return sMsg;
	}
	
	@PostMapping("/feedbackFromWx")
	public String feedbackFromWxData(String msg_signature, String timestamp, String nonce, @RequestBody String data)
			throws Exception {
//        System.out.printf("-------feedbackFromWx ------ msg_signature:%s, Long timestamp:%s, String nonce:%s, String data:%s%n",
//                msg_signature, timestamp, nonce, data);
		log.info("-------feedbackFromWx ------ msg_signature:{}, Long timestamp:{}, String nonce:{}, String data:{}%n", msg_signature, timestamp, nonce, data);
//    String sToken = "pnRQhOyo5epoQHpy";
//    String sEncodingAESKey = "WcDeO4sJCx7HkP25JE6ssjydKsZ86XaqziKaTpvS9vQ";
		WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, corpId);
		String sMsg = wxcpt.DecryptMsg(msg_signature, timestamp, nonce, data);
		
		log.info("timestamp: {}, feedback message: {}", System.currentTimeMillis(), sMsg);
		return sMsg;
	}
	
	@PostMapping(produces = "application/xml; charset=UTF-8")
	public String post(
			@RequestBody String requestBody,
			@RequestParam("msg_signature") String signature,
			@RequestParam("timestamp") String timestamp,
			@RequestParam("nonce") String nonce) {
		log.info("\n接收微信请求：[signature=[{}], timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
				signature, timestamp, nonce, requestBody);
		
		final WxCpService wxCpService = WxCpConfiguration.getCpService(agentId);
		WxCpXmlMessage inMessage = WxCpXmlMessage.fromEncryptedXml(requestBody, wxCpService.getWxCpConfigStorage(),
				timestamp, nonce, signature);
		log.debug("\n消息解密后内容为：\n{} ", JsonUtils.toJson(inMessage));
		WxCpXmlOutMessage outMessage = this.route(1000002, inMessage);
		if (outMessage == null) {
			return "";
		}
		
		String out = outMessage.toEncryptedXml(wxCpService.getWxCpConfigStorage());
		log.debug("\n组装回复信息：{}", out);
		return out;
	}
	
	private WxCpXmlOutMessage route(Integer agentId, WxCpXmlMessage message) {
		try {
			return WxCpConfiguration.getRouters().get(agentId).route(message);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return null;
	}
	
	@GetMapping("weChat/login")
	public String login(String code) {
		if (code == null || code.isEmpty()) {
			return "redirect:http://lwmes.carbononegroup.com:6039"; //返回网站首页
		}
		try {
			// 初始化WxCpService
			WxCpService wxCpService = new WxCpServiceImpl();
			WxCpDefaultConfigImpl wxCpConfigStorage = new WxCpDefaultConfigImpl();
			wxCpConfigStorage.setCorpId(corpId); //企业ID
			wxCpConfigStorage.setAgentId(agentId);//应用ID
			wxCpConfigStorage.setCorpSecret(secret);//应用对应的秘钥
			wxCpService.setWxCpConfigStorage(wxCpConfigStorage);
			// 根据oauth2获取到的code获取用户基本信息
			WxCpOAuth2Service oauth2Service = wxCpService.getOauth2Service();
			//获取用户基本信息：默认授权--设备id和用户id；手动授权：设备id、用户id、ticket
			WxCpOauth2UserInfo userInfo = oauth2Service.getUserInfo(code);
			//获取用户详细信息:用户id（唯一）、邮箱、手机号、工号、头像、名称等信息
			String ticket = userInfo.getUserTicket();
			WxCpUserDetail userDetail = oauth2Service.getUserDetail(ticket);
			//操作数据库，判断用户是否存在。存在绑定企微用户id，不存在则新建用户信息，绑定企微
			SysUserVo sysUserVo = sysUserService.selectUserByUserName(userDetail.getUserId());
			String token;
			if (sysUserVo == null) {
				SysUserBo sysUser = new SysUserBo();
				sysUser.setUserName(userDetail.getUserId());
				sysUser.setPassword(RandomUtil.randomString(8));
				sysUser.setPhonenumber(userDetail.getMobile());
				sysUser.setSex(userDetail.getGender());
				sysUser.setNickName(userDetail.getName());
				sysUser.setAvatar(userDetail.getAvatar());
				sysUser.setStatus("0");
				SysUser user = sysUserService.registerUser(sysUser, "000000");
				token = loginService.login(user.getTenantId(), user.getUserName(), user.getPassword(), null, null);
			} else {
				token = loginService.login(sysUserVo.getTenantId(), sysUserVo.getUserName(), sysUserVo.getPassword(), null, null);
			}
			//做一些事：生成token
			log.info("企微单点登录-->>{}", JSONUtil.toJsonStr(userDetail));
			return "redirect:http://lwmes.carbononegroup.com:6039?token=" + token; //携带token
		} catch (Exception e) {
			e.printStackTrace();
			log.error("企微单点登录报错-->>{}", e.getMessage());
			return "redirect:http://lwmes.carbononegroup.com:6039"; //返回网站首页
		}
	}
	
	
}
