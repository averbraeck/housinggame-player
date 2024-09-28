package nl.tudelft.simulation.housinggame.player.viewimprovements;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

import nl.tudelft.simulation.housinggame.common.MeasureTypeList;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasuretypeRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.PlayerUtils;

@WebServlet("/check-improvements-costs")
public class CheckImprovementsCostsServlet extends HttpServlet
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

        String retValue = "OK";
        StringBuilder s = new StringBuilder();
        s.append("<div id=\"calculation-result\">\n");

        try
        {
            // reload the round with the latest state
            data.readDynamicData();

            // return OK if the button for the current screen can be enabled, an empty string otherwise
            List<MeasuretypeRecord> activeMeasureList =
                    MeasureTypeList.getActiveMeasureListRecords(data, data.getScenario().getId(), data.getPlayerRound());
            int measureCost = 0;
            int measureSat = 0;
            // String jsp = request.getParameter("jsp");
            String form = request.getParameter("form").strip();
            // regex escape characters: <([{\^-=$!|]})?*+.>
            if (form.length() > 1)
            {
                String[] measureArray = form.split("&");
                for (String m : measureArray)
                {
                    String measureTypeIdStr = m.split("\\=")[1].strip();
                    int measureTypeId = Integer.parseInt(measureTypeIdStr);
                    MeasuretypeRecord mt = PlayerUtils.readRecordFromId(data, Tables.MEASURETYPE, measureTypeId);
                    boolean found = activeMeasureList.contains(mt);
                    if (!found) // new measure
                    {
                        measureCost += data.getMeasurePrice(mt);
                        measureSat += data.getSatisfactionDeltaIfBought(mt);
                    }
                }
            }
            boolean canAfford = data.getPlayerRound().getSpendableIncome() - measureCost >= 0;

            s.append("  <p>\n");
            s.append("      Total measure cost: " + data.k(measureCost) + "<br/>\n");
            s.append("      Spendable income: " + data.k(data.getPlayerRound().getSpendableIncome()) + "<br/>\n");
            s.append("  </p>\n");
            if (canAfford)
            {
                s.append("  <p class=\"hg-box-green\">\n");
                s.append("      You CAN afford these measures / bought satisfaction.<br/>\n");
                s.append("      Your satisfaction will grow with " + (measureSat)
                        + " points, growing your personal satisfaction from " + data.getPlayerRound().getSatisfactionTotal()
                        + " to " + (measureSat + data.getPlayerRound().getSatisfactionTotal()) + ".\n");
                s.append("  </p>\n");
                retValue = "OK";
            }
            else if (measureCost > 0)
            {
                s.append("  <p class=\"hg-box-red\">\n");
                s.append(
                        "      You CANNOT afford these measures / bought satisfaction with your current spendable income.<br/>\n");
                s.append("  </p>\n");
                retValue = "NO";
            }
            else
            {
                s.append("  <p>\n");
                s.append("      You did not select any measures (yet).\n");
                s.append("  </p>\n");
                retValue = "OK";
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            s.append("  <p class=\"hg-box-red\">\n");
            s.append("      An error occurred: " + e.getMessage() + ".\n");
            s.append("  </p>\n");
        }
        s.append("</div>\n");

        JsonObject json = new JsonObject();
        json.addProperty("ok", retValue);
        json.addProperty("html", s.toString());

        response.setContentType("text/plain");
        response.getWriter().write(json.toString());
    }

}
