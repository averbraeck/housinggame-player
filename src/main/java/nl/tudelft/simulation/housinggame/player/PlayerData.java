package nl.tudelft.simulation.housinggame.player;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GamesessionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GameversionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.ScenarioRecord;

public class PlayerData
{

    /**
     * the SQL datasource representing the database's connection pool.<br>
     * the datasource is shared among the servlets and stored as a ServletContext attribute.
     */
    private DataSource dataSource;

    /**
     * the id of the player player logged in to this session.<br>
     * if null, no player is logged in.<br>
     * filled by the PlayerLoginServlet.<br>
     * used by: server.
     */
    private String playerCode;

    /** There is always a gamesession to which the player eblongs. */
    private GamesessionRecord gameSession;

    /** There is always a group to which the player belongs. */
    private GroupRecord group;

    /** labels in the language of the game session (or the player!). */
    private Map<String, String> labelMap = new HashMap<>();

    /** The game might not have started, but a player ALWAYS has a highest player round (0 if not started). */
    private PlayerroundRecord playerRound;

    /** The game might not have started, but a groep ALWAYS has a highest group round (0 if not started). */
    private GrouproundRecord groupRound;

    /** The scenario. Always there. */
    private ScenarioRecord scenario;

    /** The game version. Always there. */
    private GameversionRecord gameVersion;

    private int currentRound = 0;

    /**
     * the player Player record for the logged in player.<br>
     * this record has the USERNAME to display on the screen.<br>
     * filled by the PlayerLoginServlet.<br>
     * used by: server and in servlet.<br>
     */
    private PlayerRecord player;

    private String contentHtml = "";

    /* ================================= */
    /* FULLY DYNAMIC INFO IN THE SESSION */
    /* ================================= */

    /**
     * which menu has been chosen, to maintain persistence after a POST. <br>
     */
    private String menuChoice = "";

    /**
     * when 0, do not show popup; when 1: show popup. <br>
     * filled and updated by RoundServlet.
     */
    private int showModalWindow = 0;

    /**
     * client info (dynamic) for popup.
     */
    private String modalWindowHtml = "";

    /**
     * Error
     */
    private boolean error = false;

    /* ******************* */
    /* GETTERS AND SETTERS */
    /* ******************* */

    public DataSource getDataSource()
    {
        if (this.dataSource == null)
        {
            try
            {
                // determine the connection pool, and create one if it does not yet exist (first use after server restart)
                try
                {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(new ServletException(e));
                }

                try
                {
                    Context ctx = new InitialContext();
                    try
                    {
                        ctx.lookup("/housinggame-player_datasource");
                    }
                    catch (NamingException ne)
                    {
                        final HikariConfig config = new HikariConfig();
                        config.setJdbcUrl("jdbc:mysql://localhost:3306/housinggame");
                        config.setUsername("housinggame");
                        config.setPassword("tudHouse#4");
                        config.setMaximumPoolSize(2);
                        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                        DataSource dataSource = new HikariDataSource(config);
                        ctx.bind("/housinggame-player_datasource", dataSource);
                    }
                }
                catch (NamingException e)
                {
                    throw new RuntimeException(new ServletException(e));
                }

                setDataSource((DataSource) new InitialContext().lookup("/housinggame-player_datasource"));
            }
            catch (NamingException e)
            {
                throw new RuntimeException(new ServletException(e));
            }
        }
        return this.dataSource;
    }

    public void setDataSource(final DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public String getPlayerCode()
    {
        return this.playerCode;
    }

    public void setPlayerCode(final String playerCode)
    {
        this.playerCode = playerCode;
    }

    public PlayerRecord getPlayer()
    {
        return this.player;
    }

    public void setPlayer(final PlayerRecord player)
    {
        this.player = player;
    }

    public GamesessionRecord getGameSession()
    {
        return this.gameSession;
    }

    public void setGameSession(final GamesessionRecord gameSession)
    {
        this.gameSession = gameSession;
    }

    public GroupRecord getGroup()
    {
        return this.group;
    }

    public void setGroup(final GroupRecord group)
    {
        this.group = group;
    }

    public int getShowModalWindow()
    {
        return this.showModalWindow;
    }

    public void setShowModalWindow(final int showModalWindow)
    {
        this.showModalWindow = showModalWindow;
    }

    public String getMenuChoice()
    {
        return this.menuChoice;
    }

    public void setMenuChoice(final String menuChoice)
    {
        this.menuChoice = menuChoice;
    }

    public String getTopMenu()
    {
        return PlayerServlet.getTopMenu(this);
    }

    public String getContentHtml()
    {
        return this.contentHtml;
    }

    public void setContentHtml(final String contentHtml)
    {
        this.contentHtml = contentHtml;
    }

    public String getModalWindowHtml()
    {
        return this.modalWindowHtml;
    }

    public void setModalWindowHtml(final String modalClientWindowHtml)
    {
        this.modalWindowHtml = modalClientWindowHtml;
    }

    public boolean isError()
    {
        return this.error;
    }

    public void setError(final boolean error)
    {
        this.error = error;
    }

    /*-
    <option value="3">Ommen morning</option>
    <option value="6">Ommen afternoon</option>
    */
    public String getValidSessionOptions()
    {
        LocalDateTime now = LocalDateTime.now();
        DSLContext dslContext = DSL.using(getDataSource(), SQLDialect.MYSQL);
        List<GamesessionRecord> gsList = dslContext.selectFrom(Tables.GAMESESSION).fetch();
        StringBuilder s = new StringBuilder();
        for (GamesessionRecord gs : gsList)
        {
            if (gs.getStartTime() == null || now.isAfter(gs.getStartTime()))
            {
                if (gs.getEndTime() == null || now.isBefore(gs.getEndTime()))
                {
                    s.append("<option value=\"");
                    s.append(gs.getId().intValue());
                    s.append("\">");
                    s.append(gs.getName());
                    s.append("</option>\n");
                }
            }
        }
        return s.toString();
    }

    public String getLabel(final String key)
    {
        String label = this.labelMap.get(key) == null ? "!" + key + "!" : this.labelMap.get(key);
        if (label.contains("$"))
        {
            label = label.replace("$group$", this.group.getName());
            label = label.replace("$player$", this.getPlayerCode());
            label = label.replace("$round$", "" + this.getCurrentRound());
            label = label.replace("$rating$", "" + this.playerRound.getPreferredHouseRating().intValue());
            label = label.replace("$income$", "" + this.playerRound.getIncome().intValue());
            label = label.replace("$satisfaction$", "" + this.playerRound.getSatisfaction().intValue());
            label = label.replace("$savings$", "" + this.playerRound.getSaving().intValue());
            label = label.replace("$maxmortgage$", "???");
        }
        return label;
    }

    public void setLabelMap(final Map<String, String> labelMap)
    {
        this.labelMap = labelMap;
    }

    public PlayerroundRecord getPlayerRound()
    {
        return this.playerRound;
    }

    public void setPlayerRound(final PlayerroundRecord playerRound)
    {
        this.playerRound = playerRound;
    }

    public GrouproundRecord getGroupRound()
    {
        return this.groupRound;
    }

    public void setGroupRound(final GrouproundRecord groupRound)
    {
        this.groupRound = groupRound;
    }

    public int getCurrentRound()
    {
        return this.currentRound;
    }

    public void setCurrentRound(final int currentRound)
    {
        this.currentRound = currentRound;
    }

    public ScenarioRecord getScenario()
    {
        return this.scenario;
    }

    public void setScenario(final ScenarioRecord scenario)
    {
        this.scenario = scenario;
    }

    public GameversionRecord getGameVersion()
    {
        return this.gameVersion;
    }

    public void setGameVersion(final GameversionRecord gameVersion)
    {
        this.gameVersion = gameVersion;
    }

}
