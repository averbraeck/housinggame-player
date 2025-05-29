package nl.tudelft.simulation.housinggame.player.buyhousewait;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nl.tudelft.simulation.housinggame.common.GroupState;
import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.TransactionStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.PlayerUtils;

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
                HousetransactionRecord transaction = PlayerUtils.readRecordFromId(data, Tables.HOUSETRANSACTION, transactionId);
                if (transaction.getTransactionStatus().equals(TransactionStatus.REJECTED_BUY))
                {
                    response.setContentType("text/plain");
                    response.getWriter().write("REJECTED");
                    return;
                }
                else if (transaction.getTransactionStatus().equals(TransactionStatus.APPROVED_BUY))
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
