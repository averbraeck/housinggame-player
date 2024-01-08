package nl.tudelft.simulation.housinggame.player.stayhousewait;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.GroupState;
import nl.tudelft.simulation.housinggame.common.TransactionStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;

@WebServlet("/check-stay-status")
public class CheckStayStatus extends HttpServlet
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

        PlayerroundRecord prr = data.getPlayerRound();
        Integer transactionId = prr.getActiveTransactionId();
        if (transactionId == null)
        {
            System.err.println("No transaction found");
        }
        else
        {
            if (checkStatus(data))
            {
                HousetransactionRecord transaction = SqlUtils.readRecordFromId(data, Tables.HOUSETRANSACTION, transactionId);
                if (transaction.getTransactionStatus().equals(TransactionStatus.REJECTED_STAY))
                {
                    response.setContentType("text/plain");
                    response.getWriter().write("REJECTED");
                    return;
                }
                else if (transaction.getTransactionStatus().equals(TransactionStatus.APPROVED_STAY))
                {
                    response.setContentType("text/plain");
                    response.getWriter().write("APPROVED");
                    return;
                }
            }
        }

        response.setContentType("text/plain");
        response.getWriter().write("WAIT");
    }

    private static boolean checkStatus(final PlayerData data)
    {
        PlayerState playerState = PlayerState.valueOf(data.getPlayerRound().getPlayerState());
        GroupState groupState = GroupState.valueOf(data.getGroupRound().getGroupState());

        if (data.getHighestGroupRoundNumber() > data.getPlayerRoundNumber())
            return true;
        if (groupState.nr >= playerState.nr)
            return true;
        return false;
    }

}
