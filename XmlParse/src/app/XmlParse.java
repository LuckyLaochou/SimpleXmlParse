package app;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import app.SqlType;

/**
 * XmlParse
 */
public class XmlParse {


    private Document createDocument(String fileName){
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(fileName);
        } catch (DocumentException e) {
            System.out.println("read file is failed");
            e.printStackTrace();
		}
        return document;
    }

    /**
     * parse xml to create sql 
     * @param rootElement
     * @return 
     */
    private void parseXml(Element rootElement){
        // get all children nodes
        List<Element> listElements = rootElement.elements();
        listElements.forEach(element -> {
            // get the current element the attribute for name and type
            String viewType = element.attributeValue("type");
            // 根据类别来进行生成sql语句
            switch(SqlType.getSqlTypeByName(viewType)){
                case INSERT:
                    createInsertSql(element.elements().get(0));
                    break;
                case UPDATE:
                    createUpdateSql(element.elements().get(0));
                    break;
                case SELECT:
                    createQuerySql(element.elements().get(0));
                    break;
                case DELETE:
                    createDeleteSql(element.elements().get(0));
                    break;
                default:
                    break;
            }
        });

    }


    private List<String> createInsertSql(Element tables){
        List<String> sqls = new ArrayList<>();
        String prefix = "INSERT INTO ";
        StringBuffer stringBuffer = new StringBuffer(prefix);
        // get the list for table
        List<Element> table = tables.elements();
        table.forEach(item -> {
            // get the tables info
            String tableName = item.attributeValue("name");
            stringBuffer.append(tableName).append("(");
            // get the mapping info
            List<Element> mappings = item.elements();
            // it must use to record the column's size
            int size = 0;
            List<String> columns = new ArrayList<>();
            for(Element mapping : mappings){
                columns.add(mapping.attributeValue("column"));
                size++;
            }
            String columnStr = columns.stream().map(String::valueOf).collect(Collectors.joining(","));
            stringBuffer.append(columnStr).append(") VALUES(");
            String markStr = Stream.generate(() -> "?").limit(size).collect(Collectors.joining(","));
            stringBuffer.append(markStr);
            stringBuffer.append(")");
            sqls.add(stringBuffer.toString());
            // 清除stringBuffer的内容
            stringBuffer.delete(0, stringBuffer.length());
            stringBuffer.append(prefix);
        });
        sqls.stream().forEach(System.out::println);
        return sqls;
    }

    private String[] createQuerySql(Element tables){
        return null;
    }

    private String[] createUpdateSql(Element tables){
        return null;
    }

    private String createDeleteSql(Element tables){
        return null;
    }


    public void parse(){
        Document document = createDocument("E:\\VsCode\\XmlParse\\XmlParse\\src\\view.xml");
        Element rootElement = document.getRootElement();
        parseXml(rootElement);
    }


    public static void main(String[] args) {
        XmlParse xmlParse = new XmlParse();
        xmlParse.parse();
    }


    
}