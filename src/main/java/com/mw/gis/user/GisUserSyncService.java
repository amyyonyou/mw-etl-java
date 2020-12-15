package com.mw.gis.user;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;
import com.mw.plugins.jdbc.Finder;
import com.mw.plugins.jdbc.JdbcDao;
import com.mw.utils.mapper.JsonKit;

@Service
public class GisUserSyncService {
	@Resource
	private JdbcDao jdbcDao;
	
	public void sync() throws IOException {
		Finder finder = new Finder("select");
		finder.append("id as user_id, (case when lb_chi_name is null then lb_eng_name else lb_chi_name end) as user_name, lb_ad as user_account, lb_department as dept_id, lb_department as dept_name");
		finder.append("from temployee");
		finder.append("where 1=1");
		finder.append("and in_ad_active = 'Y'");
		finder.append("and id like '00%' and id not in('00000')");
		finder.setOrderBy("order by id");
		List<GisUserData> list = jdbcDao.getList(finder, GisUserData.class);
		
		String json = JsonKit.toJson(list);
		Files.write(json.getBytes(), new File("d:/Test/gisUserSync.json"));
		System.out.println(json);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(GisUserSyncService.class);
	
}
