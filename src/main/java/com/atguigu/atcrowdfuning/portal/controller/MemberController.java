package com.atguigu.atcrowdfuning.portal.controller;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.aspectj.apache.bcel.classfile.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.atguigu.atcrowdfuning.portal.service.ActService;
import com.atguigu.atcrowdfuning.portal.service.MemberService;
import com.atguigu.crowdfuning.common.bean.BaseController;
import com.atguigu.crowdfuning.common.bean.Cert;
import com.atguigu.crowdfuning.common.bean.Datas;
import com.atguigu.crowdfuning.common.bean.Member;
import com.atguigu.crowdfuning.common.bean.MemberCert;
import com.atguigu.crowdfuning.common.bean.Ticket;
import com.atguigu.crowdfuning.common.constant.AttrConst;

@Controller
@RequestMapping("/member")
public class MemberController extends BaseController {

	@Autowired
	private MemberService memberService;
	@Autowired
	private ActService actService;
	@ResponseBody
	@RequestMapping("/finishApply")
	public Object finishApply( HttpSession session, String authcode ) {
		start();
		
		try {
			// 获取会员信息
			Member loginMember =
				(Member)session.getAttribute(AttrConst.SESSION_MEMBER);
			
			// 获取流程审批单
			Ticket t = memberService.queryTicketByMenberid(loginMember.getId());
			
			// 获取邮件验证码
			// 判断验证码是否正确
			if ( authcode.equals(t.getAuthcode()) ) {
				
				// 更新会员实名认证状态
				loginMember.setAuthstatus("1");
				session.setAttribute(AttrConst.SESSION_MEMBER, loginMember);
				memberService.updateAuthstatus(loginMember);
				success();
			} else {
				fail();
			}
		} catch( Exception e ) {
			e.printStackTrace();
			fail();
		}
		
		return end();
	}
	@ResponseBody
	@RequestMapping("/sendMail")
	public Object sendMail(HttpSession session,String email) {
		start();		
		try {
			// 获取当前的会员信息
			Member loginMember = (Member) session.getAttribute(AttrConst.SESSION_MEMBER);
			loginMember.setEmail(email);
			session.setAttribute(AttrConst.SESSION_MEMBER, loginMember);
			//更新会员地址
			memberService.updateEmail(loginMember);
			success();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return end();
	}
	@ResponseBody
	@RequestMapping("/uploadCerts")
	public Object uploadCerts(HttpSession session, Datas ds) {
		start();
		try {
			// 获取当前的会员信息
			Member loginMember = (Member) session.getAttribute(AttrConst.SESSION_MEMBER);
			
			List<MemberCert> mcs = ds.getMcs();
			for (MemberCert mc : mcs) {
				//保存图片
				mc.setMemberid(loginMember.getId());
				MultipartFile file = mc.getFile();
				//获取照片的原始名称
				String originalFilename = file.getOriginalFilename();
				//前缀
				 String uuString=UUID.randomUUID().toString();
				 //后缀
				String suffix=originalFilename.substring(originalFilename.lastIndexOf("."));
				File destFile=new File("F:\\resources\\bluepic\\cert\\"+uuString+suffix);
				
				file.transferTo(destFile);
				
				mc.setIconpath(uuString+suffix);
				
				mc.setFile(null);
				
				
			}
			memberService.inserMemberCerts(mcs);
			success();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return end();
	}

	@ResponseBody
	@RequestMapping("/updateBasicinfo")
	public Object updateBasicinfo(HttpSession session, Member member) {
		start();

		try {
			// 获取当前的会员信息
			Member loginMember = (Member) session.getAttribute(AttrConst.SESSION_MEMBER);
			loginMember.setRealname(member.getRealname());
			loginMember.setCardnum(member.getCardnum());
			loginMember.setTel(member.getTel());
			memberService.updateBasicinfo(loginMember);
			session.setAttribute(AttrConst.SESSION_MEMBER, loginMember);
			success();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return end();
	}

	@ResponseBody
	@RequestMapping("/updateAccountType")
	public void updateAccountType(HttpSession session, Member member) {
		start();

		try {
			// 获取当前的会员信息
			Member loginMember = (Member) session.getAttribute(AttrConst.SESSION_MEMBER);

			loginMember.setAccttype(member.getAccttype());

			memberService.updateAccountType(loginMember);

			session.setAttribute(AttrConst.SESSION_MEMBER, loginMember);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		success();
	}

	@RequestMapping("/apply")
	public String apply(HttpSession session, Model model) {

		// 获取当前的会员信息
		Member loginMember = (Member) session.getAttribute(AttrConst.SESSION_MEMBER);
		// 会员流程审批单
		Ticket t = memberService.queryTicketByMenberid(loginMember.getId());
		// 当第一次申请的时候不需要流程步骤，跳转到账户类型页面
		if (t == null) {
			// 启动流程
			// 获取流程实例的id
			String piid = actService.startProcessInstance(loginMember.getLoginacct());
			t = new Ticket();
			t.setMemberid(loginMember.getId());
			t.setPstep("acctselect");
			t.setPiid(piid);
			t.setStatus("0");
			memberService.insertTicket(t);

			return "member/apply-accttype-select";
		} else {
			// 根据流程步骤跳转页面
			String step = t.getPstep();
			if ("basicinfo".equals(step)) {
				return "member/apply-basic-info";
			} else if ("certfile".equals(step)) {
				// 查詢當前會員需要提交的證明文件列表
				List<Cert> certs = memberService.queryCerByAccountTyope(loginMember.getAccttype());
				model.addAttribute("certs", certs);
				return "member/apply-cert-upload";
			}else if("email".equals(step)){
				return "member/apply-email";
			}else if("checkcode".equals(step)) {
				return "member/apply-check-code";
			}
			else {
				return "member/apply-accttype-select";
			}
		}
	}
}
