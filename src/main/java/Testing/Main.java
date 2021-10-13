package Testing;

import OrmFramework.Orm;
import OrmFramework.metamodel._Entity;
import Testing.TestClasses.InsertObject;
import Testing.TestClasses.ModifyObject;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;

public class Main {

    public static void main(String[] args) throws NoSuchMethodException, SQLException, FileNotFoundException {

        Orm.connect("jdbc:mariadb://localhost:3306?user=root&password=");
        System.out.println("Connection established......");
        //Initialize the script runner
        ScriptRunner sr = new ScriptRunner(Orm.getConnection());
        //Creating a reader object
        Reader reader = new BufferedReader(new FileReader("src/main/resources/test.sql"));
        //Running the script
        sr.runScript(reader);
        Orm.connect("jdbc:mariadb://localhost:3306/school?user=root&password=");

        InsertObject.show();
        ModifyObject.show();

    }

}
