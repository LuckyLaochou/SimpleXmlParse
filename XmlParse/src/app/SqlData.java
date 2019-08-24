package app;

import java.util.List;
import java.util.Map;

/**
 * SqlData
 */
public class SqlData {

    private String view;

    private String type;

    /**
     * string : 表名
     * string : 条件
     * string : 属性
     * string : 字段
     */
    private Map<String, Map<String, Map<String,String>>> params;

    // common condition 
    private List<String> conditions;

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Map<String, Map<String, String>>> getParams() {
        return params;
    }

    public void setParams(Map<String, Map<String, Map<String, String>>> params) {
        this.params = params;
    }


    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    @Override
    public String toString() {
        return "SqlData [conditions=" + conditions + ", params=" + params + ", type=" + type + ", view=" + view + "]";
    }
    
}