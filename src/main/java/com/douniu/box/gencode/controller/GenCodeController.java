package com.douniu.box.gencode.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.douniu.box.gencode.MybatisGeneratorRunner;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/gen-code")
public class GenCodeController {
    public static final String TEMP_TARGET = File.separator + "tempcode" + File.separator;
    public static final String GEN_TARGET = TEMP_TARGET + "code";
    public static final String ZIP_TARGET = TEMP_TARGET +"code.zip";

    // http://127.0.0.1:8080/gen-code/download?tableNames=game_rank_day_log,game_rank_daily_data
    @GetMapping("/download")
    public void downloadCode(@RequestParam("tableNames") List<String> tableNames, HttpServletResponse response) throws Exception {
        OutputStream out = null;
        File sourceFile = new File(GEN_TARGET);
        try {
            MybatisGeneratorRunner.main(tableNames.toArray(new String[0]));
            if (!sourceFile.exists()) {
                System.out.println("sourceFile not exists");
                return;
            }

            // 创建 ZIP 文件的父目录
            File zipfile = new File(ZIP_TARGET);
            System.out.println(zipfile.getAbsolutePath());

            // 压缩文件列表到指定的 ZIP 文件
            ZipUtil.zip(zipfile, true, sourceFile);
            System.out.println("zip file: " + ZIP_TARGET);

            byte[] bytes = FileUtil.readBytes(zipfile);
            response.reset(); // 非常重要
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("code.zip", "UTF-8"));
            response.setContentType("application/octet-stream");
            out = response.getOutputStream();
            out.write(bytes);


        } finally {
            if (sourceFile.exists()) {
                String absolutePath = sourceFile.getParent();
                System.out.println("delete file: " + absolutePath);
                FileUtil.del(absolutePath);
            }
            if (out != null) {
                out.close();
            }
        }

    }

}
