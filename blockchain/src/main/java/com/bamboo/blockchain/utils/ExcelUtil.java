package com.bamboo.blockchain.utils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * excel导出表格功能
 * 需要依赖的maven如下<p>
 *
 *     <!-- https://mvnrepository.com/artifact/com.google.guava/guava -->
 <dependency>
 <groupId>com.google.guava</groupId>
 <artifactId>guava</artifactId>
 <version>25.1-jre</version>
 </dependency>


 <dependency>
 <groupId>org.apache.poi</groupId>
 <artifactId>poi</artifactId>
 <version>3.17</version>
 </dependency>
 *
 *
 *
 * </p>
 *
 *@author  zjcjava@163.com
 *
 */
public class ExcelUtil {
    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);
    static Map<String, Function> types = Maps.newConcurrentMap();
    static SimpleDateFormat sdfYMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static SimpleDateFormat sdfymdhms = new SimpleDateFormat("yyyyMMddHHmmss");

    static {
        types.put("java.lang.Integer", obj -> Objects.isNull(obj) ? null : Integer.valueOf(obj.toString()));
        types.put("java.lang.Double", obj -> Objects.isNull(obj) ? null : Double.valueOf(obj.toString()));
        types.put("java.lang.Float", obj -> Objects.isNull(obj) ? null : Float.valueOf(obj.toString()));
        types.put("java.lang.Long", obj -> Objects.isNull(obj) ? null : Long.valueOf(obj.toString()));
        types.put("java.lang.Short", obj -> Objects.isNull(obj) ? null : Short.valueOf(obj.toString()));
        types.put("java.lang.Byte", obj -> Objects.isNull(obj) ? null : Byte.valueOf(obj.toString()));
        types.put("java.lang.Boolean", obj -> Objects.isNull(obj) ? null : Boolean.valueOf(obj.toString()));
        types.put("java.lang.Character", obj -> Objects.isNull(obj) ? null : (char) (obj));
        types.put("java.lang.String", obj -> Objects.isNull(obj) ? null : obj.toString());

        types.put("int", obj -> Objects.isNull(obj) ? 0 : Integer.valueOf(obj.toString()));
        types.put("double", obj -> Objects.isNull(obj) ? 0 : Double.valueOf(obj.toString()));
        types.put("float", obj -> Objects.isNull(obj) ? 0 : Float.valueOf(obj.toString()));
        types.put("long", obj -> Objects.isNull(obj) ? 0 : Long.valueOf(obj.toString()));
        types.put("short", obj -> Objects.isNull(obj) ? 0 : Short.valueOf(obj.toString()));
        types.put("byte", obj -> Objects.isNull(obj) ? 0 : Byte.valueOf(obj.toString()));
        types.put("boolean", obj -> Objects.isNull(obj) ? false : Boolean.valueOf(obj.toString()));
        types.put("char", obj -> Objects.isNull(obj) ? 0 : (char) (obj));
    }


    public static byte[] generateXls(String[] headers, String[] properties, List<Object> rows)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        return generateXls(headers, null, properties, rows);
    }

    public static void generateXls(HttpServletResponse response, String name, String[] headers, String[] properties, List<Object> rows) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        generateXls(response, name, headers, null, properties, rows);
    }

    public static void generateXls(HttpServletResponse response, String name, String[] headers, int[] rowWidths, String[] properties, List<Object> rows) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        byte[] buf = generateXls(headers, rowWidths, properties, rows);
        response.reset();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setContentLength(buf.length);
        response.setHeader("Content-Disposition", fileName(name));

        InputStream in = new ByteArrayInputStream(buf);
        ServletOutputStream out = response.getOutputStream();
        ByteStreams.copy(in, out);
        out.flush();
        out.close();
    }


    public static void generateXlfile(String filePath,String[] headers, int[] rowWidths, String[] properties, List<Object> rows) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
           String fileName = filePath+sdfymdhms.format(new Date())+".xls";
            FileOutputStream fout = new FileOutputStream(fileName);
            byte[] buf = generateXls(headers, rowWidths, properties, rows);
            InputStream in = new ByteArrayInputStream(buf);

            ByteStreams.copy(in, fout);
            fout.flush();
            fout.close();
        } catch (Exception e)  {
            e.printStackTrace();
        }
    }


    public static byte[] generateXls(String[] headers, int[] rowWidths, String[] properties, List<Object> rows)
            throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        //设置宽度
        for (int i = 0; i < headers.length; i++) {
            if (Objects.isNull(rowWidths))
                sheet.autoSizeColumn(i);
            else
                sheet.setColumnWidth(i, rowWidths[i] * 256);
        }
        //设置头
        List<Object[]> fillRows = Lists.newArrayList();
        fillRows.add(headers);
        //设置内容
        for (Object row : rows) {
            Object[] oneRow = new Object[headers.length];
            int r = 0;
            for (String property : properties) {
                String method4Get = method4Get(property);
                Method declaredMethod = row.getClass().getDeclaredMethod(method4Get);
                Object cell = declaredMethod.invoke(row);
                oneRow[r] = formatCell(cell);
                r++;
            }
            fillRows.add(oneRow);
        }
        //填充到excel
        for (int i = 0; i < fillRows.size(); i++) {
            HSSFRow row = sheet.createRow(i);
            for (int j = 0; j < headers.length; j++) {
                HSSFCell cell = row.createCell(j);
                setCell(cell, fillRows.get(i)[j]);
            }
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        wb.write(stream);
        wb.close();

        return stream.toByteArray();
    }


    /**
     * @param in
     * @param ignoreLine
     * @param properties
     * @param clazz:需要提供默认的构造方法
     * @return
     */
    public static <T> List<T> parse(InputStream in, int ignoreLine, String[] properties, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        try (HSSFWorkbook wb = new HSSFWorkbook(in)) {
            HSSFSheet sheet = wb.getSheetAt(0);
            log.info("批量发货数据行数：num={}", sheet.getLastRowNum());
            for (int i = ignoreLine; i <= sheet.getLastRowNum(); i++) {
                HSSFRow row = sheet.getRow(i);
                if (Objects.isNull(row))
                    continue;
                T t = clazz.newInstance();
                for (int column = 0; column < properties.length; column++) {
                    String property = properties[column];
                    HSSFCell cell = row.getCell(column);
                    Method getMethod = clazz.getDeclaredMethod(method4Get(property));
                    Function function = types.get(getMethod.getReturnType().getName());
                    if (Objects.isNull(function)) {
                        log.info("不是基本类型，不能失败:line={},column={},cell={}", i, column, cell);
                        continue;
                    }
                    Method method = clazz.getDeclaredMethod(method4Set(property), getMethod.getReturnType());
                    method.invoke(t, function.apply(formatCell(cell)));
                    result.add(t);
                }
                log.info("批量发货数据行数：line={}", i);
            }
        } catch (Exception e) {
            log.error("批量发货：文件解析失败", e);
        }
        return result;
    }

    private static String method4Get(String property) {
        return "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
    }

    private static String method4Set(String property) {
        return "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
    }

    private static void setCell(HSSFCell hssfCell, Object cell) {
        if (cell instanceof String) {
            hssfCell.setCellValue((String) cell);
        } else if (cell instanceof Long) {
            hssfCell.setCellValue((Long) cell);
        } else if (cell instanceof Integer) {
            hssfCell.setCellValue((Integer) cell);
        } else if (cell instanceof Double) {
            hssfCell.setCellValue((Double) cell);
        } else if (cell instanceof Boolean) {
            hssfCell.setCellValue((Boolean) cell);
        } else if (cell instanceof Byte) {
            hssfCell.setCellValue((Byte) cell);
        } else if (cell instanceof Float) {
            hssfCell.setCellValue((Float) cell);
        } else {
            hssfCell.setCellValue((String) cell);
        }
    }


    private static Object formatCell(HSSFCell cell) {
        switch (cell.getCellTypeEnum()) {
            case NUMERIC:
                return String.valueOf(new DecimalFormat("0").format(cell.getNumericCellValue()));
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA: // 公式
                return cell.getCellFormula();
            case BLANK: // 空值
                return "";
            default:
                return "";
        }
    }

    private static Object formatCell(Object cell) {
        if (Objects.isNull(cell))
            return null;
        if (cell instanceof Date){
            return (Date)cell == null ? null : sdfYMDHMS.format((Date)cell);
        }

        return cell;
    }


    private static String fileName(String name) {
        if (!name.contains(".xls")) {
            name = name + ".xls";
        }
        return "attachment;filename=".concat(new String(name.getBytes(), Charset.forName("iso-8859-1")));
    }
}
