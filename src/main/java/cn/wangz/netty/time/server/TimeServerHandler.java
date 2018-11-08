package cn.wangz.netty.time.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

/**
 * Created by hadoop on 2018/11/7.
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 因为将会忽略任何接收到的数据，而只是在连接被创建发送一个消息，所以这次我们不能使用 channelRead() 方法了，代替他的是 channelActive() 方法
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final ByteBuf timeBuf = ctx.alloc().buffer(4);
        int time = (int) (System.currentTimeMillis() / 1000 + 2208988800L);
        timeBuf.writeInt(time);

        final ChannelFuture f = ctx.writeAndFlush(timeBuf)
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
