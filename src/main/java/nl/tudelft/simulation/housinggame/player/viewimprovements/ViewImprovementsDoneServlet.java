package nl.tudelft.simulation.housinggame.player.viewimprovements;

import java.io.IOException;
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

import nl.tudelft.simulation.housinggame.common.PlayerState;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HousegroupRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasureRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasuretypeRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.WelfaretypeRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.SqlUtils;

@WebServlet("/view-improvements-done")
public class ViewImprovementsDoneServlet extends HttpServlet
{

    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession();

        PlayerData data = (PlayerData) session.getAttribute("playerData");
        if (data == null || request.getParameter("nextScreen") == null)
        {
            System.err.println("data == null or nextScreen == null");
            response.sendRedirect("/housinggame-player/login");
            return;
        }

        String satPointsStr = request.getParameter("form-selected-points");
        if (satPointsStr == null)
        {
            data.errorRedirect(response,
                    "Player app called view-improvements-done servlet, but form-selected-points field is null");
            return;
        }
        satPointsStr = satPointsStr.strip();

        String formOptions = request.getParameter("form-options");
        if (formOptions == null)
        {
            data.errorRedirect(response, "Player app called view-improvements-done servlet, but form-options field is null");
            return;
        }
        formOptions = formOptions.strip();

        try
        {
            // the next screen button indicates the INTENTION of the player, not the screen it originates from.
            String nextScreen = request.getParameter("nextScreen");

            // player clicked BUY IMPROVEMENTS on the view-improvements screen
            if (nextScreen.equals("answer-survey"))
            {
                // reload the round with the latest state
                data.readDynamicData();
                PlayerroundRecord prr = data.getPlayerRound();
                HousegroupRecord hgr = SqlUtils.readRecordFromId(data, Tables.HOUSEGROUP, prr.getFinalHousegroupId());

                DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
                WelfaretypeRecord wft =
                        SqlUtils.readRecordFromId(data, Tables.WELFARETYPE, data.getPlayer().getWelfaretypeId());
                List<MeasureRecord> measureList = dslContext.selectFrom(Tables.MEASURE)
                        .where(Tables.MEASURE.HOUSEGROUP_ID.eq(prr.getFinalHousegroupId())).fetch();
                int measureCost = 0;
                int measureSat = 0;
                if (formOptions.length() > 1)
                {
                    // regex escape characters: <([{\^-=$!|]})?*+.>
                    String[] measureArray = formOptions.split("&");
                    for (String m : measureArray)
                    {
                        String measureTypeIdStr = m.split("\\=")[1].strip();
                        int measureTypeId = Integer.parseInt(measureTypeIdStr);
                        MeasuretypeRecord mt = SqlUtils.readRecordFromId(data, Tables.MEASURETYPE, measureTypeId);
                        boolean found = false;
                        for (MeasureRecord mr : measureList)
                        {
                            if (mr.getMeasuretypeId().equals(measureTypeId))
                                found = true;
                        }
                        if (!found) // new measure
                        {
                            MeasureRecord measure = dslContext.newRecord(Tables.MEASURE);
                            measure.setRoundNumber(data.getGroupRound().getRoundNumber());
                            measure.setMeasuretypeId(mt.getId());
                            measure.setHousegroupId(hgr.getId());
                            measure.setConsumedInRound(null);
                            measure.store();
                            measureCost += mt.getPrice();
                            measureSat += mt.getSatisfactionDelta();
                        }
                    }
                }

                int satPoints = satPointsStr.length() == 0 ? 0 : Integer.parseInt(satPointsStr);
                int satPointCost = satPoints * wft.getSatisfactionCostPerPoint();

                hgr.setHouseSatisfaction(hgr.getHouseSatisfaction() + measureSat);
                prr.setSatisfactionHouseMeasures(measureSat);
                prr.setCostMeasuresBought(measureCost);
                hgr.store();

                prr.setSatisfactionBought(satPoints);
                prr.setCostSatisfactionBought(satPointCost);
                prr.setPersonalSatisfaction(prr.getPersonalSatisfaction() + satPoints);
                prr.setSpendableIncome(prr.getSpendableIncome() - measureCost - satPointCost);
                prr.setPlayerState(PlayerState.ANSWER_SURVEY.toString());
                prr.store();

                response.sendRedirect("/housinggame-player/answer-survey");
                return;
            }

            // if the player did not click 'view house' and enters the read-news-done servlet, something is wrong
            data.errorRedirect(response,
                    "Player app called view-improvements-done servlet, but NextScreen button is " + nextScreen);
        }
        catch (Exception e)
        {
            data.errorRedirect(response,
                    "Player app called view-improvements-done servlet, but error occurred: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
