package database;
import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Query {

    private String query;

    public Query(String query) {
        this.query = query;
    }

    public Query() {
        this.query = "";
    }

    //String builder pattern
    public Query with(String x) {
        query += x + " ";
        return this;
    }

    public Query alterTable(String tableName){
        return with("ALTER TABLE " + tableName);
    }

    public Query alterColumn(String columnName,String newDataType){
        return with("ALTER COLUMN "+ columnName + " TYPE " + newDataType);
    }

    public Query insert(String tableName, Object... values) {
        String sValues = Arrays.stream(values).map(obj -> "'" + obj.toString() + "'").collect(Collectors.joining(","));
        return with("INSERT INTO " + tableName + " VALUES (" + sValues + ")");
    }

    public static Query build() {
        return new Query();
    }

    //Checks Column DataType
    public String checkCol(String tableName, String checkCol) throws SQLException {
        return Database.getInstance().checkColDataType(tableName, checkCol);
    }

    public int executeUpdate(){
        return Database.getInstance().executeUpdate(query);
    }

    public ResultSet execute() {
        return Database.getInstance().execute(query);
    }







}
