package data.scrap;

import com.xdg.util.*;
import data.scrap.util.GetUtil;
import data.scrap.util.HeaderUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Downloader {
    private int threadNum = 1;
    private String url;
   volatile private long byteSize;
    private String sizeUnit;
    private String unitfiedSize;
    private String fileName;
    private final static Log log = LogFactory.getLog(Downloader.class);
    volatile private boolean stopMonitor;

    public Downloader(String url, int threadNum) {
        this.url = url;
        this.threadNum = threadNum;
    }

    public Downloader(String url) {
        this.url = url;
    }

    /**
     * entry point to download files
     * @param saveDir
     * @param saveName file name to save, use original file name if kept null
     * @throws Exception
     */
    public void download(String saveDir, String saveName) throws Exception {
        if (FileUtil.isNotExist(saveDir)){
            FileUtil.makeDirs(saveDir);
        }

        long startTime = System.currentTimeMillis();
        ExeResult result1 = GetUtil.doGet(url);
        if (result1.getResponse().getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new HttpException("Can't connect to the remote server at '"+url+"'");
        }
        if (!HeaderUtil.isFile(result1.getResponse())) {
            throw new HttpException("No file found at '" + url + "'");
        }
        byteSize = HeaderUtil.getSize(result1.getResponse());
        fileName = HeaderUtil.getFileName(result1.getResponse(),url);
        convertSizeUnit();

        log.debug("the file size is " + unitfiedSize + sizeUnit + "/" + byteSize + "B");
        final ArrayList<DownloadWorker> workers = new ArrayList<DownloadWorker>();
        ExecutorService pool = Executors.newFixedThreadPool(15);
        long sizePerThread = byteSize / threadNum;
        for (int i = 0; i < threadNum; i++) {
            DownloadWorker worker;
            if (i == 0) {
                worker = new DownloadWorker(result1, (i + 1), 0, sizePerThread);
            } else if (i < threadNum - 1) {
                worker = new DownloadWorker(url, (i + 1), i * sizePerThread, sizePerThread);
            } else {
                worker = new DownloadWorker(url, (i + 1), i * sizePerThread, byteSize - sizePerThread * i);
            }

            workers.add(worker);
        }
        monitorDownloadState(workers);
        try {
            pool.invokeAll(workers);
        } catch (InterruptedException e) {
            log.error(e);
            throw e;
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
        pool.shutdown();
        stopMonitor = true;
        if (isDownloadOK(workers)) {
            String savePath = null;
            if (saveName == null) {
                savePath = saveDir + "/" + fileName;
            } else {
                savePath = saveDir + "/" + saveName;
            }

            log.info("partial files are being merged into one last file '"+savePath+"'");
            mergePartialFiles(workers, savePath);
            log.info("merging finished, seconds elapsed is " + TimeUtil.elapsedSecond(startTime));
        }

    }

    /**
     * merge temporary and partial files into one file
     * @param workers
     * @param savePath
     */
    private void mergePartialFiles(ArrayList<DownloadWorker> workers, String savePath) {
        ArrayList<File> files = new ArrayList<File>();
        for (DownloadWorker worker : workers) {
            files.add(new File(worker.getFilePath()));
        }

        FileUtil.mergePartialFilesLinear(files.toArray(new File[]{}), savePath);

    }

    private boolean isDownloadOK(ArrayList<DownloadWorker> workers) {
        for (DownloadWorker worker : workers) {
            if (!worker.isSuccessful()) {
                log.error("Thread "+worker.getId()+" fail to download all bytes, expected bytes : "
                        +worker.getLength()+", actual bytes: "+worker.getReadBytes());
                return false;
            }
        }

        return true;
    }

    private void monitorDownloadState(final ArrayList<DownloadWorker> workers) {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    boolean exit = false;
                    for (DownloadWorker worker : workers) {
                        if (worker.isInvoked()) {
                            exit = true;
                            break;
                        }
                    }

                    if (!exit) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            log.error(e);
                        }
                    } else {
                        break;
                    }

                }

                long startTime = System.currentTimeMillis();
                long lastDownloadedBytes=0;
                while (!stopMonitor) {
                    long downloadedBytes = 0;
                    for (DownloadWorker worker : workers) {
                        if (worker.isInvoked()) {
                            downloadedBytes += worker.getReadBytes();
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }

                    if (lastDownloadedBytes!=downloadedBytes){
                        lastDownloadedBytes=downloadedBytes;
                    } else {
                        continue;
                    }

                    log.info(downloadedBytes / 1024 + "KB(" + downloadedBytes * 100 / byteSize + "%) has been downloaded, average speed is " +
                            NumUtil.truncateDecimal(downloadedBytes / 1024.0 / ((System.currentTimeMillis() - startTime) / 1000.0), 2)+ "KB/S");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }
                }
            }
        }).start();
    }

    private void convertSizeUnit() {
        if (byteSize < SizeUnit.MB) {
            unitfiedSize = NumUtil.truncateDecimal(byteSize / 1024, 0);
            sizeUnit = "KB";
        } else if (byteSize >= SizeUnit.MB && byteSize < SizeUnit.GB) {
            unitfiedSize = NumUtil.truncateDecimal(byteSize / SizeUnit.MB, 2);
            sizeUnit = "MB";
        } else {
            unitfiedSize = NumUtil.truncateDecimal(byteSize / SizeUnit.GB, 2);
            sizeUnit = "GB";
        }
    }

}

class DownloadWorker implements Callable<Object> {
    private String url;
    private long start;
    private long length;
    private ExeResult exeResult;
    private final static Log log = LogFactory.getLog(DownloadWorker.class);
    private int id;
    private long readBytes;
    private boolean invoked;
    private boolean successful;
    private String filePath;

    DownloadWorker(String url, int id, long start, long length) {
        this(GetUtil.doGet(url), id, start, length);
        this.url = url;
    }

    DownloadWorker(String url, int id, long start) {
        this(GetUtil.doGet(url), id, start);
        this.url = url;
    }

    DownloadWorker(ExeResult exeResult, int id, long start, long length) {
        this.exeResult = exeResult;
        this.start = start;
        this.length = length;
        this.id = id;
        this.url = exeResult.getUrl();
    }

    DownloadWorker(ExeResult exeResult, int id, long start) {
        this.exeResult = exeResult;
        this.start = start;
        this.id = id;
        this.url = exeResult.getUrl();
    }

    public long getReadBytes() {
        return readBytes;
    }

    public Object call() throws Exception {
        if (exeResult.getResponse().getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new HttpException("Thread " + id + " can't connect to the remote server at '"+url+"'");
        }

        if (!HeaderUtil.isFile(exeResult.getResponse())) {
            throw new HttpException("No file found'" + url + "'");
        }

        invoked = true;
        filePath = SysPropUtil.getTempDir() + HeaderUtil.getFileName(exeResult.getResponse(),url) + ".part" + id;

        log.debug("Thread " + id + " is download bytes of " + start + " to " + (start + length - 1) + ", path : " + filePath);
        HttpResponse response = exeResult.getResponse();
//        byte[] buffer = new byte[4096];
        InputStream is = null;
        try {
            is = response.getEntity().getContent();
        } catch (IOException e) {
            log.error(e);
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            log.error(e);
        }

        try {
            long writtenBytes = FileUtil.writePart(fos, is, HeaderUtil.getSize(exeResult.getResponse()), start, length, new FileWriteProgress() {
                public void changed(long readByteCnt) {
                    readBytes = readByteCnt;
                }
            });

            if (writtenBytes >= length){
                successful = true;
            }
            EntityUtils.consume(exeResult.getResponse().getEntity());
        } catch (IOException e) {
            log.error(e);
        } finally {
            exeResult.getHttpClient().getConnectionManager().shutdown();
        }

        return null;
    }

    public boolean isInvoked() {
        return invoked;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getId() {
        return id;
    }

    public long getLength() {
        return length;
    }
}
