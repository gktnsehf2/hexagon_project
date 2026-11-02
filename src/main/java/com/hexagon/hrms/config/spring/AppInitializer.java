package com.hexagon.hrms.config.spring;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/*
 web.xml 대신 java로 DispatcherServlet과 필터를 등록하는 클래스
 - getRootConfigClasses()   : 모든 서블릿이 함께 쓰는 빈(서비스, 매퍼, 시큐리티 등) → RootConfig
 - getServletConfigClasses(): DispatcherServlet 전용 빈(컨트롤러, 뷰리졸버 등) → AppConfig
*/
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[] { RootConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] { AppConfig.class };
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	// 파일 업로드 (증명사진) 허용: 파일 1개 5MB, 요청 전체 10MB 까지
	// 실제 허용 크기(2MB)는 화면(자바스크립트)과 PhotoService 에서 검사해 안내 문구를 보여준다
	// (5MB 를 넘으면 컨트롤러에 오기 전에 톰캣이 거절함)
	@Override
	protected void customizeRegistration(ServletRegistration.Dynamic registration) {
		registration.setMultipartConfig(new MultipartConfigElement("", 5 * 1024 * 1024, 10 * 1024 * 1024, 0));
	}

	@Override
	protected Filter[] getServletFilters() {
		// 한글 인코딩 필터 (시큐리티 필터보다 먼저 와야 로그인 파라미터 한글이 깨지지 않음)
		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		encodingFilter.setEncoding("UTF-8");
		encodingFilter.setForceEncoding(true);

		// 스프링 시큐리티 필터 프록시 (빈 이름은 반드시 springSecurityFilterChain)
		DelegatingFilterProxy securityFilter = new DelegatingFilterProxy("springSecurityFilterChain");

		return new Filter[] { encodingFilter, securityFilter };
	}
}
