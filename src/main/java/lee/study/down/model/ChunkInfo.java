package lee.study.down.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.channel.Channel;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChunkInfo implements Serializable {

  private static final long serialVersionUID = 231649750985696846L;
  private int index;
  private long oriStartPosition; //原本的起始字节位
  private long nowStartPosition; //重新计算后的起始字节位
  private long endPosition; //结束字节位
  private long downSize = 0;
  private long totalSize;
  private long startTime = 0;
  private long lastTime = 0;
  private int status = 0; //0.待下载 //1.下载中 2.下载完成 3.下载失败

  @JsonIgnore
  private transient Channel channel;
  @JsonIgnore
  private transient FileChannel fileChannel;

  public ChunkInfo() {
  }
}
