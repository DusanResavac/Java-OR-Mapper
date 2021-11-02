package Testing;

import OrmFramework.metamodel.Orm;
import Testing.TestClasses.*;
import Testing.Subtests.TestClassesOneToOne.InsertOneToOneObject;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Main {

    List<Teacher> b = new ArrayList<>();

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

        Field field = Main.class.getDeclaredField("b");
        ParameterizedType t = (ParameterizedType) field.getGenericType();
        Class<?> c = (Class<?>) t.getActualTypeArguments()[0];

        InsertOneToOneObject.show();
        InsertObject.show();
        ModifyObject.show();
        WithForeignKey.show();
        WithForeignKeyList.show();
        WithNToM.show();
    }

}
