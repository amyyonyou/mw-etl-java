package com.mw.plugins.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.mw.utils.Reflections;

public class ImportExcel {

	private static Logger logger = LoggerFactory.getLogger(ImportExcel.class);

	/**
	 * 工作薄对象
	 */
	private Workbook wb;

	/**
	 * 工作表对象
	 */
	private Sheet sheet;

	/**
	 * 标题行号
	 */
	private int headerNum;

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            导入文件，读取第一个工作表
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(String fileName, int headerNum) throws InvalidFormatException, IOException {
		this(new File(fileName), headerNum);
	}

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            导入文件对象，读取第一个工作表
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(File file, int headerNum) throws InvalidFormatException, IOException {
		this(file, headerNum, 0);
	}

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            导入文件
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(String fileName, int headerNum, int sheetIndex) throws InvalidFormatException, IOException {
		this(new File(fileName), headerNum, sheetIndex);
	}

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            导入文件对象
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(File file, int headerNum, int sheetIndex) throws InvalidFormatException, IOException {
		this(file.getName(), new FileInputStream(file), headerNum, sheetIndex);
	}

	/**
	 * 构造函数
	 * 
	 * @param file
	 *            导入文件对象
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(MultipartFile multipartFile, int headerNum, int sheetIndex) throws InvalidFormatException, IOException {
		this(multipartFile.getOriginalFilename(), multipartFile.getInputStream(), headerNum, sheetIndex);
	}

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            导入文件对象
	 * @param headerNum
	 *            标题行号，数据行号=标题行号+1
	 * @param sheetIndex
	 *            工作表编号
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(String fileName, InputStream is, int headerNum, int sheetIndex) throws InvalidFormatException, IOException {
		if (StringUtils.isBlank(fileName)) {
			throw new RuntimeException("Invalid file name.");
		} else if (fileName.toLowerCase().endsWith("xls")) {
			this.wb = new HSSFWorkbook(is);
		} else if (fileName.toLowerCase().endsWith("xlsx")) {
			this.wb = new XSSFWorkbook(is);
		} else {
			throw new RuntimeException("Invalid file type.");
		}
		if (this.wb.getNumberOfSheets() < sheetIndex) {
			throw new RuntimeException("No sheet in excel.");
		}
		this.sheet = this.wb.getSheetAt(sheetIndex);
		this.headerNum = headerNum;
		logger.debug("Initialize success.");
	}

	/**
	 * 获取行对象
	 * 
	 * @param rownum
	 * @return
	 */
	public Row getRow(int rownum) {
		return this.sheet.getRow(rownum);
	}

	/**
	 * 获取数据行号
	 * 
	 * @return
	 */
	public int getDataRowNum() {
		return headerNum + 1;
	}

	/**
	 * 获取最后一个数据行号
	 * 
	 * @return
	 */
	public int getLastDataRowNum() {
		return this.sheet.getPhysicalNumberOfRows();
	}

	/**
	 * 获取最后一个列号
	 * 
	 * @return
	 */
	public int getLastCellNum() {
		return this.getRow(headerNum).getLastCellNum();
	}

	/**
	 * 获取单元格值
	 * 
	 * @param row
	 *            获取的行
	 * @param column
	 *            获取单元格列号
	 * @return 单元格值
	 */
	public Object getCellValue(Row row, int column) {
		Object val = "";
		//try {
		Cell cell = row.getCell(column);
		if (cell != null) {
			if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				val = cell.getNumericCellValue();
			} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				val = cell.getStringCellValue();
			} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
				/* val = cell.getCellFormula(); */
				switch (cell.getCachedFormulaResultType()) {
				case Cell.CELL_TYPE_NUMERIC:
					val = cell.getNumericCellValue();
					break;
				case Cell.CELL_TYPE_STRING:
					val = cell.getRichStringCellValue();
					break;
				}
			} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
				val = cell.getBooleanCellValue();
			} else if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
				val = cell.getErrorCellValue();
			}
		}
		//} catch (Exception e) {
		//	return val;
		//}
		return val;
	}

	/**
	 * 获取导入数据列表
	 * 
	 * @param cls
	 *            导入对象类型
	 * @param groups
	 *            导入分组
	 */
	public <E> List<E> getDataList(Class<E> cls, int... groups) throws InstantiationException, IllegalAccessException {
		List<Object[]> annotationList = Lists.newArrayList();
		// Get annotation field 
		Field[] fs = cls.getDeclaredFields();
		for (Field f : fs) {
			ExcelField ef = f.getAnnotation(ExcelField.class);
			if (ef != null && (ef.type() == 0 || ef.type() == 2)) {
				if (groups != null && groups.length > 0) {
					boolean inGroup = false;
					for (int g : groups) {
						if (inGroup) {
							break;
						}
						for (int efg : ef.groups()) {
							if (g == efg) {
								inGroup = true;
								annotationList.add(new Object[] { ef, f });
								break;
							}
						}
					}
				} else {
					annotationList.add(new Object[] { ef, f });
				}
			}
		}
		// Get annotation method
		Method[] ms = cls.getDeclaredMethods();
		for (Method m : ms) {
			ExcelField ef = m.getAnnotation(ExcelField.class);
			if (ef != null && (ef.type() == 0 || ef.type() == 2)) {
				if (groups != null && groups.length > 0) {
					boolean inGroup = false;
					for (int g : groups) {
						if (inGroup) {
							break;
						}
						for (int efg : ef.groups()) {
							if (g == efg) {
								inGroup = true;
								annotationList.add(new Object[] { ef, m });
								break;
							}
						}
					}
				} else {
					annotationList.add(new Object[] { ef, m });
				}
			}
		}
		// Field sorting
		Collections.sort(annotationList, new Comparator<Object[]>() {
			public int compare(Object[] o1, Object[] o2) {
				return new Integer(((ExcelField) o1[0]).sort()).compareTo(new Integer(((ExcelField) o2[0]).sort()));
			};
		});
		//log.debug("Import column count:"+annotationList.size());
		// Get excel data
		List<E> dataList = Lists.newArrayList();
		for (int i = this.getDataRowNum(); i < this.getLastDataRowNum(); i++) {
			E e = (E) cls.newInstance();
			int column = 0;
			Row row = this.getRow(i);
			StringBuilder sb = new StringBuilder();
			for (Object[] os : annotationList) {
				Object val = this.getCellValue(row, column++);
				if (val != null) {
					ExcelField ef = (ExcelField) os[0];
					// Get param type and type cast
					Class<?> valType = Class.class;
					if (os[1] instanceof Field) {
						valType = ((Field) os[1]).getType();
					} else if (os[1] instanceof Method) {
						Method method = ((Method) os[1]);
						if ("get".equals(method.getName().substring(0, 3))) {
							valType = method.getReturnType();
						} else if ("set".equals(method.getName().substring(0, 3))) {
							valType = ((Method) os[1]).getParameterTypes()[0];
						}
					}
					logger.debug("Import value type: [" + i + "," + column + "] " + valType);
					try {
						if (valType == String.class) {
							String s = String.valueOf(val.toString());
							if (StringUtils.endsWith(s, ".0")) {
								val = StringUtils.substringBefore(s, ".0");
							} else {
								val = String.valueOf(val.toString());
							}
						} else if (valType == Integer.class) {
							val = Double.valueOf(val.toString()).intValue();
						} else if (valType == Long.class) {
							val = Double.valueOf(val.toString()).longValue();
						} else if (valType == Double.class) {
							val = Double.valueOf(val.toString());
						} else if (valType == Float.class) {
							val = Float.valueOf(val.toString());
						} else if (valType == Date.class) {
							val = DateUtil.getJavaDate((Double) val);
						} else {
							if (ef.fieldType() != Class.class) {
								val = ef.fieldType().getMethod("getValue", String.class).invoke(null, val.toString());
							} else {
								val = Class.forName(this.getClass().getName().replaceAll(this.getClass().getSimpleName(), "fieldtype." + valType.getSimpleName() + "Type")).getMethod("getValue", String.class).invoke(null, val.toString());
							}
						}
					} catch (NumberFormatException nfe) {
						logger.info("NumberFormatException Error at get cell value-> row={}, column={}, val={}", i, column, val);
						if(StringUtils.isBlank(val.toString())){
							val = null;
						}else{
							throw new RuntimeException(nfe);
						}
					} catch (Exception ex) {
						logger.info("Exception Error at get cell value-> row={}, column={}, val={}", i, column, val);
						throw new RuntimeException(ex);
					}
					// set entity value
					if (os[1] instanceof Field) {
						Reflections.invokeSetter(e, ((Field) os[1]).getName(), val);
					} else if (os[1] instanceof Method) {
						String mthodName = ((Method) os[1]).getName();
						if ("get".equals(mthodName.substring(0, 3))) {
							mthodName = "set" + StringUtils.substringAfter(mthodName, "get");
						}
						Reflections.invokeMethod(e, mthodName, new Class[] { valType }, new Object[] { val });
					}
				}
				sb.append(val + ", ");
			}
			dataList.add(e);
			logger.debug("Read success: [" + i + "] " + sb.toString());
		}
		return dataList;
	}
	
	/**
	 * 导入测试
	 */
	public static void main(String[] args) throws Exception {

		ImportExcel ei = new ImportExcel("W://Operations/數據中心 Data Center/數據上傳專用 data upload/測試數據/zh/20150721.xls", 1, 0);
		System.out.print(ei.sheet.getPhysicalNumberOfRows());

		for (int i = 0; i <= ei.sheet.getLastRowNum(); i++) {
			Row row = ei.sheet.getRow(i);
			if (row == null) {
				System.err.println("ReadCells(): Row " + i + " was null!");
				System.exit(2);
			}
		}

	}
}
