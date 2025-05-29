package nl.tudelft.simulation.housinggame.player.welcomewait;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nl.tudelft.simulation.housinggame.common.GroupState;
import nl.tudelft.simulation.housinggame.player.PlayerData;

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
        if (data.getHighestGroupRoundNumber() <= 0
                || GroupState.LOGIN.eq(data.getGroupRoundList().get(data.getHighestGroupRoundNumber()).getGroupState()))
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
