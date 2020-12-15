package com.mw.etl.ctdb.rtdb.inputdata;

import java.util.Map;

import javax.annotation.Resource;

import java.util.List;
import java.util.Collection;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import com.mw.plugins.excel.ImportExcel;
import com.mw.utils.DateTimeUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
@Service
public class InputDataService {
	
	@Value("${inputdata.filepath.zhraw}")
	private String filePathOfZhRaw;
	@Value("${inputdata.filepath.bak}")
	private String filePathOfBak;
	@Value("${inputdata.errorlogfile}")
	private String errorLogFile;
	
	private final static String separator = File.separator;

	private final static String FIELDNAME_SENSORDD = "sensordd";

	private final static Integer MAX_READDAYS = 7;

	private final static String ITAG_YYYYMMDD = "iTagYYYYMMDD";
	private final static String ITAG_HH = "iTagHH";

	private final static String tableNameOfZH = "ZH";
	private final static Integer headerNbOfS0 = 3;
	private final static Integer headerNbOfS1 = 51;
	private final static Integer headerNbOfS2 = 2;
	private final static Integer headerNbOfS3 = 2;
	private final static Integer headerNbOfS4 = 2;
	private final static String[] cellNamesOfS0 = { ITAG_YYYYMMDD, ITAG_HH, "ZH_source_HW_out_salinity", "ZH_source_HW_in_salinity", "ZH_source_HW_PumpStatus", "ZH_source_HW_out_level", "ZH_source_HW_in_level", "ZH_source_HW_out_turbidity", "ZH_source_HW_in_turbidity", "ZH_source_HW_pH", "ZH_source_HW_NH3N", "ZH_source_HW_smell", "ZH_source_GC_out_turbidity", "ZH_source_GC_in_turbidity", "ZH_source_GC_out_salinity", "ZH_source_GC_in_salinity", "ZH_source_GC_out_NH3N", "ZH_source_GC_in_NH3N",
			"ZH_source_GC_out_level", "ZH_source_GC_in_level", "ZH_source_GC_in_pH", "ZH_source_GC_PumpStatus", "ZH_source_GC_in_smell" };
	private final static String[] cellNamesOfS1 = { ITAG_YYYYMMDD, ITAG_HH, "ZH_source_PG_salinity", "ZH_source_ZZT_salinity", "ZH_source_PG_PumpStatus", "ZH_source_ZZT_PumpStatus", "ZH_Res_zhuyin_level", "ZH_Res_zhuyin_capacity10000" };
	private final static String[] cellNamesOfS2 = { ITAG_YYYYMMDD, "ZH_daily_meter1", "ZH_daily_meter2", "ZH_daily_meter3", "ZH_daily_meter4", "", "", "" };
	private final static String[] cellNamesOfS3 = { ITAG_YYYYMMDD, "ZH_accumulated_meter1", "ZH_accumulated_meter2", "ZH_accumulated_meter3", "ZH_accumulated_meter4", "", "", "", "" };
	private final static String[] cellNamesOfS4 = { ITAG_YYYYMMDD, "ZH_Res_Lapa_level", "ZH_Res_Lapa_rainfall", "ZH_Res_Lapa_salinity", "ZH_Res_Lapa_capacity10000", "ZH_Res_YK_level", "ZH_Res_YK_rainfall", "ZH_Res_YK_salinity", "ZH_Res_YK_capacity10000", "ZH_Res_SDK_level", "ZH_Res_SDK_rainfall", "ZH_Res_SDK_salinity", "ZH_Res_SDK_capacity10000", "ZH_Res_NP_level", "ZH_Res_NP_rainfall", "ZH_Res_NP_up_salinity", "ZH_Res_NP_down_salinity", "ZH_Res_NP_capacity10000" };
	

	private static final Logger logger = LoggerFactory.getLogger(InputDataService.class);
	@Resource
	public NamedParameterJdbcTemplate scada;
	
	@Scheduled(cron = "${cron.inputdata.save}")
	public void saveData() throws Exception {
		List<Map<String, Object>> dataList = null;

		File folderOfZhRaw = new File(filePathOfZhRaw);
		Collection<File> filesOfZhRaw = FileUtils.listFiles(folderOfZhRaw, new String[] { "xls", "xlsx" }, false);
		Object[] objsOfZhRaw = filesOfZhRaw.toArray();
		for (int i = 0; i < objsOfZhRaw.length; i++) {
			File fileOfZhRaw = (File) objsOfZhRaw[i];
			if (StringUtils.startsWith(fileOfZhRaw.getName(), "~$")) {
				continue;
			}

			dataList = parseData(fileOfZhRaw, 0, headerNbOfS0, cellNamesOfS0, MAX_READDAYS);
			convert(tableNameOfZH, dataList);

			dataList = parseData(fileOfZhRaw, 1, headerNbOfS1, cellNamesOfS1, MAX_READDAYS);
			convert(tableNameOfZH, dataList);

			dataList = parseData(fileOfZhRaw, 2, headerNbOfS2, cellNamesOfS2, MAX_READDAYS);
			convert(tableNameOfZH, dataList);

			dataList = parseData(fileOfZhRaw, 3, headerNbOfS3, cellNamesOfS3, MAX_READDAYS);
			convert(tableNameOfZH, dataList);

			dataList = parseData(fileOfZhRaw, 4, headerNbOfS4, cellNamesOfS4, MAX_READDAYS);
			convert(tableNameOfZH, dataList);

			moveTo(fileOfZhRaw);
		}

	}
	
	public List<Map<String, Object>> parseData(File file, Integer sheetIndex, Integer headerNum, String[] cellNames, Integer maxReadDays) throws Exception {
		ImportExcel ei = new ImportExcel(file, headerNum, sheetIndex);
		Integer maxReadRows = maxReadDays;
		for (String cellName : cellNames) {
			if (StringUtils.equalsIgnoreCase(cellName, ITAG_HH)) {
				maxReadRows = 24 * maxReadDays;
			}
		}
		int cellSize = cellNames.length;

		Integer rowReading = 0;
		Integer startRowNb = headerNum + 1;
		int lastDataRowNb = ei.getLastDataRowNum();
		int firstDataRowNb = ei.getDataRowNum();
		boolean isDataRow = false;
		
		for (int i = lastDataRowNb; i > firstDataRowNb; i--) {// 從最後一行開始遍歷,遍歷maxReadRows筆記錄,最小到數據起始行.

			Row row = ei.getRow(i);
			if (row == null) {
				continue;
			}

			for (int j = 0; j < cellSize; j++) {
				try {
					String cellName = cellNames[j];
					Object val = ei.getCellValue(row, j);
					if (StringUtils.isNotBlank(cellName) && !StringUtils.equalsIgnoreCase(cellName, ITAG_YYYYMMDD) && !StringUtils.equalsIgnoreCase(cellName, ITAG_HH) && val != null && StringUtils.isNotBlank(val.toString())) {// 除了時間外的其它任何cell有值都算一條數據。
						isDataRow = true;
					}
				} catch (Exception e) {
					logger.info("parseData-> i={}, j={}", i, j);
					throw new RuntimeException(e);
				}
			}

			if (isDataRow) {
				rowReading++;
			}
			if (rowReading >= maxReadRows) {
				startRowNb = i;
				break;
			}
		}
		
		List<String> errorMsgs = new LinkedList<String>();
		List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();

		int endRowNb = startRowNb + maxReadRows;
		logger.info("rowNb-> firstDataRowNb={}, lastDataRowNb={}, startRowNb={}, endRowNb={}", firstDataRowNb, lastDataRowNb, startRowNb, endRowNb);
		for (int i = startRowNb; i < endRowNb; i++) {
			Row row = ei.getRow(i);

			isDataRow = false;
			for (int j = 0; j < cellSize; j++) {
				String cellName = cellNames[j];
				Object val = ei.getCellValue(row, j);
				if (StringUtils.isNotBlank(cellName) && !StringUtils.equalsIgnoreCase(cellName, ITAG_YYYYMMDD) && !StringUtils.equalsIgnoreCase(cellName, ITAG_HH) && val != null && StringUtils.isNotBlank(val.toString())) {// 除了時間外的其它任何cell有值都算一條數據。
					isDataRow = true;
				}
			}
			if (!isDataRow) {
				continue;
			}

			Map<String, Object> map = new HashMap<String, Object>();
			list.add(map);

			Date yyyymmdd = null;
			String hh = null;

			for (int j = 0; j < cellSize; j++) {
				String cellName = null;
				Object cellValue = null;
				try {
					cellName = cellNames[j];
					cellValue = ei.getCellValue(row, j);
					if (StringUtils.equalsIgnoreCase(cellName, ITAG_YYYYMMDD)) {
						yyyymmdd = parseDate(cellValue.toString());
					} else if (StringUtils.equalsIgnoreCase(cellName, ITAG_HH)) {
						Date dt = parseDate(cellValue.toString());
						hh = DateTimeUtils.date2Str(dt, "HH");
					} else if (StringUtils.isNotBlank(cellName)) {
						if (cellValue != null && StringUtils.isNotBlank(cellValue.toString())) {
							if (StringUtils.startsWithIgnoreCase(cellName, "date")) {
								Date val = parseDate(cellValue.toString());
								map.put(cellName, val);
							} else {
								Double val = Double.parseDouble(cellValue.toString());
								map.put(cellName, val);
							}
						}
					}

				} catch (Exception e) {
					errorMsgs.add(String.format("\n%s: File=%s, Row=%s, Column=%s, Name=%s, Value=%s", DateTimeUtils.date2Str(new Date(), DateTimeUtils.FORMAT_YYYY_MM_DD_HH_MM_SS), file.getName(), i + 1, j + 1, cellName, cellValue));
				}
			}

			
			String yyyymmddStr = DateTimeUtils.date2Str(yyyymmdd, DateTimeUtils.FORMAT_YYYY_MM_DD);
			if (StringUtils.isBlank(hh)) {// 根據susie要求，珠海原水數據中如果沒有小時的時間一律當8點處理
				hh = "08";
			}
			StringBuilder ymdh = new StringBuilder(yyyymmddStr).append(" ").append(hh);
			yyyymmdd = DateTimeUtils.str2Date(ymdh.toString(), "yyyy-MM-dd HH");
			
		
			 map.put(FIELDNAME_SENSORDD, yyyymmdd);
		}

		if (errorMsgs.size() > 0) {
			writeErrorLog(errorMsgs);
			throw new Exception("Error when import file.");
		}

		return list;
	}

	public void convert(String tableName, List<Map<String, Object>> rowList) throws ParseException {
		for (Map<String, Object> rowMap : rowList) {
			SqlParameterSource param = new MapSqlParameterSource(rowMap);
			String sql = null;
			try {
				sql = getAddSql(tableName,rowMap);
				scada.update(sql, param);
			} catch (DuplicateKeyException e) {
				
				sql = getUpdSql(tableName,rowMap);
				scada.update(sql, param);
			}
			logger.info("sql-> {}", sql);
			logger.info("rowMap-> {}", rowMap);
		}
	}
	 
	private Date parseDate(String time) throws Exception {
		try {
			if (time.contains("/")) {
				SimpleDateFormat sdf = null;
				if (time.length() < 11) {
					sdf = new SimpleDateFormat("dd/MM/yyyy");
				} else {
					sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault());
				}
				return sdf.parse(time);
			} else {
				return DateUtils.round(DateUtil.getJavaDate(Double.parseDouble(time)), Calendar.MINUTE);
			}
		} catch (Exception e) {
			logger.warn(String.format("time: %s; parseDate", time));
			throw new Exception();
		}
	}
	
	//insert file update
	private String getAddSql(String tableName, Map<String, Object> rowMap) {
		StringBuilder fieldNameBuilder = new StringBuilder();
		StringBuilder fieldValueBuilder = new StringBuilder();
		int i=0;
		for(String key : rowMap.keySet()){
			Object val = rowMap.get(key);
			if (val == null) {
				continue;
			}
			if (i != 0) {
				fieldNameBuilder.append(", ");
				fieldValueBuilder.append(", ");
			}		
			i++;
			fieldNameBuilder.append(key);
			fieldValueBuilder.append(":").append(key);
		}
		StringBuilder sqlSb = new StringBuilder("insert into scada_data.");
		//StringBuilder sqlSb = new StringBuilder("insert into ");
		sqlSb.append(tableName);
		sqlSb.append("(");
		sqlSb.append(fieldNameBuilder);
		sqlSb.append(") values(");
		sqlSb.append(fieldValueBuilder);
		sqlSb.append(")");

		return sqlSb.toString();
	}
	
	private void writeErrorLog(List<String> errorMsgs) {
		for (String errorMsg : errorMsgs) {
			logger.info(errorMsg);
			try {
				Files.append(errorMsg, new File(errorLogFile), Charset.forName("utf-8"));
				
			} catch (IOException e) {
				logger.warn("writeErrorLog", e);
			}
		}
	}
	
	//update file data
	private String getUpdSql(String tableName, Map<String, Object> rowMap) {
		StringBuilder fieldNameBuilder = new StringBuilder();
		int i =0;
		for(String key : rowMap.keySet()) {
			Object val = rowMap.get(key);
			if (val == null) {
				continue;
			}
			if (i != 0) {
				fieldNameBuilder.append(", ");
			}
			i++;
			fieldNameBuilder.append(key).append("=").append(":").append(key);
		}
		StringBuilder sqlSb = new StringBuilder("update scada_data.");
		//StringBuilder sqlSb = new StringBuilder("update ");
		sqlSb.append(tableName);
		sqlSb.append(" set ");
		sqlSb.append(fieldNameBuilder);
		sqlSb.append(" where ");
		sqlSb.append(FIELDNAME_SENSORDD).append("=:").append(FIELDNAME_SENSORDD);

		return sqlSb.toString();
	}
	public void moveTo(String filePath) throws IOException {
		moveTo(new File(filePath));
	}
	public void moveTo(File file) throws IOException {
		String yyyymmdd = DateTimeUtils.date2Str(new Date(), DateTimeUtils.FORMAT_YYYYMMDD);

		StringBuilder sb = new StringBuilder(filePathOfBak);
		sb.append(separator).append(yyyymmdd);
		String backupFolderPath = sb.toString();

		File backupFolder = new File(backupFolderPath);
		if (!backupFolder.exists()) {
			backupFolder.mkdirs();
		}

		String backupFileName = new StringBuilder(backupFolderPath).append(separator).append(file.getName()).append(".").append(new Date().getTime()).toString();
		Files.move(file, new File(backupFileName));
	}
	
}
