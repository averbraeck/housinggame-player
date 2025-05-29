package nl.tudelft.simulation.housinggame.player.answersurvey;

import java.io.IOException;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nl.tudelft.simulation.housinggame.data.Tables;
import nl.tudelft.simulation.housinggame.data.tables.records.QuestionRecord;
import nl.tudelft.simulation.housinggame.data.tables.records.QuestionitemRecord;
import nl.tudelft.simulation.housinggame.player.PlayerData;
import nl.tudelft.simulation.housinggame.player.house.HouseAccordion;
import nl.tudelft.simulation.housinggame.player.readbudget.BudgetAccordion;
import nl.tudelft.simulation.housinggame.player.readnews.NewsAccordion;
import nl.tudelft.simulation.housinggame.player.viewimprovements.ImprovementsAccordion;
import nl.tudelft.simulation.housinggame.player.viewtaxes.TaxAccordion;

@WebServlet("/answer-survey")
public class AnswerSurveyServlet extends HttpServlet
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

        data.getContentHtml().clear();
        BudgetAccordion.makeBudgetAccordion(data);
        NewsAccordion.makeNewsAccordion(data);
        HouseAccordion.makeHouseConfirmationAccordion(data);
        TaxAccordion.makeTaxAccordion(data);
        ImprovementsAccordion.makeBoughtImprovementsAccordion(data);
        makeSurveyAccordion(response, data);

        response.sendRedirect("jsp/player/answer-survey.jsp");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

    public static void makeSurveyAccordion(final HttpServletResponse response, final PlayerData data) throws IOException
    {
        DSLContext dslContext = DSL.using(data.getDataSource(), SQLDialect.MYSQL);
        List<QuestionRecord> questionList =
                dslContext.selectFrom(Tables.QUESTION).where(Tables.QUESTION.SCENARIO_ID.eq(data.getScenario().getId())).fetch()
                        .sortAsc(Tables.QUESTION.QUESTION_NUMBER);
        StringBuilder s = new StringBuilder();
        s.append("            <div>\n");
        s.append("<p>Please answer the following questions:</p>\n");
        for (var question : questionList)
        {
            s.append(question.getName() + " " + question.getDescription() + "<br/>\n");
            switch (question.getType())
            {
                case "SELECT":
                {
                    s.append("<select class=\"hg-required\" id=\"id-" + question.getId() + "\" name=\"id-" + question.getId()
                            + "\">\n");
                    List<QuestionitemRecord> itemList = dslContext.selectFrom(Tables.QUESTIONITEM)
                            .where(Tables.QUESTIONITEM.QUESTION_ID.eq(question.getId())).fetch()
                            .sortAsc(Tables.QUESTIONITEM.CODE);
                    s.append("<option value=\"\"></option>\n");
                    for (var item : itemList)
                        s.append("<option value=\"" + item.getCode() + "\">" + item.getCode() + " " + item.getName()
                                + "</option>\n");
                    s.append("</select>\n");
                    break;
                }
                case "STRING":
                {
                    s.append("<input type=\"text\" class=\"hg-required\" id=\"id-" + question.getId() + "\" name=\"id-"
                            + question.getId() + "\">\n");
                    break;
                }
                case "INTEGER":
                {
                    s.append("<input type=\"number\" class=\"hg-required\" id=\"id-" + question.getId() + "\" name=\"id-"
                            + question.getId() + "\">\n");
                    break;
                }
                case "TEXT":
                {
                    s.append("<textarea class=\"hg-required\" id=\"id-" + question.getId() + "\" name=\"id-" + question.getId()
                            + "\"></textarea>\n");
                    break;
                }
                default:
                {
                    data.errorRedirect(response, "Unexpected value in survey question: " + question.getType());
                    return;
                }
            }
            s.append("<br/><br/>\n");
        }
        s.append("            </div>\n");
        data.getContentHtml().put("house/survey", s.toString());
    }

}
