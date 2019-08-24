package app;

/**
 * SqlType
 */
public enum SqlType {

    INSERT("insert"),
    SELECT("select"),
    UPDATE("update"),
    DELETE("delete");

    private String name;

    private SqlType(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public static SqlType getSqlTypeByName(String name){
        for(SqlType sqlType : values()){
            if(sqlType.getName().equals(name)){
                return sqlType;
            }
        }
        return null;
    }

    
    
}