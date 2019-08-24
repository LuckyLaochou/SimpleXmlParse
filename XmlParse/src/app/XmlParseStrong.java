package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * XmlParseTwo
 * the class is the second implements for xml parse
 */
public class XmlParseStrong {

    private static JSONObject jsonObject = new JSONObject();

    public Element getRootElement(String fileName) {
        // create a reader
        SAXReader reader = new SAXReader();
        Element element = null;
        try {
            element = reader.read(this.getClass().getClassLoader().getResourceAsStream(fileName)).getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return element;
    }

    // for each of the tree
    public Map<String, Object> xmlParse(Element element) {
        Map<String, Object> elementMap = new HashMap<>();
        // get the name for the element
        String elementName = element.getName();
        // first get all attribute
        List<Attribute> listAttr = element.attributes();
        // the map will contains the attributes of the element
        Map<String, String> mapAttr = new HashMap<>();
        // if the element haven't attributes , we shouldn't traverse the element
        if (listAttr != null) {
            listAttr.stream().forEach(attribute -> {
                mapAttr.put(attribute.getName(), attribute.getValue());
            });
            elementMap.put(elementName + "_attr", mapAttr);
        }
        // second get all child element
        List<Element> elements = element.elements();
        // if the elements haven't child, we shouldn't traverse the element
        List<Object> listChild = new ArrayList<>();
        if (elements != null) {
            for (Element item : elements) {
                Map<String, Object> itemMap = xmlParse(item);
                listChild.add(itemMap);
            }
            elementMap.put(elementName + "_child", listChild);
        }
        return elementMap;
    }

    // parse json
    public List<SqlData> parseJson(String jsonStr) {

        List<SqlData> list = new ArrayList<>();
        // start parse
        jsonObject = (JSONObject) JSON.parse(jsonStr);
        JSONArray jsonArray = jsonObject.getJSONArray("views_child");
        for (int i = 0; i < jsonArray.size(); i++) {
            boolean isExistConditions = false;
            JSONObject viewJSONObject = jsonArray.getJSONObject(i);
            JSONObject viewAttrJSONObject = viewJSONObject.getJSONObject("view_attr");
            SqlData sqlData = new SqlData();
            sqlData.setView(viewAttrJSONObject.getString("name"));
            sqlData.setType(viewAttrJSONObject.getString("type"));
            JSONArray viewJSONArray = viewJSONObject.getJSONArray("view_child");
            JSONObject tablesJSONObject = viewJSONObject.getJSONArray("view_child").getJSONObject(0);
            JSONObject conditionsJSONObject = null;
            if (viewJSONArray.size() == 2) {
                conditionsJSONObject = viewJSONObject.getJSONArray("view_child").getJSONObject(1);
                isExistConditions = true;
            }
            // used to stored the tables
            Map<String, Map<String, Map<String, String>>> params = new HashMap<>();
            // get the list of table
            JSONArray tableJSONArray = tablesJSONObject.getJSONArray("tables_child");
            if (isExistConditions) {
                JSONArray conditionJSONArray = conditionsJSONObject.getJSONArray("conditions_child");
                List<String> conditions = new ArrayList<>();
                for (int j = 0; j < conditionJSONArray.size(); j++) {
                    JSONObject conditionJSONObject = conditionJSONArray.getJSONObject(j);
                    // get the attr
                    JSONObject conditionAttrJSONObject = conditionJSONObject.getJSONObject("condition_attr");
                    String value = conditionAttrJSONObject.getString("value");
                    conditions.add(value);
                }
                sqlData.setConditions(conditions);
            }
            for (int j = 0; j < tableJSONArray.size(); j++) {
                JSONObject tableJSONObject = tableJSONArray.getJSONObject(j);
                JSONObject tableAttrJSONObject = tableJSONObject.getJSONObject("table_attr");
                String tableName = tableAttrJSONObject.getString("name");
                JSONArray paramsJSONArray = tableJSONObject.getJSONArray("table_child");
                Map<String, Map<String, String>> mappings = new HashMap<>();
                Map<String, String> mapping = new HashMap<>();
                Map<String, String> where = new HashMap<>();
                for (int k = 0; k < paramsJSONArray.size(); k++) {
                    JSONObject paramJSONObject = paramsJSONArray.getJSONObject(k);
                    // at here only two type : mapping_attr, where_attr
                    JSONObject mappingJSONObject = paramJSONObject.getJSONObject("mapping_attr");
                    if (mappingJSONObject != null) {
                        String property = mappingJSONObject.getString("property");
                        String column = mappingJSONObject.getString("column");
                        mapping.put(property, column);
                    } else {
                        JSONObject whereJSONObject = paramJSONObject.getJSONObject("where_attr");
                        String property = whereJSONObject.getString("property");
                        String column = whereJSONObject.getString("column");
                        where.put(property, column);
                    }
                    mappings.put("mapping", mapping);
                    mappings.put("where", where);
                }
                params.put(tableName, mappings);
            }
            sqlData.setParams(params);
            list.add(sqlData);
        }
        return list;
    }

    /**
     * make the object of sqlData traverse sql
     * 
     * @param sqlData
     * @return
     */
    private List<String> sqlDataToSQL(SqlData sqlData) {
        List<String> sqls = null;
        switch (SqlType.getSqlTypeByName(sqlData.getType())) {
        case INSERT:
            sqls = sqlDataToInsertSQL(sqlData);
            break;
        case SELECT:
            sqls = sqlDataToSelectSQL(sqlData);

        default:
            break;
        }
        return sqls;
    }

    /**
     * make the object of sqlData traverse sql statement
     * 
     * @param sqlData
     * @return
     */
    private List<String> sqlDataToInsertSQL(SqlData sqlData) {
        Map<String, Map<String, Map<String, String>>> params = sqlData.getParams();
        List<String> sqls = new ArrayList<>();
        for (String tableName : params.keySet()) {
            List<String> mappings = fieldToList(sqlData.getParams().get(tableName), tableName, "mapping");
            sqls.add(getInsertSQL(mappings, tableName));
        }
        return sqls;
    }

    private List<String> sqlDataToSelectSQL(SqlData sqlData) {
        Map<String, Map<String, Map<String, String>>> params = sqlData.getParams();
        List<String> conditions = sqlData.getConditions();
        List<String> sqls = new ArrayList<>();
        List<String> mappings = new ArrayList<>();
        List<String> wheres = new ArrayList<>();
        List<String> tableNames = new ArrayList<>();
        for (String tableName : params.keySet()) {
            List<String> tmpMappings = fieldToList(sqlData.getParams().get(tableName), tableName, "mapping");
            if (tmpMappings != null) {
                tmpMappings.stream().forEach(item -> mappings.add(item));
            }
            List<String> tmpWheres = fieldToList(sqlData.getParams().get(tableName), tableName, "where");
            if (tmpWheres != null) {
                tmpWheres.stream().forEach(item -> wheres.add(item));
            }
            tableNames.add(tableName);
        }
        sqls.add(getSelectSQL(mappings, wheres, conditions, tableNames.stream().collect(Collectors.joining(","))));
        return sqls;
    }

    /**
     * make the field params traverse a list
     * 
     * @param table
     * @param tableName
     * @return
     */
    private List<String> fieldToList(Map<String, Map<String, String>> table, String tableName, String condition) {
        List<String> sqlParams = new ArrayList<String>();
        Map<String, String> fields = table.get(condition);
        StringBuilder sqlParam = new StringBuilder(tableName);
        if (!fields.isEmpty()) {
            for (String paramName : fields.keySet()) {
                sqlParam.append(".");
                sqlParam.append(fields.get(paramName));
                sqlParams.add(sqlParam.toString());
                // retain the tableName
                sqlParam.delete(tableName.length(), sqlParam.length());
            }
            return sqlParams;
        } else {
            return null;
        }
    }

    /**
     * through the conditions make the sql of insert
     * 
     * @param params
     * @param tableName
     * @return
     */
    private String getInsertSQL(List<String> params, String tableName) {
        String variables = Stream.generate(() -> "?").limit(params.size()).map(String::valueOf)
                .collect(Collectors.joining(","));
        StringBuilder insertSql = new StringBuilder("INSERT INTO ");
        insertSql.append(tableName).append(" ( ").append(listToStr(params, ",")).append(") VALUES (").append(variables)
                .append(");");
        return insertSql.toString();
    }

    /**
     * through the conditions make the sql of select
     * 
     * @param mappings
     * @param wheres
     * @param tableName
     * @return
     */
    private String getSelectSQL(List<String> mappings, List<String> wheres, List<String> conditions, String tableName) {
        StringBuilder selectSql = new StringBuilder("SELECT ");
        if (mappings == null) {
            selectSql.append("*").append(" FROM ").append(tableName);
        } else {
            selectSql.append(listToStr(mappings, ",")).append(" FROM ").append(tableName);
        }
        if (wheres != null) {
            selectSql.append(" WHERE ")
                    .append(wheres.stream().map(where -> where + "=?").collect(Collectors.joining(" and ")));
        }
        if (conditions != null) {
            if (wheres != null) {
                selectSql.append(" and ");
            }
            selectSql.append(conditions.stream().collect(Collectors.joining(" and ")));
        }
        return selectSql.append(";").toString();
    }

    public String listToStr(List<String> params, String separator) {
        return params.stream().map(String::valueOf).collect(Collectors.joining(separator));
    }

    public static void main(String[] args) {
        XmlParseStrong xmlParseTwo = new XmlParseStrong();
        String jsonStr = JSON.toJSONString(xmlParseTwo.xmlParse(xmlParseTwo.getRootElement("view.xml")));
        xmlParseTwo.parseJson(jsonStr).stream().forEach(item -> {
            xmlParseTwo.sqlDataToSQL(item).stream().forEach(System.out::println);
        });
    }

    
}