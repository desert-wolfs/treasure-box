package com.douniu.box.netty.simple.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer(4);
        buffer.writeInt((int) (System.currentTimeMillis() /1000L + 2208988800L));

        ChannelFuture channelFuture = ctx.writeAndFlush(buffer);
        channelFuture.addListener((ChannelFutureListener) future -> {
            System.out.println("TimeServerHandler.channelActive");
            ctx.close();
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
