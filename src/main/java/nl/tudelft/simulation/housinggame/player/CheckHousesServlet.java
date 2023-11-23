package nl.tudelft.simulation.housinggame.player;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;

import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.HouseRecord;

@WebServlet("/check-houses")
public class CheckHousesServlet extends HttpServlet
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

        // fill the options list

        /*-
           <option value="NONE"></option>
           <option value="D01">D01</option>
           <option value="N04">N04</option>
         */

        // loop through the houses that are valid for this round
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        try
        {
            Result<org.jooq.Record> resultList =
                    dslContext.fetch("SELECT house.id FROM house INNER JOIN community ON house.community_id=community.id "
                            + "WHERE community.gameversion_id=3;");
            SortedMap<String, HouseRecord> houseMap = new TreeMap<>();

            // fill the house names
            StringBuilder s = new StringBuilder();
            s.append("<option value=\"NONE\"></option>\n");
            for (org.jooq.Record record : resultList)
            {
                UInteger id = (UInteger) record.get(0);
                HouseRecord house = SqlUtils.readRecordFromId(data, Tables.HOUSE, id);
                if (house.getAvailableRound().intValue() == data.getPlayerRoundNumber())
                {
                    houseMap.put(house.getAddress(), house);
                    s.append("<option value=\"" + house.getAddress() + "\">" + house.getAddress() + "</option>\n");
                }
            }
            data.getContentHtml().put("house/options", s.toString());

            /*-
            <div class="house-details" id="house-details-D01" style="display: none;">
              <div class="hg-house-row">
            <div class="hg-house-icon"><i class="material-icons md-36">euro</i></div>
            <div class="hg-house-text">
              Price:425k<br>Yearly Mortgage (payment per round): 42.5k
            </div>
              </div>
              <div class="hg-house-row">
            <div class="hg-house-icon"><i class="material-icons md-36">star</i></div>
            <div class="hg-house-text">
              House Rate: 9<br>Your satisfaction will be affected by this
            </div>
              </div>
              <div class="hg-house-row">
            <div class="hg-house-icon"><i class="material-icons md-36">thunderstorm</i></div>
            <div class="hg-house-text">
              Pluvial protection: 0<br>Amount of protection from rain flooding
            </div>
              </div>
              <div class="hg-house-row">
            <div class="hg-house-icon"><i class="material-icons md-36">houseboat</i></div>
            <div class="hg-house-text">
              Fluvial protection: 0<br>Amount of protection from river flooding
            </div>
              </div>
            </div>
             */

            // fill the house details
            s = new StringBuilder();
            for (HouseRecord house : houseMap.values())
            {
                s.append("        <div class=\"house-details\" id=\"house-details-" + house.getAddress()
                        + "\" style=\"display: none;\">\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">euro</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              Price: " + data.k(house.getPrice().intValue())
                        + "<br>Yearly Mortgage (payment per round): " + data.k(house.getPrice().intValue() / 10) + "\n");
                s.append("            </div>\n");
                s.append("          </div>\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">star</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              House Rate: " + house.getRating() + "<br>Your satisfaction will be affected by this\n");
                s.append("            </div>\n");
                s.append("          </div>\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">thunderstorm</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              Pluvial protection: " + house.getInitialPluvialProtection()
                        + "<br>Amount of protection from rain flooding\n");
                s.append("            </div>\n");
                s.append("          </div>\n");
                s.append("          <div class=\"hg-house-row\">\n");
                s.append("            <div class=\"hg-house-icon\"><i class=\"material-icons md-36\">houseboat</i></div>\n");
                s.append("            <div class=\"hg-house-text\">\n");
                s.append("              Fluvial protection: " + house.getInitialFluvialProtection()
                        + "<br>Amount of protection from river flooding\n");
                s.append("            </div>\n");
                s.append("          </div>\n");
                s.append("        </div>\n\n");
            }
            data.getContentHtml().put("house/details", s.toString());

            response.sendRedirect("jsp/player/check-houses.jsp");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            response.sendRedirect("jsp/player/login.jsp");
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

}
