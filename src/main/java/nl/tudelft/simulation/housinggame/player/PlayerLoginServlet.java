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

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GamesessionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.GroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerRecord;

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
        PlayerData data = SessionUtils.getData(session);

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
                        data.readPlayerData(player);
                    }
                }
            }
        }

        if (ok)
        {
            PlayerStateUtils.redirect(data, response);
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
