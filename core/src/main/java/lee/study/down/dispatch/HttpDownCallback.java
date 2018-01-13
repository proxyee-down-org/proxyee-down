package lee.study.down.dispatch;

import lee.study.down.model.ChunkInfo;
import lee.study.down.model.HttpDownInfo;

public interface HttpDownCallback {

  void onStart(HttpDownInfo httpDownInfo) throws Exception;

  void onChunkStart(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception;

  void onProgress(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception;

  void onPause(HttpDownInfo httpDownInfo) throws Exception;

  void onContinue(HttpDownInfo httpDownInfo) throws Exception;

  void onError(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo, Throwable cause) throws Exception;

  void onChunkDone(HttpDownInfo httpDownInfo, ChunkInfo chunkInfo) throws Exception;

  void onDone(HttpDownInfo httpDownInfo) throws Exception;
}
