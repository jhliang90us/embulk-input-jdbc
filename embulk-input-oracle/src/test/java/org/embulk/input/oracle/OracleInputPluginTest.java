package org.embulk.input.oracle;

import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import org.embulk.input.AbstractJdbcInputPluginTest;
import org.embulk.input.OracleInputPlugin;
import org.embulk.spi.InputPlugin;
import org.junit.Test;

public class OracleInputPluginTest extends AbstractJdbcInputPluginTest
{
    @Override
    protected void prepare() throws SQLException
    {
        tester.addPlugin(InputPlugin.class, "oracle", OracleInputPlugin.class);

        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: you should put 'ojdbc7.jar' in 'embulk-input-oracle/driver' directory in order to test.");
            return;
        }

        try {
            connect();
        } catch (SQLException e) {
            System.err.println(e);
            System.err.println(String.format(ENGLISH, "Warning: prepare a schema on Oracle 12c (server = %s, port = %d, database = %s, user = %s, password = %s, charset = UTF-8).",
                    getHost(), getPort(), getDatabase(), getUser(), getPassword()));
            // for example
            //   CREATE USER TEST_USER IDENTIFIED BY "test_pw";
            //   GRANT DBA TO TEST_USER;
            return;
        }

        enabled = true;

        String drop1 = "DROP TABLE TEST1";
        executeSQL(drop1, true);

        String create1 =
                "CREATE TABLE TEST1 ("
                + "ID  CHAR(2),"
                + "C1  DECIMAL(12,2),"
                + "C2  CHAR(8),"
                + "C3  VARCHAR2(8),"
                + "C4  NVARCHAR2(8),"
                + "C5  DATE,"
                + "C6  TIMESTAMP,"
                + "C7  TIMESTAMP(3),"
                + "PRIMARY KEY(ID))";
        executeSQL(create1);

        String insert1 =
                "INSERT INTO TEST1 VALUES("
                + "'10',"
                + "NULL,"
                + "NULL,"
                + "NULL,"
                + "NULL,"
                + "NULL,"
                + "NULL,"
                + "NULL)";
        executeSQL(insert1);

        String insert2 =
                "INSERT INTO TEST1 VALUES("
                + "'11',"
                + "-1234567890.12,"
                + "'ABCDEF',"
                + "'XYZ',"
                + "'ＡＢＣＤＥＦＧＨ',"
                + "'2015-06-04',"
                + "'2015-06-05 23:45:06',"
                + "'2015-06-06 23:45:06.789')";
        executeSQL(insert2);
    }

    @Test
    public void test() throws Exception
    {
        if (enabled) {
            test("/oracle/yml/input.yml");
            assertEquals(Arrays.asList(
                    "C1,C2,C3,C4,C5,C6,C7",
                    "-1.23456789012E9,ABCDEF  ,XYZ,ＡＢＣＤＥＦＧＨ,2015-06-04,2015-06-05 23:45:06,2015-06-06 23:45:06.789",
                    ",,,,,,"),
                    read("oracle-input000.00.csv"));
        }
    }

    @Test
    public void testLower() throws Exception
    {
        if (enabled) {
            test("/oracle/yml/input-lower.yml");
            assertEquals(Arrays.asList(
                    "C1,C2,C3,C4,C5,C6,C7",
                    "-1.23456789012E9,ABCDEF  ,XYZ,ＡＢＣＤＥＦＧＨ,2015-06-04,2015-06-05 23:45:06,2015-06-06 23:45:06.789",
                    ",,,,,,"),
                    read("oracle-input000.00.csv"));
        }
    }

    @Test
    public void testQuery() throws Exception
    {
        if (enabled) {
            test("/oracle/yml/input-query.yml");
            assertEquals(Arrays.asList(
                    "C1,C2,C3,C4,C5,C6,C7",
                    "-1.23456789012E9,ABCDEF  ,XYZ,ＡＢＣＤＥＦＧＨ,2015-06-04,2015-06-05 23:45:06,2015-06-06 23:45:06.789",
                    ",,,,,,"),
                    read("oracle-input000.00.csv"));
        }
    }

    @Test
    public void testQueryLower() throws Exception
    {
        if (enabled) {
            test("/oracle/yml/input-query-lower.yml");
            assertEquals(Arrays.asList(
                    "C1,C2,C3,C4,C5,C6,C7",
                    "-1.23456789012E9,ABCDEF  ,XYZ,ＡＢＣＤＥＦＧＨ,2015-06-04,2015-06-05 23:45:06,2015-06-06 23:45:06.789",
                    ",,,,,,"),
                    read("oracle-input000.00.csv"));
        }
    }

    @Test
    public void testColumnOptions() throws Exception
    {
        if (enabled) {
            test("/oracle/yml/input-column-options.yml");
            assertEquals(Arrays.asList(
                    "ID,C1,C2,C3,C4,C5,C6,C7",
                    "10,,,,,,,",
                    "11,-1.23456789012E9,ABCDEF  ,XYZ,ＡＢＣＤＥＦＧＨ,2015/06/04,2015/06/05 23:45:06,2015/06/06 23:45:06.789"),
                    read("oracle-input000.00.csv"));
        }
    }

    @Test
    public void testColumnOptionsLower() throws Exception
    {
        if (enabled) {
            test("/oracle/yml/input-column-options-lower.yml");
            assertEquals(Arrays.asList(
                    "ID,C1,C2,C3,C4,C5,C6,C7",
                    "10,,,,,,,",
                    "11,-1.23456789012E9,ABCDEF  ,XYZ,ＡＢＣＤＥＦＧＨ,2015/06/04,2015/06/05 23:45:06,2015/06/06 23:45:06.789"),
                    read("oracle-input000.00.csv"));
        }
    }

    @Override
    protected Connection connect() throws SQLException
    {
        return DriverManager.getConnection(String.format(ENGLISH, "jdbc:oracle:thin:@%s:%d:%s", getHost(), getPort(), getDatabase()),
                getUser(), getPassword());
    }
}
