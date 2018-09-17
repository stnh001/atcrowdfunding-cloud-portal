package com.atguigu.atcrowdfuning.portal.service;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.atguigu.crowdfuning.common.bean.Cert;
import com.atguigu.crowdfuning.common.bean.Member;
import com.atguigu.crowdfuning.common.bean.MemberCert;
import com.atguigu.crowdfuning.common.bean.Ticket;

@FeignClient("eureka-member-service")
public interface MemberService {

	@RequestMapping("/login/{loginacct}")
	public Member login( @PathVariable("loginacct")String loginacct );
	/**
	 * 查询流程审批单
	 * @param id 流程审批单的id
	 * @return
	 */
	@RequestMapping("/queryTicketByMenberid/{id}")
	public Ticket queryTicketByMenberid(@PathVariable("id")Integer id);
	/**
	 * 插入流程审批单
	 * @param t 流程审批单对象
	 */
	@RequestMapping("/insertTicket")
	public void insertTicket(@RequestBody Ticket t);
	
	@RequestMapping("/updateAccountType")
	public void updateAccountType(@RequestBody Member loginMember);
	
	@RequestMapping("/updateBasicinfo")
	public void updateBasicinfo(@RequestBody Member loginMember);
	
	@RequestMapping("/queryCerByAccountTyope/{accttype}")
	public List<Cert> queryCerByAccountTyope(@PathVariable("accttype")String accttype);
	
	@RequestMapping("/inserMemberCerts")
	public void inserMemberCerts(@RequestBody List<MemberCert> mcs);
	
	@RequestMapping("/updateEmail")
	public void updateEmail(@RequestBody Member loginMember);
	
	@RequestMapping("/updateAuthstatus")
	public void updateAuthstatus(Member loginMember);

}
