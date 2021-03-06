package com.hongyb.excel.core;

import com.hongyb.excel.converter.BasicConverter;
import com.hongyb.excel.converter.Converter;
import com.hongyb.excel.converter.ConverterManager;
import com.hongyb.excel.utils.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;

/**
 * HSSF  Excel 97-2007
 * 作者:hongyanbo
 * 时间:2018/3/9
 */
public class ExcelWriter {
    /**
     * default sheet name
     * 默认的sheet名称
     */
    private String sheetName = "newSheet" ;
    /**
     * 标题名称
     */
    private String titleName = null ;
    /**
     * 数据列表
     */
    List<?> dataList = null;
    /**
     *  workbook
     */
    Workbook workbook = null;
    /**
     * 标题样式
     */
    CellStyle titleStyle = null ;
    /**
     *  数据格样式
     */
    CellStyle cellStyle = null ;

    
    private static BasicConverter basicConverter = new BasicConverter();
    /**
     * 菜单style
     */
    private CellStyle menuStyle =null ;

    public ExcelWriter(String sheetName, String titleName, List<?> dataList, Workbook hssfWorkbook, CellStyle titleStyle, CellStyle cellStyle, CellStyle menuStyle) {
        this.sheetName = sheetName;
        this.titleName = titleName;
        this.dataList = dataList;
        this.workbook = hssfWorkbook;
        this.titleStyle = titleStyle;
        this.cellStyle = cellStyle;
        this.menuStyle = menuStyle ;
    }

    /**
     * 导出excel
     */
    public void write(OutputStream output) throws IOException {

        Field[] declaredFields = null ;
        int startRow = 0 ;
        if(Collections.isNotBlank(dataList)){
            declaredFields = dataList.get(0).getClass().getDeclaredFields();
        }
        Sheet sheet = workbook.createSheet(sheetName);
        // title处理
        if(StringUtils.isNotBlank(titleName)){
            // 设置标题样式和内容
            Row titleRow = sheet.createRow(startRow);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellStyle(titleStyle);
            titleCell.setCellValue(titleName);
            // 合并多少行 默认5
            int mergeColCount = 5 ;
            if(Arrays.isNotBlank(declaredFields)){
                mergeColCount=declaredFields.length-1;
            }
            // 标题单元格合并
            sheet.addMergedRegion(new CellRangeAddress(startRow,0,0,mergeColCount));
            startRow++;
        }
        // 列小标题处理
        if(Arrays.isNotBlank(declaredFields)){
            if(declaredFields != null && declaredFields.length !=0){
                Row menuRow = sheet.createRow(startRow);
                menuProcess(declaredFields,menuRow);
                startRow++;
            }
        }
        // 数据list处理
        if(Collections.isNotBlank(dataList)){
            // 循环写
            for (Object rowData : dataList) {
                Row row = sheet.createRow(startRow++);
                Field[] fields = rowData.getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    int order = ColumnUtils.getOrder(field);
                    Cell cell = row.createCell(order);
                    cell.setCellStyle(cellStyle);
                    Class<? extends Converter> converterClazz = ColumnUtils.getConverter(field);
                    Converter converter = ConverterManager.getConverter(converterClazz);
                    cell.setCellValue(converter.convert(ReflectUtil.getValueOfField(rowData,field)));
                }
            }

        }
        // 输出文件
        workbook.write(output);
        output.close();
    }

    /**
     * 处理小标题
     * @param declaredFields
     * @param menuRow
     */
    private void menuProcess(Field[] declaredFields, Row menuRow) {
        for (Field field : declaredFields) {
            int order = ColumnUtils.getOrder(field);
            Cell cell = menuRow.createCell(order);
            cell.setCellStyle(menuStyle);
            String menu = ColumnUtils.getMenu(field);
            cell.setCellValue(menu);
        }
    }

    public void write(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        write(fileOutputStream);
    }
}
