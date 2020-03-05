package com.imooc.netty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MoveAction;

import org.apache.commons.lang3.StringUtils;

import com.imooc.SpringUtil;
import com.imooc.enums.MsgActionEnum;
import com.imooc.service.UserService;
import com.imooc.utils.JsonUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.AsciiHeadersEncoder.NewlineType;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
//deal with message frame is message container
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame>{
	//used for record and manage all channel in clients
	public static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		// TODO Auto-generated method stub
		String content = msg.text();
		DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
		Channel currentChannel = ctx.channel();
		Integer action = dataContent.getAction();
		
		if (action == MsgActionEnum.CONNECT.type) {
			// 	2.1  当websocket 第一次open的时候，初始化channel，把用的channel和userid关联起来
			String senderId = dataContent.getChatMsg().getSenderId();
			UserChannelRel.put(senderId, currentChannel);
			
			// 测试
			for (Channel c : users) {
				System.out.println(c.id().asLongText());
			}
			UserChannelRel.output();
		} else if (action == MsgActionEnum.CHAT.type) {
			//  2.2  聊天类型的消息，把聊天记录保存到数据库，同时标记消息的签收状态[未签收]
			ChatMsg chatMsg = dataContent.getChatMsg();
			String msgText = chatMsg.getMsg();
			String receiverId = chatMsg.getReceiverId();
			String senderId = chatMsg.getSenderId();
			
			// 保存消息到数据库，并且标记为 未签收
			UserService userService = (UserService)SpringUtil.getBean("userServiceImpl");
			String msgId = userService.saveMsg(chatMsg);
			chatMsg.setMsgId(msgId);
			
			DataContent dataContentMsg = new DataContent();
			dataContentMsg.setChatMsg(chatMsg);
			
			// 发送消息
			// 从全局用户Channel关系中获取接受方的channel
			Channel receiverChannel = UserChannelRel.get(receiverId);
			if (receiverChannel == null) {
				
			} else {
				// 当receiverChannel不为空的时候，从ChannelGroup去查找对应的channel是否存在
				Channel findChannel = users.find(receiverChannel.id());
				if (findChannel != null) {
					// 用户在线
					receiverChannel.writeAndFlush(
							new TextWebSocketFrame(
									JsonUtils.objectToJson(dataContentMsg)));
				} else {
					// 用户离线 TODO 推送消息
				}
			}
			
		} else if (action == MsgActionEnum.SIGNED.type) {
			//  2.3  签收消息类型，针对具体的消息进行签收，修改数据库中对应消息的签收状态[已签收]表示用户客户端收到消息
			UserService userService = (UserService)SpringUtil.getBean("userServiceImpl");
			// 扩展字段在signed类型的消息中，代表需要去签收的消息id，逗号间隔
			String msgIdsStr = dataContent.getExtand();
			String msgIds[] = msgIdsStr.split(",");
			
			List<String> msgIdList = new ArrayList<>();
			for (String mid : msgIds) {
				if (StringUtils.isNotBlank(mid)) {
					msgIdList.add(mid);
				}
			}
			
			System.out.println(msgIdList.toString());
			
			if (msgIdList != null && !msgIdList.isEmpty() && msgIdList.size() > 0) {
				// 批量签收
				userService.updateMsgSigned(msgIdList);
			}
			
		} else if (action == MsgActionEnum.KEEPALIVE.type) {
			//  2.4  心跳类型的消息
			System.out.println("收到来自channel为[" + currentChannel + "]的心跳包...");
		}
		
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		// open connect bt clients and server get the channel and put it into channel group

		users.add(ctx.channel());
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		//channel group move handler automaticly
		users.remove(ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
		ctx.channel().close();
		users.remove(ctx.channel());
	}

}
