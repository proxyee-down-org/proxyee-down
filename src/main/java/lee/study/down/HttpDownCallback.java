package lee.study.down;

import lee.study.model.ChunkInfo;
import lee.study.model.TaskInfo;

public interface HttpDownCallback {

  void start(TaskInfo taskInfo);

  void chunkStart(TaskInfo taskInfo,ChunkInfo chunkInfo);

  void progress(TaskInfo taskInfo, ChunkInfo chunkInfo, long chunkDownSize, long chunkTotalSize,
      long fileDownSize,
      long fileTotalSize);

  void error(TaskInfo taskInfo, ChunkInfo chunkInfo,Throwable cause);

  void chunkDone(TaskInfo taskInfo, ChunkInfo chunkInfo);

  void done(TaskInfo taskInfo);
}
