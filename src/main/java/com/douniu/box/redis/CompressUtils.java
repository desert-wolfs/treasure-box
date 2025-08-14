package com.douniu.box.redis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * CompressUtils
 *
 * @author ZhangYiDong
 * @date 2022/11/7 10:51
 */
@Slf4j
public class CompressUtils {

    /**
     * Gzip压缩
     *
     * @param data 数据
     * @return 压缩数据
     */
    public static byte[] compressGzip(String data) {
        ByteArrayOutputStream bos = null;
        GZIPOutputStream gzip = null;
        try {
            bos = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(bos);

            gzip.write(data.getBytes(StandardCharsets.UTF_8));
            gzip.finish();
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Gzip压缩失败", e);
            return null;
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(gzip);
        }
    }

    /**
     * Gzip解压缩
     *
     * @param data 压缩数据
     * @return 解压数据
     */
    public static String decompressGzip(byte[] data) {
        ByteArrayOutputStream bos = null;
        GZIPInputStream gzip = null;
        try {
            bos = new ByteArrayOutputStream();
            gzip = new GZIPInputStream(new ByteArrayInputStream(data));

            IOUtils.copy(gzip, bos);
            bos.flush();
            return bos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Gzip解压失败", e);
            return null;
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(gzip);
        }
    }

}
