package com.epam.catgenome.util.aws;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.utils.CountingInputStream;
import com.amazonaws.services.s3.AmazonS3URI;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.util.Log;
import htsjdk.samtools.util.RuntimeIOException;

/**
 * Wrapper class for S3ParallelStream that supports @<code>seek()</code> method.
 * If position skip requested by seek() is small, just skips bytes. Otherwise, recreates stream.
 */
public class SeekableS3Stream extends SeekableStream {

    private static final Log log = Log.getInstance(SeekableS3Stream.class);
    private static final long SEEK_MIN_DISTANCE =
            Configuration.getNumberOfConnections() *
                    (Configuration.getMaxDownloadPartSize() + Configuration.getMinDownloadPartSize())
                    / 2;

    private final AmazonS3URI s3Source;
    private CountingInputStream currentDataStream;
    private final long length;
    private long offset;
    private final S3InputStreamFactory factory;

    SeekableS3Stream(AmazonS3URI source, S3Client client) {
        this.s3Source = source;
        this.factory = new S3InputStreamFactory(client);
        length = client.getFileSize(s3Source);
        recreateInnerStream();
    }

    private void recreateInnerStream() {
        if (null != currentDataStream) {
            try {
                currentDataStream.close();
            } catch (IOException e) {
                throw new RuntimeIOException(e.getMessage() + "failed to close the data stream", e);
            }
        }

        final InputStream fileOnOffsetDataStream =
                new S3ParallelStream(s3Source, offset, length(), factory);
        this.currentDataStream = new CountingWithSkipInputStream(fileOnOffsetDataStream);
        log.debug("A new data stream was launched on offset = ", offset);
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public long position() throws IOException {
        return offset + currentDataStream.getBytesRead();
    }

    /**
     * A method that jumps to a specific position in file.
     *
     * @param targetPosition target position in file.
     */
    @Override
    public void seek(long targetPosition) throws IOException {
        final long distance = targetPosition - position();
        log.debug("Seeking from ", position(), " to ", targetPosition);

        if (distance < SEEK_MIN_DISTANCE && distance > 0) {
            log.debug("Short seek distance - skip instead. Distance: ", distance);
            long skipped = currentDataStream.skip(distance);
            log.debug("Skipped bytes: ", skipped);
            return;
        }

        this.offset = targetPosition;
        log.debug("Seek on position = ", targetPosition);
        recreateInnerStream();
    }

    @Override
    public int read() throws IOException {
        return currentDataStream.read();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return currentDataStream.read(buffer, offset, length);
    }

    @Override
    public void close() throws IOException {
        PerformanceMonitor.printSummary();
        currentDataStream.close();
    }

    @Override
    public boolean eof() throws IOException {
        return position() == length();
    }

    @Override
    public String getSource() {
        return s3Source.toString();
    }

    /**
     * We should count data skipped because we want to count all data loaded.
     */
    private static class CountingWithSkipInputStream extends CountingInputStream {
        CountingWithSkipInputStream(InputStream in) {
            super(in);
        }

        @Override
        public long skip(long n) throws IOException {
            long bytesSkipped = in.skip(n);
            count(bytesSkipped);

            return bytesSkipped;
        }
    }
}

