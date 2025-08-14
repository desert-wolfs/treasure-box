package com.douniu.box.gencode;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenCodeService {

    public void genCode(GenCodeRequest request) {
        try {
            MybatisGeneratorRunner.genCodes(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record GenCodeRequest(List<String> tableNames,
                                 List<String> newFileNames) {
    }
}
