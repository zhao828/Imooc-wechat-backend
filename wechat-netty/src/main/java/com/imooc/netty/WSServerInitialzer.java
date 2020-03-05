package com.imooc.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class WSServerInitialzer extends ChannelInitializer<SocketChannel>{

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		// TODO Auto-generated method stub
		ChannelPipeline pipeline = ch.pipeline();
		
		//websocket based on http ,servercode is needed
		pipeline.addLast(new HttpServerCodec());
		
		// support writing large data stream
		pipeline.addLast(new ChunkedWriteHandler());
		
		//create http message aggregator 
		pipeline.addLast(new HttpObjectAggregator(1024*64));
		
		
		//http support above
		
		//websocket server protocol, used for router /ws
		// this handler helps to deal with handshaking
		
		pipeline.addLast(new IdleStateHandler(8, 10, 12));
		// 自定义的空闲状态检测
		pipeline.addLast(new HeartBeatHandler());
		pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
		
		//customized handler
		pipeline.addLast(new ChatHandler());
		
		
		
		
		
	}
	
	

}
