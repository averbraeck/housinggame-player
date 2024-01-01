<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game - Wait for the Dice</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

  <div class="form-container">
    
    <jsp:include page="header.jsp"></jsp:include>

    <h1 style="text-align: center; color: blue;">Wait for survey completion</h1>

    <div class="panel-group pmd-accordion" id="hg-accordion" role="tablist" aria-multiselectable="true" > 
      <jsp:include page="accordion1.jsp"></jsp:include>
      <jsp:include page="accordion2.jsp"></jsp:include>
      <jsp:include page="accordion3.jsp"></jsp:include>
      <jsp:include page="accordion4.jsp"></jsp:include>
      <jsp:include page="accordion5.jsp"></jsp:include>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading6">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#hg-accordion" href="#collapse6" aria-expanded="false" 
              aria-controls="collapse6" data-expandable="false">
              6. Answer survey
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse6" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="heading6">
          <div class="panel-body">
            <p>
              Let's wait until all players in the group have completed the survey.
            </p>
            <p>
              After this, we will roll the dice to calculate the fluvial (river flooding) 
              and pluvial (rainfall) damage to the community and to the houses for this round.
            </p>
          </div>
        </div>
      </div>

    </div>

    
    <form action="/housinggame-player/survey-completed-done" method="post">
      <div class="hg-button">
        <input type="hidden" name="nextScreen" value="view-damage" />
        <input type="submit" value='WAIT FOR THE DICE' class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
    
  </div>
  
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'survey-completed'},
        function(data, status) {
          if (data == "OK") {
            $("#hg-submit").removeAttr("disabled");
          } else {
            setTimeout(check, 5000);
          }
        });
    }
  </script>
  
</body>
</html>
