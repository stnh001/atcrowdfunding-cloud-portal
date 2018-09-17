package com.atguigu.atcrowdfuning.portal.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.atguigu.atcrowdfuning.portal.service.MemberService;
import com.atguigu.crowdfuning.common.bean.BaseController;
import com.atguigu.crowdfuning.common.bean.MD5Util;
import com.atguigu.crowdfuning.common.bean.Member;
import com.atguigu.crowdfuning.common.constant.AttrConst;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Controller
public class DispatcherController extends BaseController {

	@Autowired
	private MemberService memberService;
	
	@RequestMapping("/login")
	public String login() {
		return "login";
	}
	
	@RequestMapping("/main")
	public String main() {
		return "main";
	}
	
	//@HystrixCommand(fallbackMethod="checkLoginError")
	@ResponseBody
	@RequestMapping("/checkLogin")
	public Object checkLogin( Member member, HttpSession session ) {
		start();
		
		try {
			// 查询会员信息
			Member dbMember = memberService.login(member.getLoginacct());
			if ( dbMember == null ) {
				fail();
			} else {
				if ( dbMember.getMemberpswd().equals(MD5Util.digest(member.getMemberpswd())) ) {
					session.setAttribute(AttrConst.SESSION_MEMBER, dbMember);
					success();	
				} else {
				    fail();	
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			fail();
		}
		
		return end();
	}
//	public Object checkLoginError( Member member ) {
//		System.out.println("error....");
//		start();
//		fail();
//		return end();
//	}
}
