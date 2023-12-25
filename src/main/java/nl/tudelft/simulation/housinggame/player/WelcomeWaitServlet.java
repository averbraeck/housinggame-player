package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.RoundState;

@WebServlet("/welcome-wait")
public class WelcomeWaitServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        PlayerData data = (PlayerData) session.getAttribute("playerData");
        if (data == null)
        {
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        // depending on whether the game has started, a test is shown to wait for the facilitator (or not)
        if (data.getGroupRoundNumber() <= 0 || RoundState.LOGIN.eq(data.getGroupRound().getRoundState()))
            data.putContentHtml("welcome-wait/wait-or-not", data.getLabel("welcome/wait/text"));
        else
            data.putContentHtml("welcome-wait/wait-or-not", data.getLabel("welcome/wait/nowait"));

        response.sendRedirect("jsp/player/welcome-wait.jsp");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
