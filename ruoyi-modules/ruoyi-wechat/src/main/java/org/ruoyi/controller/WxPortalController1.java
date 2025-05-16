package org.ruoyi.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpOAuth2Service;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.bean.WxCpOauth2UserInfo;
import me.chanjar.weixin.cp.bean.WxCpUserDetail;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import org.ruoyi.common.core.utils.ServletUtils;
import org.ruoyi.system.domain.SysUser;
import org.ruoyi.system.domain.bo.SysUserBo;
import org.ruoyi.system.domain.vo.SysUserVo;
import org.ruoyi.system.service.ISysUserService;
import org.ruoyi.system.service.SysLoginService;
import org.ruoyi.system.service.SysRegisterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 微信公众号登录校验
 *
 * @author ageerle
 * @date 2025-05-03
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/wx/cp")
@Slf4j
public class WxPortalController1 {

//	@GetMapping("weChat/login")
//	public String login1(String code) {
//		log.info("重定向测试：{}", code);
//		return "http://localhost:1002/qywx.html?token=zxcvbnm"; //返回网站首页
//	}

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


	@GetMapping("weChat/login")
	public ModelAndView login(String code) {
		log.info("code: {}",code);
		ModelAndView modelAndView = new ModelAndView();
		if (code == null || code.isEmpty()) {
			modelAndView.setViewName("http://lwmes.carbononegroup.com:6039");
			return modelAndView; //返回网站首页
		}
		try {
			// 初始化WxCpService
			WxCpService wxCpService = new WxCpServiceImpl();
			WxCpDefaultConfigImpl wxCpConfigStorage = new WxCpDefaultConfigImpl();
			wxCpConfigStorage.setCorpId(corpId); //企业ID
			wxCpConfigStorage.setAgentId(agentId);//应用ID
			wxCpConfigStorage.setCorpSecret(secret);//应用对应的秘钥
			log.info("企微配置入参: {}，{}，{}",corpId,agentId,secret);
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
			log.info("userDetail：{}", JSONUtil.toJsonStr(userDetail));
			String token;
			if (sysUserVo == null) {
				SysUserBo sysUser = new SysUserBo();
				sysUser.setUserName(userDetail.getUserId());
				sysUser.setPassword(RandomUtil.randomString(8));
				sysUser.setPhonenumber(userDetail.getMobile());
				sysUser.setSex(userDetail.getGender());
				sysUser.setNickName("fake");
				sysUser.setAvatar(userDetail.getAvatar());
				sysUser.setStatus("0");
				sysUser.setDomainName(ServletUtils.getClientIP());
				SysUser user = sysUserService.registerUser(sysUser, "00000");
				token = loginService.loginByWechat(user.getTenantId(), user.getUserName(), user.getPassword(), null, null);
			} else {
				token = loginService.loginByWechat(sysUserVo.getTenantId(), sysUserVo.getUserName(), sysUserVo.getPassword(), null, null);
				log.info("token ： {}",token);
			}
			//做一些事：生成token
			log.info("企微单点登录-->>{}", JSONUtil.toJsonStr(userDetail));
			String url = "redirect:http://lwmes.carbononegroup.com:6039/qywx.html?token=" + token;
			log.info("token ： {}",url);
			modelAndView.setViewName("http://lwmes.carbononegroup.com:6039");
			modelAndView.addObject("token",token);
			return modelAndView;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("企微单点登录报错-->>{}", e.getMessage());
			modelAndView.setViewName("http://lwmes.carbononegroup.com:6039");
			return modelAndView; //返回网站首页
		}
	}
}
