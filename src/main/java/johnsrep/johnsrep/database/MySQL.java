package johnsrep.johnsrep.database;

import johnsrep.johnsrep.Commands.Reputation;
import johnsrep.johnsrep.config.Configuration;
import johnsrep.johnsrep.config.MainConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.sql.*;

public class MySQL {

    public MySQL(Configuration<MainConfiguration> conf) {
        config = conf;

    }

    Configuration<MainConfiguration> config;



    private Connection con;

    static ConsoleCommandSender console = Bukkit.getConsoleSender();

    // connect
    public void connect() {
        if (!isConnected()) {
            try {
                con = DriverManager.getConnection("jdbc:mysql://" +
                        config.data().database().ipDB() + ":" +
                        config.data().database().portDB() + "/" +
                        config.data().database().nameDB(),
                        config.data().database().usernameDB(),
                        config.data().database().passwordDB());
                console.sendMessage("connect");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // disconnect
    public void disconnect() {
        if (isConnected()) {
            try {
                con.close();
                console.sendMessage("disconnect");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // isConnected
    public boolean isConnected() {
        return (con != null);
    }

    // getConnection
    public Connection getConnection() {
        return con;
    }
    public void createTable() throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Data (" +
                "UUIDto VARCHAR(100)," +
                "UUIDfrom VARCHAR(100)," +
                "Value INTEGER," +
                "Comment VARCHAR(100)," +
                "UNIQUE(`UUIDto`, `UUIDfrom`))");
        ps.executeUpdate();
    }
    //InsertData
    public void insertInTable(OfflinePlayer playerTo, OfflinePlayer playerFrom, int repValue, String repComment) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("INSERT INTO " +
                "Data (`UUIDto`, `UUIDfrom`, `Value`, `Comment`) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE `Value`=VALUES(`Value`), `Comment`=VALUES(`Comment`)");
        ps.setString(1, playerTo.getUniqueId().toString());
        ps.setString(2, playerFrom.getUniqueId().toString());
        ps.setInt(3, repValue);
        ps.setString(4, repComment);
        ps.executeUpdate();
    }
    //GetReputation
    public int getFromTable(Player playerTo) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("SELECT Value FROM Data WHERE UUIDto = ?");
        ps.setString(1, playerTo.getUniqueId().toString());
        ResultSet rs = ps.executeQuery();
        int sum = 0;
        while (rs.next()) {
            int repValue = rs.getInt("Value");
            sum += repValue;
        }
        return sum;

    }
    //GetReputation with comments

    public Reputation getAllFromTable(OfflinePlayer playerTo) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("SELECT Value, Comment, UUIDfrom FROM Data WHERE UUIDto = ?");
        ps.setString(1, playerTo.getUniqueId().toString());
        ResultSet rs = ps.executeQuery();
        Reputation reputation = new Reputation();
        reputation.player = playerTo;
        while (rs.next()) {
            reputation.fromPlayer.add(Bukkit.getOfflinePlayerIfCached(rs.getString("UUIDfrom")));
            reputation.values.add(rs.getInt("Value"));
            reputation.comments.add( rs.getString("Comment"));
        }

        return reputation;

    }

}
