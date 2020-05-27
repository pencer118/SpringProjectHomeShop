package kr.koreait.main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import kr.koreait.mybatis.MybatisDAO;
import kr.koreait.utill.FileUtills;
import kr.koreait.vo.CartVO;
import kr.koreait.vo.GoodsVO;
import kr.koreait.vo.NoticeList;
import kr.koreait.vo.NoticeVO;
import kr.koreait.vo.ReviewList;
import kr.koreait.vo.ReviewVO;
import kr.koreait.vo.StokeVO;

/**
 * 
 * @author DAYE KIM
 * @version 1.0
 *
 */


@Controller
public class DK_HomeController {
	@Autowired
	public SqlSession sqlSession, sqlSession1, sqlSession2, sqlSession3;
	@Autowired
	HttpSession session;
	
	@Resource(name= "uploadPath1")
	private String uploadPath1;
	@Resource(name= "uploadPath2")
	private String uploadPath2;
	@Resource(name= "uploadPath3")
	private String uploadPath3;
	@Resource(name= "uploadPath_ACC")
	private String uploadPath_ACC;
	@Resource(name= "uploadPath_TOP")
	private String uploadPath_TOP;
	@Resource(name= "uploadPath_BOTTOM")
	private String uploadPath_BOTTOM;
	
	private static final Logger logger = LoggerFactory.getLogger(DK_HomeController.class);
    
//	다예부분!!
	
//	브라우저에 출력할 1페이지 분량의 글 얻어오기
	/**
	 * 게시판에 1페이지 분량의 글 얻어오기
	 * 
	 * @return 한페이지 분량의 공지 글 
	 */
	@RequestMapping("/list")
	public String list(HttpServletRequest request, Model model) {
		System.out.println("list() 실행");
		MybatisDAO mapper = sqlSession1.getMapper(MybatisDAO.class);
		
		int pageSize = 10;
		int currentPage = 1;
		try {
		currentPage = Integer.parseInt(request.getParameter("currentPage"));
		} catch(NumberFormatException e) { }
		int totalCount = mapper.selectCount();
//		System.out.println(totalCount);
		
		AbstractApplicationContext ctx = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
		NoticeList noticelist = ctx.getBean("noticeList", NoticeList.class);
		noticelist.initNoticeList(pageSize, totalCount, currentPage);
		
		HashMap<String, Integer> hmap = new HashMap<String, Integer>();
		hmap.put("startNo", noticelist.getStartNo());
		hmap.put("endNo", noticelist.getEndNo());
		noticelist.setNoticeList(mapper.selectList(hmap));
//		System.out.println(noticelist);
		
		model.addAttribute("noticeList", noticelist);
		return "list";

	}
	
	/**
	 * 글 쓰기버튼을 누를시 
	 * 
	 * @return 글쓰기 창 띄움
	 */
	@RequestMapping("/insert")
	public String insert(HttpServletRequest request, Model model){
		System.out.println("insert 실행");
		return "insert";
	}
	
	/**
	 * 글 저장
	 *
	 * @param noticeVO 
	 * @return SQL문을 이용해 글을 저장하고 LIST로 돌아감.
	 */
	@RequestMapping("/insertOK")
	public String insertOK(HttpServletRequest request, Model model, NoticeVO noticeVO) {
		System.out.println("insertOK 실행");
		MybatisDAO mapper = sqlSession1.getMapper(MybatisDAO.class);
		mapper.insert(noticeVO);
		
		return "redirect:list";
	}
	
	/**
	 * 글 내용 보기
	 * 
	 * @return 글번호(IDX)에 해당하는 글을 얻어와 화면에 뿌려준다.
	 */
	@RequestMapping("/contentView")
	public String contentView(HttpServletRequest request, Model model) {
		System.out.println("contentView 실행");
		MybatisDAO mapper = sqlSession1.getMapper(MybatisDAO.class);
		int idx = Integer.parseInt(request.getParameter("idx"));
		AbstractApplicationContext ctx = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
		NoticeVO noticeVO = ctx.getBean("noticeVO", NoticeVO.class);
		noticeVO = mapper.selectByIdx(idx);
		model.addAttribute("vo", noticeVO);
		model.addAttribute("currentPage", Integer.parseInt(request.getParameter("currentPage")));

		return "contentView";
	}
	
	/**
	 * 글 삭제
	 * @return 선택된 글을 IDX번호를 가져와 SQL문을 이용해 DB에서 삭제 후 게시판으로 넘어감.
	 * 
	 */
	@RequestMapping("/delete")
	public String delete(HttpServletRequest request, Model model) {
		System.out.println("delete 실행");
		MybatisDAO mapper = sqlSession1.getMapper(MybatisDAO.class);
		int idx = Integer.parseInt(request.getParameter("idx"));
		mapper.delete(idx);
		
		model.addAttribute("currentPage", Integer.parseInt(request.getParameter("currentPage")));
		return "redirect:list";
	}
	
	/**
	 * 글 수정
	 * @param noticeVO 
	 * 
	 * @return 수정하기 버튼을 누르면 수정할수 있는 창이 뜨고 SQL문을 이용해 UPDATE 하고 게시판으로 돌아간다.
	 */
	@RequestMapping("/update")
	public String update(HttpServletRequest request, Model model, NoticeVO noticeVO) {
		System.out.println("update 실행");
		MybatisDAO mapper = sqlSession1.getMapper(MybatisDAO.class);
		mapper.update(noticeVO);
		
		model.addAttribute("currentPage", Integer.parseInt(request.getParameter("currentPage")));
		return "redirect:list";
		
	}
	
	/**
	 * 제품 상세보기
	 * @return IDX를 이용해 상품내역 얻어오기 그리고 REVIEW 부분을 위해 리뷰내역도 얻어오기
	 * 
	 */
	@RequestMapping(value="/contentView_goods")
	   public String contentView_goods(HttpServletRequest request, Model model){
	      System.out.println("아이템 자세히 보기");
	      int idx=Integer.parseInt(request.getParameter("idx"));
	      int currentPage = 1;
	      try {
	    	  currentPage = Integer.parseInt(request.getParameter("currentPage"));
	      } catch(NumberFormatException e) { }
	      MybatisDAO mapper = sqlSession1.getMapper(MybatisDAO.class);
	      GoodsVO vo = mapper.selectGoods(idx);
	      model.addAttribute("vo",vo);
	      model.addAttribute("currentPage", currentPage);
	      ArrayList<StokeVO> svo = mapper.selectColor(idx);
	      model.addAttribute("idx", svo.get(0).getIdx());
	      model.addAttribute("stc", svo);
	      
//	      리뷰 가져오기
	      MybatisDAO mapper1 = sqlSession3.getMapper(MybatisDAO.class);
			int pageSize = 8;
			
			int totalCount = mapper1.selectCount1(idx);//해당아이템의 리뷰개수
			
			AbstractApplicationContext ctx = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
			ReviewList reviewList = ctx.getBean("reviewList", ReviewList.class);
			reviewList.initReviewList(pageSize, totalCount, currentPage);
			HashMap<String, Integer> hmap = new HashMap<String, Integer>();
			hmap.put("startNo", reviewList.getStartNo());
			hmap.put("endNo", reviewList.getEndNo());
			hmap.put("goodsidx", idx);
			reviewList.setReviewList(mapper1.selectList2(hmap));
			model.addAttribute("reviewList", reviewList);
	      
	      return "contentView_goods";
	   }

	
	   @RequestMapping(value = "/uploadForm1", method = RequestMethod.GET)
	   public void uploadForm1GET(Locale locale, Model model) {
	      logger.info("uploadForm1 GET");
	   }
	   /**
	    * 공지글, 리뷰 이미지 파일 업로드
	    * @param file 이미지 파일
	    * @return IDX와 날짜를 사용해 이름을 지정한후 지정된 폴더에 사진 저장후 게시판으로 돌아감.
	    * @throws Exception
	    */
	   @RequestMapping(value = "/uploadForm1", method = RequestMethod.POST)
	   public String uploadForm1POST(MultipartFile file, Model model, NoticeVO vo) throws Exception {
	      MybatisDAO mapper = sqlSession1.getMapper(MybatisDAO.class);
	      int noticeIdx = mapper.selectNoticeIdx();
	      vo.setGoodsidx(noticeIdx);
	      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	      String date = sdf.format(new Date());
	      String savedFileName = " ";
	      // 사진없이 글만 적을 경우.
	      if(file.getOriginalFilename().equals("")) {
	         savedFileName = " ";
	      } else {
	         savedFileName = FileUtills.uploadFile(file,uploadPath1, noticeIdx, date);
	      }
	      vo.setAttached(savedFileName);
	      mapper.insert(vo);
	      model.addAttribute("savedFileName", savedFileName);
	      return "redirect: list";
	   }
	   
	   /**
	    * 내역보던중 수정하고 싶을때 수정하기 버튼 클릭시 작동
	    * @return 필요한 내용을 가지고 UPDATE 창으로넘어감.
	    */
	   @RequestMapping("/view2")
	   public String view2(HttpServletRequest request, Model model) {
	      System.out.println("contentView2 실행");
	      MybatisDAO mapper = sqlSession1.getMapper(MybatisDAO.class);
	      int idx = Integer.parseInt(request.getParameter("idx"));
	      AbstractApplicationContext ctx = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
	      NoticeVO noticeVO = ctx.getBean("noticeVO", NoticeVO.class);
	      noticeVO = mapper.selectByIdx(idx);
	      
	      model.addAttribute("vo", noticeVO);
	      model.addAttribute("currentPage", Integer.parseInt(request.getParameter("currentPage")));
	      return "update";
	   }
	   
	   /**
	    * 리뷰게시판
	    * 
	   
	    * @return 브라우저에 출력할 1페이지 분량의 글을 얻어온다.
	    */
//	    리뷰게시판!!!   
//	    브라우저에 출력할 1페이지 분량의 글 얻어오기     
	     @RequestMapping("/reviewList")
	    public String list1(HttpServletRequest request, Model model) {
	       System.out.println("list() 실행");
//	       db에서 전체 review리스트 가져오기.      
	       MybatisDAO mapper = sqlSession3.getMapper(MybatisDAO.class);
	       
	       int pageSize = 10;
	       int currentPage = 1;
	       try {
	       currentPage = Integer.parseInt(request.getParameter("currentPage"));
	       } catch(NumberFormatException e) { }
	       int totalCount = mapper.selectCount();
//	       System.out.println(totalCount);
	       
	       AbstractApplicationContext ctx = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
	       ReviewList reviewList = ctx.getBean("reviewList", ReviewList.class);
	       reviewList.initReviewList(pageSize, totalCount, currentPage);
	       
	       
	       HashMap<String, Integer> hmap = new HashMap<String, Integer>();
	       hmap.put("startNo", reviewList.getStartNo());
	       hmap.put("endNo", reviewList.getEndNo());
	       reviewList.setReviewList(mapper.selectList1(hmap));
	       
	       model.addAttribute("reviewList", reviewList);
	       
//	       상품별로 리스트 가져오기.
	       MybatisDAO mapper1 = sqlSession1.getMapper(MybatisDAO.class);
	       ArrayList<GoodsVO> goodslist = new ArrayList<GoodsVO>(); 
	       try {
		
	       for (int i = 0; i <  10; i++) {
	          ReviewVO vo = reviewList.getReviewList().get(i);
	          System.out.println(vo);
	          int idx = vo.getGoodsidx();
	          GoodsVO goodsVO = mapper1.selectGoods(idx);
	          goodslist.add(goodsVO);
	          System.out.println(goodslist);
	       }
	       
	       model.addAttribute("goodslist", goodslist);
	       } catch (IndexOutOfBoundsException e) {
			}
	       
	       return "reviewList";

	    }
	     
	     @RequestMapping("/showReview")
	     public void showReview(HttpServletRequest request, Model model, HttpServletResponse response, ReviewVO vo) throws IOException {
	    	 System.out.println("리뷰보여주는 아작스입니다.....");
	    	 MybatisDAO mapper = sqlSession3.getMapper(MybatisDAO.class);
	    	 int pageSize = 8;
				int idx = Integer.parseInt(request.getParameter("idx"));
				int currentPage = Integer.parseInt(request.getParameter("page"));//여기서 에러,.
				System.out.println(currentPage);
				int totalCount = mapper.selectCount1(idx);//해당아이템의 리뷰개수
				
				
				response.getWriter().write(show(pageSize, idx, currentPage, totalCount));
	    	 
	     }
	    
	      private String show(int pageSize, int idx, int currentPage, int totalCount) {
	    	  System.out.println("show() 작동??");
	    	  
	    	  AbstractApplicationContext ctx = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
				ReviewList reviewList = ctx.getBean("reviewList", ReviewList.class);
				reviewList.initReviewList(pageSize, totalCount, currentPage);
				HashMap<String, Integer> hmap = new HashMap<String, Integer>();
				hmap.put("startNo", reviewList.getStartNo());
				hmap.put("endNo", reviewList.getEndNo());
				hmap.put("goodsidx", idx);
				ArrayList<ReviewVO> reviewA =  sqlSession3.getMapper(MybatisDAO.class).selectList2(hmap);
				StringBuffer result = new StringBuffer();
				result.append("{\"result\":[");
				for (int i = 0; i < reviewA.size(); i++) {
					result.append("[{\"value\":\"" + reviewA.get(i).getContent() + "\"},");
					result.append("{\"value\":\"" + reviewA.get(i).getName()+ "\"},");
					result.append("{\"value\":\"" + reviewA.get(i).getWriteDate() + "\"},");
					result.append("{\"value\":\"" + reviewA.get(i).getAttached() + "\"},");
					result.append("{\"value\":\"" + reviewA.get(i).getStar() + "\"}],");
				}
				result.append("]}");
				System.out.println(result);
				return result.toString();
		}



		String savedFileName= "";
//	    리뷰 작성하기
	      /**
	       * 리뷰 업로드
	       * @param file 이미지
	       * @return 이미지를 DB에 저장.
	       * @throws Exception
	       */
	     @RequestMapping("/uploadReview")
		public String uploadReview(MultipartFile file, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
	   	  System.out.println("파일 업로드 부터 했음 좋겠다~~");
		      MybatisDAO mapper = sqlSession3.getMapper(MybatisDAO.class);
		      int reviewIdx = mapper.selectReviewIdx();
		      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		      String date = sdf.format(new Date());
//		      redirect를 위해 idx를 받아주고 설정해줌.
		      int idx = Integer.parseInt(request.getParameter("idx"));
		      System.out.println("idx: " + idx);
		      model.addAttribute("idx", idx);
		       savedFileName = " ";
		      // 사진없이 글만 적을 경우.
		      if(file.getOriginalFilename().equals("")) {
		         savedFileName = " ";
		      } else {
		         savedFileName = FileUtills.uploadFile(file,uploadPath3, reviewIdx, date);
		      }
		      return "redirect: contentView_goods";
		   }
	     
	     /**
	      * AJAX를 사용해 리뷰 글 작성
	      * @throws IOException
	      */
	     @RequestMapping("/insertReview")
	     public void insertReview(HttpServletRequest request, Model model, HttpServletResponse response, ReviewVO vo) throws IOException {
	   	  System.out.println("insertReview() 메소드");
	   	 
	   	  String name = request.getParameter("name");
		  		String content = request.getParameter("content");
		  		int star = Integer.parseInt(request.getParameter("star"));
		  		String attached = savedFileName;
		  		System.out.println(attached);
		  		int goodsidx = Integer.parseInt(request.getParameter("idx"));
		  		response.getWriter().write(insert1(name, content, star, attached, goodsidx) + ""); 
	     }
	     
	     private int insert1(String name, String content, int star, String attached, int goodsidx) {
	   	  System.out.println("리뷰 insert작동??");
	   	  ReviewVO vo = new ReviewVO();
	   	  try {
	   		  vo.setName(name);
	   		  vo.setContent(content);
	   		  vo.setGoodsidx(goodsidx);
	   		  vo.setStar(star);
	   		  vo.setAttached(attached);
	   	  }catch (Exception e) {
	   		  return 0;
	   	  }
	   	  	return sqlSession3.getMapper(MybatisDAO.class).insertReview(vo); 
				
	     }
	     
	     @RequestMapping("/imagePopup")
	     public String imagePopup(HttpServletRequest request, Model model, HttpServletResponse response) {
	    	 System.out.println("이미지 팝업!");
	    	String img = request.getParameter("image");
	    	System.out.println(img);
	    	 return "imagePopup";
	     } 

}
