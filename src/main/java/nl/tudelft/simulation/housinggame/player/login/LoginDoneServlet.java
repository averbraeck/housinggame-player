package nl.tudelft.simulation.housinggame.player.login;

import java.io.IOException;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GamesessionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.PlayerUtils;
import nl.tudelft.simulation.housinggame.player.ValidStates;

@WebServlet("/login-done")
public class LoginDoneServlet extends HttpServlet
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
        if (data == null)
        {
            ok = false;
        }
        else
        {
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
                GamesessionRecord gs = PlayerUtils.readRecordFromId(data, Tables.GAMESESSION, gameSessionId);
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
                            data.readPlayerData(player);
                        }
                    }
                }
            }
        }

        if (ok)
        {
            redirect(data, response);
        }
        else
        {
            session.removeAttribute("playerData");
            try
            {
                response.sendRedirect("/housinggame-player/login");
            }
            catch (Exception e)
            {

            }
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        doPost(request, response);
    }

    public static void redirect(final PlayerData data, final HttpServletResponse response) throws IOException, ServletException
    {
        if (!ValidStates.isValidState(data))
        {
            response.sendRedirect("/housinggame-player/error");
            return;
        }
        PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());

        switch (playerState)
        {
            case LOGIN:
                response.sendRedirect("/housinggame-player/welcome-wait");
                break;

            case READ_BUDGET:
                response.sendRedirect("/housinggame-player/read-budget");
                break;

            case READ_NEWS:
                response.sendRedirect("/housinggame-player/read-news");
                break;

            case VIEW_SELL_HOUSE:
                response.sendRedirect("/housinggame-player/sell-house");
                break;

            case SELL_HOUSE_WAIT:
                response.sendRedirect("/housinggame-player/sell-house-wait");
                break;

            case STAY_HOUSE_WAIT:
                response.sendRedirect("/housinggame-player/stay-house-wait");
                break;

            case VIEW_BUY_HOUSE:
                response.sendRedirect("/housinggame-player/buy-house");
                break;

            case BUY_HOUSE_WAIT:
                response.sendRedirect("/housinggame-player/buy-house-wait");
                break;

            case BOUGHT_HOUSE:
                response.sendRedirect("/housinggame-player/bought-house");
                break;

            case STAYED_HOUSE:
                response.sendRedirect("/housinggame-player/stayed-house");
                break;

            case VIEW_TAXES:
                response.sendRedirect("/housinggame-player/view-taxes");
                break;

            case VIEW_IMPROVEMENTS:
                response.sendRedirect("/housinggame-player/view-improvements");
                break;

            case ANSWER_SURVEY:
                response.sendRedirect("/housinggame-player/answer-survey");
                break;

            case SURVEY_COMPLETED:
                response.sendRedirect("/housinggame-player/survey-completed");
                break;

            case VIEW_DAMAGE:
                response.sendRedirect("/housinggame-player/view-damage");
                break;

            case VIEW_SUMMARY:
                response.sendRedirect("/housinggame-player/view-summary");
                break;

            default:
                throw new IllegalArgumentException(
                        "Unexpected value for PlayerState: " + data.getPlayerRound().getPlayerState());
        }

    }

}
