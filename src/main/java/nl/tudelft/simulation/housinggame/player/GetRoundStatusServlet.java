package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

        PlayerData data = (PlayerData) session.getAttribute("playerData");
        if (data == null)
        {
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        // reload the round with the latest state
        data.readDynamicData();

        // return OK if the button for the current screen can be enabled, an empty string otherwise
        String jsp = request.getParameter("jsp");
        boolean ok = false;
        if (jsp == null)
            data.setError("nextScreen called, but no jsp parameter transferred");
        else
            ok = ValidStates.checkNextScreenButton(data, jsp);
        if (!ok && data.getError().length() > 0)
            System.err.println("ERROR: checkNextScreenButton for player " + data.getPlayerCode() + ": " + data.getError());

        response.setContentType("text/plain");
        response.getWriter().write(ok ? "OK" : "");
    }

}
