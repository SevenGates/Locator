package mah.sys.locator;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Created by Alex on 04-Apr-16.
 */
public abstract class SQLTransformer {

    public static String getSQLOverhead(String room){
        StringBuilder SQL = new StringBuilder("SELECT Buildings.OverheadView FROM Buildings JOIN Levels ON Buildings.Name=Levels.Building JOIN Rooms ON Levels.ID = Rooms.Level WHERE RoomName = ");
        // SELECT Buildings.OverheadView FROM Buildings JOIN Levels ON Buildings.Name=Levels.Building JOIN Rooms ON Levels.ID = Rooms.Level WHERE RoomName = NI0305
        SQL.append(room);
        return SQL.toString();
    }

    public static String getSQLLevelNr(String room){
        StringBuilder SQL = new StringBuilder("SELECT Rooms.Level FROM Buildings JOIN Levels ON Buildings.Name=Levels.Building JOIN Rooms ON Levels.ID = Rooms.Level WHERE RoomName = ");
        // SELECT Buildings.OverheadView FROM Buildings JOIN Levels ON Buildings.Name=Levels.Building JOIN Rooms ON Levels.ID = Rooms.Level WHERE RoomName = NI0305
        SQL.append(room);
        return SQL.toString();
    }

    public static String getSQLLevelMap(String room){
        StringBuilder SQL = new StringBuilder("SELECT Levels.Map FROM Buildings JOIN Levels ON Buildings.Name=Levels.Building JOIN Rooms ON Levels.ID = Rooms.Level WHERE RoomName = ");
        // SELECT Buildings.OverheadView FROM Buildings JOIN Levels ON Buildings.Name=Levels.Building JOIN Rooms ON Levels.ID = Rooms.Level WHERE RoomName = NI0305
        SQL.append(room);
        return SQL.toString();
    }
}
