package lee.study.down;

public interface HttpDownCallback {

  void start();

  void progress(int index, long chunkDownSize, long chunkTotalSize, long fileDownSize,
      long fileTotalSize);

  void error(int index);

  void done(int index);
}
