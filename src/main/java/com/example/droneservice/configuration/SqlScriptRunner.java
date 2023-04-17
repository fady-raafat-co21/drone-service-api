package com.example.droneservice.configuration;


import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Stream;

@Component
public class SqlScriptRunner implements ApplicationRunner {

    private final DataSource dataSource;

    public SqlScriptRunner() {
        // Create and configure an H2 JdbcDataSource object
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("SA");

        // Assign the JdbcDataSource object to the dataSource field
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // Execute the SQL script
        executeSqlScript("" +
                "insert into drone(serial_number,model,weight_limit,battery,state) values('564sdsd','LIGHTWEIGHT',400,89,'IDLE');\n" +
                "insert into drone(serial_number,model,weight_limit,battery,state) values('dasdq21','LIGHTWEIGHT',244,22,'IDLE');\n" +
                "insert into drone(serial_number,model,weight_limit,battery,state) values('df324ff','MIDDLEWEIGHT',344,45,'IDLE');\n" +
                "insert into drone(serial_number,model,weight_limit,battery,state) values('aad2212','LIGHTWEIGHT',444,55,'IDLE');\n" +
                "insert into drone(serial_number,model,weight_limit,battery,state) values('ffw1211','HEAVYWEIGHT',333,77,'IDLE');\n" +
                "insert into drone(serial_number,model,weight_limit,battery,state) values('vasd123','LIGHTWEIGHT',244,34,'IDLE');\n" +
                "insert into drone(serial_number,model,weight_limit,battery,state) values('31dasdw','CRUISERWEIGHT',477,44,'IDLE');\n" +
                "insert into drone(serial_number,model,weight_limit,battery,state) values('123dasa','LIGHTWEIGHT',354,100,'IDLE');\n" +
                "insert into drone(serial_number,model,weight_limit,battery,state) values('asd1231','MIDDLEWEIGHT',257,88,'IDLE');\n" +
                "insert into drone(serial_number,model,weight_limit,battery,state) values('asd1342','HEAVYWEIGHT',462,99,'IDLE');");
    }


    private void executeSqlScript(String sqlScript) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // Split the SQL script into individual statements
            String[] sqlStatements = sqlScript.split(";");
            for (String sqlStatement : sqlStatements) {
                if (!sqlStatement.trim().isEmpty()) {
                    // Execute each non-empty SQL statement
                    statement.execute(sqlStatement);
                }
            }
        } catch (Exception e) {
            // Log and re-throw any exception that occurs during script execution
            System.err.println("Failed to execute SQL script: " + e.getMessage());
            throw e;
        }
    }
}
