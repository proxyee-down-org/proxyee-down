package lee.study.down.dispatch;

import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;

public interface HttpDownCallback {

  void onStart(TaskInfo taskInfo);

  void onChunkStart(TaskInfo taskInfo,ChunkInfo chunkInfo);

  void onProgress(TaskInfo taskInfo, ChunkInfo chunkInfo);

  void onPause(TaskInfo taskInfo);

  void onContinue(TaskInfo taskInfo);

  void onError(TaskInfo taskInfo, ChunkInfo chunkInfo,Throwable cause);

  void onChunkDone(TaskInfo taskInfo, ChunkInfo chunkInfo);

  void onDone(TaskInfo taskInfo);

  void onDelete(TaskInfo taskInfo);
}
