<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game: Select House</title>
  
<style>
.hg-house-row {
  display: flex;
  flex-direction: row;
  padding: 2px;
  margin: 2px;  
}

.hg-house-icon {
  padding: 2px;
  margin: 2px;
  display-vertical: center;
  opacity: 50%;
}
</style>

</head>

<body style="background-color: white;" onLoad = initPage()>

  <div class="form-container">
    
    <jsp:include page="header.jsp"></jsp:include>
    
    <div class="hg-title">Wait for the facilitator<br/>to check your buying request</div>
    
    <div class="panel-group pmd-accordion" id="welcome-accordion" role="tablist" aria-multiselectable="true" > 
      
      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading1">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse1" aria-expanded="true" 
              aria-controls="collapse1" data-expandable="false">
              1. Your budget and expectations
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse1" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading1">
          <div class="panel-body">
            ${playerData.getContentHtml("panel/budget") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading2">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse2" aria-expanded="false" 
              aria-controls="collapse2" data-expandable="false">
              2. News for this round
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse2" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading2">
          <div class="panel-body">
            ${playerData.getContentHtml("news/summary/1") }
          </div>
        </div>
      </div>
      
      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading3">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse3" aria-expanded="false" 
              aria-controls="collapse3" data-expandable="false">
              3. Your house choice
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse3" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="heading3">
          <div class="panel-body">
            <p>
              Select a house from the list to check the key features. 
              You will pay ${playerData.getMortgagePercentage() }% 
              of the house price as the round mortgage. 
            </p>
            <p style="background-color:lightgrey;">
              You cannot select a house whose price is higher than the maximum mortgage
               (${playerData.k(playerData.getPlayerRound().getMaximumMortgage()) }) 
              + your savings (${playerData.k(playerData.getSavings()) })
              - your debt (${playerData.k(playerData.getDebt()) })
              = ${playerData.k(playerData.getMaxMortgagePlusSavings()) }
            </p>
            
            <form action="/housinggame-player/advance-state" method="post">
              <div class="form-group pmd-textfield form-group-sm">
                <label for="houses" class="control-label pmd-textfield-floating-label">Select house*</label> 
                <select name="houses" id="houses" class="form-control">
                   ${playerData.getContentHtml("house/options") }
                </select>
                ${playerData.getContentHtml("house/prices") }
              </div>
            </form>
            
            <div>
              ${playerData.getContentHtml("house/details") }
            </div>
          </div>
        </div>
      </div>

    </div>
   
   <div style="display:flex; flex-direction: row; justify-content: space-around;"> 
    <form action="/housinggame-player/advance-state" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value="move-in" />
        <input type="submit" value="APPROVED: MOVE IN" class="btn btn-primary" id="hg-movein" disabled />
      </div>
     </form>
     <form action="/housinggame-player/advance-state" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value="reject-buy" />
        <input type="submit" value="REJECTED: GO BACK" class="btn btn-primary" id="hg-goback" disabled />
      </div>
     </form>
    </div>
    
    <br/>&nbsp;<br/>
    
  </div>
  
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
        $.post("/housinggame-player/check-buy-status", {jsp: 'buy-house-wait'},
          function(data, status) {
            if (data == "APPROVED") {
              $("#hg-movein").removeAttr("disabled");
            } else if (data == "REJECTED") {
              $("#hg-goback").removeAttr("disabled");
            } else {
              setTimeout(check, 5000);
            }
          });
    }
  </script>
  
</body>
</html>
