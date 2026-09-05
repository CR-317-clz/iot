package com.management.iot.configuration.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * TCP客户端封装，支持连接设备、发送消息、接收数据
 */
@Slf4j
public class TcpClient {

    private String host;
    private int topic;
    private Channel channel;
    private EventLoopGroup group;
    private DataCallback callback;
    private boolean connected = false;

    public interface DataCallback {
        void onMessage(String message);
    }

    public TcpClient(String host, int topic, DataCallback callback) {
        this.host = host;
        this.topic = topic;
        this.callback = callback;
    }

    /**
     * 建立TCP连接
     */
    public void connect() {
        System.out.println("🔌 尝试连接到 " + host + ":" + topic);
        
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                 .option(ChannelOption.SO_KEEPALIVE, true)
                 .handler(new ChannelInitializer<Channel>() {
                     @Override
                     protected void initChannel(Channel ch) {
                         System.out.println("🔧 初始化通道处理器 - 使用原始字节处理器");
                         
                         // 使用原始字节处理器，避免编解码问题
                         ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {

                             @Override
                             public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                 connected = true;
                                 System.out.println("✅ TCP连接已建立: " + host + ":" + topic);
                                 
                                 // 连接建立后立即发送初始化消息 "qa"
                                 System.out.println("🚀 发送设备初始化消息 'qa'");
                                 sendInitializationMessage(ctx);
                                 
                                 super.channelActive(ctx);
                             }

                             @Override
                             public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                 connected = false;
                                 System.out.println("❌ TCP连接已断开: " + host + ":" + topic);
                                 super.channelInactive(ctx);
                             }

                             @Override
                             protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
                                 // 获取原始字节数据
                                 int readableBytes = byteBuf.readableBytes();
                                 byte[] bytes = new byte[readableBytes];
                                 byteBuf.readBytes(bytes);
                                 
                                 System.out.println("=".repeat(70));
                                 System.out.println("🎯 收到设备数据!");
                                 System.out.println("=".repeat(70));
                                 
                                 // 显示基本信息
                                 System.out.println("📊 数据包信息:");
                                 System.out.println("   📏 数据长度: " + readableBytes + " 字节");
                                 System.out.println("   🕒 接收时间: " + java.time.LocalDateTime.now());
                                 
                                 // 显示原始字节
                                 System.out.println("🔢 原始字节: " + bytesToHex(bytes));
                                 
                                 // 尝试用不同编码解析
                                 displayAsAscii(bytes);
                                 displayAsUtf8(bytes);
                                 displayAsGb2312(bytes);
                                 
                                 // 回调处理
                                 if (callback != null) {
                                     String message = new String(bytes, StandardCharsets.UTF_8);
                                     System.out.println("🔄 调用回调函数处理消息");
                                     callback.onMessage(message);
                                 }
                                 
                                 System.out.println("=".repeat(70));
                             }

                             @Override
                             public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                 System.out.println("💥 TCP连接异常: " + cause.getMessage());
                                 log.error("TCP Error {}:{}", host, topic, cause);
                                 ctx.close();
                             }
                         });
                     }
                 });

        // 异步连接到服务器
        ChannelFuture future = bootstrap.connect(host, topic);
        future.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                channel = f.channel();
                System.out.println("🎉 成功连接到设备 " + host + ":" + topic);
                System.out.println("📨 初始化流程: 连接建立 → 发送'qa' → 等待设备响应");
                log.info("Connected to {}:{}", host, topic);
                
            } else {
                System.out.println("💥 连接失败 " + host + ":" + topic + " - " + f.cause().getMessage());
                log.error("Failed to connect {}:{}", host, topic, f.cause());
                group.shutdownGracefully();
            }
        });
    }

    /**
     * 发送设备初始化消息
     */
    private void sendInitializationMessage(ChannelHandlerContext ctx) {
        String initMessage = "qa";
        System.out.println("📨 发送设备初始化消息: '" + initMessage + "'");
        System.out.println("🔢 初始化消息字节: " + bytesToHex(initMessage.getBytes(StandardCharsets.UTF_8)));
        System.out.println("🔤 初始化消息ASCII: " + getAsciiCodes(initMessage));
        
        ByteBuf buf = Unpooled.wrappedBuffer(initMessage.getBytes(StandardCharsets.UTF_8));
        ChannelFuture future = ctx.channel().writeAndFlush(buf);
        future.addListener(f -> {
            if (f.isSuccess()) {
                System.out.println("✅ 设备初始化消息发送成功");
                System.out.println("⏳ 等待设备响应数据...");
            } else {
                System.out.println("❌ 设备初始化消息发送失败: " + f.cause().getMessage());
            }
        });
    }

    /**
     * 将字节数组转换为16进制显示
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X ", b));
        }
        return hex.toString().trim();
    }

    /**
     * 获取字符串的ASCII码
     */
    private String getAsciiCodes(String str) {
        StringBuilder ascii = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            ascii.append((int) c);
            if (i < str.length() - 1) {
                ascii.append(" ");
            }
        }
        return ascii.toString();
    }

    /**
     * 显示ASCII分析
     */
    private void displayAsAscii(byte[] bytes) {
        System.out.println("🔤 ASCII分析:");
        StringBuilder asciiBuilder = new StringBuilder();
        StringBuilder asciiCodes = new StringBuilder();
        
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            int unsignedByte = b & 0xFF;
            
            asciiCodes.append(String.format("%3d", unsignedByte));
            if (i < bytes.length - 1) {
                asciiCodes.append(" ");
            }
            
            if (unsignedByte >= 32 && unsignedByte <= 126) {
                // 可打印ASCII字符
                asciiBuilder.append((char) unsignedByte);
            } else {
                // 控制字符或不可打印字符
                asciiBuilder.append(".");
            }
        }
        
        System.out.println("   📟 ASCII码: " + asciiCodes.toString());
        System.out.println("   📝 可打印字符: " + asciiBuilder.toString());
        
        // 显示特殊字符分析
        displaySpecialCharacters(bytes);
    }

    /**
     * 显示特殊字符分析
     */
    private void displaySpecialCharacters(byte[] bytes) {
        System.out.println("   🔍 特殊字符分析:");
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            int unsignedByte = b & 0xFF;
            
            String charDesc = getCharDescription(unsignedByte);
            if (!charDesc.equals("可打印字符")) {
                System.out.printf("      [位置%02d] ASCII %3d: %s%n", i, unsignedByte, charDesc);
            }
        }
    }

    /**
     * 获取字符描述
     */
    private String getCharDescription(int ascii) {
        switch (ascii) {
            case 0: return "空字符(NULL)";
            case 10: return "换行符(LF)";
            case 13: return "回车符(CR)";
            case 9: return "制表符(TAB)";
            case 27: return "退出符(ESC)";
            case 8: return "退格符(BS)";
            case 7: return "响铃符(BEL)";
            case 12: return "换页符(FF)";
            case 3: return "文本结束(ETX)";
            case 4: return "传输结束(EOT)";
            case 26: return "替换符(SUB)";
            case 28: return "文件分隔符(FS)";
            case 29: return "组分隔符(GS)";
            case 30: return "记录分隔符(RS)";
            case 31: return "单元分隔符(US)";
            case 127: return "删除符(DEL)";
            default:
                if (ascii < 32) return "控制字符";
                if (ascii >= 32 && ascii <= 126) return "可打印字符";
                return "扩展ASCII";
        }
    }

    /**
     * 显示UTF-8编码
     */
    private void displayAsUtf8(byte[] bytes) {
        try {
            String utf8String = new String(bytes, StandardCharsets.UTF_8);
            System.out.println("🌐 UTF-8解码: " + utf8String);
        } catch (Exception e) {
            System.out.println("❌ UTF-8解码失败");
        }
    }

    /**
     * 显示GB2312编码（中文设备常用）
     */
    private void displayAsGb2312(byte[] bytes) {
        try {
            String gbString = new String(bytes, "GB2312");
            System.out.println("🇨🇳 GB2312解码: " + gbString);
        } catch (Exception e) {
            System.out.println("❌ GB2312解码失败");
        }
    }

    /**
     * 断开TCP连接
     */
    public void disconnect() {
        System.out.println("🔌 断开TCP连接: " + host + ":" + topic);
        connected = false;
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    /**
     * 发送消息到设备
     */
    public void sendMessage(String message) {
        if (channel != null && channel.isActive()) {
            System.out.println("📤 发送消息到设备 " + host + ":" + topic);
            System.out.println("📝 消息内容: " + message);
            System.out.println("🔢 消息字节: " + bytesToHex(message.getBytes(StandardCharsets.UTF_8)));
            
            ByteBuf buf = Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8));
            ChannelFuture future = channel.writeAndFlush(buf);
            future.addListener(f -> {
                if (f.isSuccess()) {
                    System.out.println("✅ 消息发送成功");

                } else {
                    System.out.println("❌ 消息发送失败: " + f.cause().getMessage());
                }
            });
        } else {
            System.out.println("⚠️ 无法发送消息，连接未激活: " + host + ":" + topic);
        }
    }

    /**
     * 判断TCP连接是否有效
     */
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    /**
     * 获取连接状态
     */
    public boolean isConnected() {
        return connected;
    }
}