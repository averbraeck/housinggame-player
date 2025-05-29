package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import nl.tudelft.simulation.housinggame.common.CommonData;
import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.TransactionStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GamesessionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GameversionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HouseRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.LabelRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.LanguageRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.LanguagegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasuretypeRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerstateRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.ScenarioRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.ScenarioparametersRecord;

public class PlayerData extends CommonData
{

    /** the Player record for the logged in player. Static during session. */
    private PlayerRecord player;

    /** There is always a gamesession to which the player belongs. Static during session. */
    private GamesessionRecord gameSession;

    /** There is always a group to which the player belongs. Static during session. */
    private GroupRecord group;

    /** The scenario. Static during session. */
    private ScenarioRecord scenario;

    /** The scenario parameters. */
    private ScenarioparametersRecord scenarioParameters;

    /** The game version. Static during session. */
    private GameversionRecord gameVersion;

    /** labels in the language of the game session (or the player!). Static during session. */
    private Map<String, String> labelMap = new HashMap<>();

    /** The list of groupRoundRecords until now. This list is DYNAMIC. */
    private List<GrouproundRecord> groupRoundList = new ArrayList<>();

    /** The highest round of the group. This is DYNAMIC. Equal to or higher than the currentPlayerRound. */
    private int highestGroupRoundNumber = -1;

    /** The groupround that MATCHES the playerRound (null if not started). */
    private GrouproundRecord groupRound;

    /** The current round of the player. This is DYNAMIC. */
    private int playerRoundNumber = -1;

    /** The game might not have started, but a player ALWAYS has a highest player round (null if not started). */
    private PlayerroundRecord playerRound;

    /** Previous player round; null when not started, same as playerRound in round 0, previous one otherwise. */
    private PlayerroundRecord prevPlayerRound;

    /* ================================= */
    /* FULLY DYNAMIC INFO IN THE SESSION */
    /* ================================= */

    /** Content that ready for the jsp page to display. */
    private Map<String, String> contentHtmlMap = new HashMap<>();

    /** When 0, do not show popup; when 1: show popup. */
    private int showModalWindow = 0;

    /** client info (dynamic) for popup. */
    private String modalWindowHtml = "";

    /** error message for error servlet. */
    private String error = "";

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

                this.dataSource = (DataSource) new InitialContext().lookup("/housinggame-player_datasource");
            }
            catch (NamingException e)
            {
                throw new RuntimeException(new ServletException(e));
            }
        }
        return this.dataSource;
    }

    public void readPlayerData(final PlayerRecord player)
    {
        this.player = player;
        this.group = PlayerUtils.readRecordFromId(this, Tables.GROUP, player.getGroupId());
        this.gameSession = PlayerUtils.readRecordFromId(this, Tables.GAMESESSION, this.group.getGamesessionId());
        this.scenario = PlayerUtils.readRecordFromId(this, Tables.SCENARIO, this.group.getScenarioId());
        this.gameVersion = PlayerUtils.readRecordFromId(this, Tables.GAMEVERSION, this.scenario.getGameversionId());
        this.scenarioParameters =
                PlayerUtils.readRecordFromId(this, Tables.SCENARIOPARAMETERS, this.scenario.getScenarioparametersId());
        setLanguageLabels(this.scenario);
        readDynamicData();
    }

    public boolean readDynamicData()
    {
        try
        {
            DSLContext dslContext = DSL.using(getDataSource(), SQLDialect.MYSQL);
            this.groupRound = null;
            this.highestGroupRoundNumber = -1;
            this.groupRoundList.clear();
            for (int i = 0; i <= this.scenario.getHighestRoundNumber(); i++)
            {
                GrouproundRecord gr = dslContext.selectFrom(Tables.GROUPROUND).where(Tables.GROUPROUND.ROUND_NUMBER.eq(i))
                        .and(Tables.GROUPROUND.GROUP_ID.eq(this.group.getId())).fetchAny();
                if (gr == null)
                    break;
                this.groupRound = gr;
                this.highestGroupRoundNumber = i;
                this.groupRoundList.add(gr);
            }

            if (this.groupRound == null)
            {
                this.groupRound = PlayerUtils.makeGroupRound0(this);
                this.groupRoundList.add(this.groupRound);
                this.highestGroupRoundNumber = 0;
            }

            this.playerRound = null;
            this.prevPlayerRound = null;
            this.playerRoundNumber = -1;
            for (int i = 0; i < this.groupRoundList.size(); i++)
            {
                PlayerroundRecord pr = dslContext.selectFrom(Tables.PLAYERROUND)
                        .where(Tables.PLAYERROUND.GROUPROUND_ID.eq(this.groupRoundList.get(i).getId()))
                        .and(Tables.PLAYERROUND.PLAYER_ID.eq(this.player.getId())).fetchAny();
                if (pr != null)
                {
                    this.prevPlayerRound = this.playerRound == null ? pr : this.playerRound;
                    this.playerRound = pr;
                    this.playerRoundNumber = i;
                }
            }

            if (this.playerRound == null)
            {
                GrouproundRecord groupRound0 =
                        dslContext.selectFrom(Tables.GROUPROUND).where(Tables.GROUPROUND.ROUND_NUMBER.eq(0))
                                .and(Tables.GROUPROUND.GROUP_ID.eq(this.group.getId())).fetchAny();
                this.playerRound = PlayerUtils.makePlayerRound0(this, groupRound0);
                this.prevPlayerRound = this.playerRound;
                this.playerRoundNumber = 0;
            }

            this.groupRound = this.groupRoundList.get(this.playerRoundNumber);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public ScenarioRecord getScenario()
    {
        return this.scenario;
    }

    public GameversionRecord getGameVersion()
    {
        return this.gameVersion;
    }

    public ScenarioparametersRecord getScenarioParameters()
    {
        return this.scenarioParameters;
    }

    public String getPlayerCode()
    {
        return this.player.getCode();
    }

    public PlayerRecord getPlayer()
    {
        return this.player;
    }

    public GamesessionRecord getGameSession()
    {
        return this.gameSession;
    }

    public GroupRecord getGroup()
    {
        return this.group;
    }

    public PlayerroundRecord getPlayerRound()
    {
        return this.playerRound;
    }

    public PlayerroundRecord getPrevPlayerRound()
    {
        return this.prevPlayerRound;
    }

    public List<PlayerroundRecord> getPlayerRoundList()
    {
        DSLContext dslContext = DSL.using(getDataSource(), SQLDialect.MYSQL);
        return dslContext.selectFrom(Tables.PLAYERROUND).where(Tables.PLAYERROUND.PLAYER_ID.eq(getPlayer().getId())).fetch();
    }

    public GrouproundRecord getGroupRound()
    {
        return this.groupRound;
    }

    public int getPlayerRoundNumber()
    {
        return this.playerRoundNumber;
    }

    public int getHighestGroupRoundNumber()
    {
        return this.highestGroupRoundNumber;
    }

    public List<GrouproundRecord> getGroupRoundList()
    {
        return this.groupRoundList;
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

    public void putContentHtml(final String key, final String value)
    {
        this.contentHtmlMap.put(key, value);
    }

    public String getModalWindowHtml()
    {
        return this.modalWindowHtml;
    }

    public void setModalWindowHtml(final String modalClientWindowHtml)
    {
        this.modalWindowHtml = modalClientWindowHtml;
    }

    public String getError()
    {
        return this.error;
    }

    public void setError(final String error)
    {
        this.error = error;
    }

    public void errorRedirect(final HttpServletResponse response, final String message) throws IOException
    {
        System.err.println(message);
        setError(message);
        response.sendRedirect("/housinggame-player/error");
    }

    public void errorNoRedirect(final HttpServletResponse response, final String message) throws IOException
    {
        System.err.println(message);
        setError(message);
        JsonObject json = new JsonObject();
        json.addProperty("error", message);
        response.setContentType("text/plain");
        response.getWriter().write(json.toString());
    }

    public HouseRecord getHouse()
    {
        if (this.playerRound == null)
            return null;
        if (this.playerRound.getFinalHousegroupId() != null)
        {
            HousegroupRecord hgr =
                    PlayerUtils.readRecordFromId(this, Tables.HOUSEGROUP, this.playerRound.getFinalHousegroupId());
            return PlayerUtils.readRecordFromId(this, Tables.HOUSE, hgr.getHouseId());
        }
        if (this.playerRound.getStartHousegroupId() != null)
        {
            HousegroupRecord hgr =
                    PlayerUtils.readRecordFromId(this, Tables.HOUSEGROUP, this.playerRound.getStartHousegroupId());
            return PlayerUtils.readRecordFromId(this, Tables.HOUSE, hgr.getHouseId());
        }
        return null;
    }

    public HousegroupRecord getHouseGroup()
    {
        if (this.playerRound == null)
            return null;
        if (this.playerRound.getFinalHousegroupId() != null)
        {
            return PlayerUtils.readRecordFromId(this, Tables.HOUSEGROUP, this.playerRound.getFinalHousegroupId());
        }
        if (this.playerRound.getStartHousegroupId() != null)
        {
            return PlayerUtils.readRecordFromId(this, Tables.HOUSEGROUP, this.playerRound.getStartHousegroupId());
        }
        return null;
    }

    public String getHouseCode()
    {
        if (this.playerRound == null)
            return "--";
        if (this.playerRound.getFinalHousegroupId() == null)
            return "--";
        HousegroupRecord hgr = PlayerUtils.readRecordFromId(this, Tables.HOUSEGROUP, this.playerRound.getFinalHousegroupId());
        if (hgr == null)
            return "??";
        HouseRecord house = PlayerUtils.readRecordFromId(this, Tables.HOUSE, hgr.getHouseId());
        if (house == null)
            return "??";
        if (hgr.getStatus().equals(TransactionStatus.UNAPPROVED_BUY))
            return "(in option)";
        return house.getCode();
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
            label = label.replace("$round$", Integer.toString(this.getPlayerRoundNumber()));
            label = label.replace("$rating$", Integer.toString(this.playerRound.getPreferredHouseRating()));
            label = label.replace("$income_per_round$", k(this.playerRound.getRoundIncome()));
            label = label.replace("$maxmortgage$", k(this.playerRound.getMaximumMortgage()));
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
        if (Math.abs(nr) < 1000)
            return Integer.toString(nr);
        else
            return Integer.toString(nr / 1000) + " k";
    }

    protected void setLanguageLabels(final ScenarioRecord scenario)
    {
        DSLContext dslContext = DSL.using(getDataSource(), SQLDialect.MYSQL);
        ScenarioparametersRecord spr =
                PlayerUtils.readRecordFromId(this, Tables.SCENARIOPARAMETERS, scenario.getScenarioparametersId());
        GameversionRecord gameVersion = PlayerUtils.readRecordFromId(this, Tables.GAMEVERSION, scenario.getGameversionId());
        int languageId = spr.getDefaultLanguageId();
        LanguageRecord language = PlayerUtils.readRecordFromId(this, Tables.LANGUAGE, languageId);
        LanguagegroupRecord languageGroup =
                PlayerUtils.readRecordFromId(this, Tables.LANGUAGEGROUP, gameVersion.getLanguagegroupId());
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
        this.labelMap = labelMap;
    }

    public int getMeasurePrice(final MeasuretypeRecord mt)
    {
        int housePrice = getHouse() == null ? 0 : getHouse().getPrice();
        int income = getPlayerRound().getRoundIncome();
        return mt.getCostAbsolute() + (int) Math.round(mt.getCostPercentageHouse() * housePrice / 100.0)
                + (int) Math.round(mt.getCostPercentageIncome() * income / 100.0);
    }

    public int getSatisfactionDeltaIfBought(final MeasuretypeRecord mt)
    {
        return mt.getSatisfactionDeltaOnce() > 0 ? mt.getSatisfactionDeltaOnce() : mt.getSatisfactionDeltaPermanent();
    }

    public int getExpectedMortgage()
    {
        HouseRecord house = getHouse();
        if (house == null)
            return 0;
        // TODO: bid?
        ScenarioparametersRecord spr =
                PlayerUtils.readRecordFromId(this, Tables.SCENARIOPARAMETERS, this.scenario.getScenarioparametersId());
        return (int) (house.getPrice() / spr.getMortgagePercentage());
    }

    public int getExpectedTaxes()
    {
        HouseRecord house = getHouse();
        if (house == null)
            return 0;
        // TODO: get mid score from database?
        return 15000;
    }

    public int getHouseSatisfaction()
    {
        int currentHouseSatisfaction = 0;
        if (this.playerRound.getFinalHousegroupId() != null)
        {
            HousegroupRecord hgr =
                    PlayerUtils.readRecordFromId(this, Tables.HOUSEGROUP, this.playerRound.getFinalHousegroupId());
            currentHouseSatisfaction = hgr.getHouseSatisfaction();
        }
        else if (this.playerRound.getStartHousegroupId() != null)
        {
            HousegroupRecord hgr =
                    PlayerUtils.readRecordFromId(this, Tables.HOUSEGROUP, this.playerRound.getStartHousegroupId());
            currentHouseSatisfaction = hgr.getHouseSatisfaction();
        }
        return currentHouseSatisfaction;
    }

    public int getTotalSatisfaction()
    {
        return this.playerRound.getSatisfactionTotal();
    }

    public int getSavings()
    {
        return this.playerRound.getSpendableIncome() > 0 ? this.playerRound.getSpendableIncome() : 0;
    }

    public int getDebt()
    {
        return this.playerRound.getSpendableIncome() < 0 ? this.playerRound.getSpendableIncome() : 0;
    }

    public int getMaxMortgagePlusSavings()
    {
        return this.playerRound.getMaximumMortgage() + this.playerRound.getSpendableIncome();
    }

    public int getMortgagePercentage()
    {
        ScenarioparametersRecord spr =
                PlayerUtils.readRecordFromId(this, Tables.SCENARIOPARAMETERS, this.scenario.getScenarioparametersId());
        return spr.getMortgagePercentage().intValue();
    }

    public void newPlayerState(final PlayerroundRecord playerRound, final PlayerState newState, final String content)
    {
        DSLContext dslContext = DSL.using(getDataSource(), SQLDialect.MYSQL);
        playerRound.setPlayerState(newState.toString());
        playerRound.store();
        PlayerstateRecord playerState =
                dslContext.selectFrom(Tables.PLAYERSTATE).where(Tables.PLAYERSTATE.PLAYERROUND_ID.eq(playerRound.getId()))
                        .and(Tables.PLAYERSTATE.PLAYER_STATE.eq(newState.toString())).fetchAny();
        if (playerState == null)
        {
            playerState = dslContext.newRecord(Tables.PLAYERSTATE);
            playerState.setPlayerState(newState.toString());
            playerState.setPlayerroundId(playerRound.getId());
            playerState.setContent(content);
            playerState.setTimestamp(LocalDateTime.now());
            playerState.store();
        }
    }

    public boolean ltState(final PlayerState state)
    {
        return PlayerState.valueOf(this.getPlayerRound().getPlayerState()).lt(state);
    }

    public boolean leState(final PlayerState state)
    {
        return PlayerState.valueOf(this.getPlayerRound().getPlayerState()).le(state);
    }

    public boolean eqState(final PlayerState state)
    {
        return PlayerState.valueOf(this.getPlayerRound().getPlayerState()).eq(state);
    }

    public boolean neState(final PlayerState state)
    {
        return PlayerState.valueOf(this.getPlayerRound().getPlayerState()).ne(state);
    }

    public boolean geState(final PlayerState state)
    {
        return PlayerState.valueOf(this.getPlayerRound().getPlayerState()).ge(state);
    }

    public boolean gtState(final PlayerState state)
    {
        return PlayerState.valueOf(this.getPlayerRound().getPlayerState()).gt(state);
    }

}
