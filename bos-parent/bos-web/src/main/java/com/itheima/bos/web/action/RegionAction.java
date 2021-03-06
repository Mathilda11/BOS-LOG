package com.itheima.bos.web.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.itheima.bos.domain.Region;
import com.itheima.bos.service.IRegionService;
import com.itheima.bos.utils.PinYin4jUtils;
import com.itheima.bos.web.action.base.BaseAction;

@Controller
@Scope("prototype")
public class RegionAction extends BaseAction<Region> {
	//属性驱动接受上传的文件
	private File regionFile;
	@Autowired
	private IRegionService regionService;

	public void setRegionFile(File regionFile) {
		this.regionFile = regionFile;
	}
	/**
	 * 区域导入
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public String importXls() throws FileNotFoundException, IOException{
		List<Region> regionList = new ArrayList<Region>();
 		HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(regionFile));
		HSSFSheet hssfSheet = workbook.getSheet("Sheet1");
		for (Row row : hssfSheet) {
			int rowNum = row.getRowNum();
			if(rowNum==0){
				//排除标题行
				continue;
			}
			String id = row.getCell(0).getStringCellValue();
			String province = row.getCell(1).getStringCellValue();
			String city = row.getCell(2).getStringCellValue();
			String district = row.getCell(3).getStringCellValue();
			String postcode = row.getCell(4).getStringCellValue();
			//包装成一个区域对象
			Region region  = new Region(id, province, city, district, postcode, null, null, null);
			province = province.substring(0, province.length() - 1);
			city = city.substring(0, city.length() - 1);
			district = district.substring(0, district.length() - 1);
			String info = province + city + district;
			String[] headByString = PinYin4jUtils.getHeadByString(info);
			String shortcode = StringUtils.join(headByString);
			//城市编码---->>shijiazhuang
			String citycode = PinYin4jUtils.hanziToPinyin(city, "");
			
			region.setShortcode(shortcode);
			region.setCitycode(citycode);
			regionList.add(region);
		}
		//减少开启事务数量 批量保存
		regionService.saveBatch(regionList);
		return NONE;
	}
	
	/**
	 * 分页查询
	 * @throws Exception 
	 */
	public String pageQuery() throws Exception{
		regionService.pageQuery(pageBean);
		this.java2Json(pageBean, 
					new String[]{"currentPage","detachedCriteria","pageSize","subareas"});
		return NONE;
	}
	private String q;
	
	/**
	 * 查询所有区域，写回json数据
	 * @return
	 */
	public String listajax(){
		List<Region> list = null;
		if(StringUtils.isNotBlank(q)){
			list = regionService.findListByQ(q);
		}else{
			list = regionService.findAll();
		}
		this.java2Json(list, new String[]{"subareas"});
		return NONE;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}
	
}
