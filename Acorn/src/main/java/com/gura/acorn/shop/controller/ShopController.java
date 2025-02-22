package com.gura.acorn.shop.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.gura.acorn.es.ElasticsearchService;
import com.gura.acorn.exception.loginException;
import com.gura.acorn.shop.dto.ShopDto;
import com.gura.acorn.shop.dto.ShopMenuDto;
import com.gura.acorn.shop.dto.ShopReviewDto;
import com.gura.acorn.shop.service.ShopService;

@Controller
public class ShopController {

	@Autowired
	private ShopService service;
	@Autowired
	private ElasticsearchService Esservice;
	
	@Value("${file.location}")
	private String fileLocation;
	
	@GetMapping(
			value="/shop/images/{imageName}",
			produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE}
		)
	@ResponseBody
	public byte[] profileImage(@PathVariable("imageName") String imageName) throws IOException {

		String absolutePath = fileLocation + File.separator + imageName;
		// 파일에서 읽어들일 InputStream
		InputStream is = new FileInputStream(absolutePath);
		// 이미지 데이터(byte) 를 읽어서 배열에 담아서 클라이언트에게 응답한다.
		return IOUtils.toByteArray(is);
	}
	
	@RequestMapping("/search/review_list")
	public String reviewList(HttpServletRequest request) {
		service.getReviewList(request);
		return "search/review_list";
	}
   
	@RequestMapping("/search/search")
	public String search(HttpServletRequest request) {
		service.getSearchList(request);
		service.getReviewList(request);
		return "search/search";
	}
	
	//index 페이지에서 가게리스트 출력
	@RequestMapping("/")
	public String index(HttpServletRequest request, HttpSession session) {
		service.getTopList(request);
		return "index";
	}
	
	//월별 PV, UV, 일일 PV, 총 PV 검색
	@RequestMapping("/es/test")
	@ResponseBody
	public List<Map<String, Object>> test(){
		String index = "testlog6";
		String field = "date";
		
		try {
			Map<String, Object> PVMonthCount = Esservice.aggPVByMonth();
			Map<String, Object> PVDayCount = Esservice.searchDayPV(index, field);
			Map<String, Object> PVTotalCount = Esservice.getTotalPV();
			Map<String, Object> PVMaxCount = Esservice.getMaxPVStore();
			
			List<Map<String, Object>> resultList = new ArrayList<>();
			resultList.add(PVMonthCount);
			resultList.add(PVDayCount);
			resultList.add(PVTotalCount);
			resultList.add(PVMaxCount);
			
			return resultList;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return null;
	}
	
	//현재기준 1분전의 error로그를 검색
	@RequestMapping("/es/test2")
	@ResponseBody
	public List<Map<String, Object>> test2(){
		try {
			return Esservice.searchError();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return null;
	}
	
	//월별 UV를 검색
	@RequestMapping("/es/test3")
	@ResponseBody
	public List<Map<String, Object>> test3(){
		try {
			Map<String, Object> UVMonthCount = Esservice.aggrUVByMonth();
			
			List<Map<String, Object>> resultList = new ArrayList<>();
			resultList.add(UVMonthCount);
			
			return resultList;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return null;
	}
	
	@RequestMapping("/index")
	public String index2(HttpServletRequest request) {
		service.getTopList(request);
		return "index";
	}
	
	@RequestMapping("/shop/list")
	public String list(HttpServletRequest request) {
		service.getList(request);
		return "shop/list";
	}
	
	//글 작성폼 이동
	@GetMapping("/shop/insertform")
	public String insertform(HttpSession session) {
		String id = (String)session.getAttribute("id");
		
		//로그인 하지 않았다면 로그인 페이지로, 관리자가 아니라면 권한페이지로 이동
		if(id == null) {
			throw new loginException("needLogin");
		}else if(!id.equals("admin")) {
			throw new loginException("needAuthority");
		}
		return "shop/insertform";
	}
	
	//글 작성
	@RequestMapping("/shop/insert")
	public String insert(ShopDto dto, HttpServletRequest request) {
		String imagePath = (String)request.getParameter("imagePath");
		dto.setImagePath(imagePath);
		service.saveContent(dto);
		return "shop/insert";
	}
	
	//글 섬네일 등록을 위한 메소드
	@ResponseBody
	@RequestMapping(value = "/shop/image_upload", method = RequestMethod.POST)
	public Map<String, Object> imageUpload(MultipartFile image, HttpServletRequest request) {
		return service.saveImagePath(request, image);
	}
	
	//가게정보 상세보기
	@GetMapping("/shop/detail")
	public String detail(HttpServletRequest request) {
		service.test(request);  
		service.getDetail(request);
		service.menuGetList(request);
		return "shop/detail";
	}
	
	//가게 정보 업데이트
	@GetMapping("/shop/updateform")
	public String updateform(HttpServletRequest request, HttpSession session) {
		String id = (String)session.getAttribute("id");
		
		//로그인 하지 않았다면 로그인 페이지로, 관리자가 아니라면 권한페이지로 이동
		if(id == null) {
			throw new loginException("needLogin");
		}else if(!id.equals("admin")) {
			throw new loginException("needAuthority");
		}
		service.getData(request);
		return "shop/updateform";
	}
	
	@GetMapping("/shop/update")
	public String update(ShopDto dto, HttpServletRequest request) {
		service.updateContent(dto, request);
		return "shop/update";
	}
	
	@GetMapping("/shop/delete")
	public String delete(int num, HttpServletRequest request, HttpSession session) {
		String id = (String)session.getAttribute("id");
		
		//로그인 하지 않았다면 로그인 페이지로, 관리자가 아니라면 권한페이지로 이동
		if(id == null) {
			throw new loginException("needLogin");
		}else if(!id.equals("admin")) {
			throw new loginException("needAuthority");
		}
		service.deleteContent(num, request);
		return "redirect:/";
	}
	
	//리뷰 작성
	@RequestMapping("/shop/review_insert")
	public String reviewInsert(HttpServletRequest request, int ref_group) {
		service.saveReview(request);
		return "redirect:/shop/detail?num="+ref_group;
	}
	
	//리뷰 더보기 요청 처리
	@RequestMapping("/shop/ajax_review_list")
	public String moreReviewList(HttpServletRequest request) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		service.moreReviewList(request);

		return "shop/ajax_review_list";
	}
	
	//리뷰 삭제
	@RequestMapping("/shop/review_delete")
	@ResponseBody
	public Map<String, Object> reviewDelete(HttpServletRequest request) {
		service.deleteReview(request);
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("isSuccess", true);
		return map;
	}
	
	//댓글 수정
	@RequestMapping("/shop/review_update")
	@ResponseBody
	public Map<String, Object> reviewUpdate(ShopReviewDto dto, HttpServletRequest request){
		service.updateReview(dto);
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("isSuccess", true);
		return map;
	}
	
	//리뷰 이미지 업로드
	@ResponseBody
	@RequestMapping(value = "/shop/review_image_upload", method = RequestMethod.POST)
	public Map<String, Object> reviewImageUpload(MultipartFile image, HttpServletRequest request) {
		return service.saveImagePath(request, image);
	}
	
	@RequestMapping("/shop/menulist")
	public String getList(HttpServletRequest request) {
		service.menuGetList(request);
		return "shop/menulist";
	}
	
	@RequestMapping("/shop/menu_insertform")
	public String menuinsertform(HttpSession session) {
		String id = (String)session.getAttribute("id");
		
		//로그인 하지 않았다면 로그인 페이지로, 관리자가 아니라면 권한페이지로 이동
		if(id == null) {
			throw new loginException("needLogin");
		}else if(!id.equals("admin")) {
			throw new loginException("needAuthority");
		}
		return "shop/menu_insertform";
	}
	
	@GetMapping("/shop/menu_insert")
	public String menuinsert(ShopMenuDto dto, HttpServletRequest request) {
		dto.setNum(Integer.parseInt(request.getParameter("num")));
		service.saveMenu(dto, request);		
		return "shop/menu_insert";
	}
}
