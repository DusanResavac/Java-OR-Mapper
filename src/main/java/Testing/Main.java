package Testing;

import OrmFramework.metamodel.Orm;
import Testing.TestClasses.InsertObject;
import Testing.TestClasses.ModifyObject;
import Testing.TestClasses.WithForeignKey;
import Testing.TestClasses.WithForeignKeyList;
import Testing.Subtests.TestClassesOneToOne.InsertOneToOneObject;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

public class Main {

    public static void main(String[] args) throws Exception {

        Orm.connect("jdbc:mariadb://localhost:3306?user=root&password=");
        System.out.println("Connection established......");
        //Initialize the script runner
        ScriptRunner sr = new ScriptRunner(Orm.getConnection());
        //Creating a reader object
        Reader reader = new BufferedReader(new FileReader("src/main/resources/test.sql"));
        //Running the script
        sr.runScript(reader);
        Orm.connect("jdbc:mariadb://localhost:3306/school?user=root&password=");

        InsertOneToOneObject.show();
        InsertObject.show();
        ModifyObject.show();
        WithForeignKey.show();
        WithForeignKeyList.show();
    }

}
