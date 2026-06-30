package com.xuelian.career.util;

import com.xuelian.career.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 简历文本提取工具类
 * <p>
 * 支持 PDF、DOCX、TXT 三种常见简历格式，基于已有 Apache POI 与 PDFBox 依赖实现，
 * 提取失败时抛出业务异常，由调用方降级到兜底方案。
 * </p>
 */
@Slf4j
public final class ResumeTextExtractor {

    /** 简历文本最大长度，超过则截断并追加提示，避免 AI Token 超限 */
    private static final int MAX_RESUME_TEXT_LENGTH = 8000;

    /** 截断后追加的提示语 */
    private static final String TRUNCATE_HINT = "\n\n[简历内容过长，已截断展示]";

    private ResumeTextExtractor() {
    }

    /**
     * 从上传文件中提取简历文本
     *
     * @param file 上传的简历文件
     * @return 清洗后的简历正文
     * @throws BusinessException 文件为空、格式不支持或提取失败时抛出
     */
    public static String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("简历文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename);
        try (InputStream is = file.getInputStream()) {
            String text = switch (ext) {
                case "pdf" -> extractPdf(is);
                case "docx" -> extractDocx(is);
                case "txt" -> extractTxt(is);
                default -> throw new BusinessException("不支持的简历格式，仅支持 PDF、DOCX、TXT");
            };
            return cleanText(text);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("简历文本提取失败: fileName={}", originalFilename, e);
            throw new BusinessException("简历解析失败，请检查文件是否损坏或格式是否正确");
        }
    }

    /**
     * 从本地文件路径提取简历文本
     *
     * @param path 本地文件路径
     * @param ext  文件扩展名（pdf/docx/txt）
     * @return 清洗后的简历正文
     */
    public static String extractTextFromPath(Path path, String ext) {
        if (path == null || !Files.exists(path)) {
            throw new BusinessException("简历文件不存在");
        }

        String lowerExt = ext == null ? "" : ext.toLowerCase();
        try {
            String text = switch (lowerExt) {
                case "pdf" -> extractPdf(Files.newInputStream(path));
                case "docx" -> extractDocx(Files.newInputStream(path));
                case "txt" -> extractTxt(Files.newInputStream(path));
                default -> throw new BusinessException("不支持的简历格式，仅支持 PDF、DOCX、TXT");
            };
            return cleanText(text);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("简历文本提取失败: path={}", path, e);
            throw new BusinessException("简历解析失败，请检查文件是否损坏或格式是否正确");
        }
    }

    /**
     * 使用 PDFBox 提取 PDF 文本
     *
     * @param is 文件输入流
     * @return 原始文本
     * @throws IOException 解析异常
     */
    private static String extractPdf(InputStream is) throws IOException {
        try (PDDocument document = PDDocument.load(is)) {
            if (document.isEncrypted()) {
                throw new BusinessException("无法解析加密的 PDF 文件");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setAddMoreFormatting(false);
            return stripper.getText(document);
        }
    }

    /**
     * 使用 Apache POI 提取 DOCX 文本
     *
     * @param is 文件输入流
     * @return 原始文本
     * @throws IOException 解析异常
     */
    private static String extractDocx(InputStream is) throws IOException {
        try (XWPFDocument document = new XWPFDocument(is)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    /**
     * 读取 TXT 纯文本
     *
     * @param is 文件输入流
     * @return 原始文本
     * @throws IOException 读取异常
     */
    private static String extractTxt(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 清洗简历文本：去除多余空行、首尾空白、统一换行符，并按最大长度截断
     *
     * @param text 原始文本
     * @return 清洗后的文本
     */
    private static String cleanText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        // 统一换行符并去除首尾空白
        String cleaned = text.trim()
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        // 合并连续空行
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n");

        // 按最大长度截断
        if (cleaned.length() > MAX_RESUME_TEXT_LENGTH) {
            cleaned = cleaned.substring(0, MAX_RESUME_TEXT_LENGTH) + TRUNCATE_HINT;
        }

        return cleaned;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 小写扩展名
     */
    private static String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
}
