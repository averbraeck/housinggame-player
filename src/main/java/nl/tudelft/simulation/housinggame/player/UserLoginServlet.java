package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;

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
import nl.tudelft.simulation.housinggame.data.tables.records.WelfaretypeRecord;

@WebServlet("/login")
public class UserLoginServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException
    {
        super.init();
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {

        String gamesession = request.getParameter("gamesession");
        String group = request.getParameter("group");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        /*-
        MessageDigest md;
        String hashedPassword;
        try
        {
            // https://www.baeldung.com/java-md5
            md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            hashedPassword = DatatypeConverter.printHexBinary(digest).toLowerCase();
        }
        catch (NoSuchAlgorithmException e1)
        {
            throw new ServletException(e1);
        }
        */

        HttpSession session = request.getSession();

        PlayerData data = (PlayerData) session.getAttribute("playerData");

        boolean ok = true;
        int gameSessionId = 0;
        if (gamesession == null)
            ok = false;
        else
        {
            try
            {
                gameSessionId = Integer.parseInt(gamesession);
            }
            catch (Exception e)
            {
                ok = false;
            }
        }
        if (ok)
        {
            DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
            GamesessionRecord gs = SqlUtils.readRecordFromId(data, Tables.GAMESESSION, gameSessionId);
            GroupRecord groupRecord = dslContext.selectFrom(Tables.GROUP)
                    .where(Tables.GROUP.GAMESESSION_ID.eq(gs.getId()).and(Tables.GROUP.NAME.eq(group))).fetchAny();
            if (groupRecord == null)
                ok = false;
            else
            {
                if (!groupRecord.getPassword().equals(password))
                    ok = false;
                else
                {
                    PlayerRecord player =
                            dslContext.selectFrom(Tables.PLAYER).where(Tables.PLAYER.GROUP_ID.eq(groupRecord.getId()))
                                    .and(Tables.PLAYER.CODE.eq(username)).fetchAny();
                    if (player == null)
                        ok = false;
                    else
                    {
                        data.setPlayer(player);
                        data.setPlayerCode(player.getCode());
                        data.setGroup(groupRecord);
                        data.setGameSession(gs);
                        ScenarioRecord scenario = SqlUtils.readRecordFromId(data, Tables.SCENARIO, groupRecord.getScenarioId());
                        data.setScenario(scenario);
                        GameversionRecord gameVersion =
                                SqlUtils.readRecordFromId(data, Tables.GAMEVERSION, scenario.getGameversionId());
                        data.setGameVersion(gameVersion);
                        setOrCreateRound(data);
                        setLanguageLabels(data);
                    }
                }
            }
        }
        if (ok)
            response.sendRedirect("jsp/player/welcome.jsp");
        else
        {
            session.removeAttribute("playerData");
            response.sendRedirect("jsp/player/login.jsp");
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        response.sendRedirect("jsp/player/login.jsp");
    }

    protected void setOrCreateRound(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        // is there a current (highest) groupRound? Execute this code with a database lock around it (!)
        List<GrouproundRecord> grList = new ArrayList<>();
        try
        {
            // dslContext.execute("LOCK TABLES housinggame.groupround");
            grList = dslContext.selectFrom(Tables.GROUPROUND).where(Tables.GROUPROUND.GROUP_ID.eq(data.getGroup().getId()))
                    .fetch();
            if (grList.isEmpty())
            {
                GrouproundRecord newGr = dslContext.newRecord(Tables.GROUPROUND);
                newGr.setGroupId(data.getGroup().getId());
                // find the round with the lowest number: SELECT * from round WHERE round.scenario_id=3
                // AND round_number=(SELECT MIN(round_number) FROM round WHERE round.scenario_id=3 );
                int minRoundNumber = dslContext
                        .execute("SELECT round_number FROM round WHERE round.scenario_id=" + data.getScenario().getId());
                RoundRecord lowestRound =
                        dslContext.selectFrom(Tables.ROUND).where(Tables.ROUND.SCENARIO_ID.eq(data.getScenario().getId()))
                                .and(Tables.ROUND.ROUND_NUMBER.eq(minRoundNumber)).fetchOne();
                newGr.setRoundId(lowestRound.getId());
                newGr.setFluvialFloodIntensity(0);
                newGr.setPluvialFloodIntensity(0);
                // newGr.setStartTime(LocalDateTime.now());
                newGr.store();
                grList.add(newGr);
            }
        }
        finally
        {
            // dslContext.execute("UNLOCK TABLES");
        }
        // find the GrouproundRecord with the highest associated round number
        GrouproundRecord groupRound = null;
        int currentRound = -1;
        for (int i = 0; i < grList.size(); i++)
        {
            UInteger grid = grList.get(i).getRoundId();
            RoundRecord roundRecord = SqlUtils.readRecordFromId(data, Tables.ROUND, grid);
            if (roundRecord.getRoundNumber().intValue() > currentRound)
            {
                currentRound = roundRecord.getRoundNumber().intValue();
                groupRound = grList.get(i);
            }
        }

        // set the data
        data.setCurrentRound(currentRound);
        data.setGroupRound(groupRound);

        // is there a playerRound belonging to the current groupRound?
        List<PlayerroundRecord> prList = dslContext.selectFrom(Tables.PLAYERROUND)
                .where(Tables.PLAYERROUND.PLAYER_ID.eq(data.getPlayer().getId())).fetch();
        if (prList.isEmpty())
        {
            PlayerroundRecord newPr = dslContext.newRecord(Tables.PLAYERROUND);
            WelfaretypeRecord welfareType = dslContext.selectFrom(Tables.WELFARETYPE)
                    .where(Tables.WELFARETYPE.ID.eq(data.getPlayer().getWelfaretypeId())).fetchAny();
            newPr.setPlayerId(data.getPlayer().getId());
            newPr.setSatisfaction(welfareType.getInitialSatisfaction());
            newPr.setSaving(welfareType.getInitialMoney());
            newPr.setCurrentWealth(welfareType.getInitialMoney().intValue());
            newPr.setDebt(UInteger.valueOf(0));
            newPr.setFluvialDamage(UInteger.valueOf(0));
            newPr.setPluvialDamage(UInteger.valueOf(0));
            newPr.setGrouproundId(groupRound.getId());
            newPr.setHouseId(null);
            newPr.setHousePriceBought(UInteger.valueOf(0));
            newPr.setHousePriceSold(UInteger.valueOf(0));
            newPr.setIncome(welfareType.getIncome());
            newPr.setLivingCosts(welfareType.getLivingCosts());
            newPr.setMeasureBought(UInteger.valueOf(0));
            newPr.setMortgage(UInteger.valueOf(0));
            newPr.setMovingReason("");
            newPr.setPaidOffDebt(UInteger.valueOf(0));
            newPr.setPreferredHouseRating(welfareType.getPreferredHouseRating());
            newPr.setRepairedDamage(null);
            newPr.setSatisfactionCostPerPoint(welfareType.getSatisfactionCostPerPoint());
            newPr.setSatisfactionPointBought(UInteger.valueOf(0));
            newPr.setSpentSavingsForBuyingHouse(UInteger.valueOf(0));
            newPr.store();
            prList.add(newPr);

        }
        PlayerroundRecord pr;
    }

    protected void setLanguageLabels(final PlayerData data)
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        ScenarioparametersRecord spr =
                SqlUtils.readRecordFromId(data, Tables.SCENARIOPARAMETERS, data.getScenario().getScenarioparametersId());
        UInteger languageId = spr.getDefaultLanguageId();
        LanguageRecord language = SqlUtils.readRecordFromId(data, Tables.LANGUAGE, languageId);
        LanguagegroupRecord languageGroup =
                SqlUtils.readRecordFromId(data, Tables.LANGUAGEGROUP, data.getGameVersion().getLanguagegroupId());
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
        data.setLabelMap(labelMap);
    }
}
