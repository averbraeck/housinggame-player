package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.common.TransactionStatus;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousetransactionRecord;
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
            HousetransactionRecord transaction = SqlUtils.readRecordFromId(data, Tables.HOUSETRANSACTION, transactionId);
            if (transaction.getTransactionStatus().equals(TransactionStatus.REJECTED_BUY))
            {
                response.setContentType("text/plain");
                response.getWriter().write("REJECTED");
                prr.setFinalHousegroupId(null);
                prr.setActiveTransactionId(null);
                prr.store();
                prr.setPlayerState(PlayerState.VIEW_BUY_HOUSE.toString());
                return;
            }
            else if (transaction.getTransactionStatus().equals(TransactionStatus.APPROVED_BUY))
            {
                response.setContentType("text/plain");
                response.getWriter().write("APPROVED");
                prr.setFinalHousegroupId(transaction.getHousegroupId());
                prr.setActiveTransactionId(null);
                prr.store();
                return;
            }
        }

        response.setContentType("text/plain");
        response.getWriter().write("");
    }

}
