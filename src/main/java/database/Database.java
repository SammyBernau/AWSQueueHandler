package database;

import java.sql.*;

public class Database {

    private static Database instance;
    private final Connection connection;

    public String checkColDataType(String database,String checkCol) throws SQLException {
        String columnType = "";
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getColumns(null, "public", database, "%");

            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                if (columnName.equals(checkCol)) {
                    columnType = rs.getString("TYPE_NAME");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columnType;
    }

    public static Database getInstance() {
        if (instance == null) {
            try {
                instance = new Database();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    Database() throws SQLException, ClassNotFoundException {
        String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres";
        connection = DriverManager.getConnection(DATABASE_URL);
    }

    public int executeUpdate(String sql){
        try{
            PreparedStatement statement = connection.prepareStatement(sql);
            if(sql.contains("ALTER")) return statement.executeUpdate();
            else {
                statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ResultSet execute(String sql) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            if (sql.contains("SELECT")) return statement.executeQuery();
            else {
                statement.execute();
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}//End file

