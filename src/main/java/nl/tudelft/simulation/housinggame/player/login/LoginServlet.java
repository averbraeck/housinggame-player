package nl.tudelft.simulation.housinggame.player.login;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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
import nl.tudelft.simulation.housinggame.player.PlayerData;

@WebServlet("/login")
public class LoginServlet extends HttpServlet
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
        PlayerData data = new PlayerData();
        HttpSession session = request.getSession();
        session.setAttribute("playerData", data);
        data.putContentHtml("sessionOptions", getValidSessionOptions(data));
        response.sendRedirect("jsp/player/login.jsp");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        doPost(request, response);
    }

    /*-
    <option value="3">Session 1</option>
    <option value="6">Session 2</option>
    */
    private String getValidSessionOptions(final PlayerData data)
    {
        LocalDateTime now = LocalDateTime.now();
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<GamesessionRecord> gsList = dslContext.selectFrom(Tables.GAMESESSION).fetch();
        StringBuilder s = new StringBuilder();
        for (GamesessionRecord gs : gsList)
        {
            if (gs.getStartTime() == null || now.isAfter(gs.getStartTime()))
            {
                if (gs.getEndTime() == null || now.isBefore(gs.getEndTime()))
                {
                    s.append("<option value=\"");
                    s.append(gs.getId());
                    s.append("\">");
                    s.append(gs.getName());
                    s.append("</option>\n");
                }
            }
        }
        return s.toString();
    }
}
