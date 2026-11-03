package com.hexagon.hrms.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hexagon.hrms.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

/*
 증명사진 파일 저장 / 조회
 - 파일은 webapp 밖 업로드 폴더(app.properties 의 upload.dir)에 저장 → 재배포해도 지워지지 않음
 - DB(emp.photo_path)에는 업로드 폴더 기준 상대 경로만 저장 (예: emp/20260014_1730000000000.jpg)
 - 화면에서는 /employees/{empId}/photo 로 읽어 감 (EmpController)
*/
@Slf4j
@Service
public class PhotoService {

	public static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB

	@Value("${upload.dir}")
	private String uploadDir;

	// 올릴 수 있는 사진인지 검사 (문제가 있으면 안내 문구, 없으면 null)
	// 컨트롤러에서 먼저 불러 입력 화면에 오류를 보여주고, save 에서 한 번 더 확인한다
	public String validate(MultipartFile file) {
		if (getExtension(file.getContentType()) == null) {
			return "증명사진은 JPG 또는 PNG 파일만 올릴 수 있습니다.";
		}
		if (file.getSize() > MAX_SIZE) {
			return "증명사진은 2MB 이하만 올릴 수 있습니다.";
		}
		// 확장자만 바꾼 다른 파일을 막기 위해 실제로 이미지로 읽히는지 확인
		try (InputStream in = file.getInputStream()) {
			BufferedImage image = ImageIO.read(in);
			return image == null ? "이미지 파일이 아닙니다. JPG 또는 PNG 파일을 올려 주세요." : null;
		} catch (IOException e) {
			return "사진 파일을 읽을 수 없습니다.";
		}
	}

	// 저장하고 상대 경로를 돌려준다
	public String save(MultipartFile file, String empNo) {
		String error = validate(file);
		if (error != null) {
			throw new BusinessException(error);
		}
		String ext = getExtension(file.getContentType());
		String path = "emp/" + empNo + "_" + System.currentTimeMillis() + ext;
		File dest = new File(uploadDir, path);
		dest.getParentFile().mkdirs();
		try (InputStream in = file.getInputStream()) {
			Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("사진 저장 실패 {}", dest, e);
			throw new BusinessException("사진을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.");
		}
		log.debug("증명사진 저장 {}", dest);
		return path;
	}

	// DB 에 저장된 상대 경로 → 실제 파일 (없거나 업로드 폴더 밖을 가리키면 null)
	public File getFile(String path) {
		if (path == null) {
			return null;
		}
		try {
			File root = new File(uploadDir).getCanonicalFile();
			File file = new File(root, path).getCanonicalFile();
			if (!file.toPath().startsWith(root.toPath()) || !file.isFile()) {
				return null;
			}
			return file;
		} catch (IOException e) {
			return null;
		}
	}

	// 사진을 바꿨을 때 예전 파일 지우기 (실패해도 업무에는 지장 없음)
	public void delete(String path) {
		File file = getFile(path);
		if (file != null && !file.delete()) {
			log.warn("예전 사진을 지우지 못함 {}", file);
		}
	}

	public static String getContentType(File file) {
		return file.getName().endsWith(".png") ? "image/png" : "image/jpeg";
	}

	private String getExtension(String contentType) {
		if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType) || "image/pjpeg".equals(contentType)) {
			return ".jpg";
		}
		if ("image/png".equals(contentType)) {
			return ".png";
		}
		return null;
	}
}
