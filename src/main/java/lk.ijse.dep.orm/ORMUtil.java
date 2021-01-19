package lk.ijse.dep.orm;

import lk.ijse.dep.annotation.Column;
import lk.ijse.dep.annotation.Entity;
import lk.ijse.dep.annotation.Id;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ORMUtil {
    public static void init(Properties dbproperties, Class... entities) {
        String username = dbproperties.getProperty("javax.persistence.username");
        String password = dbproperties.getProperty("javax.persistence.password");
        String url = dbproperties.getProperty("javax.persistence.url");
        String driverClassName = dbproperties.getProperty("javax.persistence.jdbc.driver_class");
        Connection connection = null;
        String sqlScript = "";

        if (username == null || password == null || url == null || driverClassName == null) {
            throw new RuntimeException("Unable to intialize ORM Without Database Details");
        }

        try {
            Class.forName(driverClassName);
            Connection connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e)
        }catch (SQLException throwables){
            throw new RuntimeException("Failed to Connect",throwables);
        }


        for (Class entity : entities) {
            boolean pk = false;
            Annotation entityAnnotation = entity.getDeclaredAnnotation(Entity.class);

            if (entityAnnotation == null) {
                throw new RuntimeException("Invalid entity class" + entity.getName());
            }

            String [] split = entity.getName().split("[.]");
            String ddl = "CREATE TABLE" + split[split.length - 1] + "(\n";

            Field[] declareFields = entity.getDeclaredFields();

            for (Field declareField : declareFields) {
                Column columnAnnotation = declareField.getDeclaredAnnotation(Column.class);
                Id idAnnotation = declareField.getDeclaredAnnotation(Id.class);

                if (columnAnnotation != null) {
                    String columnName = (columnAnnotation.name().trim().isEmpty()) ? declareField.getName() : columnAnnotation.name();
                    String columnDef = null;
                    if (declareField.getType() == String.class) {
                        columnDef = "VARCHAR(255)";
                    } else if (declareField.getType() == int.class || declareField.getType() == long.class || declareField.getType() == short.class) {
                        columnDef = "INT";
                    } else if (declareField.getType() == double.class || declareField.getType() == float.class || declareField.getType() == BigDecimal.class) {
                        columnDef = "DECIMAL";
                    } else if (declareField.getType() == boolean.class) {
                        columnDef = "BOOLEAN";
                    } else {
                        throw new RuntimeException("Invalid dta type");
                    }
                    ddl += columnName + " " + columnDef;

                    if(pk && idAnnotation != null){
                        throw new RuntimeException("Composite Keys are not supported yet");
                    }

                    if(idAnnotation != null){
                        ddl += "PRIMARY KEY";
                    }
                    ddl += ",\n";
                }
            }
            ddl += ");";
           sqlScript += ddl;
        }

        try {
            Statement stm = connection.createStatement();
            stm.execute(sqlScript);
            connection.close();
        } catch (SQLException throwables) {
            throw new RuntimeException("Failed to create tables", throwables);
        }


    }
}
