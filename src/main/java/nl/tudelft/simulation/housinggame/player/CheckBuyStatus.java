package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.HouseRoundStatus;
import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HouseroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;

@WebServlet("/check-buy-status")
public class CheckBuyStatus extends HttpServlet
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
        data.readDynamicData();

        PlayerroundRecord prr = data.getPlayerRound();
        int hrrId = prr.getFinalHouseroundId();
        if (hrrId != 0)
        {
            HouseroundRecord hrr = SqlUtils.readRecordFromId(data, Tables.HOUSEROUND, hrrId);
            if (hrr.getStatus().equals(HouseRoundStatus.REJECTED_BUY))
            {
                response.setContentType("text/plain");
                response.getWriter().write("REJECTED");
                prr.setFinalHouseroundId(null);
                prr.store();
                hrr.delete();
                prr.setPlayerState(PlayerState.VIEW_BUY_HOUSE.toString());
                return;
            }
            else if (hrr.getStatus().equals(HouseRoundStatus.APPROVED_BUY))
            {
                response.setContentType("text/plain");
                response.getWriter().write("APPROVED");
                return;
            }
        }
        response.setContentType("text/plain");
        response.getWriter().write("");
    }

}
