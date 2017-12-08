package lee.study.down.dispatch;

import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;

public interface HttpDownCallback {

  void start(TaskInfo taskInfo);

  void chunkStart(TaskInfo taskInfo,ChunkInfo chunkInfo);

  void progress(TaskInfo taskInfo, ChunkInfo chunkInfo);

  void error(TaskInfo taskInfo, ChunkInfo chunkInfo,Throwable cause);

  void chunkDone(TaskInfo taskInfo, ChunkInfo chunkInfo);

  void done(TaskInfo taskInfo);
}
