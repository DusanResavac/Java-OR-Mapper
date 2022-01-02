package Testing;

import OrmFramework.TrackingCache;
import OrmFramework.metamodel.Orm;
import Testing.Subtests.Maus;
import Testing.Subtests.Mausrad;
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
import java.util.logging.Level;

public class Main {

    List<Teacher> b = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        Orm.connect("jdbc:mariadb://localhost:3306/school?user=root&password=");
        Orm.deleteTables("school");
        Orm.createTables(Mausrad.class, Maus.class, Teacher.class, SClass.class, Course.class, Student.class);
        System.out.println("Connection established......");

        // If there is a problem with the Orm.deleteTables or Orm.createTables method, uncomment lines and use the scriptRunner

        //Initialize the script runner
        /*ScriptRunner sr = new ScriptRunner(Orm.getConnection());
        //Creating a reader object
        Reader reader = new BufferedReader(new FileReader("src/main/resources/test.sql"));
        //Running the script
        sr.runScript(reader);*/
        Orm.connect("jdbc:mariadb://localhost:3306/school?user=root&password=");
        Orm.setLevel(Level.INFO);

        Field field = Main.class.getDeclaredField("b");
        ParameterizedType t = (ParameterizedType) field.getGenericType();
        Class<?> c = (Class<?>) t.getActualTypeArguments()[0];
        // Viele edge cases wurden mit caching überlegt, jedoch kann es sein, dass es noch Probleme gibt, weswegen
        // bei komischem Verhalten empfohlen wird, ihn ausgeschaltet zu lassen
        // Orm.set_cache(new TrackingCache());

        InsertOneToOneObject.show();
        InsertObject.show();
        ModifyObject.show();
        WithForeignKey.show();
        WithForeignKeyList.show();
        WithNToM.show();
        WithCache.show();
        WithQuery.show();
    }

}
