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
import org.jooq.types.UInteger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GamesessionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GameversionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.LabelRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.LanguageRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.LanguagegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.RoundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.ScenarioRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.ScenarioparametersRecord;

public class PlayerData
{

    /**
     * the SQL datasource representing the database's connection pool.<br>
     * the datasource is shared among the servlets and stored as a ServletContext attribute.
     */
    private DataSource dataSource;

    /** the Player record for the logged in player. */
    private PlayerRecord player;

    /** There is always a gamesession to which the player belongs. */
    private GamesessionRecord gameSession;

    /** There is always a group to which the player belongs. */
    private GroupRecord group;

    /** labels in the language of the game session (or the player!). */
    private Map<String, String> labelMap = new HashMap<>();

    /** The game might not have started, but a player ALWAYS has a highest player round (0 if not started). */
    private PlayerroundRecord playerRound;

    /** The game might not have started, but a groep ALWAYS has a highest group round (0 if not started). */
    private GrouproundRecord groupRound;

    /** the current round as a record. Always there. */
    private RoundRecord round;

    /** The scenario. Always there. */
    private ScenarioRecord scenario;

    /** The game version. Always there. */
    private GameversionRecord gameVersion;

    /* ================================= */
    /* FULLY DYNAMIC INFO IN THE SESSION */
    /* ================================= */

    /** Content that ready for the jsp page to display. */
    private Map<String, String> contentHtmlMap = new HashMap<>();

    /** When 0, do not show popup; when 1: show popup. */
    private int showModalWindow = 0;

    /** client info (dynamic) for popup. */
    private String modalWindowHtml = "";

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
        return this.player.getCode();
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

    public String getContentHtml(final String key)
    {
        return this.contentHtmlMap.get(key);
    }

    public Map<String, String> getContentHtml()
    {
        return this.contentHtmlMap;
    }

    public String getModalWindowHtml()
    {
        return this.modalWindowHtml;
    }

    public void setModalWindowHtml(final String modalClientWindowHtml)
    {
        this.modalWindowHtml = modalClientWindowHtml;
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
        return this.round.getRoundNumber();
    }

    public RoundRecord getRound()
    {
        return this.round;
    }

    public void setRound(final RoundRecord round)
    {
        this.round = round;
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

    /*-
    <option value="3">Session 1</option>
    <option value="6">Session 2</option>
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

    /**
     * Return a language-dependent label for a key, and replace dynamic strings.
     * @param key String; the language key
     * @return String; language-dependent label for the key
     */
    public String getLabel(final String key)
    {
        String label = this.labelMap.get(key) == null ? "!" + key + "!" : this.labelMap.get(key);
        if (label.contains("$"))
        {
            label = label.replace("$group$", this.group.getName());
            label = label.replace("$player$", this.getPlayerCode());
            label = label.replace("$round$", Integer.toString(this.getCurrentRound()));
            label = label.replace("$rating$", Integer.toString(this.playerRound.getPreferredHouseRating().intValue()));
            label = label.replace("$income$", k(this.playerRound.getIncome().intValue()));
            label = label.replace("$satisfaction$", k(this.playerRound.getSatisfaction().intValue()));
            label = label.replace("$savings$", k(this.playerRound.getSavings().intValue()));
            label = label.replace("$maxmortgage$", k(this.playerRound.getMaximumMortgage().intValue()));
        }
        return label;
    }

    /**
     * Express a number in thousands.
     * @param nr int; the number to display
     * @return String; the number if less than 1000, or the rounded number divided by 1000, followed by 'k'
     */
    public String k(final int nr)
    {
        if (nr < 1000)
            return Integer.toString(nr);
        else
            return Integer.toString(nr / 1000) + " k";
    }

    protected void setLanguageLabels(final ScenarioRecord scenario)
    {
        DSLContext dslContext = DSL.using(getDataSource(), SQLDialect.MYSQL);
        ScenarioparametersRecord spr =
                SqlUtils.readRecordFromId(this, Tables.SCENARIOPARAMETERS, scenario.getScenarioparametersId());
        GameversionRecord gameVersion = SqlUtils.readRecordFromId(this, Tables.GAMEVERSION, scenario.getGameversionId());
        UInteger languageId = spr.getDefaultLanguageId();
        LanguageRecord language = SqlUtils.readRecordFromId(this, Tables.LANGUAGE, languageId);
        LanguagegroupRecord languageGroup =
                SqlUtils.readRecordFromId(this, Tables.LANGUAGEGROUP, gameVersion.getLanguagegroupId());
        int languageNr = 1;
        if (languageGroup.getLanguageId1().equals(language.getId()))
            languageNr = 1;
        else if (languageGroup.getLanguageId2().equals(language.getId()))
            languageNr = 2;
        else if (languageGroup.getLanguageId3().equals(language.getId()))
            languageNr = 3;
        else if (languageGroup.getLanguageId4().equals(language.getId()))
            languageNr = 4;
        List<LabelRecord> labelList =
                dslContext.selectFrom(Tables.LABEL).where(Tables.LABEL.LANGUAGEGROUP_ID.eq(languageGroup.getId())).fetch();
        Map<String, String> labelMap = new HashMap<>();
        for (LabelRecord label : labelList)
        {
            String key = label.getKey();
            String value = switch (languageNr)
            {
                case 1 -> label.getValue1();
                case 2 -> label.getValue2();
                case 3 -> label.getValue3();
                case 4 -> label.getValue4();
                default -> "!" + key + "!";
            };
            labelMap.put(key, value);
        }
        setLabelMap(labelMap);
    }

}
