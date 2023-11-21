package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GamesessionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GameversionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.RoundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.ScenarioRecord;

@WebServlet("/login")
public class PlayerLoginServlet extends HttpServlet
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
                        data.setGroup(groupRecord);
                        data.setGameSession(gs);
                        ScenarioRecord scenario = SqlUtils.readRecordFromId(data, Tables.SCENARIO, groupRecord.getScenarioId());
                        data.setScenario(scenario);
                        GameversionRecord gameVersion =
                                SqlUtils.readRecordFromId(data, Tables.GAMEVERSION, scenario.getGameversionId());
                        data.setGameVersion(gameVersion);
                        GrouproundRecord groupRound = SqlUtils.getOrMakeLatestGroupRound(data, data.getGroup());
                        RoundRecord round = dslContext.selectFrom(Tables.ROUND)
                                .where(Tables.ROUND.ID.eq(groupRound.getRoundId())).fetchOne();
                        data.setRound(round);
                        data.setGroupRound(groupRound);
                        PlayerroundRecord playerRound = SqlUtils.getOrMakePlayerRound(data, groupRound);
                        data.setPlayerRound(playerRound);
                        data.setLanguageLabels(scenario);
                    }
                }
            }
        }

        if (ok)
        {
            PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());
            if (playerState == null)
                throw new IllegalArgumentException(
                        "Unexpected value for PlayerState: " + data.getPlayerRound().getPlayerState());
            switch (playerState)
            {
                case INIT:
                    response.sendRedirect("jsp/player/welcome-wait.jsp");
                    break;

                case READ_NEWS:
                    response.sendRedirect("jsp/player/news.jsp");
                    break;

                case CHECK_HOUSE:
                case HOUSE:
                    if (data.getCurrentRound() == 1)
                        response.sendRedirect("jsp/player/new-house.jsp");
                    else
                        response.sendRedirect("jsp/player/buy-sell-house.jsp");
                    break;

                case BOUGHT_HOUSE:
                    response.sendRedirect("jsp/player/house-calc.jsp");
                    break;

                case MOVED_IN:
                    response.sendRedirect("jsp/player/measure.jsp");
                    break;

                case BOUGHT_MEASURES:
                    response.sendRedirect("jsp/player/measure-calc.jsp");
                    break;

                case SURVEY:
                    response.sendRedirect("jsp/player/survey.jsp");
                    break;

                case DAMAGE:
                    response.sendRedirect("jsp/player/damage-calc.jsp");
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unexpected value for PlayerState: " + data.getPlayerRound().getPlayerState());
            }
        }
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
}
