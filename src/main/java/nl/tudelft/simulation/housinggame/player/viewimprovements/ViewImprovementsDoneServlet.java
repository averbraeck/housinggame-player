package nl.tudelft.simulation.housinggame.player.viewimprovements;

import java.io.IOException;
import java.util.ArrayList;
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
import nl.tudelft.simulation.housinggame.data.tables.records.HousemeasureRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.MeasuretypeRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PersonalmeasureRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.PlayerroundRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.PlayerUtils;

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
                HousegroupRecord hgr = PlayerUtils.readRecordFromId(data, Tables.HOUSEGROUP, prr.getFinalHousegroupId());

                DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
                // TODO: filter out ones you can buy again
                List<HousemeasureRecord> houseMeasureList = dslContext.selectFrom(Tables.HOUSEMEASURE)
                        .where(Tables.HOUSEMEASURE.HOUSEGROUP_ID.eq(data.getPlayerRound().getFinalHousegroupId())).fetch();
                List<PersonalmeasureRecord> personalMeasureList = new ArrayList<>();
                for (var playerRound : data.getPlayerRoundList())
                {
                    // TODO: filter out ones you can buy again
                    personalMeasureList.addAll(dslContext
                            .selectFrom(
                                    Tables.PERSONALMEASURE.where(Tables.PERSONALMEASURE.PLAYERROUND_ID.eq(playerRound.getId())))
                            .fetch());
                }
                int measureCost = 0;
                int measureHouseSat = 0;
                int measurePersSat = 0;
                if (formOptions.length() > 1)
                {
                    // regex escape characters: <([{\^-=$!|]})?*+.>
                    String[] measureArray = formOptions.split("&");
                    for (String m : measureArray)
                    {
                        String measureTypeIdStr = m.split("\\=")[1].strip();
                        int measureTypeId = Integer.parseInt(measureTypeIdStr);
                        MeasuretypeRecord mt = PlayerUtils.readRecordFromId(data, Tables.MEASURETYPE, measureTypeId);
                        boolean found = false;
                        for (HousemeasureRecord mr : houseMeasureList)
                        {
                            if (mr.getMeasuretypeId().equals(measureTypeId))
                                found = true;
                        }
                        for (PersonalmeasureRecord measure : personalMeasureList)
                        {
                            if (measure.getMeasuretypeId().equals(measureTypeId))
                                found = true;
                        }
                        if (!found) // new measure
                        {
                            if (mt.getHouseMeasure() == (byte) 0)
                            {
                                // personal measure
                                PersonalmeasureRecord measure = dslContext.newRecord(Tables.PERSONALMEASURE);
                                measure.setMeasuretypeId(mt.getId());
                                measure.setPlayerroundId(prr.getId());
                                measure.store();

                                // calculate cost and satisfaction
                                measureCost += data.getMeasurePrice(mt);
                                measurePersSat += data.getSatisfactionDeltaIfBought(mt);
                            }
                            else
                            {
                                // house measure
                                HousemeasureRecord measure = dslContext.newRecord(Tables.HOUSEMEASURE);
                                measure.setBoughtInRound(data.getGroupRound().getRoundNumber());
                                measure.setMeasuretypeId(mt.getId());
                                measure.setHousegroupId(hgr.getId());
                                measure.setUsedInRound(-1);
                                measure.store();

                                // calculate cost and satisfaction
                                measureCost += data.getMeasurePrice(mt);
                                measureHouseSat += data.getSatisfactionDeltaIfBought(mt);

                                // increase the house protection with the measure
                                hgr.setPluvialHouseProtection(hgr.getPluvialBaseProtection() + mt.getPluvialProtectionDelta());
                                hgr.setFluvialHouseProtection(hgr.getFluvialBaseProtection() + mt.getFluvialProtectionDelta());
                                hgr.store();
                            }
                        }
                    }
                }

                hgr.setHouseSatisfaction(hgr.getHouseSatisfaction() + measureHouseSat);
                hgr.store();

                prr.setSatisfactionHouseMeasures(measureHouseSat);
                prr.setSatisfactionPersonalMeasures(measurePersSat);
                prr.setSatisfactionTotal(prr.getSatisfactionTotal() + measureHouseSat + measurePersSat);
                prr.setCostMeasuresBought(measureCost);
                prr.setSpendableIncome(prr.getSpendableIncome() - measureCost);
                prr.store();

                data.newPlayerState(prr, PlayerState.ANSWER_SURVEY, "");

                response.sendRedirect("/housinggame-player/answer-survey");
                return;
            }

            // wrong screen
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
