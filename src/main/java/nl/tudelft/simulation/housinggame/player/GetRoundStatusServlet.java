package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.GrouproundRecord;

@WebServlet("/get-round-status")
public class GetRoundStatusServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        PlayerData data = SessionUtils.getData(session);
        if (data == null)
        {
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        // reload the round with the latest state
        GrouproundRecord groupRound = SqlUtils.readRecordFromId(data, Tables.GROUPROUND, data.getGroupRound().getId());
        data.setGroupRound(groupRound);

        response.setContentType("text/plain");
        response.getWriter().write(groupRound.getRoundState());
    }

}
