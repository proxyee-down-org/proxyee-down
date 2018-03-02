package lee.study.down.dispatch;

import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;

public class HttpDownCallback {

  public void onStart(HttpDownInfo httpDownInfo) throws Exception {
  }

  public void onChunkConnecting(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception {
  }

  public void onChunkConnected(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception {
  }

  public void onProgress(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception {
  }

  public void onPause(HttpDownInfo httpDownInfo) throws Exception {
  }

  public void onContinue(HttpDownInfo httpDownInfo) throws Exception {
  }

  public void onError(HttpDownInfo httpDownInfo, Throwable cause) throws Exception {
  }

  public void onChunkError(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo, Throwable cause)
      throws Exception {
  }

  public void onChunkDone(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception {
  }

  public void onMerge(HttpDownInfo httpDownInfo) throws Exception {
  }

  public void onDone(HttpDownInfo httpDownInfo) throws Exception {
  }

  public void onDelete(HttpDownInfo httpDownInfo) throws Exception {
  }
}
