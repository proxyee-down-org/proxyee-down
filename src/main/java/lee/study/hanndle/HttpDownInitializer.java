package lee.study.hanndle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.*;
import lee.study.proxyee.NettyHttpProxyServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpDownInitializer extends ChannelInitializer {

    private File file;
    private boolean isSsl;
    private int connections;
    private List<File> chunkFileList;

    public HttpDownInitializer(File file, boolean isSsl, int connections, List<File> chunkFileList) {
        this.file = file;
        this.isSsl = isSsl;
        this.connections = connections;
        this.chunkFileList = chunkFileList;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (isSsl) {
            ch.pipeline().addLast(NettyHttpProxyServer.clientSslCtx.newHandler(ch.alloc()));
        }
        ch.pipeline().addLast("httpCodec", new HttpClientCodec());
        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

            private File chunkFile;
            private OutputStream outputStream;
            private long fileSize;
            private long chunkSize = -1;
            private long downSize = 0;

            @Override
            public void channelRead(ChannelHandlerContext ctx0, Object msg0) throws Exception {
                if (msg0 instanceof HttpResponse) {
                    HttpResponse response = (HttpResponse) msg0;
                    if (response.status().code() != 200 && response.status().code() != 206) {
                        System.out.println("下载失败");
                        ctx0.channel().close();
                    } else {
                        String contentRange = response.headers().get(HttpHeaderNames.CONTENT_RANGE);
                        fileSize = Long.valueOf(response.headers().get(HttpHeaderNames.CONTENT_LENGTH));
                        if (contentRange != null) {
                            Pattern pattern = Pattern.compile("\\d+-\\d+");
                            Matcher matcher = pattern.matcher(contentRange);
                            String range = contentRange;
                            if (matcher.find()) {
                                range = matcher.group(0);
                                chunkSize = Arrays.stream(range.split("-")).mapToLong((t) -> Long.valueOf(t)).reduce(0, (k, v) -> v - k);
                            }
                            System.out.println(ctx0.channel().id()+"\t下载文件块：" + range);
                            chunkFile = new File("chunk_" + Math.abs(file.hashCode()) + "_" + range + ".tmp");
                            if (chunkFile.exists()) {
                                chunkFile.delete();
                            }
                            chunkFile.createNewFile();
                            outputStream = new FileOutputStream(chunkFile);
                        } else {
                            System.out.println("下载文件：" + response.headers().get(HttpHeaderNames.CONTENT_LENGTH));
                            outputStream = new FileOutputStream(file);
                        }
                    }
                } else {
                    HttpContent content = (HttpContent) msg0;
                    ByteBuf byteBuf = content.content();
                    int readBytes = byteBuf.readableBytes();
                    downSize += readBytes;
                    byteBuf.readBytes(outputStream, readBytes);
                    System.out.println(ctx0.channel().id()+"\t下载进度:" + String.format("%.2f", (downSize * 100 / Double.valueOf(chunkSize != -1 ? chunkSize : fileSize))) + "%");
                    if (msg0 instanceof DefaultLastHttpContent) {
                        outputStream.close();
                        ctx0.channel().close();
                        chunkFileList.add(chunkFile);
                        //下载完成
                        if (chunkFileList.size() == connections) {
                            System.out.println("下载完成，合并成一个文件");
                            try (
                                    FileChannel appendChannel = new FileOutputStream(file, true).getChannel();
                            ) {
                                ByteBuffer buffer = ByteBuffer.allocate(8192);
                                chunkFileList.stream()
                                        .sorted((f1, f2) -> (Long.valueOf(f1.getName().split("_")[2].split("-")[0]) > Long.valueOf(f2.getName().split("_")[2].split("-")[0]) ? 1 : -1))
                                        .forEach((f) -> {
                                            try (
                                                    FileChannel chunkChannel = new FileInputStream(f).getChannel()
                                            ) {
                                                while(chunkChannel.read(buffer)!=-1){
                                                    buffer.flip();
                                                    appendChannel.write(buffer);
                                                    buffer.clear();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            } finally {
                                                f.delete();
                                            }
                                        });
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        Arrays.asList("chunk_123312321_0-123.tmp", "chunk_123312321_500-789.tmp", "chunk_123312321_124-499.tmp").stream()
                .sorted((s1, s2) -> (Long.valueOf(s1.split("_")[2].split("-")[0]) > Long.valueOf(s2.split("_")[2].split("-")[0]) ? 1 : -1))
                .forEach((file) -> {
                    System.out.println(file);
                });
    }
}
